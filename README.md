# 🔐 Password Manager Application (Java + Swing)

A secure Java-based password manager with Swing GUI featuring AES encryption and master password protection.

## ✨ Features

### 🔒 Security
- **Master Password System**
  - First-run setup with SHA-256 hash verification
  - PBKDF2 key derivation for encryption
- **AES-256 Encryption**
  - Encrypts all stored credentials
  - Encrypts the entire password database file

### 💻 Functionality
- **Credential Management**
  - Add new website credentials
  - View stored credentials (decrypted on-demand)
  - Delete existing entries
- **Data Export**
  - JSON export (`export.json`)
  - CSV export (`export.csv`)

### 📁 Data Storage
| File            | Format          | Contents                          |
|-----------------|-----------------|-----------------------------------|
| `passwords.dat` | AES-encrypted   | All user credentials              |
| `config.dat`    | Plaintext hash  | SHA-256 of master password        |

## 🛠️ Installation

### Requirements
- Java JDK 11+
- [Gson 2.10.1](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar)

### Setup
1. Download the application files
2. Place `gson-2.10.1.jar` in the `System` folder
3. Compile with:
   ```bash
   javac -cp gson-2.10.1.jar PasswordManagerApp.java
   ```

## 🚀 Usage

### Launching
```bash
# Windows:
java -cp .;gson-2.10.1.jar PasswordManagerApp

# Mac/Linux:
java -cp .:gson-2.10.1.jar PasswordManagerApp
```

### First Run
1. Set your master password
2. The system will create:
   - `config.dat` (password hash)
   - `passwords.dat` (empty encrypted database)

### Normal Operation
1. Enter master password to unlock
2. Use the GUI to:
   - Add credentials (auto-encrypts)
   - View credentials (decrypts temporarily)
   - Delete entries
   - Export data

## ⚠️ Security Notes
- The master password cannot be recovered
- Always keep backups of `passwords.dat`
- For production use, implement additional security measures

## 📜 License
Educational use only. Not recommended for sensitive production environments.

---
