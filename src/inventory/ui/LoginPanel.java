package inventory.ui;

import inventory.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private AuthService authService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Runnable onLoginSuccess;

    public LoginPanel(AuthService authService, Runnable onLoginSuccess) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;

        setLayout(new GridBagLayout());
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Grocery Store Inventory System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        add(usernameField, gbc);

        // Password
        gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        add(passwordField, gbc);

        // Login button
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        add(loginButton, gbc);

        // Info label
        gbc.gridy++;
        JLabel infoLabel = new JLabel("Default: admin / admin123");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        add(infoLabel, gbc);

        // Allow Enter key to login
        passwordField.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password",
                "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (authService.login(username, password)) {
            onLoginSuccess.run();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password",
                "Login Error", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
}