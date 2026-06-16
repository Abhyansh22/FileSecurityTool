import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;

/**
 * Handles authenticated streaming file encryption and decryption using AES-GCM.
 */
public class CryptoService {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int SALT_SIZE = 16;
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int BUFFER_SIZE = 8192;

    /**
     * Encrypts a file using AES-GCM and writes the output.
     * The output file header contains the random Salt (16 bytes) and random IV (12 bytes).
     *
     * @param source the original file to encrypt
     * @param dest the target encrypted file (.enc)
     * @param password the password used to derive the key
     * @param listener progress callback
     * @throws Exception if encryption fails
     */
    public static void encrypt(File source, File dest, char[] password, ProgressListener listener) throws Exception {
        byte[] salt = new byte[SALT_SIZE];
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        random.nextBytes(iv);

        SecretKey key = KeyManager.deriveKey(password, salt);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(dest)) {

            // Write salt and IV first
            fos.write(salt);
            fos.write(iv);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytesRead = 0;
            long fileSize = source.length();

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    fos.write(output);
                }
                totalBytesRead += bytesRead;
                if (listener != null && fileSize > 0) {
                    listener.onProgress((int) (totalBytesRead * 100 / fileSize));
                }
            }

            byte[] finalOutput = cipher.doFinal();
            if (finalOutput != null) {
                fos.write(finalOutput);
            }
        }
    }

    /**
     * Decrypts a file using AES-GCM.
     * The input file must have Salt and IV prepended.
     *
     * @param source the encrypted file
     * @param dest the target decrypted file
     * @param password the password used to derive the key
     * @param listener progress callback
     * @throws Exception if decryption or authentication fails
     */
    public static void decrypt(File source, File dest, char[] password, ProgressListener listener) throws Exception {
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(dest)) {

            byte[] salt = new byte[SALT_SIZE];
            byte[] iv = new byte[IV_SIZE];

            if (fis.read(salt) != SALT_SIZE || fis.read(iv) != IV_SIZE) {
                throw new IllegalArgumentException("File is corrupted or not in a valid encrypted format.");
            }

            SecretKey key = KeyManager.deriveKey(password, salt);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytesProcessed = SALT_SIZE + IV_SIZE;
            long fileSize = source.length();

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    fos.write(output);
                }
                totalBytesProcessed += bytesRead;
                if (listener != null && fileSize > 0) {
                    listener.onProgress((int) (totalBytesProcessed * 100 / fileSize));
                }
            }

            byte[] finalOutput = cipher.doFinal();
            if (finalOutput != null) {
                fos.write(finalOutput);
            }
        }
    }
}
