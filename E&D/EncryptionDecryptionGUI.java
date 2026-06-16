import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;

/**
 * Modernized GUI for the File Encryption/Decryption Tool.
 * Follows a clean Dark Mode design (Catppuccin Mocha palette), uses background threads 
 * for operations, supports drag-and-drop file imports, and uses AES-GCM secure encryption.
 */
public class EncryptionDecryptionGUI extends JFrame {

    private static final Color COLOR_BG = new Color(30, 30, 46);       // Mocha Base
    private static final Color COLOR_PANEL = new Color(49, 50, 68);    // Mocha Surface0
    private static final Color COLOR_INPUT = new Color(17, 17, 27);    // Mocha Mantle
    private static final Color COLOR_TEXT = new Color(205, 214, 244);  // Mocha Text
    private static final Color COLOR_SUBTEXT = new Color(166, 173, 200); // Mocha Subtext
    private static final Color COLOR_ACCENT = new Color(137, 180, 250); // Mocha Blue
    private static final Color COLOR_ACCENT_HOVER = new Color(180, 190, 254); // Mocha Lavender
    private static final Color COLOR_GREEN = new Color(166, 227, 161);  // Mocha Green
    private static final Color COLOR_GREEN_HOVER = new Color(148, 202, 143);
    private static final Color COLOR_RED = new Color(243, 139, 168);    // Mocha Red

    private DropZonePanel dropZone;
    private JLabel dropZoneLabel;
    private JPasswordField passwordField;
    private ModernButton encryptButton;
    private ModernButton decryptButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    private File selectedFile;
    private boolean uiEnabled = true;

    public EncryptionDecryptionGUI() {
        setTitle("File Security Tool - AES-GCM");
        setSize(480, 520);
        setMinimumSize(new Dimension(440, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        // Root panel with padding
        JPanel rootPanel = new JPanel();
        rootPanel.setBackground(COLOR_BG);
        rootPanel.setLayout(new GridBagLayout());
        rootPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(rootPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // 1. Title
        JLabel titleLabel = new JLabel("FILE SECURITY TOOL", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(COLOR_TEXT);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);
        rootPanel.add(titleLabel, gbc);

        // 2. Drop Zone
        dropZone = new DropZonePanel();
        dropZoneLabel = new JLabel("", SwingConstants.CENTER);
        resetDropZoneLabel();
        dropZone.add(dropZoneLabel);
        
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 15, 0);
        rootPanel.add(dropZone, gbc);

        // Click to Browse event
        dropZone.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (uiEnabled) {
                    chooseFile();
                }
            }
        });

        // Setup Drag & Drop
        new DropTarget(dropZone, new DropTargetListener() {
            public void dragEnter(DropTargetDragEvent dtde) {
                if (uiEnabled) dropZone.setDragOver(true);
            }
            public void dragOver(DropTargetDragEvent dtde) {}
            public void dropActionChanged(DropTargetDragEvent dtde) {}
            public void dragExit(DropTargetEvent dte) {
                dropZone.setDragOver(false);
            }
            @SuppressWarnings("unchecked")
            public void drop(DropTargetDropEvent dtde) {
                dropZone.setDragOver(false);
                if (!uiEnabled) {
                    dtde.rejectDrop();
                    return;
                }
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> files = (java.util.List<File>) dtde.getTransferable()
                            .getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                    if (files != null && !files.isEmpty()) {
                        selectedFile = files.get(0);
                        updateFileDisplay();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 3. Password Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(COLOR_BG);
        
        JLabel passLabel = new JLabel("Encryption/Decryption Password:");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        passLabel.setForeground(COLOR_SUBTEXT);
        
        passwordField = new JPasswordField();
        passwordField.setBackground(COLOR_INPUT);
        passwordField.setForeground(COLOR_TEXT);
        passwordField.setCaretColor(COLOR_TEXT);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_PANEL, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.weightx = 1.0;
        formGbc.gridx = 0;
        
        formGbc.gridy = 0;
        formGbc.insets = new Insets(0, 0, 6, 0);
        formPanel.add(passLabel, formGbc);
        
        formGbc.gridy = 1;
        formGbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(passwordField, formGbc);

        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        rootPanel.add(formPanel, gbc);

        // 4. Action Buttons
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        actionPanel.setBackground(COLOR_BG);

        encryptButton = new ModernButton("Encrypt File", COLOR_ACCENT, COLOR_ACCENT_HOVER);
        decryptButton = new ModernButton("Decrypt File", COLOR_GREEN, COLOR_GREEN_HOVER);

        encryptButton.addActionListener(e -> performCryptoAction(true));
        decryptButton.addActionListener(e -> performCryptoAction(false));

        actionPanel.add(encryptButton);
        actionPanel.add(decryptButton);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        rootPanel.add(actionPanel, gbc);

        // 5. Progress and Status Panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(COLOR_BG);

        statusLabel = new JLabel("Ready", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(COLOR_SUBTEXT);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setBackground(COLOR_PANEL);
        progressBar.setForeground(COLOR_ACCENT);
        progressBar.setBorder(BorderFactory.createEmptyBorder());

        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(progressBar, BorderLayout.SOUTH);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        rootPanel.add(statusPanel, gbc);
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            updateFileDisplay();
        }
    }

    private void updateFileDisplay() {
        if (selectedFile != null) {
            String sizeStr = getFormattedSize(selectedFile.length());
            dropZoneLabel.setText("<html><center><font size='6' color='#A6E3A1'><b>✓</b></font><br><br><b>Selected File:</b><br><font color='#CDD6F4'>" 
                    + selectedFile.getName() + "</font><br><font size='2' color='#A6ADC8'>(" + sizeStr + ")</font></center></html>");
        } else {
            resetDropZoneLabel();
        }
    }

    private void resetDropZoneLabel() {
        dropZoneLabel.setText("<html><center><font size='6' color='#89B4FA'><b>📁</b></font><br><br><b>Drag & Drop File Here</b><br><font size='3' color='#A6ADC8'>or click to browse local files</font></center></html>");
    }

    private String getFormattedSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void setUiEnabled(boolean enabled) {
        this.uiEnabled = enabled;
        encryptButton.setEnabled(enabled);
        decryptButton.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        dropZone.setEnabled(enabled);
    }

    private void resetUi() {
        selectedFile = null;
        passwordField.setText("");
        resetDropZoneLabel();
        setUiEnabled(true);
    }

    private void performCryptoAction(boolean encrypt) {
        if (selectedFile == null) {
            showStatus("Please select a file first.", COLOR_RED);
            return;
        }
        char[] password = passwordField.getPassword();
        if (password.length == 0) {
            showStatus("Please enter a password.", COLOR_RED);
            return;
        }

        File destFile;
        if (encrypt) {
            destFile = new File(selectedFile.getPath() + ".enc");
        } else {
            String path = selectedFile.getPath();
            if (path.endsWith(".enc")) {
                destFile = new File(path.substring(0, path.length() - 4));
            } else {
                destFile = new File(path + ".decrypted");
            }
        }

        setUiEnabled(false);
        progressBar.setValue(0);
        progressBar.setVisible(true);
        showStatus(encrypt ? "Encrypting file..." : "Decrypting file...", COLOR_ACCENT);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (encrypt) {
                    CryptoService.encrypt(selectedFile, destFile, password, progress -> publish(progress));
                } else {
                    CryptoService.decrypt(selectedFile, destFile, password, progress -> publish(progress));
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int progress = chunks.get(chunks.size() - 1);
                progressBar.setValue(progress);
            }

            @Override
            protected void done() {
                Arrays.fill(password, ' '); // Clear password array immediately
                progressBar.setValue(100);
                try {
                    get();
                    showStatus(encrypt ? "Encryption successful!" : "Decryption successful!", COLOR_GREEN);
                    JOptionPane.showMessageDialog(EncryptionDecryptionGUI.this,
                            (encrypt ? "Successfully encrypted to:\n" : "Successfully decrypted to:\n") + destFile.getName(),
                            "Operation Completed", JOptionPane.INFORMATION_MESSAGE);
                    resetUi();
                    progressBar.setVisible(false);
                    showStatus("Ready", COLOR_SUBTEXT);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    cause.printStackTrace();
                    String errorMsg = cause.getMessage();
                    if (cause instanceof javax.crypto.AEADBadTagException) {
                        errorMsg = "Authentication failed. Incorrect password or corrupted data.";
                    }
                    showStatus("Error: " + errorMsg, COLOR_RED);
                    JOptionPane.showMessageDialog(EncryptionDecryptionGUI.this,
                            "Operation failed:\n" + errorMsg,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    progressBar.setValue(0);
                    progressBar.setVisible(false);
                    setUiEnabled(true);
                }
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        // Use standard cross-platform rendering for consistent theme look-and-feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            EncryptionDecryptionGUI gui = new EncryptionDecryptionGUI();
            gui.setVisible(true);
        });
    }

    // Custom UI Components for Premium Style

    private static class ModernButton extends JButton {
        private final Color normalColor;
        private final Color hoverColor;

        public ModernButton(String text, Color normalColor, Color hoverColor) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(COLOR_BG); // Dark text on bright buttons
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (!isEnabled()) {
                g2.setColor(COLOR_PANEL);
                setForeground(COLOR_SUBTEXT);
            } else if (getModel().isPressed()) {
                g2.setColor(normalColor.darker());
                setForeground(COLOR_BG);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverColor);
                setForeground(COLOR_BG);
            } else {
                g2.setColor(normalColor);
                setForeground(COLOR_BG);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(getForeground());
            g2.setFont(getFont());
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    private static class DropZonePanel extends JPanel {
        private boolean dragOver = false;
        private final Color borderColor = COLOR_ACCENT;
        private final Color dragColor = COLOR_ACCENT_HOVER;
        private final Color bgColor = COLOR_PANEL;

        public DropZonePanel() {
            setBackground(bgColor);
            setLayout(new GridBagLayout());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void setDragOver(boolean dragOver) {
            this.dragOver = dragOver;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw dashed border
            g2.setColor(dragOver ? dragColor : borderColor);
            float[] dash = {8.0f, 4.0f};
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            g2.drawRoundRect(6, 6, getWidth() - 13, getHeight() - 13, 16, 16);
            
            g2.dispose();
        }
    }
}
