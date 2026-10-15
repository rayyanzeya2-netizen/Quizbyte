package com.quizbyte.ui;

import com.quizbyte.model.User;
import com.quizbyte.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern login and student registration panel.
 * Features a clean card aesthetic with seamless toggling between Login and Registration.
 */
public class LoginPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService;

    // Card switcher for Login vs Register
    private final CardLayout authCardLayout;
    private final JPanel authCardContainer;

    // Login Form Fields
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JComboBox<String> loginRolePicker;
    private JButton loginButton;

    // Register Form Fields
    private JTextField regUsernameField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;
    private JButton registerButton;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.authService = mainFrame.getAuthService();

        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_LIGHT);

        // Main Auth Card Container
        this.authCardLayout = new CardLayout();
        this.authCardContainer = new JPanel(authCardLayout);
        this.authCardContainer.setOpaque(false);

        JPanel loginCard = buildLoginCard();
        JPanel registerCard = buildRegisterCard();

        authCardContainer.add(loginCard, "LOGIN_MODE");
        authCardContainer.add(registerCard, "REGISTER_MODE");

        // Wrapper Panel with Branding Header
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setOpaque(false);
        wrapper.add(buildBrandingHeader(), BorderLayout.NORTH);
        wrapper.add(authCardContainer, BorderLayout.CENTER);

        add(wrapper);
    }

    private JPanel buildBrandingHeader() {
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);

        JLabel titleLabel = new JLabel("QUIZBYTE", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.FONT_HEADER_LARGE);
        titleLabel.setForeground(UITheme.PRIMARY);

        JLabel subtitleLabel = new JLabel("Enterprise Online Quiz Platform", SwingConstants.CENTER);
        subtitleLabel.setFont(UITheme.FONT_SUBTITLE);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        header.add(titleLabel);
        header.add(subtitleLabel);
        return header;
    }

    private JPanel buildLoginCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(380, 420));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Sign in to access your dashboard");
        desc.setFont(UITheme.FONT_SMALL);
        desc.setForeground(UITheme.TEXT_MUTED);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        loginUsernameField = UITheme.createTextField(20);
        loginPasswordField = UITheme.createPasswordField(20);
        loginRolePicker = new JComboBox<>(new String[]{"STUDENT", "ADMIN"});
        loginRolePicker.setFont(UITheme.FONT_BODY);
        loginRolePicker.setBackground(Color.WHITE);

        loginButton = UITheme.createPrimaryButton("Sign In");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JButton switchToRegisterBtn = new JButton("Don't have an account? Register Student");
        switchToRegisterBtn.setFont(UITheme.FONT_SMALL);
        switchToRegisterBtn.setForeground(UITheme.PRIMARY);
        switchToRegisterBtn.setBorderPainted(false);
        switchToRegisterBtn.setContentAreaFilled(false);
        switchToRegisterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        switchToRegisterBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Assemble
        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(desc);
        card.add(Box.createVerticalStrut(20));

        card.add(createFieldWrapper("Username", loginUsernameField));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldWrapper("Password", loginPasswordField));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldWrapper("Role Picker", loginRolePicker));
        card.add(Box.createVerticalStrut(22));

        card.add(loginButton);
        card.add(Box.createVerticalStrut(12));
        card.add(switchToRegisterBtn);

        // Action Listeners
        loginButton.addActionListener(e -> performLogin());
        loginPasswordField.addActionListener(e -> performLogin());
        switchToRegisterBtn.addActionListener(e -> authCardLayout.show(authCardContainer, "REGISTER_MODE"));

        return card;
    }

    private JPanel buildRegisterCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(380, 440));

        JLabel title = new JLabel("Create Student Account");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Join Quizbyte to challenge your knowledge");
        desc.setFont(UITheme.FONT_SMALL);
        desc.setForeground(UITheme.TEXT_MUTED);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        regUsernameField = UITheme.createTextField(20);
        regPasswordField = UITheme.createPasswordField(20);
        regConfirmPasswordField = UITheme.createPasswordField(20);

        registerButton = UITheme.createSuccessButton("Create Account");
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JButton switchToLoginBtn = new JButton("Already registered? Sign In");
        switchToLoginBtn.setFont(UITheme.FONT_SMALL);
        switchToLoginBtn.setForeground(UITheme.PRIMARY);
        switchToLoginBtn.setBorderPainted(false);
        switchToLoginBtn.setContentAreaFilled(false);
        switchToLoginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        switchToLoginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(desc);
        card.add(Box.createVerticalStrut(18));

        card.add(createFieldWrapper("New Username (min 3 chars)", regUsernameField));
        card.add(Box.createVerticalStrut(10));
        card.add(createFieldWrapper("Password (min 4 chars)", regPasswordField));
        card.add(Box.createVerticalStrut(10));
        card.add(createFieldWrapper("Confirm Password", regConfirmPasswordField));
        card.add(Box.createVerticalStrut(20));

        card.add(registerButton);
        card.add(Box.createVerticalStrut(12));
        card.add(switchToLoginBtn);

        // Action Listeners
        registerButton.addActionListener(e -> performRegistration());
        regConfirmPasswordField.addActionListener(e -> performRegistration());
        switchToLoginBtn.addActionListener(e -> authCardLayout.show(authCardContainer, "LOGIN_MODE"));

        return card;
    }

    private JPanel createFieldWrapper(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BODY_BOLD);
        label.setForeground(UITheme.TEXT_MAIN);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        return panel;
    }

    private void performLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword()).trim();
        String expectedRole = (String) loginRolePicker.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Authenticating...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Sign In");
                try {
                    boolean success = get();
                    if (success) {
                        User user = authService.getCurrentUser();
                        // Verify role matches selected role
                        if (!user.getRole().equalsIgnoreCase(expectedRole)) {
                            JOptionPane.showMessageDialog(LoginPanel.this,
                                    "Account role mismatch: Your account is registered as " + user.getRole() +
                                    ", not " + expectedRole + ".",
                                    "Role Mismatch", JOptionPane.WARNING_MESSAGE);
                            authService.logout();
                            return;
                        }

                        if (user.isAdmin()) {
                            mainFrame.showAdminDashboard();
                        } else {
                            mainFrame.showStudentDashboard();
                        }
                    } else {
                        JOptionPane.showMessageDialog(LoginPanel.this,
                                "Invalid username or password. Please check your credentials.",
                                "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginPanel.this,
                            "Database error during authentication: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void performRegistration() {
        String username = regUsernameField.getText().trim();
        String password = new String(regPasswordField.getPassword()).trim();
        String confirmPassword = new String(regConfirmPasswordField.getPassword()).trim();

        if (username.length() < 3) {
            JOptionPane.showMessageDialog(this, "Username must be at least 3 characters long.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return authService.register(username, password, "STUDENT");
            }

            @Override
            protected void done() {
                registerButton.setEnabled(true);
                registerButton.setText("Create Account");
                try {
                    boolean created = get();
                    if (created) {
                        JOptionPane.showMessageDialog(LoginPanel.this,
                                "Account created successfully! You can now log in with your credentials.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        loginUsernameField.setText(username);
                        loginPasswordField.setText("");
                        loginRolePicker.setSelectedItem("STUDENT");
                        authCardLayout.show(authCardContainer, "LOGIN_MODE");
                    } else {
                        JOptionPane.showMessageDialog(LoginPanel.this,
                                "Registration failed: Username '" + username + "' is already taken.",
                                "Registration Error", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginPanel.this,
                            "Database error during registration: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void resetFields() {
        loginUsernameField.setText("");
        loginPasswordField.setText("");
        regUsernameField.setText("");
        regPasswordField.setText("");
        regConfirmPasswordField.setText("");
        authCardLayout.show(authCardContainer, "LOGIN_MODE");
    }
}
