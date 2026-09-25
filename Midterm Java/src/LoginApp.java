import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class LoginApp extends JFrame {

    // Question 23 — Fix the Text File Format
    // We use a pipe delimiter '|' to separate username and password hash: username|passwordHash
    // split("\\|") uses regex escaping because '|' is a special character in regular expressions.
    private static final String USER_FILE = "users.txt";
    
    // Question 19 — Change Font
    // Using Segoe UI consistently across titles, fields, labels, and dialogs.
    private static final String FONT_NAME = "Segoe UI";

    // Question 3 — Change the Primary Color & Question 4 — Change the Application Background
    // Exam-compliant dark green palette and required RGB background.
    private static final Color PAGE = new Color(235, 238, 242);
    private static final Color WHITE = Color.WHITE;
    private static final Color PRIMARY = new Color(22, 101, 52);
    private static final Color PRIMARY_DARK = new Color(15, 76, 39);
    private static final Color PRIMARY_SOFT = new Color(232, 243, 235);
    private static final Color TEXT = new Color(15, 23, 42);
    private static final Color MUTED = new Color(92, 102, 96);
    private static final Color BORDER = new Color(203, 213, 225);
    private static final Color DANGER = new Color(180, 48, 48);
    private static final Color AMBER = new Color(180, 118, 26);
    private static final Color SHADOW = new Color(15, 23, 42, 28);

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckBox;
    private JButton eyeButton;
    private JLabel loginStatus;

    // ============================================================
    // MIDTERM IMPROVEMENT — TASK MANAGER
    // Stores tasks for the current running session.
    // ============================================================
    private final List<TaskItem> taskItems = new ArrayList<>();
    private JPanel taskListPanel;
    private String activeUsername;

    // ============================================================
    // PHASE 5 — EDITABLE CLASS SCHEDULE
    // Stores schedule entries for the logged-in account.
    // ============================================================
    private final List<ScheduleItem> scheduleItems = new ArrayList<>();
    private JPanel scheduleListPanel;

    // ============================================================
    // MIDTERM IMPROVEMENT — EDITABLE PROFILE
    // Stores editable student profile information separately from login credentials.
    // ============================================================
    private static final String DEFAULT_STUDENT_ID = "2026-0001";
    private static final String DEFAULT_PROGRAM = "BS Computer Science";
    private static final String DEFAULT_YEAR_LEVEL = "1st Year";
    private static final String DEFAULT_SECTION = "1-A";

    // ============================================================
    // MIDTERM IMPROVEMENT — GRADE / GPA CALCULATOR
    // Grades are saved locally for the current student account.
    // ============================================================
    private static final String[] SUBJECT_CODES = {
        "CIC 111", "CFP 110", "GE 100", "GE 7",
        "CPC 121", "GE 1", "NSTP 1", "PATHFIT 101"
    };

    private static final String[] SUBJECT_NAMES = {
        "Introduction to Computing",
        "Fundamentals in Programming",
        "Mathematics in the Modern World",
        "Understanding the Self",
        "Professional Issues in Computing",
        "Purposive Communication",
        "National Service Training Program 1",
        "Physical Activities Toward Health and Fitness 101"
    };

    private static final double[] SUBJECT_UNITS = {
        3, 3, 3, 3, 3, 3, 3, 2
    };

    public LoginApp() {
        createLoginUI();
    }

    // Load the custom logo from the project's resources folder.
    // The file-based path makes this work in a normal NetBeans project
    // where "resources" is beside the "src" folder.
    private ImageIcon loadLogo(int maxWidth, int maxHeight) {
        // Try several locations so the logo works when launched from NetBeans
        // or directly from the project folder.
        String[] paths = {
            "src/logo_white.png",
            "src/logo.png",
            "resources/logo_white.png",
            "resources/logo.png",
            "../resources/logo_white.png",
            "../resources/logo.png"
        };

        File file = null;
        for (String path : paths) {
            File candidate = new File(path);
            if (candidate.exists() && candidate.isFile()) {
                file = candidate;
                break;
            }
        }

        if (file == null) {
            System.out.println("Logo not found. Tried these locations:");
            for (String path : paths) {
                System.out.println("  " + new File(path).getAbsolutePath());
            }
            return null;
        }

        ImageIcon source = new ImageIcon(file.getAbsolutePath());
        if (source.getIconWidth() <= 0 || source.getIconHeight() <= 0) {
            System.out.println("Logo file could not be loaded: " + file.getAbsolutePath());
            return null;
        }

        Image original = source.getImage();
        double scale = Math.min(
                (double) maxWidth / original.getWidth(null),
                (double) maxHeight / original.getHeight(null)
        );

        int newWidth = Math.max(1, (int) (original.getWidth(null) * scale));
        int newHeight = Math.max(1, (int) (original.getHeight(null) * scale));

        Image scaled = original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    // ============================================================
    // LOGIN SCREEN
    // ============================================================
    private void createLoginUI() {
        // Question 1 — Change the Window Title
        setTitle("Student Login System");

        // Use the same custom logo for the application window icon.
        ImageIcon appLogo = loadLogo(64, 64);
        if (appLogo != null) {
            setIconImage(appLogo.getImage());
        }

        // Question 2 — Resize the Login Window
        // Resized window dimensions with centering via setLocationRelativeTo(null).
        setSize(1000, 600);
        setMinimumSize(new Dimension(960, 540));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(PAGE);
        root.setBorder(new EmptyBorder(26, 34, 26, 34));

        RoundedPanel shell = new RoundedPanel(28, WHITE);
        shell.setLayout(new BorderLayout());
        shell.setPreferredSize(new Dimension(920, 510));
        shell.setBorder(new EmptyBorder(0, 0, 0, 0));
        shell.setShadow(true);

        shell.add(createBrandPanel(), BorderLayout.WEST);
        shell.add(createLoginPanel(), BorderLayout.CENTER);

        root.add(shell);
        setContentPane(root);
        setVisible(true);
        
        // Question 10 — Cursor Should Return to Username
        // Standardized focus request ensuring caret sits in the username field upon display.
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    private JPanel createBrandPanel() {
        RoundedPanel brand = new RoundedPanel(28, PRIMARY);
        brand.setPreferredSize(new Dimension(400, 510));
        brand.setLayout(new GridBagLayout());
        brand.setBorder(new EmptyBorder(45, 48, 45, 48));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel logoWrap = new JPanel(new GridBagLayout());
        logoWrap.setOpaque(false);
        logoWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoWrap.setPreferredSize(new Dimension(104, 104));
        logoWrap.setMaximumSize(new Dimension(104, 104));
        ImageIcon logo = loadLogo(100, 80);
        if (logo != null) {
            JLabel logoLabel = new JLabel(logo);
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            logoWrap.add(logoLabel);
        }

        JLabel brandName = new JLabel("ACADEMIA ONE");
        brandName.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandName.setForeground(Color.WHITE);
        brandName.setFont(new Font(FONT_NAME, Font.BOLD, 15));

        JLabel welcome = new JLabel("Welcome Back");
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font(FONT_NAME, Font.BOLD, 34));

        JLabel headline = new JLabel("Your Academic Workspace");
        headline.setAlignmentX(Component.CENTER_ALIGNMENT);
        headline.setForeground(new Color(205, 226, 211));
        headline.setFont(new Font(FONT_NAME, Font.PLAIN, 16));

        JLabel description = new JLabel("<html><div style='text-align:center; width:220px;'>"
                + "Sign in to access your dashboard, schedules, and tools."
                + "</div></html>");
        description.setAlignmentX(Component.CENTER_ALIGNMENT);
        description.setForeground(new Color(224, 237, 228));
        description.setFont(new Font(FONT_NAME, Font.PLAIN, 14));

        content.add(logoWrap);
        content.add(Box.createVerticalStrut(24));
        content.add(brandName);
        content.add(Box.createVerticalStrut(12));
        content.add(welcome);
        content.add(Box.createVerticalStrut(6));
        content.add(headline);
        content.add(Box.createVerticalStrut(16));
        content.add(description);
        content.add(Box.createVerticalStrut(30));
        JLabel trust = new JLabel("Academic Access • Student Services");
        trust.setAlignmentX(Component.CENTER_ALIGNMENT);
        trust.setForeground(new Color(183, 214, 191));
        trust.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        content.add(trust);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        brand.add(content, gbc);
        return brand;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        
        // Expanded height bounds so the buttons are fully visible
        Dimension contentSize = new Dimension(380, 480);
        content.setPreferredSize(contentSize);
        content.setMinimumSize(contentSize);
        content.setMaximumSize(contentSize);

        JLabel eyebrow = new JLabel("STUDENT ACCESS");
        eyebrow.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        eyebrow.setForeground(PRIMARY);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Question 5 — Change the Login Heading
        JLabel title = new JLabel("Student Portal Login");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 32));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Enter your credentials to continue");
        subtitle.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(eyebrow);
        content.add(Box.createVerticalStrut(6));
        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(16));

        content.add(createFieldLabel("USERNAME"));
        content.add(Box.createVerticalStrut(6));
        
        usernameField = createTextField("Enter your username", new UserIcon());
        // Question 6 — Username Field Does Not Appear
        // The field must be added to its container, or it never becomes visible.
        content.add(usernameField);

        content.add(Box.createVerticalStrut(12));
        content.add(createFieldLabel("PASSWORD"));
        content.add(Box.createVerticalStrut(6));
        content.add(createPasswordRow());

        JPanel options = new JPanel(new BorderLayout());
        options.setOpaque(false);
        options.setMaximumSize(new Dimension(380, 25));
        options.setAlignmentX(Component.LEFT_ALIGNMENT);

        showPasswordCheckBox = new JCheckBox("Show password");
        showPasswordCheckBox.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        showPasswordCheckBox.setForeground(MUTED);
        showPasswordCheckBox.setOpaque(false);
        showPasswordCheckBox.setFocusPainted(false);
        // Question 8 — Show Password Is Not Working
        // The listener toggles the field's echo character so the checkbox actually does something.
        showPasswordCheckBox.addActionListener(e -> setPasswordVisible(showPasswordCheckBox.isSelected()));

        JLabel hint = new JLabel("Password is case-sensitive");
        hint.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        hint.setForeground(new Color(145, 155, 149));

        options.add(showPasswordCheckBox, BorderLayout.WEST);
        options.add(hint, BorderLayout.EAST);
        content.add(options);

        loginStatus = new JLabel(" ");
        loginStatus.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        loginStatus.setForeground(DANGER);
        loginStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(Box.createVerticalStrut(2));
        content.add(loginStatus);

        content.add(Box.createVerticalStrut(10));

        // Button Container - Explicit height to preserve full button view
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(380, 48));
        buttonPanel.setPreferredSize(new Dimension(380, 48));

        // Both buttons included clearly side by side
        // Question 17 — Add a Login Icon
        JButton loginButton = createPrimaryButton("SIGN IN", new ArrowIcon());
        loginButton.setPreferredSize(new Dimension(150, 42));
        loginButton.addActionListener(e -> login());
        getRootPane().setDefaultButton(loginButton);

        JButton clearButton = createSecondaryButton("CLEAR");
        clearButton.setPreferredSize(new Dimension(150, 42));
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);

        content.add(buttonPanel);
        content.add(Box.createVerticalStrut(14));

        JPanel registration = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        registration.setOpaque(false);
        registration.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel prompt = new JLabel("Don't have an account?");
        prompt.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        prompt.setForeground(MUTED);

        JButton createAccount = new JButton("Create Account");
        createAccount.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        createAccount.setForeground(PRIMARY);
        createAccount.setBorderPainted(false);
        createAccount.setContentAreaFilled(false);
        createAccount.setFocusPainted(false);
        createAccount.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createAccount.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { createAccount.setForeground(PRIMARY_DARK); }
            @Override public void mouseExited(MouseEvent e) { createAccount.setForeground(PRIMARY); }
        });
        // Question 21 — Registration Window Does Not Open
        createAccount.addActionListener(e -> openRegistration());

        registration.add(prompt);
        registration.add(createAccount);
        content.add(registration);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(content, gbc);
        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        label.setForeground(TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // Question 20 — Improve Text Field Appearance
    private JTextField createTextField(String placeholder, Icon icon) {
        JTextField field = new JTextField();
        field.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setCaretColor(PRIMARY);
        field.setBorder(new RoundedFieldBorder(BORDER, 1, 13, icon, placeholder));
        field.setPreferredSize(new Dimension(380, 48));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JPanel createPasswordRow() {
        JPanel row = new JPanel(new BorderLayout(9, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(380, 48));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Question 7 — Password Field Is Not Masked
        passwordField = new JPasswordField();
        passwordField.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        passwordField.setForeground(TEXT);
        passwordField.setBackground(WHITE);
        passwordField.setCaretColor(PRIMARY);
        passwordField.setEchoChar('\u2022'); // Mask character
        passwordField.setBorder(new RoundedFieldBorder(BORDER, 1, 13, new LockIcon(), "Enter your password"));
        passwordField.setPreferredSize(new Dimension(320, 48));
        passwordField.addActionListener(e -> login());

        // Question 16 — Add an Eye Icon/Button
        eyeButton = new JButton();
        eyeButton.setToolTipText("Show password");
        eyeButton.setPreferredSize(new Dimension(52, 48));
        eyeButton.setBackground(WHITE);
        eyeButton.setFocusPainted(false);
        eyeButton.setBorder(new RoundedFieldBorder(BORDER, 1, 13, null, null));
        eyeButton.setContentAreaFilled(true);
        eyeButton.setRolloverEnabled(false);
        eyeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeButton.setIcon(new EyeIcon(false));
        eyeButton.addActionListener(e -> togglePasswordVisibility());

        row.add(passwordField, BorderLayout.CENTER);
        row.add(eyeButton, BorderLayout.EAST);
        return row;
    }

    // ============================================================
    // LOGIN / AUTHENTICATION
    // ============================================================
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Question 11 — Empty Fields Validation
        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Please enter both username and password.", DANGER);
            usernameField.requestFocusInWindow();
            return;
        }

        String hashedPassword = hashPassword(password);
        
        // Question 12 & 13 — Login Messaging via JOptionPane
        // Use the username stored in users.txt as the canonical account name.
        // Login matching remains case-insensitive, but the displayed account identity
        // always uses the original capitalization chosen during registration.
        String canonicalUsername = findCanonicalUsername(username, hashedPassword);
        if (canonicalUsername != null) {
            JOptionPane.showMessageDialog(
                this, 
                "Welcome! You have successfully logged in.", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE
            );
            openDashboard(canonicalUsername);
        } else {
            JOptionPane.showMessageDialog(
                this, 
                "Login failed. Please check your username and password.", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE
            );
            passwordField.selectAll();
            passwordField.requestFocusInWindow();
        }
    }

    private void setStatus(String text, Color color) {
        loginStatus.setText(text);
        loginStatus.setForeground(color);
    }

    // Resolves any case-insensitive login to the exact username stored during registration.
    // Example: Aesc, AeSC, AesC, and aESC all resolve to the registered username Aesc.
    private String findCanonicalUsername(String username, String passwordHash) {
        File file = getDataFile(USER_FILE);
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2
                        && parts[0].trim().equalsIgnoreCase(username)
                        && parts[1].trim().equals(passwordHash)) {
                    return parts[0].trim();
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    // Question 24 — File Does Not Exist
    private boolean authenticate(String username, String passwordHash) {
        File file = getDataFile(USER_FILE);
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                
                // Question 14 — Debug the Username Comparison & Question 15 — Case-Insensitive Username
                // Avoid using '==' for String comparisons (checks reference equality).
                // Use equalsIgnoreCase() to compare string values while ignoring case sensitivity for usernames.
                if (parts.length == 2
                        && parts[0].trim().equalsIgnoreCase(username)
                        && parts[1].trim().equals(passwordHash)) {
                    return true;
                }
            }
        } catch (IOException e) {
            setStatus("Unable to access the local user database.", DANGER);
        }
        return false;
    }

    // ============================================================
    // REGISTRATION
    // ============================================================
    private void openRegistration() {
        final JDialog dialog = new JDialog(this, "Create Account", true);
        dialog.setSize(480, 580);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(PAGE);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        RoundedPanel card = new RoundedPanel(24, WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 32, 28, 32));
        card.setShadow(true);

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 26));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Set up your student portal credentials");
        sub.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(22));

        JLabel userLabel = createDialogLabel("USERNAME");
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(userLabel);
        card.add(Box.createVerticalStrut(6));
        
        JTextField regUser = createTextField("Choose a username", new UserIcon());
        regUser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        regUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(regUser);

        card.add(Box.createVerticalStrut(14));
        JLabel passLabel = createDialogLabel("PASSWORD");
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        
        JPasswordField regPass = createPasswordField("Create a password");
        regPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        regPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(regPass);

        card.add(Box.createVerticalStrut(14));
        JLabel confirmLabel = createDialogLabel("CONFIRM PASSWORD");
        confirmLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(confirmLabel);
        card.add(Box.createVerticalStrut(6));
        
        JPasswordField confirm = createPasswordField("Re-enter your password");
        confirm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        confirm.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(confirm);

        JPanel checkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkRow.setOpaque(false);
        checkRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        checkRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JCheckBox show = new JCheckBox("Show passwords");
        show.setOpaque(false);
        show.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        show.setForeground(MUTED);
        show.setFocusPainted(false);
        show.addActionListener(e -> {
            char echo = show.isSelected() ? (char) 0 : '\u2022';
            regPass.setEchoChar(echo);
            confirm.setEchoChar(echo);
        });
        checkRow.add(show);

        card.add(Box.createVerticalStrut(8));
        card.add(checkRow);

        JLabel error = new JLabel(" ");
        error.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        error.setForeground(DANGER);
        error.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(4));
        card.add(error);
        card.add(Box.createVerticalStrut(10));

        JButton create = createPrimaryButton("CREATE ACCOUNT", new CheckIcon());
        create.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        create.setAlignmentX(Component.CENTER_ALIGNMENT);
        create.addActionListener(e -> {
            String user = regUser.getText().trim();
            String pass = new String(regPass.getPassword());
            String confirmPass = new String(confirm.getPassword());

            if (user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                error.setText("Please complete all fields.");
                return;
            }
            if (user.length() < 3) {
                error.setText("Username must be at least 3 characters.");
                return;
            }
            if (pass.length() < 4) {
                error.setText("Password must be at least 4 characters.");
                return;
            }
            if (!pass.equals(confirmPass)) {
                error.setText("Passwords do not match.");
                return;
            }
            if (user.contains("|")) {
                error.setText("Username cannot contain the | character.");
                return;
            }
            
            // Question 22 — Duplicate Username Bug
            if (userExists(user)) {
                error.setText("That username is already registered.");
                return;
            }

            if (registerUser(user, pass)) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Account created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                usernameField.setText(user);
                passwordField.setText("");
                setStatus("Account created. You can now sign in.", PRIMARY);
                dialog.dispose();
                passwordField.requestFocusInWindow();
            } else {
                error.setText("Unable to create the account. Please try again.");
            }
        });
        card.add(create);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        root.add(card, gbc);

        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(create);
        dialog.setVisible(true);
    }

    private JLabel createDialogLabel(String text) {
        return createFieldLabel(text);
    }

    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setCaretColor(PRIMARY);
        field.setEchoChar('\u2022');
        field.setBorder(new RoundedFieldBorder(BORDER, 1, 13, new LockIcon(), placeholder));
        field.setPreferredSize(new Dimension(450, 46));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    // Question 22 — Duplicate Username Check
    private boolean userExists(String username) {
        File file = getDataFile(USER_FILE);
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2 && parts[0].trim().equalsIgnoreCase(username)) return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    // Question 24 — File Auto-Creation
    // Creates 'users.txt' if non-existent when appending a new registration entry.
    private boolean registerUser(String username, String password) {
        File file = getDataFile(USER_FILE);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            writer.write(username + "|" + hashPassword(password));
            writer.newLine();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String value = Integer.toHexString(0xff & b);
                if (value.length() == 1) hex.append('0');
                hex.append(value);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // ============================================================
    // DASHBOARD & FULL UI DESIGN (Question 25)
    // ============================================================
    private void openDashboard(String username) {
        // username is already canonicalized by login(). Keep it unchanged for all
        // account-specific files so different login capitalization shares one account.
        activeUsername = username;
        JFrame dashboard = new JFrame("Academia One — Dashboard");
        dashboard.getRootPane().putClientProperty("loggedInUsername", username);
        dashboard.setSize(1280, 780);
        dashboard.setMinimumSize(new Dimension(1080, 680));
        dashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashboard.setLocationRelativeTo(null);
        dashboard.setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE);

        CardLayout contentLayout = new CardLayout();
        JPanel content = new JPanel(contentLayout);
        content.setBackground(PAGE);

        // ============================================================
        // MIDTERM IMPROVEMENT — PERSISTENT TASKS
        // Load the account's saved tasks. Sample tasks are created only once for
        // a new account and are not recreated after the user deletes them.
        // ============================================================
        loadTasks(username);
        loadSchedule(username);

        content.add(createDashboardMain(username, dashboard, contentLayout, content), "dashboard");
        JPanel profilePage = createProfilePage(username, content, contentLayout);
        profilePage.setName("profilePage");
        content.add(profilePage, "profile");
        content.add(createAcademicPage(username), "academic");
        content.add(createSchedulePage(username), "schedule");
        content.add(createAnnouncementsPage(), "announcements");
        content.add(createTasksPage(), "tasks");
        content.add(createSettingsPage(username, dashboard), "settings");

        JPanel sidebar = createDashboardSidebar(username, dashboard, contentLayout, content);
        root.add(sidebar, BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);

        dashboard.setContentPane(root);
        dashboard.setVisible(true);
        dispose();
    }

    private JPanel createDashboardSidebar(String username, JFrame dashboard, CardLayout layout, JPanel content) {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(PRIMARY_DARK);
        sidebar.setPreferredSize(new Dimension(245, 780));
        sidebar.setBorder(new EmptyBorder(22, 14, 18, 14));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        brand.setOpaque(false);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        ImageIcon sidebarLogo = loadLogo(42, 42);
        if (sidebarLogo != null) {
            brand.add(new JLabel(sidebarLogo));
        }
        JLabel brandText = new JLabel("Academia One");
        brandText.setForeground(Color.WHITE);
        brandText.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        brand.add(brandText);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(27));

        sidebar.add(createSideSectionLabel("MAIN"));
        sidebar.add(Box.createVerticalStrut(7));

        List<JButton> navButtons = new ArrayList<>();
        JButton dashboardButton = createNavItem("Dashboard", new HomeIcon(), true);
        JButton profileButton = createNavItem("My Profile", new UserIcon(), false);
        JButton academicButton = createNavItem("Academic Overview", new BookIcon(), false);
        navButtons.add(dashboardButton);
        navButtons.add(profileButton);
        navButtons.add(academicButton);
        sidebar.add(dashboardButton);
        sidebar.add(profileButton);
        sidebar.add(academicButton);

        JButton tasksButton = createNavItem("My Tasks", new CheckIcon(), false);
        navButtons.add(tasksButton);
        sidebar.add(tasksButton);

        sidebar.add(Box.createVerticalStrut(19));
        sidebar.add(createSideSectionLabel("SERVICES"));
        sidebar.add(Box.createVerticalStrut(7));

        JButton scheduleButton = createNavItem("Class Schedule", new CalendarIcon(), false);
        JButton announcementsButton = createNavItem("Announcements", new BellIcon(), false);
        JButton settingsButton = createNavItem("Settings", new GearIcon(), false);
        navButtons.add(scheduleButton);
        navButtons.add(announcementsButton);
        navButtons.add(settingsButton);
        sidebar.add(scheduleButton);
        // FIX 1: Added directly without wrapping in a red badge
        sidebar.add(announcementsButton);
        sidebar.add(settingsButton);

        attachNavAction(dashboardButton, "dashboard", layout, content, navButtons);
        attachNavAction(profileButton, "profile", layout, content, navButtons);
        attachNavAction(academicButton, "academic", layout, content, navButtons);
        attachNavAction(tasksButton, "tasks", layout, content, navButtons);
        attachNavAction(scheduleButton, "schedule", layout, content, navButtons);
        attachNavAction(announcementsButton, "announcements", layout, content, navButtons);
        attachNavAction(settingsButton, "settings", layout, content, navButtons);

        sidebar.add(Box.createVerticalGlue());

        RoundedPanel userCard = new RoundedPanel(15, new Color(255, 255, 255, 20));
        userCard.setLayout(new BorderLayout(9, 0));
        userCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        userCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        userCard.setBorder(new EmptyBorder(8, 9, 8, 9));

        ProfileData sidebarProfile = loadProfile(username);
        JLabel avatar = createAvatar(getInitial(sidebarProfile.fullName), 38, Color.WHITE, PRIMARY_DARK, 15);
        avatar.setName("sidebarProfileAvatar");
        JPanel avatarHolder = new JPanel(new GridBagLayout());
        avatarHolder.setOpaque(false);
        avatarHolder.setPreferredSize(new Dimension(38, 38));
        avatarHolder.setMinimumSize(new Dimension(38, 38));
        avatarHolder.setMaximumSize(new Dimension(38, 38));
        avatarHolder.add(avatar);
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        JLabel name = createWrappedNameLabel(sidebarProfile.fullName, 12, Color.WHITE, 125, "sidebarProfileName");
        JLabel role = new JLabel("Student account");
        role.setForeground(new Color(205, 225, 211));
        role.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        names.add(name);
        names.add(Box.createVerticalStrut(2));
        names.add(role);
        userCard.add(avatarHolder, BorderLayout.WEST);
        userCard.add(names, BorderLayout.CENTER);
        sidebar.add(userCard);
        sidebar.add(Box.createVerticalStrut(7));

        JButton logout = createSidebarButton("Sign out", new LogoutIcon());
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    dashboard,
                    "Are you sure you want to sign out?",
                    "Sign out",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                dashboard.dispose();
                new LoginApp();
            }
        });
        sidebar.add(logout);
        return sidebar;
    }

    private void attachNavAction(JButton button, String card, CardLayout layout, JPanel content, List<JButton> buttons) {
        button.addActionListener(e -> {
            layout.show(content, card);
            for (JButton b : buttons) {
                b.putClientProperty("selected", b == button);
                updateNavAppearance(b, b == button);
            }
        });
    }

    private void updateNavAppearance(JButton button, boolean selected) {
        Color foreground = selected ? PRIMARY_DARK : new Color(220, 236, 224);
        button.setForeground(foreground);
        button.setBackground(selected ? Color.WHITE : new Color(255, 255, 255, 0));
        button.setOpaque(selected);
        button.setContentAreaFilled(selected);
        button.setBorder(new EmptyBorder(11, 13, 11, 13));
        if (button.getIcon() instanceof SimpleIcon) {
            ((SimpleIcon) button.getIcon()).setColor(foreground);
        }
        button.repaint();
    }

    private JLabel createAvatar(String text, int size, Color bg, Color fg, int fontSize) {
        JLabel avatar = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setOpaque(false);
        avatar.setForeground(fg);
        avatar.setFont(new Font(FONT_NAME, Font.BOLD, fontSize));
        avatar.setPreferredSize(new Dimension(size, size));
        avatar.setMinimumSize(new Dimension(size, size));
        avatar.setMaximumSize(new Dimension(size, size));
        return avatar;
    }

    private JLabel createSideSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        label.setForeground(new Color(169, 201, 178));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JButton createNavItem(String text, Icon icon, boolean selected) {
        JButton button = new JButton(text, icon);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(10);
        button.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        
        Color defaultFg = new Color(220, 236, 224);
        Color selectedFg = PRIMARY_DARK;
        
        button.setForeground(selected ? selectedFg : defaultFg);
        button.setBackground(selected ? Color.WHITE : new Color(255, 255, 255, 0));
        button.setOpaque(selected);
        button.setBorder(new EmptyBorder(10, 10, 10, 10));
        button.setFocusPainted(false);
        button.setContentAreaFilled(selected);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setPreferredSize(new Dimension(380, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.putClientProperty("selected", selected);
        
        if (icon instanceof SimpleIcon) {
            ((SimpleIcon) icon).setColor(selected ? selectedFg : defaultFg);
        }

        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!Boolean.TRUE.equals(button.getClientProperty("selected"))) {
                    button.setBackground(new Color(255, 255, 255, 40));
                    button.setForeground(Color.WHITE);
                    button.setOpaque(true);
                    button.setContentAreaFilled(true);
                    if (button.getIcon() instanceof SimpleIcon) {
                        ((SimpleIcon) button.getIcon()).setColor(Color.WHITE);
                    }
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!Boolean.TRUE.equals(button.getClientProperty("selected"))) {
                    button.setBackground(new Color(255, 255, 255, 0));
                    button.setForeground(defaultFg);
                    button.setOpaque(false);
                    button.setContentAreaFilled(false);
                    if (button.getIcon() instanceof SimpleIcon) {
                        ((SimpleIcon) button.getIcon()).setColor(defaultFg);
                    }
                }
            }
        });
        return button;
    }

    private JButton createSidebarButton(String text, Icon icon) {
        JButton button = new JButton(text, icon);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(12);
        button.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        button.setForeground(new Color(226, 237, 229));
        button.setBackground(new Color(255, 255, 255, 0));
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(11, 13, 11, 13));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(255, 255, 255, 20));
                button.setOpaque(true);
                button.setContentAreaFilled(true);
            }
            @Override public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(255, 255, 255, 0));
                button.setOpaque(false);
                button.setContentAreaFilled(false);
            }
        });
        return button;
    }

    // ============================================================
    // PREMIUM DASHBOARD
    // The dashboard is intentionally information-dense but organized:
    // summary -> progress -> today's classes -> tasks/announcements.
    // ============================================================
    private String getSavedGpaText(String username) {
        if (username == null || username.trim().isEmpty()) return "--";

        File file = getGradesFile(username);
        if (!file.exists()) return "--";

        double weightedTotal = 0;
        double totalUnits = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                if (parts.length != 2 || parts[1].trim().isEmpty()) continue;

                try {
                    double grade = Double.parseDouble(parts[1].trim());
                    if (grade >= 1.00 && grade <= 5.00) {
                        for (int i = 0; i < SUBJECT_CODES.length; i++) {
                            if (SUBJECT_CODES[i].equals(parts[0])) {
                                weightedTotal += grade * SUBJECT_UNITS[i];
                                totalUnits += SUBJECT_UNITS[i];
                                break;
                            }
                        }
                    }
                } catch (NumberFormatException ignored) {
                    // Invalid saved values are ignored until corrected in the GPA page.
                }
            }
        } catch (IOException e) {
            return "--";
        }

        return totalUnits == 0 ? "--" : String.format("%.2f", weightedTotal / totalUnits);
    }

    private JPanel createDashboardMain(String username, JFrame dashboard, CardLayout contentLayout, JPanel content) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(PAGE);

        JPanel main = new JPanel();
        main.setBackground(PAGE);
        main.setBorder(new EmptyBorder(28, 32, 30, 32));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        // Header
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JPanel greeting = new JPanel();
        greeting.setOpaque(false);
        greeting.setLayout(new BoxLayout(greeting, BoxLayout.Y_AXIS));
        ProfileData dashboardProfile = loadProfile(username);
        JLabel hello = new JLabel("Good day, " + dashboardProfile.fullName + "!");
        hello.setName("dashboardGreeting");
        hello.setFont(new Font(FONT_NAME, Font.BOLD, 30));
        hello.setForeground(TEXT);
        JLabel subtitle = new JLabel("Here's what is happening in your academic life today.");
        subtitle.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        greeting.add(hello);
        greeting.add(Box.createVerticalStrut(5));
        greeting.add(subtitle);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        headerRight.setOpaque(false);
        JButton notification = new JButton(new BellIcon());
        notification.setToolTipText("View announcements");
        notification.setPreferredSize(new Dimension(42, 42));
        notification.setFocusPainted(false);
        notification.setBorderPainted(false);
        notification.setContentAreaFilled(true);
        notification.setBackground(PRIMARY_SOFT);
        notification.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notification.addActionListener(e -> contentLayout.show(content, "announcements"));
        JLabel semester = new JLabel("AY 2026–2027  •  1st Semester");
        semester.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        semester.setForeground(MUTED);
        headerRight.add(notification);
        headerRight.add(semester);

        header.add(greeting, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);
        main.add(header);
        main.add(Box.createVerticalStrut(20));

        // Four high-level metrics.
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        stats.add(createPremiumStat("ATTENDANCE", "92%", "3 absences this term", new CalendarIcon(), PRIMARY));
        stats.add(createPremiumStat("SUBJECTS", String.valueOf(SUBJECT_CODES.length), "Currently enrolled", new BookIcon(), PRIMARY));
        stats.add(createPremiumStat("GPA", getSavedGpaText(username), "Saved grade average", new CheckIcon(), PRIMARY));
        stats.add(createPremiumStat("TASKS", "3", "Due this week", new CodeIcon(), PRIMARY));
        main.add(stats);
        main.add(Box.createVerticalStrut(18));

        // Academic progress + today's attendance status.
        JPanel progressRow = new JPanel(new GridLayout(1, 2, 18, 0));
        progressRow.setOpaque(false);
        progressRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 178));
        progressRow.add(createProgressCard());
        progressRow.add(createAttendanceSummaryCard());
        main.add(progressRow);
        main.add(Box.createVerticalStrut(18));

        // Main working area.
        JPanel workRow = new JPanel(new GridLayout(1, 2, 18, 0));
        workRow.setOpaque(false);
        workRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 282));
        workRow.add(createUpcomingWorkCard(contentLayout, content));
        workRow.add(createTodayScheduleCard(username, contentLayout, content));
        main.add(workRow);
        main.add(Box.createVerticalStrut(18));

        // Bottom area: announcements + quick actions.
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 18, 0));
        bottomRow.setOpaque(false);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 185));
        bottomRow.add(createRecentAnnouncementsCard(contentLayout, content));
        bottomRow.add(createDashboardQuickActions(contentLayout, content));
        main.add(bottomRow);

        JScrollPane scroll = new JScrollPane(main);
        scroll.setBorder(null);
        scroll.setBackground(PAGE);
        scroll.getViewport().setBackground(PAGE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    private JPanel createPremiumStat(String label, String value, String detail, Icon icon, Color accent) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(16, 17, 16, 17));
        card.setShadow(true);

        RoundedPanel iconBox = new RoundedPanel(14, PRIMARY_SOFT);
        iconBox.setPreferredSize(new Dimension(46, 46));
        iconBox.setLayout(new GridBagLayout());
        iconBox.add(new JLabel(icon));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        l.setForeground(MUTED);
        JLabel v = new JLabel(value);
        v.setFont(new Font(FONT_NAME, Font.BOLD, 23));
        v.setForeground(TEXT);
        JLabel d = new JLabel(detail);
        d.setFont(new Font(FONT_NAME, Font.PLAIN, 9));
        d.setForeground(accent);
        text.add(l);
        text.add(Box.createVerticalStrut(3));
        text.add(v);
        text.add(Box.createVerticalStrut(1));
        text.add(d);
        card.add(iconBox, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel createProgressCard() {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 20, 17, 20));
        card.setShadow(true);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Academic progress");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 17));
        title.setForeground(TEXT);
        JLabel value = new JLabel("78%");
        value.setFont(new Font(FONT_NAME, Font.BOLD, 17));
        value.setForeground(PRIMARY);
        head.add(title, BorderLayout.WEST);
        head.add(value, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel desc = new JLabel("Overall course completion");
        desc.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        desc.setForeground(MUTED);
        center.add(desc);
        center.add(Box.createVerticalStrut(10));
        center.add(createProgressBar(78));
        center.add(Box.createVerticalStrut(9));
        JPanel legend = new JPanel(new BorderLayout());
        legend.setOpaque(false);
        JLabel completed = new JLabel("Completed  •  31 of 40 learning activities");
        completed.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        completed.setForeground(MUTED);
        JLabel target = new JLabel("Target 80%");
        target.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        target.setForeground(TEXT);
        legend.add(completed, BorderLayout.WEST);
        legend.add(target, BorderLayout.EAST);
        center.add(legend);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel createAttendanceSummaryCard() {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(18, 20, 17, 20));
        card.setShadow(true);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Attendance this term");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 17));
        title.setForeground(TEXT);
        JLabel status = new JLabel("ON TRACK");
        status.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        status.setForeground(PRIMARY);
        head.add(title, BorderLayout.WEST);
        head.add(status, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 3, 10, 0));
        center.setOpaque(false);
        center.add(createMiniMetric("PRESENT", "34", PRIMARY));
        center.add(createMiniMetric("ABSENT", "3", DANGER));
        center.add(createMiniMetric("UNMARKED", "1", AMBER));
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMiniMetric(String label, String value, Color accent) {
        RoundedPanel box = new RoundedPanel(12, PAGE);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(10, 10, 9, 10));
        JLabel v = new JLabel(value);
        v.setAlignmentX(Component.CENTER_ALIGNMENT);
        v.setFont(new Font(FONT_NAME, Font.BOLD, 20));
        v.setForeground(accent);
        JLabel l = new JLabel(label);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setFont(new Font(FONT_NAME, Font.BOLD, 8));
        l.setForeground(MUTED);
        box.add(v);
        box.add(Box.createVerticalStrut(2));
        box.add(l);
        return box;
    }

    private JPanel createProgressBar(int percent) {
        JPanel track = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(225, 232, 227));
                g2.fillRoundRect(0, 0, w, h, h, h);
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, Math.max(h, w * percent / 100), h, h, h);
                g2.dispose();
            }
        };
        track.setOpaque(false);
        track.setPreferredSize(new Dimension(100, 10));
        track.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        return track;
    }

    private JPanel createUpcomingWorkCard(CardLayout layout, JPanel content) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(18, 20, 17, 20));
        card.setShadow(true);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Upcoming work");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 17));
        title.setForeground(TEXT);
        JButton view = createTextLink("View academic");
        view.addActionListener(e -> layout.show(content, "academic"));
        head.add(title, BorderLayout.WEST);
        head.add(view, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(createTaskRow("Data Structures", "Programming assignment", "Tomorrow", PRIMARY));
        list.add(Box.createVerticalStrut(7));
        list.add(createTaskRow("Discrete Mathematics", "Problem set #4", "Sep 21", AMBER));
        list.add(Box.createVerticalStrut(7));
        list.add(createTaskRow("Human Computer Interaction", "UI prototype", "Sep 23", PRIMARY));
        list.add(Box.createVerticalStrut(7));
        list.add(createTaskRow("National Service Training", "Reflection paper", "Sep 25", MUTED));
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTaskRow(String subject, String task, String due, Color accent) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        JLabel marker = new JLabel("•");
        marker.setFont(new Font(FONT_NAME, Font.BOLD, 22));
        marker.setForeground(accent);
        marker.setPreferredSize(new Dimension(16, 35));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel a = new JLabel(subject);
        a.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        a.setForeground(TEXT);
        JLabel b = new JLabel(task);
        b.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        b.setForeground(MUTED);
        text.add(a);
        text.add(Box.createVerticalStrut(2));
        text.add(b);

        JLabel d = new JLabel(due);
        d.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        d.setForeground(accent);
        row.add(marker, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(d, BorderLayout.EAST);
        return row;
    }

    private JPanel createTodayScheduleCard(String username, CardLayout layout, JPanel content) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(18, 20, 17, 20));
        card.setShadow(true);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Today's schedule");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 17));
        title.setForeground(TEXT);
        JButton view = createTextLink("Full schedule");
        view.addActionListener(e -> layout.show(content, "schedule"));
        head.add(title, BorderLayout.WEST);
        head.add(view, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        int shown = Math.min(4, scheduleItems.size());
        for (int i = 0; i < shown; i++) {
            ScheduleItem item = scheduleItems.get(i);
            String tag = i == 0 ? "NOW" : (i == 1 ? "UP NEXT" : item.time);
            list.add(createScheduleCompact(item.time, item.subject, item.room, tag, i < 2 ? PRIMARY : MUTED));
            if (i < shown - 1) list.add(Box.createVerticalStrut(6));
        }
        if (shown == 0) {
            JLabel empty = new JLabel("No classes scheduled. Add one in Class Schedule.");
            empty.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            empty.setForeground(MUTED);
            list.add(empty);
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel createScheduleCompact(String time, String subject, String room, String tag, Color accent) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        timeLabel.setForeground(accent);
        timeLabel.setPreferredSize(new Dimension(65, 35));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel a = new JLabel(subject);
        a.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        a.setForeground(TEXT);
        JLabel b = new JLabel(room);
        b.setFont(new Font(FONT_NAME, Font.PLAIN, 9));
        b.setForeground(MUTED);
        text.add(a);
        text.add(Box.createVerticalStrut(2));
        text.add(b);

        JLabel badge = new JLabel(tag);
        badge.setFont(new Font(FONT_NAME, Font.BOLD, 8));
        badge.setForeground(accent);
        row.add(timeLabel, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(badge, BorderLayout.EAST);
        return row;
    }

    private JPanel createRecentAnnouncementsCard(CardLayout layout, JPanel content) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(18, 20, 17, 20));
        card.setShadow(true);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Recent announcements");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 17));
        title.setForeground(TEXT);
        JButton view = createTextLink("See all");
        view.addActionListener(e -> layout.show(content, "announcements"));
        head.add(title, BorderLayout.WEST);
        head.add(view, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(createAnnouncementCompact("Midterm examination schedule", "Academic Office  •  Today", true));
        list.add(Box.createVerticalStrut(7));
        list.add(createAnnouncementCompact("Classroom change for Programming", "Department Office  •  Yesterday", false));
        list.add(Box.createVerticalStrut(7));
        list.add(createAnnouncementCompact("Library orientation this Friday", "Student Services  •  Sep 16", false));
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel createAnnouncementCompact(String title, String meta, boolean important) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel dot = new JLabel(important ? "●" : "○");
        dot.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        dot.setForeground(important ? PRIMARY : BORDER);
        dot.setPreferredSize(new Dimension(15, 30));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel a = new JLabel(title);
        a.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        a.setForeground(TEXT);
        JLabel b = new JLabel(meta);
        b.setFont(new Font(FONT_NAME, Font.PLAIN, 9));
        b.setForeground(MUTED);
        text.add(a);
        text.add(Box.createVerticalStrut(2));
        text.add(b);
        row.add(dot, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private JPanel createDashboardQuickActions(CardLayout layout, JPanel content) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(18, 20, 17, 20));
        card.setShadow(true);
        JLabel title = new JLabel("Quick access");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 17));
        title.setForeground(TEXT);
        card.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 8, 8));
        grid.setOpaque(false);
        grid.add(createDashboardAction("My Profile", new UserIcon(PRIMARY_DARK), "profile", layout, content));
        grid.add(createDashboardAction("Academics", new BookIcon(PRIMARY_DARK), "academic", layout, content));
        grid.add(createDashboardAction("Schedule", new CalendarIcon(PRIMARY_DARK), "schedule", layout, content));
        grid.add(createDashboardAction("Announcements", new BellIcon(PRIMARY_DARK), "announcements", layout, content));
        grid.add(createDashboardAction("My Tasks", new CheckIcon(PRIMARY_DARK), "tasks", layout, content));
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JButton createDashboardAction(String text, Icon icon, String cardName, CardLayout layout, JPanel content) {
        JButton button = new JButton(text, icon);
        button.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        button.setForeground(PRIMARY_DARK);
        button.setBackground(PRIMARY_SOFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(9);
        button.addActionListener(e -> layout.show(content, cardName));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(213, 231, 219));
            }
            @Override public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_SOFT);
            }
        });
        return button;
    }

    private JButton createTextLink(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        button.setForeground(PRIMARY);
        button.setBackground(WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setForeground(PRIMARY_DARK); }
            @Override public void mouseExited(MouseEvent e) { button.setForeground(PRIMARY); }
        });
        return button;
    }

    private JPanel createPageShell(String eyebrowText, String titleText, String subtitleText) {
        JPanel main = new JPanel(new BorderLayout(0, 22));
        main.setBackground(PAGE);
        main.setBorder(new EmptyBorder(30, 34, 30, 34));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel(eyebrowText.toUpperCase());
        eyebrow.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        eyebrow.setForeground(PRIMARY);
        JLabel title = new JLabel(titleText);
        title.setFont(new Font(FONT_NAME, Font.BOLD, 29));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        heading.add(eyebrow);
        heading.add(Box.createVerticalStrut(7));
        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(subtitle);
        main.add(heading, BorderLayout.NORTH);
        return main;
    }

    private JPanel createProfilePage(String username, JPanel content, CardLayout contentLayout) {
        ProfileData profile = loadProfile(username);
        JPanel main = createPageShell("Student record", "My Profile", "Your student identity and portal account information.");

        JPanel grid = new JPanel(new GridLayout(1, 2, 18, 0));
        grid.setOpaque(false);

        // Personal information card.
        RoundedPanel identity = new RoundedPanel(18, WHITE);
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));
        identity.setBorder(new EmptyBorder(24, 24, 24, 24));
        identity.setShadow(true);

        JPanel headerRow = new JPanel(new GridBagLayout());
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel profileAvatar = createAvatar(getInitial(profile.fullName), 76, PRIMARY, Color.WHITE, 25);
        JPanel avatarWrap = new JPanel(new GridBagLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.setPreferredSize(new Dimension(82, 82));
        avatarWrap.setMinimumSize(new Dimension(82, 82));
        avatarWrap.setMaximumSize(new Dimension(82, 82));
        avatarWrap.add(profileAvatar);

        JPanel nameBlock = new JPanel();
        nameBlock.setOpaque(false);
        nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));

        JLabel name = createWrappedNameLabel(profile.fullName, 19, TEXT, 190, "profileDisplayName");

        JLabel course = new JLabel(profile.program);
        course.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        course.setForeground(MUTED);
        course.setToolTipText(profile.program);

        nameBlock.add(name);
        nameBlock.add(Box.createVerticalStrut(4));
        nameBlock.add(course);

        GridBagConstraints avatarGbc = new GridBagConstraints();
        avatarGbc.gridx = 0;
        avatarGbc.gridy = 0;
        avatarGbc.weightx = 0;
        avatarGbc.anchor = GridBagConstraints.CENTER;
        avatarGbc.insets = new Insets(0, 0, 0, 14);
        headerRow.add(avatarWrap, avatarGbc);

        GridBagConstraints nameGbc = new GridBagConstraints();
        nameGbc.gridx = 1;
        nameGbc.gridy = 0;
        nameGbc.weightx = 1.0;
        nameGbc.fill = GridBagConstraints.HORIZONTAL;
        nameGbc.anchor = GridBagConstraints.WEST;
        nameGbc.insets = new Insets(0, 0, 0, 14);
        headerRow.add(nameBlock, nameGbc);

        // Compact action button. A fixed wrapper prevents BorderLayout from stretching it vertically.
        JButton editButton = createPrimaryButton("Edit Profile", null);
        editButton.setPreferredSize(new Dimension(104, 38));
        editButton.setMinimumSize(new Dimension(104, 38));
        editButton.setMaximumSize(new Dimension(104, 38));
        editButton.setToolTipText("Edit your student information");
        editButton.addActionListener(e -> showEditProfileDialog(username, content, contentLayout));
        JPanel editWrap = new JPanel(new GridBagLayout());
        editWrap.setOpaque(false);
        editWrap.setPreferredSize(new Dimension(104, 82));
        editWrap.setMinimumSize(new Dimension(104, 82));
        editWrap.setMaximumSize(new Dimension(104, 82));
        editWrap.add(editButton);

        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridx = 2;
        buttonGbc.gridy = 0;
        buttonGbc.weightx = 0;
        buttonGbc.anchor = GridBagConstraints.CENTER;
        headerRow.add(editWrap, buttonGbc);

        identity.add(headerRow);
        identity.add(Box.createVerticalStrut(22));
        identity.add(createProfileInfo("Student ID", profile.studentId));
        identity.add(createProfileInfo("Account status", "Active"));
        identity.add(createProfileInfo("Portal role", "Student"));
        identity.add(createProfileInfo("Password", "Protected"));

        // Academic information card.
        RoundedPanel academic = new RoundedPanel(18, WHITE);
        academic.setLayout(new BoxLayout(academic, BoxLayout.Y_AXIS));
        academic.setBorder(new EmptyBorder(24, 24, 24, 24));
        academic.setShadow(true);
        academic.add(createCardTitle("Academic identity"));
        academic.add(Box.createVerticalStrut(18));
        academic.add(createProfileInfo("Program", profile.program));
        academic.add(createProfileInfo("Year level", profile.yearLevel));
        academic.add(createProfileInfo("Section", profile.section));
        academic.add(createProfileInfo("Semester", "1st Semester"));
        academic.add(createProfileInfo("Student status", "Currently enrolled"));
        academic.add(Box.createVerticalGlue());

        JLabel note = new JLabel("<html><div style='width:300px;'>Profile information is saved locally on this computer. Your login username remains your account identifier.</div></html>");
        note.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        note.setForeground(MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        academic.add(note);

        grid.add(identity);
        grid.add(academic);
        main.add(grid, BorderLayout.CENTER);
        return main;
    }

    private JLabel createWrappedNameLabel(String value, int fontSize, Color color, int width, String componentName) {
        String safe = escapeHtml(value == null ? "" : value.trim());
        String[] words = safe.split("\\s+");
        StringBuilder wrapped = new StringBuilder("<html><div style='width:");
        wrapped.append(width).append("px; line-height:1.15;'>");
        int lineLength = 0;
        for (String word : words) {
            if (lineLength > 0 && lineLength + word.length() + 1 > (width >= 180 ? 22 : 16)) {
                wrapped.append("<br>");
                lineLength = 0;
            }
            if (lineLength > 0) wrapped.append(" ");
            wrapped.append(word);
            lineLength += word.length() + 1;
        }
        wrapped.append("</div></html>");

        JLabel label = new JLabel(wrapped.toString());
        label.setFont(new Font(FONT_NAME, Font.BOLD, fontSize));
        label.setForeground(color);
        label.setToolTipText(value);
        label.setName(componentName);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void showEditProfileDialog(String username, JPanel content, CardLayout contentLayout) {
        ProfileData profile = loadProfile(username);

        JDialog dialog = new JDialog(this, "Edit Profile", true);
        dialog.setSize(560, 610);
        dialog.setMinimumSize(new Dimension(520, 570));
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE);
        root.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("STUDENT RECORD");
        eyebrow.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        eyebrow.setForeground(PRIMARY);
        JLabel title = new JLabel("Edit Profile");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 26));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Update the information shown on your student portal.");
        subtitle.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        header.add(eyebrow);
        header.add(Box.createVerticalStrut(5));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        RoundedPanel formCard = new RoundedPanel(16, WHITE);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        formCard.setShadow(true);

        JLabel sectionTitle = createCardTitle("Personal and academic information");
        formCard.add(sectionTitle);
        JLabel sectionHint = new JLabel("These details are stored locally for this account.");
        sectionHint.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        sectionHint.setForeground(MUTED);
        formCard.add(Box.createVerticalStrut(3));
        formCard.add(sectionHint);
        formCard.add(Box.createVerticalStrut(16));

        JTextField fullNameField = createProfileInput(profile.fullName);
        JTextField studentIdField = createProfileInput(profile.studentId);
        JTextField programField = createProfileInput(profile.program);
        JTextField yearField = createProfileInput(profile.yearLevel);
        JTextField sectionField = createProfileInput(profile.section);

        formCard.add(createDialogField("Full Name", fullNameField));
        formCard.add(Box.createVerticalStrut(11));
        formCard.add(createDialogField("Student ID", studentIdField));
        formCard.add(Box.createVerticalStrut(11));
        formCard.add(createDialogField("Program", programField));
        formCard.add(Box.createVerticalStrut(11));
        formCard.add(createDialogField("Year Level", yearField));
        formCard.add(Box.createVerticalStrut(11));
        formCard.add(createDialogField("Section", sectionField));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        JButton cancel = createSecondaryButton("Cancel");
        cancel.setPreferredSize(new Dimension(100, 38));
        cancel.addActionListener(e -> dialog.dispose());

        JButton save = createPrimaryButton("Save Changes", null);
        save.setPreferredSize(new Dimension(130, 38));
        save.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String studentId = studentIdField.getText().trim();
            String program = programField.getText().trim();
            String year = yearField.getText().trim();
            String section = sectionField.getText().trim();

            if (fullName.isEmpty() || studentId.isEmpty() || program.isEmpty() || year.isEmpty() || section.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please complete all profile fields.",
                        "Incomplete Profile", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (fullName.contains("|") || studentId.contains("|") || program.contains("|")
                    || year.contains("|") || section.contains("|")) {
                JOptionPane.showMessageDialog(dialog,
                        "The character '|' cannot be used in profile information.",
                        "Invalid Character", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ProfileData updated = new ProfileData(fullName, studentId, program, year, section);
            if (!saveProfile(username, updated)) {
                JOptionPane.showMessageDialog(dialog,
                        "The profile could not be saved.",
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update the profile page, dashboard greeting, and sidebar identity immediately.
            refreshDisplayedProfile(fullName, content);

            // Rebuild only the profile card. Do not remove a card by numeric index because
            // CardLayout order can change when cards are replaced. Removing the wrong card
            // was the cause of an intermittent Academic Overview navigation bug.
            Component profileCard = findNamedComponent(content, "profilePage");
            if (profileCard != null) {
                content.remove(profileCard);
            }
            JPanel rebuiltProfile = createProfilePage(username, content, contentLayout);
            rebuiltProfile.setName("profilePage");
            content.add(rebuiltProfile, "profile");
            contentLayout.show(content, "profile");
            content.revalidate();
            content.repaint();
            dialog.dispose();
        });

        buttons.add(cancel);
        buttons.add(save);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(buttons, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);
        // Use a wrapper so the form card gets the proper spacing.
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(formCard, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private JTextField createProfileInput(String value) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(0, 36));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(7, 10, 7, 10)));
        return field;
    }

    private Component findNamedComponent(Container parent, String componentName) {
        for (Component component : parent.getComponents()) {
            if (componentName.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container) {
                Component found = findNamedComponent((Container) component, componentName);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void refreshDisplayedProfile(String fullName, JPanel content) {
        Container root = content.getParent();
        if (root == null) return;

        updateNamedLabel(root, "sidebarProfileName", fullName);
        updateNamedLabel(root, "sidebarProfileAvatar", getInitial(fullName));
        updateNamedLabel(root, "dashboardGreeting", "Good day, " + fullName + "!");
    }

    private boolean updateNamedLabel(Container parent, String componentName, String text) {
        for (Component component : parent.getComponents()) {
            if (componentName.equals(component.getName()) && component instanceof JLabel) {
                JLabel label = (JLabel) component;
                if ("sidebarProfileName".equals(componentName) || "profileDisplayName".equals(componentName)) {
                    int width = "sidebarProfileName".equals(componentName) ? 125 : 190;
                    label.setText(createWrappedNameLabel(text, label.getFont().getSize(), label.getForeground(), width, componentName).getText());
                } else {
                    label.setText(text);
                }
                label.setToolTipText(text);
                component.repaint();
                return true;
            }

            if (component instanceof Container) {
                if (updateNamedLabel((Container) component, componentName, text)) {
                    return true;
                }
            }
        }
        return false;
    }

    private JPanel createDialogField(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        label.setForeground(TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        return panel;
    }

    private JPanel createAcademicPage(String username) {
        JPanel main = createPageShell(
                "Academic services",
                "Academic Overview",
                "Review your enrolled subjects, enter grades and calculate your weighted GPA.");

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        JPanel overview = createOverviewCards();
        body.add(overview, BorderLayout.NORTH);

        RoundedPanel gradeCard = new RoundedPanel(18, WHITE);
        gradeCard.setLayout(new BorderLayout(0, 12));
        gradeCard.setBorder(new EmptyBorder(20, 22, 20, 22));
        gradeCard.setShadow(true);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(createCardTitle("Grades and GPA calculator"), BorderLayout.WEST);

        JLabel helper = new JLabel("Enter grades using your school's grading scale.");
        helper.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        helper.setForeground(MUTED);
        titleRow.add(helper, BorderLayout.EAST);
        gradeCard.add(titleRow, BorderLayout.NORTH);

        JPanel gradeRows = new JPanel();
        gradeRows.setOpaque(false);
        gradeRows.setLayout(new BoxLayout(gradeRows, BoxLayout.Y_AXIS));

        List<JTextField> gradeFields = new ArrayList<>();
        for (int i = 0; i < SUBJECT_CODES.length; i++) {
            gradeRows.add(createGradeInputRow(
                    SUBJECT_NAMES[i],
                    SUBJECT_CODES[i],
                    SUBJECT_UNITS[i],
                    loadGradeForSubject(username, SUBJECT_CODES[i]),
                    gradeFields));
        }

        JScrollPane scroll = new JScrollPane(gradeRows);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setPreferredSize(new Dimension(700, 300));
        gradeCard.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);

        JLabel result = new JLabel("GPA: --");
        result.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        result.setForeground(PRIMARY_DARK);

        JButton calculate = createPrimaryButton("Save Grades & Calculate", null);
        calculate.setPreferredSize(new Dimension(190, 40));

        String currentUsername = username;
        calculate.addActionListener(e -> {
            double weightedTotal = 0;
            double totalUnits = 0;
            int entered = 0;

            for (int i = 0; i < gradeFields.size(); i++) {
                String raw = gradeFields.get(i).getText().trim();
                if (raw.isEmpty()) {
                    continue;
                }

                try {
                    double grade = Double.parseDouble(raw);
                    if (grade < 1.00 || grade > 5.00) {
                        throw new NumberFormatException();
                    }

                    weightedTotal += grade * SUBJECT_UNITS[i];
                    totalUnits += SUBJECT_UNITS[i];
                    entered++;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Please enter a valid grade from 1.00 to 5.00 for "
                                    + SUBJECT_CODES[i] + ".",
                            "Invalid Grade",
                            JOptionPane.WARNING_MESSAGE);
                    gradeFields.get(i).requestFocusInWindow();
                    return;
                }
            }

            if (entered == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Enter at least one grade before calculating.",
                        "No Grades Entered",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            double gpa = weightedTotal / totalUnits;

            if (!saveGrades(currentUsername, gradeFields)) {
                JOptionPane.showMessageDialog(
                        this,
                        "The grades could not be saved.",
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            result.setText(String.format("GPA: %.2f", gpa));
            JOptionPane.showMessageDialog(
                    this,
                    String.format("Weighted GPA calculated: %.2f%nGrades entered: %d of %d",
                            gpa, entered, SUBJECT_CODES.length),
                    "GPA Result",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        footer.add(result, BorderLayout.WEST);
        footer.add(calculate, BorderLayout.EAST);
        gradeCard.add(footer, BorderLayout.SOUTH);

        body.add(gradeCard, BorderLayout.CENTER);
        main.add(body, BorderLayout.CENTER);
        return main;
    }

    private JPanel createGradeInputRow(
            String subject, String code, double units, String savedGrade, List<JTextField> gradeFields) {

        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(9, 0, 9, 0)));

        JPanel subjectPanel = new JPanel();
        subjectPanel.setOpaque(false);
        subjectPanel.setLayout(new BoxLayout(subjectPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(subject);
        title.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        title.setForeground(TEXT);

        JLabel details = new JLabel(code + "  •  " + formatUnits(units) + " units");
        details.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        details.setForeground(MUTED);

        subjectPanel.add(title);
        subjectPanel.add(Box.createVerticalStrut(2));
        subjectPanel.add(details);

        JTextField grade = new JTextField(savedGrade == null ? "" : savedGrade);
        grade.setHorizontalAlignment(SwingConstants.CENTER);
        grade.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        grade.setPreferredSize(new Dimension(80, 34));
        grade.setMaximumSize(new Dimension(80, 34));
        grade.setToolTipText("Enter a grade from 1.00 to 5.00");

        gradeFields.add(grade);

        row.add(subjectPanel, BorderLayout.CENTER);
        row.add(grade, BorderLayout.EAST);
        return row;
    }

    private String formatUnits(double units) {
        return units == (int) units ? String.valueOf((int) units) : String.valueOf(units);
    }

    private JPanel createAttendanceCard() {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 22, 22, 22));
        card.setShadow(true);
        card.add(createCardTitle("Attendance record"), BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBorder(new EmptyBorder(12, 0, 0, 0));
        rows.add(createAttendanceRow("Introduction to Computing", 96));
        rows.add(createAttendanceRow("Fundamentals in Programming", 90));
        rows.add(createAttendanceRow("Mathematics in the Modern World", 94));
        rows.add(createAttendanceRow("Understanding the Self", 91));
        rows.add(createAttendanceRow("Professional Issues in Computing", 88));
        rows.add(createAttendanceRow("Purposive Communication", 93));
        rows.add(createAttendanceRow("National Service Training Program 1", 95));
        rows.add(createAttendanceRow("Physical Activities Toward Health and Fitness 101", 97));
        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel createAttendanceRow(String subject, int percent) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(10, 0, 10, 0)));

        JLabel name = new JLabel(subject);
        name.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        name.setForeground(TEXT);
        row.add(name, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(createAttendanceBar(percent));

        JLabel percentLabel = new JLabel(percent + "%");
        percentLabel.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        percentLabel.setForeground(percent >= 90 ? PRIMARY : AMBER);
        percentLabel.setPreferredSize(new Dimension(32, 16));
        right.add(percentLabel);

        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JPanel createAttendanceBar(int percent) {
        JPanel track = new JPanel(null);
        track.setOpaque(false);
        track.setPreferredSize(new Dimension(140, 8));

        RoundedPanel base = new RoundedPanel(4, new Color(224, 230, 226));
        base.setBounds(0, 0, 140, 8);

        int fillWidth = Math.max(8, (int) Math.round(140 * (percent / 100.0)));
        RoundedPanel fill = new RoundedPanel(4, percent >= 90 ? PRIMARY : AMBER);
        fill.setBounds(0, 0, fillWidth, 8);

        track.add(base);
        track.add(fill);
        track.setComponentZOrder(fill, 0);
        track.setComponentZOrder(base, 1);
        return track;
    }

    private JPanel createSchedulePage(String username) {
        JPanel main = createPageShell("Academic calendar", "Class Schedule", "Manage your planned classes and activities.");

        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(22, 22, 22, 22));
        card.setShadow(true);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(createCardTitle("Your class schedule"), BorderLayout.WEST);

        JButton addButton = createPrimaryButton("ADD CLASS", new CalendarIcon());
        addButton.setPreferredSize(new Dimension(130, 38));
        addButton.addActionListener(e -> showAddScheduleDialog());
        header.add(addButton, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        scheduleListPanel = new JPanel();
        scheduleListPanel.setOpaque(false);
        scheduleListPanel.setLayout(new BoxLayout(scheduleListPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(scheduleListPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scroll, BorderLayout.CENTER);

        refreshScheduleList();
        main.add(card, BorderLayout.CENTER);
        return main;
    }

    private void refreshScheduleList() {
        if (scheduleListPanel == null) return;
        scheduleListPanel.removeAll();

        if (scheduleItems.isEmpty()) {
            JLabel empty = new JLabel("No classes yet. Click ADD CLASS to create one.");
            empty.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
            empty.setForeground(MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            scheduleListPanel.add(Box.createVerticalStrut(20));
            scheduleListPanel.add(empty);
        } else {
            for (int i = 0; i < scheduleItems.size(); i++) {
                ScheduleItem item = scheduleItems.get(i);
                scheduleListPanel.add(createScheduleManagerRow(item, i));
                if (i < scheduleItems.size() - 1) scheduleListPanel.add(Box.createVerticalStrut(10));
            }
        }

        scheduleListPanel.revalidate();
        scheduleListPanel.repaint();
    }

    private JPanel createScheduleManagerRow(ScheduleItem item, int index) {
        RoundedPanel row = new RoundedPanel(14, new Color(248, 250, 249));
        row.setLayout(new BorderLayout(14, 0));
        row.setBorder(new EmptyBorder(13, 14, 13, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel time = new JLabel(item.time);
        time.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        time.setForeground(PRIMARY);
        time.setPreferredSize(new Dimension(90, 30));
        row.add(time, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel subject = new JLabel(item.subject);
        subject.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        subject.setForeground(TEXT);
        JLabel details = new JLabel(item.room + " • " + item.type);
        details.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        details.setForeground(MUTED);
        info.add(subject);
        info.add(Box.createVerticalStrut(4));
        info.add(details);
        row.add(info, BorderLayout.CENTER);

        JButton delete = new JButton("DELETE");
        delete.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        delete.setForeground(DANGER);
        delete.setFocusPainted(false);
        delete.setBorderPainted(false);
        delete.setContentAreaFilled(false);
        delete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        delete.addActionListener(e -> {
            scheduleItems.remove(index);
            saveSchedule(activeUsername);
            refreshScheduleList();
        });
        row.add(delete, BorderLayout.EAST);
        return row;
    }

    private void showAddScheduleDialog() {
        JTextField timeField = new JTextField();
        JTextField subjectField = new JTextField();
        JTextField roomField = new JTextField();
        JTextField typeField = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setBorder(new EmptyBorder(8, 4, 4, 4));
        form.add(new JLabel("Time (example: 08:00 AM):"));
        form.add(timeField);
        form.add(new JLabel("Subject:"));
        form.add(subjectField);
        form.add(new JLabel("Room / Location:"));
        form.add(roomField);
        form.add(new JLabel("Class type (example: Lecture):"));
        form.add(typeField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Class", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String time = timeField.getText().trim();
        String subject = subjectField.getText().trim();
        String room = roomField.getText().trim();
        String type = typeField.getText().trim();

        if (time.isEmpty() || subject.isEmpty() || room.isEmpty() || type.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please complete all class fields.", "Incomplete Class", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (time.contains("|") || subject.contains("|") || room.contains("|") || type.contains("|")) {
            JOptionPane.showMessageDialog(this, "The character '|' cannot be used in schedule information.", "Invalid Character", JOptionPane.WARNING_MESSAGE);
            return;
        }

        scheduleItems.add(new ScheduleItem(time, subject, room, type));
        saveSchedule(activeUsername);
        refreshScheduleList();
    }

    // ============================================================
    // MIDTERM IMPROVEMENT — TASK MANAGER PAGE
    // A working student task list with add, complete, and delete actions.
    // ============================================================
    private JPanel createTasksPage() {
        JPanel main = createPageShell("Academic productivity", "My Tasks", "Track assignments, activities and deadlines in one place.");

        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(22, 22, 22, 22));
        card.setShadow(true);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(createCardTitle("Task list"), BorderLayout.WEST);

        JButton addButton = createPrimaryButton("ADD TASK", new CheckIcon());
        addButton.setPreferredSize(new Dimension(135, 38));
        addButton.addActionListener(e -> showAddTaskDialog());
        header.add(addButton, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        taskListPanel = new JPanel();
        taskListPanel.setOpaque(false);
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(taskListPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scroll, BorderLayout.CENTER);

        refreshTaskList();
        main.add(card, BorderLayout.CENTER);
        return main;
    }

    private void refreshTaskList() {
        if (taskListPanel == null) return;
        taskListPanel.removeAll();

        if (taskItems.isEmpty()) {
            JLabel empty = new JLabel("No tasks yet. Click ADD TASK to create one.");
            empty.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
            empty.setForeground(MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            taskListPanel.add(Box.createVerticalStrut(20));
            taskListPanel.add(empty);
        } else {
            for (int i = 0; i < taskItems.size(); i++) {
                TaskItem task = taskItems.get(i);
                taskListPanel.add(createTaskManagerRow(task, i));
                if (i < taskItems.size() - 1) {
                    taskListPanel.add(Box.createVerticalStrut(10));
                }
            }
        }

        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private JPanel createTaskManagerRow(TaskItem task, int index) {
        RoundedPanel row = new RoundedPanel(14, task.completed ? PRIMARY_SOFT : new Color(248, 250, 249));
        row.setLayout(new BorderLayout(12, 0));
        row.setBorder(new EmptyBorder(13, 14, 13, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        JCheckBox check = new JCheckBox();
        check.setOpaque(false);
        check.setSelected(task.completed);
        check.setToolTipText(task.completed ? "Mark as incomplete" : "Mark as complete");
        check.addActionListener(e -> {
            task.completed = check.isSelected();
            saveTasks(activeUsername);
            refreshTaskList();
        });
        row.add(check, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(task.title);
        title.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        title.setForeground(task.completed ? PRIMARY_DARK : TEXT);

        JLabel details = new JLabel(task.subject + " • Due " + task.due);
        details.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        details.setForeground(MUTED);

        info.add(title);
        info.add(Box.createVerticalStrut(4));
        info.add(details);
        row.add(info, BorderLayout.CENTER);

        JButton delete = new JButton("DELETE");
        delete.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        delete.setForeground(DANGER);
        delete.setFocusPainted(false);
        delete.setBorderPainted(false);
        delete.setContentAreaFilled(false);
        delete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        delete.addActionListener(e -> {
            taskItems.remove(index);
            saveTasks(activeUsername);
            refreshTaskList();
        });
        row.add(delete, BorderLayout.EAST);

        return row;
    }

    private void showAddTaskDialog() {
        JTextField titleField = new JTextField();
        JTextField subjectField = new JTextField();
        JTextField dueField = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setBorder(new EmptyBorder(8, 4, 4, 4));
        form.add(new JLabel("Task title:"));
        form.add(titleField);
        form.add(new JLabel("Subject:"));
        form.add(subjectField);
        form.add(new JLabel("Due date (example: September 30):"));
        form.add(dueField);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Add New Task",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String title = titleField.getText().trim();
        String subject = subjectField.getText().trim();
        String due = dueField.getText().trim();

        if (title.isEmpty() || subject.isEmpty() || due.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please complete all task fields.",
                    "Incomplete Task",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (title.contains("|") || subject.contains("|") || due.contains("|")) {
            JOptionPane.showMessageDialog(this,
                    "The character '|' cannot be used in task information.",
                    "Invalid Character", JOptionPane.WARNING_MESSAGE);
            return;
        }

        taskItems.add(new TaskItem(title, subject, due, false));
        saveTasks(activeUsername);
        refreshTaskList();
    }

    // ============================================================
    // MIDTERM IMPROVEMENT — PERSISTENT TASKS AND SCHEDULE
    // ============================================================
    private String canonicalizeUsernameForStorage(String username) {
        if (username == null) return "";
        String requested = username.trim();
        if (requested.isEmpty()) return requested;

        File file = getDataFile(USER_FILE);
        if (!file.exists()) return requested;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2 && parts[0].trim().equalsIgnoreCase(requested)) {
                    return parts[0].trim();
                }
            }
        } catch (IOException e) {
            // Keep the requested value if the local account file cannot be read.
        }
        return requested;
    }

    private File getAccountFile(String prefix, String username) {
        String canonical = canonicalizeUsernameForStorage(username);
        return getDataFile(prefix + hashPassword(canonical).substring(0, 12) + ".txt");
    }

    private File getTasksFile(String username) {
        return getAccountFile("tasks_", username);
    }

    private void loadTasks(String username) {
        taskItems.clear();
        File file = getTasksFile(username);
        if (!file.exists()) {
            taskItems.add(new TaskItem("Programming Activity", "CFP 110", "September 28", false));
            taskItems.add(new TaskItem("Mathematics Problem Set", "GE 100", "September 27", true));
            taskItems.add(new TaskItem("NSTP Reflection", "NSTP 1", "September 30", false));
            saveTasks(username);
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 4);
                if (parts.length == 4) {
                    taskItems.add(new TaskItem(parts[0], parts[1], parts[2], Boolean.parseBoolean(parts[3])));
                }
            }
        } catch (IOException e) {
            taskItems.clear();
        }
    }

    private boolean saveTasks(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        File file = getTasksFile(username);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (TaskItem task : taskItems) {
                writer.write(task.title + "|" + task.subject + "|" + task.due + "|" + task.completed);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private File getScheduleFile(String username) {
        return getAccountFile("schedule_", username);
    }

    private void loadSchedule(String username) {
        scheduleItems.clear();
        File file = getScheduleFile(username);
        if (!file.exists()) {
            scheduleItems.add(new ScheduleItem("08:00 AM", "Introduction to Computing", "Room 204", "Lecture"));
            scheduleItems.add(new ScheduleItem("10:00 AM", "Fundamentals in Programming", "Programming Laboratory", "Laboratory"));
            scheduleItems.add(new ScheduleItem("01:00 PM", "Mathematics in the Modern World", "Room 301", "Lecture"));
            scheduleItems.add(new ScheduleItem("03:00 PM", "Professional Issues in Computing", "Room 205", "Discussion"));
            saveSchedule(username);
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 4);
                if (parts.length == 4) {
                    scheduleItems.add(new ScheduleItem(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        } catch (IOException e) {
            scheduleItems.clear();
        }
    }

    private boolean saveSchedule(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        File file = getScheduleFile(username);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (ScheduleItem item : scheduleItems) {
                writer.write(item.time + "|" + item.subject + "|" + item.room + "|" + item.type);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ============================================================
    // MIDTERM IMPROVEMENT — PROFILE DATA
    // Keeps editable profile details in a simple text file.
    // ============================================================
    private static class ProfileData {
        String fullName;
        String studentId;
        String program;
        String yearLevel;
        String section;

        ProfileData(String fullName, String studentId, String program, String yearLevel, String section) {
            this.fullName = fullName;
            this.studentId = studentId;
            this.program = program;
            this.yearLevel = yearLevel;
            this.section = section;
        }
    }

    // Resolve local data files whether the program is launched from the project root
    // or from the src folder. This prevents login/profile/grade data from silently
    // being written to a different working directory.
    private File getDataFile(String filename) {
        File direct = new File(filename);
        if (direct.exists()) return direct;

        File srcFile = new File("src", filename);
        if (srcFile.exists()) return srcFile;

        // Prefer the src folder for new files when it is available.
        File srcDirectory = new File("src");
        if (srcDirectory.isDirectory()) return srcFile;
        return direct;
    }

    private File getProfileFile(String username) {
        return getAccountFile("profile_", username);
    }

    private ProfileData loadProfile(String username) {
        File file = getProfileFile(username);
        if (!file.exists()) {
            return new ProfileData(username, DEFAULT_STUDENT_ID, DEFAULT_PROGRAM, DEFAULT_YEAR_LEVEL, DEFAULT_SECTION);
        }

        String[] values = new String[5];
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2) {
                    switch (parts[0]) {
                        case "fullName": values[0] = parts[1]; break;
                        case "studentId": values[1] = parts[1]; break;
                        case "program": values[2] = parts[1]; break;
                        case "yearLevel": values[3] = parts[1]; break;
                        case "section": values[4] = parts[1]; break;
                        default: break;
                    }
                }
            }
        } catch (IOException e) {
            return new ProfileData(username, DEFAULT_STUDENT_ID, DEFAULT_PROGRAM, DEFAULT_YEAR_LEVEL, DEFAULT_SECTION);
        }

        return new ProfileData(
                values[0] == null || values[0].trim().isEmpty() ? username : values[0],
                values[1] == null || values[1].trim().isEmpty() ? DEFAULT_STUDENT_ID : values[1],
                values[2] == null || values[2].trim().isEmpty() ? DEFAULT_PROGRAM : values[2],
                values[3] == null || values[3].trim().isEmpty() ? DEFAULT_YEAR_LEVEL : values[3],
                values[4] == null || values[4].trim().isEmpty() ? DEFAULT_SECTION : values[4]
        );
    }

    private boolean saveProfile(String username, ProfileData profile) {
        File file = getProfileFile(username);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write("fullName|" + profile.fullName); writer.newLine();
            writer.write("studentId|" + profile.studentId); writer.newLine();
            writer.write("program|" + profile.program); writer.newLine();
            writer.write("yearLevel|" + profile.yearLevel); writer.newLine();
            writer.write("section|" + profile.section); writer.newLine();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private File getGradesFile(String username) {
        return getAccountFile("grades_", username);
    }

    private String loadGradeForSubject(String username, String code) {
        if (username == null || username.trim().isEmpty()) {
            return "";
        }

        File file = getGradesFile(username);
        if (!file.exists()) {
            return "";
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2 && parts[0].equals(code)) {
                    return parts[1];
                }
            }
        } catch (IOException e) {
            return "";
        }

        return "";
    }

    private boolean saveGrades(String username, List<JTextField> gradeFields) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        File file = getGradesFile(username);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            for (int i = 0; i < gradeFields.size(); i++) {
                String grade = gradeFields.get(i).getText().trim();
                writer.write(SUBJECT_CODES[i] + "|" + grade);
                writer.newLine();
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static class TaskItem {
        String title;
        String subject;
        String due;
        boolean completed;

        TaskItem(String title, String subject, String due, boolean completed) {
            this.title = title;
            this.subject = subject;
            this.due = due;
            this.completed = completed;
        }
    }

    private static class ScheduleItem {
        String time;
        String subject;
        String room;
        String type;

        ScheduleItem(String time, String subject, String room, String type) {
            this.time = time;
            this.subject = subject;
            this.room = room;
            this.type = type;
        }
    }

    private JPanel createAnnouncementsPage() {
        JPanel main = createPageShell("Campus communication", "Announcements", "Important notices and reminders from your student portal.");
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(createAnnouncementCard("Account security reminder", "Keep your password private and sign out when using shared computers.", "Portal notice"));
        list.add(Box.createVerticalStrut(14));
        list.add(createAnnouncementCard("Academic records", "Review your subjects and class schedule before your next session.", "Academic"));
        list.add(Box.createVerticalStrut(14));
        list.add(createAnnouncementCard("Student services", "Use the portal navigation to review your profile, schedule and academic information.", "Services"));
        main.add(list, BorderLayout.CENTER);
        return main;
    }

    // ============================================================
    // MIDTERM IMPROVEMENT — CHANGE PASSWORD
    // Allows the logged-in student to replace the stored password hash
    // after verifying the current password.
    // ============================================================
    private void showChangePasswordDialog(String username, JFrame dashboard) {
        JDialog dialog = new JDialog(dashboard, "Change Password", true);
        dialog.setSize(470, 440);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(dashboard);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(PAGE);
        root.setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("ACCOUNT SECURITY");
        eyebrow.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        eyebrow.setForeground(PRIMARY);
        JLabel title = new JLabel("Change Password");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 25));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Update the password used to sign in to this account.");
        subtitle.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        subtitle.setForeground(MUTED);
        header.add(eyebrow);
        header.add(Box.createVerticalStrut(5));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        RoundedPanel card = new RoundedPanel(16, WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setShadow(true);

        JPasswordField current = createPasswordInput();
        JPasswordField next = createPasswordInput();
        JPasswordField confirm = createPasswordInput();
        card.add(createPasswordDialogField("Current Password", current));
        card.add(Box.createVerticalStrut(11));
        card.add(createPasswordDialogField("New Password", next));
        card.add(Box.createVerticalStrut(11));
        card.add(createPasswordDialogField("Confirm New Password", confirm));

        JLabel hint = new JLabel("Use at least 8 characters for the new password.");
        hint.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        hint.setForeground(MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(8));
        card.add(hint);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton cancel = createSecondaryButton("Cancel");
        cancel.setPreferredSize(new Dimension(92, 38));
        cancel.addActionListener(e -> dialog.dispose());

        JButton save = createPrimaryButton("Update Password", null);
        save.setPreferredSize(new Dimension(145, 38));
        save.addActionListener(e -> {
            String currentPassword = new String(current.getPassword());
            String newPassword = new String(next.getPassword());
            String confirmPassword = new String(confirm.getPassword());

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please complete all password fields.",
                        "Incomplete Password", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!authenticate(username, hashPassword(currentPassword))) {
                JOptionPane.showMessageDialog(dialog, "The current password is incorrect.",
                        "Password Not Changed", JOptionPane.WARNING_MESSAGE);
                current.requestFocusInWindow();
                return;
            }

            if (newPassword.length() < 8) {
                JOptionPane.showMessageDialog(dialog, "The new password must contain at least 8 characters.",
                        "Password Too Short", JOptionPane.WARNING_MESSAGE);
                next.requestFocusInWindow();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog, "The new passwords do not match.",
                        "Password Mismatch", JOptionPane.WARNING_MESSAGE);
                confirm.requestFocusInWindow();
                return;
            }

            if (newPassword.equals(currentPassword)) {
                JOptionPane.showMessageDialog(dialog, "Choose a different password from your current one.",
                        "Password Unchanged", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!updateUserPassword(username, newPassword)) {
                JOptionPane.showMessageDialog(dialog, "The password could not be updated.",
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(dialog, "Your password has been updated successfully.",
                    "Password Updated", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        buttons.add(cancel);
        buttons.add(save);
        root.add(header, BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private JPasswordField createPasswordInput() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(0, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(7, 10, 7, 10)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JPanel createPasswordDialogField(String labelText, JPasswordField field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        label.setForeground(TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        return panel;
    }

    private boolean updateUserPassword(String username, String newPassword) {
        File file = getDataFile(USER_FILE);
        if (!file.exists()) return false;

        List<String> lines = new ArrayList<>();
        boolean updated = false;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2 && parts[0].trim().equalsIgnoreCase(username)) {
                    lines.add(parts[0].trim() + "|" + hashPassword(newPassword));
                    updated = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }

        if (!updated) return false;

        File temp = new File(USER_FILE + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            temp.delete();
            return false;
        }

        if (file.delete() && temp.renameTo(file)) {
            return true;
        }
        temp.delete();
        return false;
    }

    private JPanel createSettingsPage(String username, JFrame dashboard) {
        JPanel main = createPageShell("Preferences", "Settings", "Control the behavior of this student portal prototype.");
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        RoundedPanel appearance = createSettingCard("Portal appearance", "Clean, consistent interface styling");
        JLabel appearanceText = new JLabel("Academia One theme • Green & white interface • Segoe UI");
        appearanceText.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        appearanceText.setForeground(MUTED);
        appearance.add(Box.createVerticalStrut(14));
        appearance.add(appearanceText);

        RoundedPanel security = createSettingCard("Account security", "Local account controls");
        JLabel securityText = new JLabel("Passwords are stored as SHA-256 hashes in the local users.txt file.");
        securityText.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        securityText.setForeground(MUTED);
        security.add(Box.createVerticalStrut(14));
        security.add(securityText);
        JButton changePassword = createSecondaryButton("CHANGE PASSWORD");
        changePassword.setPreferredSize(new Dimension(180, 38));
        changePassword.setMaximumSize(new Dimension(180, 38));
        changePassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        changePassword.addActionListener(e -> showChangePasswordDialog(username, dashboard));
        security.add(Box.createVerticalStrut(12));
        security.add(changePassword);

        RoundedPanel session = createSettingCard("Session", "Sign out of the current account");
        JButton signOut = createPrimaryButton("SIGN OUT", new LogoutIcon());
        signOut.setMaximumSize(new Dimension(180, 42));
        signOut.setPreferredSize(new Dimension(180, 42));
        signOut.addActionListener(e -> {
            dashboard.dispose();
            new LoginApp();
        });
        session.add(Box.createVerticalStrut(14));
        session.add(signOut);

        RoundedPanel about = createSettingCard("About this portal", "Java Swing student project");
        JLabel aboutText = new JLabel("Built with Java, Swing and local file storage. Passwords are hashed with SHA-256.");
        aboutText.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        aboutText.setForeground(MUTED);
        about.add(Box.createVerticalStrut(14));
        about.add(aboutText);

        column.add(appearance);
        column.add(Box.createVerticalStrut(14));
        column.add(security);
        column.add(Box.createVerticalStrut(14));
        column.add(session);
        column.add(Box.createVerticalStrut(14));
        column.add(about);
        column.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("Academia One v1.0  •  Java Swing", SwingConstants.CENTER);
        footer.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        footer.setForeground(MUTED);
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(footer);

        main.add(column, BorderLayout.CENTER);
        return main;
    }

    private RoundedPanel createSettingCard(String title, String subtitle) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        card.setShadow(true);
        card.add(createCardTitle(title));
        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        sub.setForeground(MUTED);
        card.add(Box.createVerticalStrut(3));
        card.add(sub);
        return card;
    }

    private JLabel createCardTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(new Font(FONT_NAME, Font.BOLD, 17));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        return title;
    }

    private JPanel createProfileInfo(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(7, 0, 7, 0));
        JLabel left = new JLabel(label);
        left.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        left.setForeground(MUTED);
        JLabel right = new JLabel(value);
        right.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        right.setForeground(TEXT);
        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JPanel createSubjectRow(String subject, String code, String units, String status) {
        JPanel row = new JPanel(new BorderLayout(18, 0));
        row.setOpaque(false);
        row.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(12, 0, 12, 0)));
        JLabel title = new JLabel(subject);
        title.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        title.setForeground(TEXT);
        JPanel middle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        middle.setOpaque(false);
        JLabel codeLabel = new JLabel(code);
        codeLabel.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        codeLabel.setForeground(PRIMARY);
        JLabel unitLabel = new JLabel(units);
        unitLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        unitLabel.setForeground(MUTED);
        middle.add(codeLabel);
        middle.add(unitLabel);
        JLabel active = new JLabel(status);
        active.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        active.setForeground(PRIMARY);
        row.add(title, BorderLayout.WEST);
        row.add(middle, BorderLayout.CENTER);
        row.add(active, BorderLayout.EAST);
        return row;
    }

    private JPanel createScheduleRow(String time, String subject, String room, String type) {
        JPanel row = new JPanel(new BorderLayout(18, 0));
        row.setOpaque(false);
        row.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(14, 0, 14, 0)));
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        timeLabel.setForeground(PRIMARY);
        timeLabel.setPreferredSize(new Dimension(90, 25));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel subjectLabel = new JLabel(subject);
        subjectLabel.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        subjectLabel.setForeground(TEXT);
        JLabel roomLabel = new JLabel(room);
        roomLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        roomLabel.setForeground(MUTED);
        text.add(subjectLabel);
        text.add(Box.createVerticalStrut(4));
        text.add(roomLabel);
        JLabel typeLabel = new JLabel(type);
        typeLabel.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        typeLabel.setForeground(MUTED);
        row.add(timeLabel, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(typeLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel createAnnouncementCard(String titleText, String message, String tag) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setShadow(true);
        JLabel icon = new JLabel(new BellIcon());
        icon.setPreferredSize(new Dimension(24, 24));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(titleText);
        title.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        title.setForeground(TEXT);
        JLabel msg = new JLabel("<html>" + message + "</html>");
        msg.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        msg.setForeground(MUTED);
        text.add(title);
        text.add(Box.createVerticalStrut(5));
        text.add(msg);
        JLabel tagLabel = new JLabel(tag);
        tagLabel.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        tagLabel.setForeground(PRIMARY);
        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        card.add(tagLabel, BorderLayout.EAST);
        return card;
    }

    private JPanel createOverviewCards() {
        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));

        cards.add(createStatCard("ENROLLED SUBJECTS", "8", "Current semester", new BookIcon(), PRIMARY));
        cards.add(createStatCard("ATTENDANCE", "92%", "On track", new CalendarIcon(), new Color(44, 113, 71)));
        cards.add(createStatCard("ACCOUNT STATUS", "Active", "Good standing", new CheckIcon(), new Color(34, 119, 73)));
        return cards;
    }

    private JPanel createStatCard(String label, String value, String detail, Icon icon, Color accent) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(15, 0));
        card.setBorder(new EmptyBorder(17, 18, 17, 18));
        card.setShadow(true);

        RoundedPanel iconBox = new RoundedPanel(14, PRIMARY_SOFT);
        iconBox.setPreferredSize(new Dimension(48, 48));
        iconBox.setLayout(new GridBagLayout());
        iconBox.add(new JLabel(icon));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel small = new JLabel(label);
        small.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        small.setForeground(MUTED);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font(FONT_NAME, Font.BOLD, 24));
        valueLabel.setForeground(TEXT);
        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        detailLabel.setForeground(accent);
        text.add(small);
        text.add(Box.createVerticalStrut(3));
        text.add(valueLabel);
        text.add(Box.createVerticalStrut(1));
        text.add(detailLabel);

        card.add(iconBox, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel createQuickActionsCard(CardLayout layout, JPanel content) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(19, 21, 19, 21));
        card.setShadow(true);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel title = new JLabel("Quick actions");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        title.setForeground(TEXT);
        card.add(title, BorderLayout.NORTH);

        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);

        UserIcon profileIcon = new UserIcon();
        profileIcon.setColor(PRIMARY);
        BookIcon academicIcon = new BookIcon();
        CalendarIcon scheduleIcon = new CalendarIcon();
        BellIcon announceIcon = new BellIcon();
        announceIcon.setColor(PRIMARY);

        row.add(createQuickActionButton("My Profile", profileIcon, "profile", layout, content));
        row.add(createQuickActionButton("Academic", academicIcon, "academic", layout, content));
        row.add(createQuickActionButton("Schedule", scheduleIcon, "schedule", layout, content));
        row.add(createQuickActionButton("Announcements", announceIcon, "announcements", layout, content));

        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JButton createQuickActionButton(String label, Icon icon, String cardName, CardLayout layout, JPanel content) {
        JButton button = new JButton(label, icon);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setIconTextGap(8);
        button.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        button.setForeground(PRIMARY_DARK);
        button.setBackground(PRIMARY_SOFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 74));
        button.addActionListener(e -> layout.show(content, cardName));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(new Color(213, 231, 219)); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(PRIMARY_SOFT); }
        });
        return button;
    }

    private JPanel createTodayCard() {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(19, 21, 18, 21));
        card.setShadow(true);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Today");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        title.setForeground(TEXT);
        JLabel date = new JLabel("Academic overview");
        date.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        date.setForeground(MUTED);
        head.add(title, BorderLayout.WEST);
        head.add(date, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(createActivityRow("08:00 AM", "Introduction to Computing", "Room 204", new BookIcon()));
        list.add(Box.createVerticalStrut(10));
        list.add(createActivityRow("10:00 AM", "Fundamentals in Programming", "Laboratory", new CodeIcon()));
        list.add(Box.createVerticalStrut(10));
        list.add(createActivityRow("01:00 PM", "Mathematics in the Modern World", "Room 301", new CalendarIcon()));
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActivityRow(String time, String subject, String room, Icon icon) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(35, 35));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(subject);
        name.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        name.setForeground(TEXT);
        JLabel location = new JLabel(room);
        location.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        location.setForeground(MUTED);
        text.add(name);
        text.add(Box.createVerticalStrut(3));
        text.add(location);

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        timeLabel.setForeground(PRIMARY);

        row.add(iconLabel, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(timeLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel createProfileCard(String username) {
        ProfileData profile = loadProfile(username);
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(19, 21, 18, 21));
        card.setShadow(true);

        JLabel title = new JLabel("Student profile");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        title.setForeground(TEXT);
        card.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Identity panel holding avatar and user details
        JPanel identity = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        identity.setOpaque(false);

        // Creates the circular avatar badge
        JLabel avatar = createAvatar(getInitial(profile.fullName), 52, PRIMARY, Color.WHITE, 22);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        
        JLabel name = new JLabel(profile.fullName);
        name.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        name.setToolTipText(profile.fullName);
        name.setForeground(TEXT);
        
        JLabel course = new JLabel(profile.program);
        course.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        course.setForeground(MUTED);
        
        info.add(name);
        info.add(Box.createVerticalStrut(3));
        info.add(course);

        identity.add(avatar);
        identity.add(info);
        content.add(identity);

        content.add(createInfoLine("Account", "Active"));
        content.add(createInfoLine("Portal access", "Student"));
        content.add(createInfoLine("Security", "Password protected"));
        
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createInfoLine(String left, String right) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel a = new JLabel(left);
        a.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        a.setForeground(MUTED);
        JLabel b = new JLabel(right);
        b.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        b.setForeground(TEXT);
        row.add(a, BorderLayout.WEST);
        row.add(b, BorderLayout.EAST);
        return row;
    }

    private JPanel createAnnouncementBar() {
        RoundedPanel bar = new RoundedPanel(16, PRIMARY);
        bar.setLayout(new BorderLayout(13, 0));
        bar.setBorder(new EmptyBorder(13, 17, 13, 17));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        JLabel icon = new JLabel(new BellIcon());
        JLabel title = new JLabel("Portal notice");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        title.setForeground(Color.WHITE);
        JLabel message = new JLabel("Keep your account details private and always sign out on shared computers.");
        message.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        message.setForeground(new Color(224, 241, 228));

        JPanel text = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        text.setOpaque(false);
        text.add(title);
        text.add(message);
        bar.add(icon, BorderLayout.WEST);
        bar.add(text, BorderLayout.CENTER);
        return bar;
    }

    private String getInitial(String username) {
        if (username == null || username.isEmpty()) return "S";
        return username.substring(0, 1).toUpperCase();
    }

    // ============================================================
    // LOGIN HELPERS
    // ============================================================
    private void togglePasswordVisibility() {
        boolean show = passwordField.getEchoChar() != 0;
        setPasswordVisible(show);
        passwordField.requestFocusInWindow();
    }

    private void setPasswordVisible(boolean visible) {
        passwordField.setEchoChar(visible ? (char) 0 : '\u2022');
        eyeButton.setIcon(new EyeIcon(visible));
        eyeButton.setToolTipText(visible ? "Hide password" : "Show password");
        showPasswordCheckBox.setSelected(visible);
    }

    // Question 18 — Change Button Hover Effect
    private JButton createPrimaryButton(String text, Icon icon) {
        JButton button = new JButton(text, icon);
        button.setIconTextGap(10);
        button.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setRolloverEnabled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor set to hand cursor
        button.setPreferredSize(new Dimension(475, 45));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(PRIMARY_DARK); } // Darker hover
            @Override public void mouseExited(MouseEvent e) { button.setBackground(PRIMARY); } // Reverts to original
        });
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        button.setForeground(TEXT);
        button.setBackground(WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new RoundedButtonBorder(BORDER, 1, 10));
        button.setPreferredSize(new Dimension(475, 42));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(new Color(245, 248, 246)); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(WHITE); }
        });
        return button;
    }

    // Question 9 — Clear Button Does Not Clear the Password & Question 10 — Cursor Return
    private void clearFields() {
        usernameField.setText("");
        passwordField.setText(""); // Clears both fields
        setPasswordVisible(false);
        setStatus(" ", DANGER);
        usernameField.requestFocusInWindow(); // Returns focus to username
    }

    // ============================================================
    // CUSTOM COMPONENTS
    // ============================================================
    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;
        private boolean shadow;

        RoundedPanel(int radius, Color fill) {
            this.radius = radius;
            this.fill = fill;
            setOpaque(false);
        }

        void setShadow(boolean shadow) { this.shadow = shadow; }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (shadow) {
                g2.setColor(SHADOW);
                g2.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 7, radius, radius);
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedFieldBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;
        private final Icon icon;
        private final String placeholder;

        RoundedFieldBorder(Color color, int thickness, int radius, Icon icon, String placeholder) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
            this.icon = icon;
            this.placeholder = placeholder;
        }

        @Override public Insets getBorderInsets(Component c) {
            return getBorderInsets(c, new Insets(0, 0, 0, 0));
        }

        @Override public Insets getBorderInsets(Component c, Insets insets) {
            insets.top = 5;
            insets.left = icon == null ? 14 : 43;
            insets.bottom = 5;
            insets.right = 14;
            return insets;
        }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
            if (icon != null) icon.paintIcon(c, g2, x + 14, y + (height - icon.getIconHeight()) / 2);
            g2.dispose();
        }
    }

    private static class RoundedButtonBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;

        RoundedButtonBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(8, 10, 8, 10); }
    }

    private abstract static class SimpleIcon implements Icon {
        protected final int size;
        protected Color color;
        SimpleIcon(int size, Color color) { this.size = size; this.color = color; }
        void setColor(Color color) { this.color = color; }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
        protected Graphics2D graphics(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            return g2;
        }
    }

    private static class PortalLogoIcon extends SimpleIcon {
        private final Color accentColor;

        PortalLogoIcon(int size, Color mainColor, Color accentColor) {
            super(size, mainColor);
            this.accentColor = accentColor;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = graphics(g);

            g2.setColor(color);
            g2.fillOval(x, y, size, size);

            g2.setColor(accentColor);
            int padding = size / 8;
            int ringSize = size - (padding * 2);
            g2.setStroke(new BasicStroke(size * 0.04f));
            g2.drawOval(x + padding, y + padding, ringSize, ringSize);

            int centerX = x + size / 2;
            int centerY = y + size / 2;
            int capWidth = (int) (size * 0.45);
            int capHeight = (int) (size * 0.22);

            Polygon capTop = new Polygon();
            capTop.addPoint(centerX, centerY - capHeight);
            capTop.addPoint(centerX + capWidth / 2, centerY - capHeight / 2);
            capTop.addPoint(centerX, centerY);
            capTop.addPoint(centerX - capWidth / 2, centerY - capHeight / 2);

            g2.setColor(accentColor);
            g2.fillPolygon(capTop);

            int baseWidth = (int) (capWidth * 0.55);
            int baseHeight = (int) (capHeight * 0.6);
            g2.fillRoundRect(centerX - baseWidth / 2, centerY - 2, baseWidth, baseHeight, 3, 3);

            g2.setStroke(new BasicStroke(size * 0.03f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(centerX, centerY - capHeight / 2, centerX + capWidth / 2 + 2, centerY - capHeight / 4);
            g2.fillOval(centerX + capWidth / 2, centerY - capHeight / 4, (int) (size * 0.06), (int) (size * 0.06));

            g2.dispose();
        }
    }

    private static class UserIcon extends SimpleIcon {
        UserIcon() { super(18, MUTED); }
        UserIcon(Color color) { super(18, color); }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = graphics(g);
            g2.drawOval(x + 5, y + 1, 8, 8);
            g2.drawArc(x + 2, y + 8, 14, 10, 0, 180);
            g2.dispose();
        }
    }

    private static class LockIcon extends SimpleIcon {
        LockIcon() { super(18, MUTED); }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = graphics(g);
            g2.drawRoundRect(x + 4, y + 8, 10, 8, 2, 2);
            g2.drawArc(x + 5, y + 2, 8, 10, 0, 180);
            g2.dispose();
        }
    }

    private static class EyeIcon extends SimpleIcon {
        private final boolean visible;
        EyeIcon(boolean visible) { super(20, TEXT); this.visible = visible; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = graphics(g);
            g2.drawOval(x + 2, y + 5, 16, 10);
            g2.fillOval(x + 8, y + 8, 4, 4);
            if (!visible) g2.drawLine(x + 3, y + 3, x + 17, y + 17);
            g2.dispose();
        }
    }

    private static class ArrowIcon extends SimpleIcon {
        ArrowIcon() { super(16, Color.WHITE); }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = graphics(g);
            g2.drawLine(x + 2, y + 8, x + 13, y + 8);
            g2.drawLine(x + 9, y + 4, x + 13, y + 8);
            g2.drawLine(x + 9, y + 12, x + 13, y + 8);
            g2.dispose();
        }
    }

    private static class CheckIcon extends SimpleIcon {
        CheckIcon() { super(18, PRIMARY); }
        CheckIcon(Color color) { super(18, color); }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = graphics(g);
            g2.drawLine(x + 3, y + 9, x + 7, y + 13);
            g2.drawLine(x + 7, y + 13, x + 15, y + 4);
            g2.dispose();
        }
    }

    private static class HomeIcon extends SimpleIcon {
        HomeIcon() { super(18, Color.WHITE); }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = graphics(g);
            Polygon roof = new Polygon();
            roof.addPoint(x + 2, y + 8); roof.addPoint(x + 9, y + 2); roof.addPoint(x + 16, y + 8);
            g2.drawPolygon(roof);
            g2.drawRect(x + 5, y + 8, 8, 8);
            g2.dispose();
        }
    }

    private static class BookIcon extends SimpleIcon {
        BookIcon() { super(18, PRIMARY); }
        BookIcon(Color color) { super(18, color); }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = graphics(g);
            g2.drawRoundRect(x + 2, y + 2, 6, 14, 2, 2);
            g2.drawRoundRect(x + 10, y + 2, 6, 14, 2, 2);
            g2.drawLine(x + 8, y + 3, x + 8, y + 15);
            g2.dispose();
        }
    }

    private static class CalendarIcon extends SimpleIcon {
        CalendarIcon() { super(18, PRIMARY); }
        CalendarIcon(Color color) { super(18, color); }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = graphics(g);
            g2.drawRoundRect(x + 2, y + 4, 14, 12, 2, 2);
            g2.drawLine(x + 2, y + 8, x + 16, y + 8);
            g2.drawLine(x + 6, y + 2, x + 6, y + 6);
            g2.drawLine(x + 12, y + 2, x + 12, y + 6);
            g2.dispose();
        }
    }

    private static class BellIcon extends SimpleIcon {
        BellIcon() { super(18, PRIMARY_DARK); }
        BellIcon(Color color) { super(18, color); }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = graphics(g);
            g2.drawArc(x + 4, y + 3, 10, 12, 0, 180);
            g2.drawLine(x + 4, y + 9, x + 4, y + 13);
            g2.drawLine(x + 14, y + 9, x + 14, y + 13);
            g2.drawLine(x + 3, y + 14, x + 15, y + 14);
            g2.drawOval(x + 7, y + 14, 4, 3);
            g2.dispose();
        }
    }

    private static class GearIcon extends SimpleIcon {
        GearIcon() { super(18, Color.WHITE); }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = graphics(g);
            g2.drawOval(x + 3, y + 3, 12, 12);
            g2.drawOval(x + 7, y + 7, 4, 4);
            for (int i = 0; i < 8; i++) {
                double a = Math.PI * i / 4;
                int x1 = x + 9 + (int)(7 * Math.cos(a));
                int y1 = y + 9 + (int)(7 * Math.sin(a));
                int x2 = x + 9 + (int)(9 * Math.cos(a));
                int y2 = y + 9 + (int)(9 * Math.sin(a));
                g2.drawLine(x1, y1, x2, y2);
            }
            g2.dispose();
        }
    }

    private static class LogoutIcon extends SimpleIcon {
        LogoutIcon() { super(18, Color.WHITE); }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = graphics(g);
            g2.drawLine(x + 2, y + 9, x + 13, y + 9);
            g2.drawLine(x + 9, y + 5, x + 13, y + 9);
            g2.drawLine(x + 9, y + 13, x + 13, y + 9);
            g2.drawLine(x + 3, y + 4, x + 3, y + 14);
            g2.drawLine(x + 3, y + 4, x + 8, y + 4);
            g2.drawLine(x + 3, y + 14, x + 8, y + 14);
            g2.dispose();
        }
    }

    private static class CodeIcon extends SimpleIcon {
        CodeIcon() { super(18, PRIMARY); }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = graphics(g);
            g2.drawLine(x + 7, y + 4, x + 3, y + 9);
            g2.drawLine(x + 3, y + 9, x + 7, y + 14);
            g2.drawLine(x + 11, y + 4, x + 15, y + 9);
            g2.drawLine(x + 15, y + 9, x + 11, y + 14);
            g2.drawLine(x + 10, y + 3, x + 8, y + 15);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new LoginApp();
        });
    }
}