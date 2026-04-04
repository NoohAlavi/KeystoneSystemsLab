package inventory.ui;

import inventory.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPanel extends JPanel {
    private final AuthService authService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private final Runnable onLoginSuccess;

    // Consistency Theme
    private final Color DARK_BG = new Color(45, 52, 54);
    private final Color ACCENT_BLUE_GREEN = new Color(0, 184, 148);
    private final Font BUTTON_FONT = new Font("Helvetica", Font.BOLD, 14);

    public LoginPanel(AuthService authService, Runnable onLoginSuccess) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;

        setLayout(new BorderLayout());
        setBackground(DARK_BG);
        initComponents();
    }

    private void initComponents() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("KEYSTONE SYSTEMS");
        titleLabel.setFont(new Font("Helvetica", Font.BOLD, 28));
        titleLabel.setForeground(ACCENT_BLUE_GREEN);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel userLbl = new JLabel("Username:");
        userLbl.setForeground(Color.LIGHT_GRAY);
        userLbl.setFont(new Font("Helvetica", Font.PLAIN, 14));
        centerPanel.add(userLbl, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        usernameField.setBackground(new Color(60, 63, 65));
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);
        usernameField.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        centerPanel.add(usernameField, gbc);

        // Password
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(Color.LIGHT_GRAY);
        passLbl.setFont(new Font("Helvetica", Font.PLAIN, 14));
        centerPanel.add(passLbl, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setBackground(new Color(60, 63, 65));
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        centerPanel.add(passwordField, gbc);

        // Login button
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton loginButton = new JButton("LOGIN");
        loginButton.setFont(BUTTON_FONT);
        loginButton.setBackground(ACCENT_BLUE_GREEN);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(0, 40));
        loginButton.addActionListener(e -> handleLogin());
        centerPanel.add(loginButton, gbc);

        // Info label
        gbc.gridy++;
        JLabel infoLabel = new JLabel("System Access: admin / admin123");
        infoLabel.setForeground(new Color(150, 150, 150));
        infoLabel.setFont(new Font("Helvetica", Font.ITALIC, 11));
        centerPanel.add(infoLabel, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Allow Enter key to login
        passwordField.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter credentials", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (authService.login(username, password)) {
            onLoginSuccess.run();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials", "Login Error", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
}
