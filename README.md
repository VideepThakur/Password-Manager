# 🔐 Password Manager Application (Java + Swing)

A secure and modern Java-based Password Manager with GUI built using **Swing**. This application allows you to securely store website credentials (website, username, and encrypted password) protected by AES encryption and a master password.

---

## ✨ Features

- ✅ **Master Password Setup**
  - On first launch, prompts to **set a master password**.
  - On subsequent launches, the user must enter the correct master password (verified via SHA-256 hash).

- 🔐 **AES Encryption**
  - All passwords are encrypted with AES using a key derived from the master password via PBKDF2.
  - The full password storage (`passwords.dat`) is encrypted and serialized securely.

- 💾 **Persistent Encrypted Storage**
  - Stores all entries (website, username, encrypted password) in `passwords.dat` (AES encrypted).
  - Master password hash is stored in `config.dat`.

- ➕ **Manage Credentials**
  - Add, retrieve, and delete website credentials via the GUI.

- 📤 **Export Options**
  - Export all passwords (decrypted) to:
    - `export.json` (pretty printed)
    - `export.csv` (comma-separated values)

- 📦 **Portable .exe Support**
  - The ready-to-use Windows `.exe` (`app.exe`) is available in the **`App/`** folder for one-click launch.

---

## 🛠️ Getting Started (for Developers)

### ✅ Prerequisites

- **Java 11 or later** (JDK)
- **[Gson Library](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar)**  
  *(Used for JSON export)*

### 📁 Folder Structure

```
Root/
├── App/
│   └── app.exe              <-- Executable launcher (Windows)
├── System/
│   ├── PasswordManagerApp.java
│   └── gson-2.10.1.jar
```

### 📥 Installing & Building

1. Navigate to the `System` folder.

2. **Compile**:

```bash
javac -cp gson-2.10.1.jar PasswordManagerApp.java
```

3. **Run**:

```bash
java -cp .;gson-2.10.1.jar PasswordManagerApp
```

> Note: On macOS/Linux, replace `;` with `:` in the classpath:
```bash
java -cp .:gson-2.10.1.jar PasswordManagerApp
```

---

## 🔐 Data Files

| File Name        | Purpose                                             |
|------------------|-----------------------------------------------------|
| `passwords.dat`  | Stores all encrypted credentials (AES)              |
| `config.dat`     | Stores SHA-256 hash of the master password          |
| `export.json`    | (Optional) JSON export of decrypted credentials     |
| `export.csv`     | (Optional) CSV export of decrypted credentials      |

---

## 📄 License

This is a personal project intended for educational and secure local use. Do not use it for storing sensitive information in production environments without additional security enhancements.
