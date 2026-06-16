# File Security Tool (AES-GCM Encryption & Decryption)

A modernized, secure, and responsive Java Swing desktop application to encrypt and decrypt files.

---

## 🔒 Security & Performance Features

1. **Authenticated Encryption (AES-GCM):** Uses `AES/GCM/NoPadding` instead of default modes (like ECB). This ensures both confidentiality and integrity of your files. If a file is tampered with or the password is incorrect, the decryption will fail automatically.
2. **Password-Based Key Derivation (PBKDF2):** Keys are securely derived from your password using `PBKDF2WithHmacSHA256` with 65,536 iterations and a random 16-byte salt. This eliminates the vulnerability of storing static, hardcoded keys on disk (which previously got overwritten every session).
3. **Streaming File processing (Low Memory):** Files are processed in 8KB buffer streams, allowing you to encrypt/decrypt files of any size without causing `OutOfMemoryError` heap crashes.
4. **Responsive SwingWorker UI:** Long-running file operations are run on a background thread so the user interface remains responsive, featuring a real-time progress bar.
5. **Modern Drag-and-Drop Interface:** You can drag and drop any file directly into the interface to load it.

---

## 📂 File Structure

*   [EncryptionDecryptionGUI.java](file:///Users/abhyansh/Documents/GitHub/FileSecurityTool/E%26D/EncryptionDecryptionGUI.java): The user interface (Catppuccin Mocha themed dark mode, Drag & Drop panels, status logs).
*   [CryptoService.java](file:///Users/abhyansh/Documents/GitHub/FileSecurityTool/E%26D/CryptoService.java): The low-level encryption/decryption streaming handler.
*   [KeyManager.java](file:///Users/abhyansh/Documents/GitHub/FileSecurityTool/E%26D/KeyManager.java): Hashing and password-to-key generation logic.
*   [ProgressListener.java](file:///Users/abhyansh/Documents/GitHub/FileSecurityTool/E%26D/ProgressListener.java): Progress callback interface.

---

## 🚀 How to Run the App

If you have Java Development Kit (JDK) installed on your computer, you can run the program using your terminal:

1. **Open your terminal** in the `E&D` directory:
   ```bash
   cd E&D
   ```

2. **Compile all Java source files**:
   ```bash
   javac *.java
   ```

3. **Run the application**:
   ```bash
   java EncryptionDecryptionGUI
   ```
