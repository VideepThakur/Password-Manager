import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.Base64;
import com.google.gson.*;

class PasswordEntry implements Serializable {
    private String website;
    private String username;
    private String encryptedPassword;

    public PasswordEntry(String site, String name, String encryptedPass) {
        this.website = site;
        this.username = name;
        this.encryptedPassword = encryptedPass;
    }

    public String getWebsite() {
        return website;
    }

    public String getUsername() {
        return username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }
}

class EncryptionUtils {
    public static SecretKeySpec generateAESKey(String password, String salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static byte[] encryptObject(Object obj, SecretKeySpec key) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(obj);
        out.close();
        byte[] serializedData = bos.toByteArray();

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(serializedData);
    }

    public static Object decryptObject(byte[] encryptedData, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedData = cipher.doFinal(encryptedData);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decryptedData));
        return ois.readObject();
    }

    public static String encrypt(String plainText, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedText, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        return new String(cipher.doFinal(decoded));
    }
}

public class PasswordManagerApp {
    private static java.util.List<PasswordEntry> entries = new ArrayList<>();
    private static DefaultListModel<String> websiteListModel = new DefaultListModel<>();
    private static JList<String> websiteList;
    private static SecretKeySpec aesKey;
    private static final String SALT = "s0m3$@lT";
    private static final String PASSWORDS_FILE = "passwords.dat";
    private static final String CONFIG_FILE = "config.dat";

    public static void main(String[] args) {
        try {
            String masterPassword = authenticate();
            aesKey = EncryptionUtils.generateAESKey(masterPassword, SALT);
            loadEntriesFromEncryptedFile();

            JFrame frame = new JFrame("Password Manager");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 450);
            frame.setLayout(new BorderLayout());

            websiteList = new JList<>(websiteListModel);
            JScrollPane scrollPane = new JScrollPane(websiteList);

            JPanel inputPanel = new JPanel(new GridLayout(4, 2));
            JTextField websiteField = new JTextField();
            JTextField usernameField = new JTextField();
            JPasswordField passwordField = new JPasswordField();
            JCheckBox showPasswordCheck = new JCheckBox("Show Password");

            inputPanel.add(new JLabel("Website:"));
            inputPanel.add(websiteField);
            inputPanel.add(new JLabel("Username:"));
            inputPanel.add(usernameField);
            inputPanel.add(new JLabel("Password:"));
            inputPanel.add(passwordField);
            inputPanel.add(new JLabel("")); // empty space
            inputPanel.add(showPasswordCheck);

            showPasswordCheck.addActionListener(e -> {
                if (showPasswordCheck.isSelected()) {
                    passwordField.setEchoChar((char) 0); // show characters
                } else {
                    passwordField.setEchoChar('*'); // mask characters
                }
            });

            JButton addButton = new JButton("Add");
            JButton getButton = new JButton("Get");
            JButton deleteButton = new JButton("Delete");
            JButton exportJson = new JButton("Export JSON");
            JButton exportCsv = new JButton("Export CSV");

            addButton.addActionListener(e -> {
                try {
                    String site = websiteField.getText().trim();
                    String user = usernameField.getText().trim();
                    String pass = new String(passwordField.getPassword()).trim();
                    if (site.isEmpty() || user.isEmpty() || pass.isEmpty())
                        return;

                    String encryptedPass = EncryptionUtils.encrypt(pass, aesKey);
                    PasswordEntry entry = new PasswordEntry(site, user, encryptedPass);
                    entries.add(entry);
                    websiteListModel.addElement(site);
                    websiteField.setText("");
                    usernameField.setText("");
                    passwordField.setText("");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            getButton.addActionListener(e -> {
                try {
                    String selected = websiteList.getSelectedValue();
                    for (PasswordEntry entry : entries) {
                        if (entry.getWebsite().equals(selected)) {
                            usernameField.setText(entry.getUsername());
                            passwordField.setText(EncryptionUtils.decrypt(entry.getEncryptedPassword(), aesKey));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            deleteButton.addActionListener(e -> {
                String selected = websiteList.getSelectedValue();
                entries.removeIf(e1 -> e1.getWebsite().equals(selected));
                websiteListModel.removeElement(selected);
            });

            exportJson.addActionListener(e -> exportToJson());
            exportCsv.addActionListener(e -> exportToCsv());

            JPanel buttons = new JPanel();
            buttons.add(addButton);
            buttons.add(getButton);
            buttons.add(deleteButton);
            buttons.add(exportJson);
            buttons.add(exportCsv);

            frame.add(inputPanel, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(buttons, BorderLayout.SOUTH);
            frame.setVisible(true);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    saveEntriesToEncryptedFile();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Startup failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String authenticate() throws Exception {
        File config = new File(CONFIG_FILE);
        if (!config.exists()) {
            JPasswordField pf = new JPasswordField();
            int result = JOptionPane.showConfirmDialog(null, pf, "Set Master Password", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String newPass = new String(pf.getPassword());
                try (FileOutputStream fos = new FileOutputStream(config)) {
                    fos.write(hashPassword(newPass));
                }
                return newPass;
            } else {
                throw new Exception("No password set");
            }
        } else {
            JPasswordField pf = new JPasswordField();
            int result = JOptionPane.showConfirmDialog(null, pf, "Enter Master Password", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String input = new String(pf.getPassword());
                byte[] storedHash = Files.readAllBytes(Paths.get(CONFIG_FILE));
                if (MessageDigest.isEqual(hashPassword(input), storedHash)) {
                    return input;
                } else {
                    throw new Exception("Incorrect Master Password");
                }
            } else {
                throw new Exception("Cancelled");
            }
        }
    }

    private static byte[] hashPassword(String password) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        return sha.digest(password.getBytes());
    }

    private static void loadEntriesFromEncryptedFile() throws Exception {
        File file = new File(PASSWORDS_FILE);
        if (!file.exists())
            return;
        byte[] encryptedData = Files.readAllBytes(Paths.get(PASSWORDS_FILE));
        @SuppressWarnings("unchecked")
        java.util.List<PasswordEntry> loaded = (java.util.List<PasswordEntry>) EncryptionUtils
                .decryptObject(encryptedData, aesKey);
        entries = loaded;
        for (PasswordEntry entry : entries) {
            websiteListModel.addElement(entry.getWebsite());
        }
    }

    private static void saveEntriesToEncryptedFile() throws Exception {
        byte[] encrypted = EncryptionUtils.encryptObject(entries, aesKey);
        Files.write(Paths.get(PASSWORDS_FILE), encrypted);
    }

    private static void exportToJson() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            java.util.List<Map<String, String>> exportList = new ArrayList<>();
            for (PasswordEntry e : entries) {
                Map<String, String> m = new HashMap<>();
                m.put("website", e.getWebsite());
                m.put("username", e.getUsername());
                m.put("password", EncryptionUtils.decrypt(e.getEncryptedPassword(), aesKey));
                exportList.add(m);
            }
            String json = gson.toJson(exportList);
            Files.write(Paths.get("export.json"), json.getBytes());
            JOptionPane.showMessageDialog(null, "Exported to export.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void exportToCsv() {
        try (PrintWriter out = new PrintWriter("export.csv")) {
            out.println("Website,Username,Password");
            for (PasswordEntry e : entries) {
                String decrypted = EncryptionUtils.decrypt(e.getEncryptedPassword(), aesKey);
                out.println(e.getWebsite() + "," + e.getUsername() + "," + decrypted);
            }
            JOptionPane.showMessageDialog(null, "Exported to export.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
