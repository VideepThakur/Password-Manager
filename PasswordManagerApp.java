import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

class PasswordEntry implements Serializable {
    private String website;
    private String username;
    private String encryptedPassword;

    public PasswordEntry(String site, String name, String pass) {
        this.website = site;
        this.username = name;
        this.encryptedPassword = pass;
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

    public void setEncryptedPassword(String newEncryptedPassword) {
        this.encryptedPassword = newEncryptedPassword;
    }
}

class PasswordManagerApp {
    private static List<PasswordEntry> entries = new ArrayList<>();
    private static DefaultListModel<String> websiteListModel = new DefaultListModel<>();
    private static JList<String> websiteList;
    private static boolean isAuthenticated = false;
    private static final String MASTER_PASSWORD_HASH = "56018fe135c84cb3af995473a79669e46d0a02b12b8b3141aba2d2ad19d66f70";
    private static final String PASSWORDS_FILE = "passwords.dat";

    public static void main(String[] args) {
        loadEntriesFromFile();
        checkMasterPassword();

        JFrame frame = new JFrame("Password Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        websiteList = new JList<>(websiteListModel);
        JScrollPane scrollPane = new JScrollPane(websiteList);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        JTextField websiteField = new JTextField();
        JTextField usernameField = new JTextField();
        JTextField passwordField = new JTextField();

        JLabel websiteLabel = new JLabel("Website:");
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");

        JButton addButton = new JButton("Add Entry");
        JButton retrieveButton = new JButton("Retrieve Entry");
        JButton deleteButton = new JButton("Delete Entry");

        // Disable buttons until authenticated
        addButton.setEnabled(false);
        retrieveButton.setEnabled(false);
        deleteButton.setEnabled(false);

        addButton.addActionListener(e -> {
            String website = websiteField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();

            PasswordEntry entry = new PasswordEntry(website, username, password);
            entries.add(entry);
            websiteListModel.addElement(website);

            // Clear input fields after adding entry
            websiteField.setText("");
            usernameField.setText("");
            passwordField.setText("");
        });

        retrieveButton.addActionListener(e -> {
            String website = websiteList.getSelectedValue();
            PasswordEntry entry = getPasswordEntry(website);

            if (entry != null) {
                usernameField.setText(entry.getUsername());
                passwordField.setText(entry.getEncryptedPassword());
            } else {
                usernameField.setText("");
                passwordField.setText("");
            }
        });

        deleteButton.addActionListener(e -> {
            String website = websiteList.getSelectedValue();
            PasswordEntry entry = getPasswordEntry(website);

            if (entry != null) {
                entries.remove(entry);
                websiteListModel.removeElement(website);
                websiteList.clearSelection();

                usernameField.setText("");
                passwordField.setText("");
            }
        });

        inputPanel.add(websiteLabel);
        inputPanel.add(websiteField);
        inputPanel.add(usernameLabel);
        inputPanel.add(usernameField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(retrieveButton);
        buttonPanel.add(deleteButton);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        Runtime.getRuntime().addShutdownHook(new Thread(PasswordManagerApp::saveEntriesToFile));

        if (isAuthenticated) {
            enableButtons(addButton, retrieveButton, deleteButton);
        }
    }

    private static void enableButtons(JButton... buttons) {
        for (JButton button : buttons) {
            button.setEnabled(true);
        }
    }

    private static void checkMasterPassword() {
        String inputPassword = JOptionPane.showInputDialog("Enter Master Password:");
        if (inputPassword != null && verifyPasswordHash(inputPassword)) {
            isAuthenticated = true;
        } else {
            JOptionPane.showMessageDialog(null, "Incorrect Master Password. Exiting.");
            System.exit(0);
        }
    }

    private static void loadEntriesFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PASSWORDS_FILE))) {
            entries = (List<PasswordEntry>) ois.readObject();
            for (PasswordEntry entry : entries) {
                websiteListModel.addElement(entry.getWebsite());
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    private static void saveEntriesToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PASSWORDS_FILE))) {
            oos.writeObject(entries);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    private static PasswordEntry getPasswordEntry(String website) {
        for (PasswordEntry entry : entries) {
            if (entry.getWebsite().equalsIgnoreCase(website)) {
                return entry;
            }
        }
        return null;
    }

    private static boolean verifyPasswordHash(String password) {
        try {
            byte[] inputHash = hashPassword(password);
            byte[] storedHash = hexStringToByteArray(MASTER_PASSWORD_HASH);
            return MessageDigest.isEqual(storedHash, inputHash);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error verifying master password hash: " + e.getMessage());
        }
        return false;
    }

    private static byte[] hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(password.getBytes());
    }

    private static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
