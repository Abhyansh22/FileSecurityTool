import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;

public class EncryptionDecryptionGUI extends JFrame implements ActionListener {

    private JButton encryptButton, decryptButton, exitButton;
    private JFileChooser fileChooser;
    private File selectedFile;

    public EncryptionDecryptionGUI() {
        setTitle("File Encryption/Decryption");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        fileChooser = new JFileChooser();
        encryptButton = new JButton("Encrypt File");
        decryptButton = new JButton("Decrypt File");
        exitButton = new JButton("Exit");

        encryptButton.addActionListener(this);
        decryptButton.addActionListener(this);
        exitButton.addActionListener(this);

        add(encryptButton);
        add(decryptButton);
        add(exitButton);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EncryptionDecryptionGUI gui = new EncryptionDecryptionGUI();
            gui.setVisible(true);
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == encryptButton) {
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                encryptFile(selectedFile);
            }
        } else if (e.getSource() == decryptButton) {
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                decryptFile(selectedFile);
            }
        } else if (e.getSource() == exitButton) {
            System.exit(0); // Exit the program
        }
    }

    private void encryptFile(File file) {
        try {
            Key key = generateKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] fileContent = readFile(file);
            byte[] encryptedData = cipher.doFinal(fileContent);

            writeFile(new File(file.getPath() + ".enc"), encryptedData);
            JOptionPane.showMessageDialog(this, "File encrypted successfully.");
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void decryptFile(File file) {
        try {
            Key key = retrieveKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] fileContent = readFile(file);
            byte[] decryptedData = cipher.doFinal(fileContent);

            writeFile(new File(file.getPath().replace(".enc", "")), decryptedData);
            JOptionPane.showMessageDialog(this, "File decrypted successfully.");
        } catch (Exception e) {
            handleError(e);
        }
    }

    private Key generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // Key size
        SecretKey secretKey = keyGen.generateKey();
        saveKey(secretKey);
        return secretKey;
    }

    private void saveKey(SecretKey key) throws Exception {
        byte[] keyBytes = key.getEncoded();
        writeFile(new File("secret.key"), keyBytes);
    }

    private Key retrieveKey() throws Exception {
        byte[] keyBytes = readFile(new File("secret.key"));
        return new SecretKeySpec(keyBytes, "AES");
    }

    private void handleError(Exception e) {
        JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    private byte[] readFile(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] fileContent = fis.readAllBytes();
        fis.close();
        return fileContent;
    }

    private void writeFile(File file, byte[] data) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
    }
}
