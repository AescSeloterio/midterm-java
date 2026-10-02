import javax.swing.*;
import javax.imageio.ImageIO;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
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
    private int scheduleYear = 1;
    private int scheduleSemester = 1;
    private int studyLoadYear = 1;
    private int studyLoadSemester = 1;

    // ============================================================
    // PHASE 6 — DASHBOARD REFRESH / ANNOUNCEMENTS / ATTENDANCE
    // These references let saved changes immediately appear on the dashboard.
    // ============================================================
    private final List<AnnouncementItem> announcementItems = new ArrayList<>();
    private JPanel announcementListPanel;
    private JPanel dashboardContent;
    private CardLayout dashboardContentLayout;
    private JFrame activeDashboard;

    // Dashboard / academic term selectors. These keep the selected overview term
    // when the UI is refreshed after saving subjects, grades, or attendance.
    private int dashboardYear = 1;
    private int dashboardSemester = 1;
    private int academicYear = 1;
    private int academicSemester = 1;

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
    // Editable curriculum seed. These are used only the first time an account opens
    // Academic Overview; after that, the account's curriculum file is authoritative.
    private static final String[] DEFAULT_SUBJECT_CODES = {
        "CIC 111", "CFP 110", "GE 100", "GE 7",
        "CPC 121", "GE 1", "NSTP 1", "PATHFIT 101"
    };

    private static final String[] DEFAULT_SUBJECT_NAMES = {
        "Introduction to Computing",
        "Fundamentals in Programming",
        "Mathematics in the Modern World",
        "Understanding the Self",
        "Professional Issues in Computing",
        "Purposive Communication",
        "National Service Training Program 1",
        "Physical Activities Toward Health and Fitness 101"
    };

    private static final double[] DEFAULT_SUBJECT_UNITS = {
        3, 3, 3, 3, 3, 3, 3, 2
    };


    public LoginApp() {
        createLoginUI();
    }

    // Campus photo used by the polished login / registration brand panels.
    // The image is bundled locally so the application does not depend on an internet
    // connection at runtime. It is based on the school-building photo supplied with
    // this project.
    private Image loadCampusImage() {
        String[] paths = {
            "src/campus.png",
            "campus.png"
        };
        for (String path : paths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                    return icon.getImage();
                }
            }
        }
        return null;
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
            "logo_white.png",
            "logo.png"
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
        CampusBrandPanel brand = new CampusBrandPanel(28, loadCampusImage());
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
        brandName.setFont(new Font(FONT_NAME, Font.BOLD, 16));

        JLabel headline = new JLabel("Your Academic Workspace");
        headline.setAlignmentX(Component.CENTER_ALIGNMENT);
        headline.setForeground(Color.WHITE);
        headline.setFont(new Font(FONT_NAME, Font.BOLD, 21));

        JLabel description = new JLabel("<html><div style='text-align:center; width:245px;'>"
                + "Access your classes, schedule, tasks and academic tools — all in one place."
                + "</div></html>");
        description.setAlignmentX(Component.CENTER_ALIGNMENT);
        description.setForeground(new Color(235, 246, 238));
        description.setFont(new Font(FONT_NAME, Font.PLAIN, 13));

        JPanel featureRow = new JPanel(new GridLayout(1, 3, 14, 0));
        featureRow.setOpaque(false);
        featureRow.setMaximumSize(new Dimension(270, 62));
        featureRow.add(createBrandFeature(whiteIcon(new BookIcon()), "Learn"));
        featureRow.add(createBrandFeature(whiteIcon(new CodeIcon()), "Track"));
        featureRow.add(createBrandFeature(whiteIcon(new UserIcon()), "Grow"));

        content.add(logoWrap);
        content.add(Box.createVerticalStrut(8));
        content.add(brandName);
        content.add(Box.createVerticalStrut(25));
        content.add(headline);
        content.add(Box.createVerticalStrut(9));
        content.add(description);
        content.add(Box.createVerticalStrut(30));
        content.add(featureRow);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        brand.add(content, gbc);
        return brand;
    }

    private JPanel createBrandFeature(Icon icon, String text) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(textLabel);
        return panel;
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
        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 34));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to your account to continue to Academia One.");
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
        // Adding usernameField to the content panel places the username input
        // into the login interface so it becomes visible to the user.
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
        dialog.setSize(1000, 600);
        dialog.setMinimumSize(new Dimension(920, 560));
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setIconImage(getIconImage());

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(PAGE);
        root.setBorder(new EmptyBorder(26, 34, 26, 34));

        RoundedPanel shell = new RoundedPanel(28, WHITE);
        shell.setLayout(new BorderLayout());
        shell.setPreferredSize(new Dimension(920, 510));
        shell.setShadow(true);
        shell.add(createRegistrationBrandPanel(), BorderLayout.WEST);

        JPanel formWrap = new JPanel(new GridBagLayout());
        formWrap.setOpaque(false);
        formWrap.setBorder(new EmptyBorder(24, 48, 24, 48));

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(400, 450));
        card.setMinimumSize(new Dimension(400, 450));
        card.setMaximumSize(new Dimension(400, 450));

        JLabel eyebrow = new JLabel("STUDENT ACCESS");
        eyebrow.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        eyebrow.setForeground(PRIMARY);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 32));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Set up your student portal credentials.");
        sub.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(eyebrow);
        card.add(Box.createVerticalStrut(6));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(22));

        JLabel userLabel = createDialogLabel("USERNAME");
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(userLabel);
        card.add(Box.createVerticalStrut(6));
        JTextField regUser = createTextField("Choose a username", new UserIcon());
        regUser.setMaximumSize(new Dimension(400, 46));
        regUser.setPreferredSize(new Dimension(400, 46));
        regUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(regUser);

        card.add(Box.createVerticalStrut(14));
        JLabel passLabel = createDialogLabel("PASSWORD");
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        JPasswordField regPass = createPasswordField("Create a password");
        regPass.setMaximumSize(new Dimension(400, 46));
        regPass.setPreferredSize(new Dimension(400, 46));
        regPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(regPass);

        card.add(Box.createVerticalStrut(14));
        JLabel confirmLabel = createDialogLabel("CONFIRM PASSWORD");
        confirmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(confirmLabel);
        card.add(Box.createVerticalStrut(6));
        JPasswordField confirm = createPasswordField("Re-enter your password");
        confirm.setMaximumSize(new Dimension(400, 46));
        confirm.setPreferredSize(new Dimension(400, 46));
        confirm.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(confirm);

        JPanel checkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkRow.setOpaque(false);
        checkRow.setMaximumSize(new Dimension(400, 25));
        checkRow.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        error.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(4));
        card.add(error);
        card.add(Box.createVerticalStrut(8));

        JButton create = createPrimaryButton("CREATE ACCOUNT", new CheckIcon());
        create.setMaximumSize(new Dimension(400, 44));
        create.setPreferredSize(new Dimension(400, 44));
        create.setAlignmentX(Component.LEFT_ALIGNMENT);
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

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        formGbc.weightx = 1;
        formGbc.weighty = 1;
        formGbc.anchor = GridBagConstraints.CENTER;
        formWrap.add(card, formGbc);

        shell.add(formWrap, BorderLayout.CENTER);
        root.add(shell);
        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(create);
        dialog.setVisible(true);
    }

    private JPanel createRegistrationBrandPanel() {
        CampusBrandPanel brand = new CampusBrandPanel(28, loadCampusImage());
        brand.setPreferredSize(new Dimension(400, 510));
        brand.setLayout(new GridBagLayout());
        brand.setBorder(new EmptyBorder(45, 48, 45, 48));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        ImageIcon logo = loadLogo(100, 80);
        JPanel logoWrap = new JPanel(new GridBagLayout());
        logoWrap.setOpaque(false);
        logoWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoWrap.setPreferredSize(new Dimension(104, 104));
        logoWrap.setMaximumSize(new Dimension(104, 104));
        if (logo != null) logoWrap.add(new JLabel(logo));

        JLabel brandName = new JLabel("ACADEMIA ONE");
        brandName.setForeground(Color.WHITE);
        brandName.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        brandName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel headline = new JLabel("Start Your Academic Journey");
        headline.setForeground(Color.WHITE);
        headline.setFont(new Font(FONT_NAME, Font.BOLD, 21));
        headline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel description = new JLabel("<html><div style='text-align:center; width:245px;'>"
                + "Create your account to access your classes, schedule, tasks and more."
                + "</div></html>");
        description.setForeground(new Color(235, 246, 238));
        description.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        description.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel featureRow = new JPanel(new GridLayout(1, 3, 14, 0));
        featureRow.setOpaque(false);
        featureRow.setMaximumSize(new Dimension(270, 62));
        featureRow.add(createBrandFeature(whiteIcon(new GearIcon()), "Secure"));
        featureRow.add(createBrandFeature(whiteIcon(new UserIcon()), "Student Focused"));
        featureRow.add(createBrandFeature(whiteIcon(new BookIcon()), "All in One"));

        content.add(logoWrap);
        content.add(Box.createVerticalStrut(8));
        content.add(brandName);
        content.add(Box.createVerticalStrut(25));
        content.add(headline);
        content.add(Box.createVerticalStrut(9));
        content.add(description);
        content.add(Box.createVerticalStrut(30));
        content.add(featureRow);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        brand.add(content, gbc);
        return brand;
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
        activeUsername = canonicalizeUsernameForStorage(username);
        JFrame dashboard = new JFrame("Academia One — Dashboard");
        dashboard.getRootPane().putClientProperty("loggedInUsername", username);
        // Open the dashboard maximized so the student does not have to manually
        // stretch the window before the full dashboard becomes usable. The content
        // still scrolls vertically on smaller displays.
        dashboard.setSize(1280, 780);
        dashboard.setMinimumSize(new Dimension(1000, 650));
        dashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashboard.setResizable(true);
        dashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE);

        CardLayout contentLayout = new CardLayout();
        JPanel content = new JPanel(contentLayout);
        content.setBackground(PAGE);
        dashboardContent = content;
        dashboardContentLayout = contentLayout;
        activeDashboard = dashboard;

        // ============================================================
        // MIDTERM IMPROVEMENT — PERSISTENT TASKS
        // Load the account's saved tasks. Sample tasks are created only once for
        // a new account and are not recreated after the user deletes them.
        // ============================================================
        loadTasks(username);
        loadSchedule(username);
        loadAnnouncements(username);

        JPanel dashboardCard = createDashboardMain(activeUsername, dashboard, contentLayout, content);
        dashboardCard.setName("dashboardCard");
        content.add(dashboardCard, "dashboard");
        JPanel profilePage = createProfilePage(username, content, contentLayout);
        profilePage.setName("profilePage");
        content.add(profilePage, "profile");
        JPanel academicPage = createAcademicPage(username);
        academicPage.setName("academicPage");
        content.add(academicPage, "academic");
        JPanel studyLoadPage = createStudyLoadPage(username);
        studyLoadPage.setName("studyLoadPage");
        content.add(studyLoadPage, "studyLoad");
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

    private void refreshAcademicPage() {
        if (dashboardContent == null || dashboardContentLayout == null || activeUsername == null) return;

        Component oldAcademic = null;
        boolean academicWasVisible = false;
        for (Component component : dashboardContent.getComponents()) {
            if ("academicPage".equals(component.getName())) {
                oldAcademic = component;
                academicWasVisible = component.isVisible();
                break;
            }
        }
        if (oldAcademic != null) {
            dashboardContent.remove(oldAcademic);
        }

        JPanel refreshed = createAcademicPage(activeUsername);
        refreshed.setName("academicPage");
        dashboardContent.add(refreshed, "academic");
        dashboardContent.revalidate();
        dashboardContent.repaint();
        if (academicWasVisible) {
            dashboardContentLayout.show(dashboardContent, "academic");
        }
    }

    private void refreshStudyLoadPage() {
        if (dashboardContent == null || dashboardContentLayout == null || activeUsername == null) return;

        Component oldStudyLoad = null;
        boolean wasVisible = false;
        for (Component component : dashboardContent.getComponents()) {
            if ("studyLoadPage".equals(component.getName())) {
                oldStudyLoad = component;
                wasVisible = component.isVisible();
                break;
            }
        }
        if (oldStudyLoad != null) dashboardContent.remove(oldStudyLoad);

        JPanel refreshed = createStudyLoadPage(activeUsername);
        refreshed.setName("studyLoadPage");
        dashboardContent.add(refreshed, "studyLoad");
        dashboardContent.revalidate();
        dashboardContent.repaint();
        if (wasVisible) dashboardContentLayout.show(dashboardContent, "studyLoad");
    }

    private void refreshDashboard() {
        if (dashboardContent == null || dashboardContentLayout == null || activeDashboard == null) return;

        Component oldDashboard = null;
        boolean dashboardWasVisible = false;
        for (Component component : dashboardContent.getComponents()) {
            if ("dashboardCard".equals(component.getName())) {
                oldDashboard = component;
                dashboardWasVisible = component.isVisible();
                break;
            }
        }

        if (oldDashboard != null) {
            dashboardContent.remove(oldDashboard);
        }

        JPanel refreshed = createDashboardMain(activeUsername, activeDashboard, dashboardContentLayout, dashboardContent);
        refreshed.setName("dashboardCard");
        dashboardContent.add(refreshed, "dashboard");
        dashboardContent.revalidate();
        dashboardContent.repaint();
        if (dashboardWasVisible) {
            dashboardContentLayout.show(dashboardContent, "dashboard");
        }
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
        JButton studyLoadButton = createNavItem("Study Load", new BookIcon(), false);
        navButtons.add(dashboardButton);
        navButtons.add(profileButton);
        navButtons.add(academicButton);
        navButtons.add(studyLoadButton);
        sidebar.add(dashboardButton);
        sidebar.add(profileButton);
        sidebar.add(academicButton);
        sidebar.add(studyLoadButton);

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
        attachNavAction(studyLoadButton, "studyLoad", layout, content, navButtons);
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
        JLabel avatar = createProfileAvatar(sidebarProfile.profilePicture, 38, getInitial(sidebarProfile.fullName));
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
            if ("academic".equals(card)) {
                refreshAcademicPage();
            } else if ("studyLoad".equals(card)) {
                refreshStudyLoadPage();
            }
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
        button.setBorder(new EmptyBorder(10, 10, 10, 10));
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

    private static final String DEFAULT_AVATAR = "default.png";
    private static final String[] AVATAR_FILES = {
            "avatar1.png", "avatar2.png", "avatar3.png", "avatar4.png",
            "avatar5.png", "avatar6.png", "avatar7.png", "avatar8.png"
    };

    private JLabel createProfileAvatar(String picture, int size, String fallbackInitial) {
        ImageIcon icon = loadAvatarIcon(picture, size);
        if (icon != null) {
            JLabel label = new JLabel(icon, SwingConstants.CENTER);
            label.setOpaque(false);
            label.setPreferredSize(new Dimension(size, size));
            label.setMinimumSize(new Dimension(size, size));
            label.setMaximumSize(new Dimension(size, size));
            return label;
        }
        return createAvatar(fallbackInitial, size, PRIMARY, Color.WHITE, Math.max(12, size / 3));
    }

    private ImageIcon loadAvatarIcon(String picture, int size) {
        String fileName = picture == null || picture.trim().isEmpty() ? DEFAULT_AVATAR : picture.trim();
        File[] candidates = {
                new File("src/avatars", fileName),
                new File("avatars", fileName),
                new File(fileName)
        };
        for (File file : candidates) {
            if (!file.isFile()) continue;
            try {
                BufferedImage source = ImageIO.read(file);
                if (source == null) continue;
                Image scaled = source.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            } catch (IOException ignored) {
                // Try the next supported local path.
            }
        }
        return null;
    }

    private String chooseProfilePicture(Component parent, String currentPicture) {
        final String[] selected = {currentPicture == null || currentPicture.trim().isEmpty()
                ? DEFAULT_AVATAR : currentPicture.trim()};

        JDialog picker = new JDialog(SwingUtilities.getWindowAncestor(parent), "Choose Profile Picture", Dialog.ModalityType.APPLICATION_MODAL);
        picker.setSize(520, 500);
        picker.setResizable(false);
        picker.setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(PAGE);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Choose a profile picture");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 22));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Select one of the eight student avatars, or use the default profile.");
        subtitle.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        subtitle.setForeground(MUTED);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        root.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 3, 12, 12));
        grid.setOpaque(false);
        ButtonGroup group = new ButtonGroup();

        List<JToggleButton> options = new ArrayList<>();
        List<String> names = new ArrayList<>();
        names.add(DEFAULT_AVATAR);
        for (String file : AVATAR_FILES) names.add(file);

        for (String fileName : names) {
            JToggleButton option = new JToggleButton();
            option.setFocusPainted(false);
            option.setOpaque(true);
            option.setBackground(WHITE);
            option.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1),
                    new EmptyBorder(8, 8, 8, 8)));
            option.setCursor(new Cursor(Cursor.HAND_CURSOR));
            ImageIcon icon = loadAvatarIcon(fileName, 100);
            if (icon != null) option.setIcon(icon);
            option.setToolTipText(DEFAULT_AVATAR.equals(fileName) ? "Default profile" : "Avatar " + fileName.substring(6, 7));
            option.addActionListener(e -> {
                selected[0] = fileName;
                for (JToggleButton other : options) {
                    boolean active = other == option;
                    other.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(active ? PRIMARY : BORDER, active ? 2 : 1),
                            new EmptyBorder(active ? 7 : 8, active ? 7 : 8, active ? 7 : 8, active ? 7 : 8)));
                }
            });
            group.add(option);
            options.add(option);
            grid.add(option);
            if (fileName.equals(selected[0])) {
                option.setSelected(true);
            }
        }

        // Apply the same visual selection treatment to the current option.
        int selectedIndex = names.indexOf(selected[0]);
        if (selectedIndex < 0) selectedIndex = 0;
        JToggleButton current = options.get(selectedIndex);
        current.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 2),
                new EmptyBorder(7, 7, 7, 7)));

        root.add(grid, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton cancel = createSecondaryButton("Cancel");
        JButton use = createPrimaryButton("Use Selected", null);
        cancel.setPreferredSize(new Dimension(100, 36));
        use.setPreferredSize(new Dimension(120, 36));
        cancel.addActionListener(e -> {
            selected[0] = currentPicture == null || currentPicture.trim().isEmpty() ? DEFAULT_AVATAR : currentPicture.trim();
            picker.dispose();
        });
        use.addActionListener(e -> picker.dispose());
        buttons.add(cancel);
        buttons.add(use);
        root.add(buttons, BorderLayout.SOUTH);

        picker.setContentPane(root);
        picker.getRootPane().setDefaultButton(use);
        picker.setVisible(true);
        return selected[0];
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
    // ============================================================
    // GPA SUMMARY
    // The dashboard can be viewed by any year/semester, so GPA values are
    // calculated from the saved grades and the selected curriculum term:
    //   • Semester = selected year + selected semester
    //   • Year     = all graded subjects in the selected year
    //   • Overall  = all graded subjects in the curriculum
    // Only subjects with valid saved grades are included in the weighted average.
    // ============================================================
    private String getGpaForTerm(String username, int year, int semester) {
        return calculateGpa(username, year, semester, false);
    }

    private String getGpaForYear(String username, int year) {
        return calculateGpa(username, year, 0, false);
    }

    private String getOverallGpa(String username) {
        return calculateGpa(username, 0, 0, true);
    }

    private String calculateGpa(String username, int year, int semester, boolean overall) {
        if (username == null || username.trim().isEmpty()) return "--";

        File file = getGradesFile(username);
        if (!file.exists()) return "--";

        List<CurriculumSubject> curriculum = loadCurriculum(username);
        double weightedTotal = 0;
        double totalUnits = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                if (parts.length != 2 || parts[1].trim().isEmpty()) continue;

                CurriculumSubject subject = findSubjectByCode(curriculum, parts[0].trim());
                if (subject == null) continue;
                if (!overall && subject.year != year) continue;
                if (!overall && semester > 0 && subject.semester != semester) continue;

                try {
                    double grade = Double.parseDouble(parts[1].trim());
                    if (grade >= 1.00 && grade <= 5.00 && subject.units > 0) {
                        weightedTotal += grade * subject.units;
                        totalUnits += subject.units;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore invalid saved values until corrected in Academic Overview.
                }
            }
        } catch (IOException e) {
            return "--";
        }

        return totalUnits == 0 ? "--" : String.format("%.2f", weightedTotal / totalUnits);
    }

    private JPanel createGpaSummaryStat(String username) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(new EmptyBorder(13, 15, 13, 15));
        card.setShadow(true);

        RoundedPanel iconBox = new RoundedPanel(12, PRIMARY_SOFT);
        iconBox.setPreferredSize(new Dimension(42, 42));
        iconBox.setLayout(new GridBagLayout());
        iconBox.add(new JLabel(new CheckIcon()));
        card.add(iconBox, BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout(0, 5));
        content.setOpaque(false);
        JLabel label = new JLabel("GPA");
        label.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        label.setForeground(MUTED);
        content.add(label, BorderLayout.NORTH);

        JPanel values = new JPanel(new GridLayout(1, 3, 7, 0));
        values.setOpaque(false);
        values.add(createGpaMiniValue("SEMESTER", getGpaForTerm(username, dashboardYear, dashboardSemester)));
        values.add(createGpaMiniValue("YEAR", getGpaForYear(username, dashboardYear)));
        values.add(createGpaMiniValue("OVERALL", getOverallGpa(username)));
        content.add(values, BorderLayout.CENTER);

        JLabel detail = new JLabel("Saved grade averages");
        detail.setFont(new Font(FONT_NAME, Font.PLAIN, 8));
        detail.setForeground(PRIMARY);
        content.add(detail, BorderLayout.SOUTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createGpaMiniValue(String label, String value) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        valueLabel.setForeground(TEXT);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel labelLabel = new JLabel(label, SwingConstants.CENTER);
        labelLabel.setFont(new Font(FONT_NAME, Font.BOLD, 7));
        labelLabel.setForeground(MUTED);
        labelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(valueLabel);
        panel.add(Box.createVerticalStrut(1));
        panel.add(labelLabel);
        return panel;
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

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        headerRight.setOpaque(false);
        JButton notification = new JButton(new BellIcon());
        notification.setToolTipText("View announcements");
        notification.setPreferredSize(new Dimension(38, 38));
        notification.setFocusPainted(false);
        notification.setBorderPainted(false);
        notification.setContentAreaFilled(true);
        notification.setBackground(PRIMARY_SOFT);
        notification.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notification.addActionListener(e -> contentLayout.show(content, "announcements"));

        JLabel academicYearLabel = new JLabel("AY 2026–2027");
        academicYearLabel.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        academicYearLabel.setForeground(MUTED);
        String[] dashboardTerms = {
                "1st Year • 1st Semester", "1st Year • 2nd Semester",
                "2nd Year • 1st Semester", "2nd Year • 2nd Semester",
                "3rd Year • 1st Semester", "3rd Year • 2nd Semester",
                "4th Year • 1st Semester", "4th Year • 2nd Semester"
        };
        JComboBox<String> dashboardTermSelector = new JComboBox<>(dashboardTerms);
        int selectedTermIndex = ((dashboardYear - 1) * 2) + (dashboardSemester - 1);
        dashboardTermSelector.setSelectedIndex(Math.max(0, Math.min(7, selectedTermIndex)));
        styleComboBox(dashboardTermSelector);
        dashboardTermSelector.setPreferredSize(new Dimension(180, 30));
        dashboardTermSelector.setToolTipText("Choose the year and semester to overview");
        dashboardTermSelector.addActionListener(e -> {
            int index = dashboardTermSelector.getSelectedIndex();
            if (index < 0) return;
            dashboardYear = (index / 2) + 1;
            dashboardSemester = (index % 2) + 1;
            refreshDashboard();
        });
        headerRight.add(notification);
        headerRight.add(academicYearLabel);
        headerRight.add(dashboardTermSelector);

        header.add(greeting, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);
        main.add(header);
        main.add(Box.createVerticalStrut(20));

        // Four high-level metrics.
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        stats.setMinimumSize(new Dimension(0, 112));
        AttendanceSummary attendanceSummary = getAttendanceSummary(username, dashboardYear, dashboardSemester);
        String attendanceValue = attendanceSummary.markedSessions == 0 ? "--" : attendanceSummary.getPercentText();
        String attendanceDetail = attendanceSummary.markedSessions == 0
                ? "No attendance recorded for this term"
                : attendanceSummary.present + " present • " + attendanceSummary.absent + " absent";
        stats.add(createPremiumStat("ATTENDANCE", attendanceValue, attendanceDetail, new CalendarIcon(), PRIMARY));
        int totalSubjects = loadCurriculum(username).size();
        stats.add(createPremiumStat("SUBJECTS", String.valueOf(totalSubjects),
                totalSubjects == 1 ? "1 subject in curriculum" : totalSubjects + " subjects in curriculum", new BookIcon(), PRIMARY));
        stats.add(createGpaSummaryStat(username));
        stats.add(createPremiumStat("TASKS", String.valueOf(taskItems.size()), "Saved tasks", new CodeIcon(), PRIMARY));
        main.add(stats);
        main.add(Box.createVerticalStrut(18));

        // Academic progress + today's attendance status.
        JPanel progressRow = new JPanel(new GridLayout(1, 2, 18, 0));
        progressRow.setOpaque(false);
        progressRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 178));
        progressRow.setMinimumSize(new Dimension(0, 178));
        progressRow.add(createProgressCard());
        progressRow.add(createAttendanceSummaryCard(username, dashboardYear, dashboardSemester));
        main.add(progressRow);
        main.add(Box.createVerticalStrut(18));

        // Main working area.
        JPanel workRow = new JPanel(new GridLayout(1, 2, 18, 0));
        workRow.setOpaque(false);
        workRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 282));
        workRow.setMinimumSize(new Dimension(0, 282));
        workRow.add(createUpcomingWorkCard(contentLayout, content));
        workRow.add(createTodayScheduleCard(username, contentLayout, content));
        main.add(workRow);
        main.add(Box.createVerticalStrut(18));

        // Bottom area: announcements + quick actions.
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 18, 0));
        bottomRow.setOpaque(false);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 185));
        bottomRow.setMinimumSize(new Dimension(0, 185));
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

        int total = taskItems.size();
        int completedCount = 0;
        for (TaskItem task : taskItems) {
            if (task.completed) completedCount++;
        }
        int percent = total == 0 ? 0 : (int) Math.round((completedCount * 100.0) / total);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Task progress");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 17));
        title.setForeground(TEXT);
        JLabel value = new JLabel(total == 0 ? "--" : percent + "%");
        value.setFont(new Font(FONT_NAME, Font.BOLD, 17));
        value.setForeground(PRIMARY);
        head.add(title, BorderLayout.WEST);
        head.add(value, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel desc = new JLabel(total == 0 ? "Add tasks to track your progress" : "Completed tasks");
        desc.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        desc.setForeground(MUTED);
        center.add(desc);
        center.add(Box.createVerticalStrut(10));
        center.add(createProgressBar(percent));
        center.add(Box.createVerticalStrut(9));
        JLabel completed = new JLabel(total == 0
                ? "No tasks recorded yet"
                : "Completed  •  " + completedCount + " of " + total + " tasks");
        completed.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        completed.setForeground(MUTED);
        center.add(completed);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel createAttendanceSummaryCard(String username, int year, int semester) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(18, 20, 17, 20));
        card.setShadow(true);

        AttendanceSummary summary = getAttendanceSummary(username, year, semester);
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Attendance • " + yearName(year) + " • " + semesterName(semester));
        title.setFont(new Font(FONT_NAME, Font.BOLD, 17));
        title.setForeground(TEXT);
        JLabel status = new JLabel(summary.markedSessions == 0 ? "NO DATA" : "RECORDED");
        status.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        status.setForeground(summary.markedSessions == 0 ? MUTED : PRIMARY);
        head.add(title, BorderLayout.WEST);
        head.add(status, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 3, 10, 0));
        center.setOpaque(false);
        center.add(createMiniMetric("PRESENT", String.valueOf(summary.present), PRIMARY));
        center.add(createMiniMetric("ABSENT", String.valueOf(summary.absent), DANGER));
        center.add(createMiniMetric("UNMARKED", String.valueOf(summary.unmarked), AMBER));
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
        JButton view = createTextLink("View tasks");
        view.addActionListener(e -> layout.show(content, "tasks"));
        head.add(title, BorderLayout.WEST);
        head.add(view, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        int shown = 0;
        for (TaskItem task : taskItems) {
            if (task.completed) continue;
            Color accent = shown % 2 == 0 ? PRIMARY : AMBER;
            list.add(createTaskRow(task.subject, task.title, task.due, accent));
            shown++;
            if (shown >= 4) break;
            list.add(Box.createVerticalStrut(7));
        }
        if (shown == 0) {
            JLabel empty = new JLabel(taskItems.isEmpty() ?
                    "No tasks yet. Add one in My Tasks." : "All saved tasks are completed.");
            empty.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            empty.setForeground(MUTED);
            list.add(empty);
        }
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
        String today = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        List<ScheduleItem> todaysClasses = new ArrayList<>();
        for (ScheduleItem item : scheduleItems) {
            if (item.year == dashboardYear && item.semester == dashboardSemester
                    && item.day.equalsIgnoreCase(today)) {
                todaysClasses.add(item);
            }
        }

        int shown = Math.min(4, todaysClasses.size());
        for (int i = 0; i < shown; i++) {
            ScheduleItem item = todaysClasses.get(i);
            String tag = i == 0 ? "FIRST" : (i == 1 ? "NEXT" : item.getTimeRange());
            list.add(createScheduleCompact(item.getTimeRange(), item.subject, item.room, tag, i < 2 ? PRIMARY : MUTED));
            if (i < shown - 1) list.add(Box.createVerticalStrut(6));
        }
        if (shown == 0) {
            JLabel empty = new JLabel("No classes scheduled for today. Add one in Class Schedule.");
            empty.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            empty.setForeground(MUTED);
            list.add(empty);
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel createScheduleCompact(String timeRange, String subject, String room, String tag, Color accent) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JLabel timeLabel = new JLabel("<html><div style='text-align:left;'>" + escapeHtml(timeRange) + "</div></html>");
        timeLabel.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        timeLabel.setForeground(accent);
        timeLabel.setPreferredSize(new Dimension(105, 38));

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
        JButton view = createTextLink("Manage");
        view.addActionListener(e -> layout.show(content, "announcements"));
        head.add(title, BorderLayout.WEST);
        head.add(view, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        int shown = Math.min(3, announcementItems.size());
        for (int i = 0; i < shown; i++) {
            AnnouncementItem item = announcementItems.get(i);
            list.add(createAnnouncementCompact(item.title, item.tag, i == 0));
            if (i < shown - 1) list.add(Box.createVerticalStrut(7));
        }
        if (shown == 0) {
            JLabel empty = new JLabel("No announcements yet. Add one in Announcements.");
            empty.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            empty.setForeground(MUTED);
            list.add(empty);
        }
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
        String compactTitle = title == null ? "" : title.trim();
        String fullTitle = compactTitle;
        if (compactTitle.length() > 68) compactTitle = compactTitle.substring(0, 65) + "...";
        JLabel a = new JLabel(compactTitle);
        a.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        a.setForeground(TEXT);
        a.setToolTipText(fullTitle);
        JLabel b = new JLabel(meta == null ? "" : meta);
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
        grid.add(createDashboardAction("Study Load", new BookIcon(PRIMARY_DARK), "studyLoad", layout, content));
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
        JPanel main = createPageShell(
                "Student record",
                "My Profile",
                "Your student identity, academic information and portal account details.");

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // Profile hero — deliberately larger than the old two-card layout so the page
        // feels like a real student profile rather than a small settings panel.
        RoundedPanel hero = new RoundedPanel(20, WHITE);
        hero.setLayout(new BorderLayout(20, 0));
        hero.setBorder(new EmptyBorder(24, 26, 24, 26));
        hero.setShadow(true);
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel profileAvatar = createProfileAvatar(profile.profilePicture, 88, getInitial(profile.fullName));
        JPanel avatarHolder = new JPanel(new GridBagLayout());
        avatarHolder.setOpaque(false);
        avatarHolder.setPreferredSize(new Dimension(92, 92));
        avatarHolder.add(profileAvatar);
        hero.add(avatarHolder, BorderLayout.WEST);

        JPanel identity = new JPanel();
        identity.setOpaque(false);
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));
        JLabel name = createWrappedNameLabel(profile.fullName, 22, TEXT, 430, "profileDisplayName");
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel program = new JLabel(profile.program + " • " + profile.yearLevel + " • Section " + profile.section);
        program.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        program.setForeground(MUTED);
        program.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel id = new JLabel("Student ID  " + profile.studentId);
        id.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        id.setForeground(PRIMARY);
        id.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel status = new JLabel("●  Currently enrolled");
        status.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        status.setForeground(new Color(35, 125, 68));
        status.setAlignmentX(Component.LEFT_ALIGNMENT);
        identity.add(name);
        identity.add(Box.createVerticalStrut(5));
        identity.add(program);
        identity.add(Box.createVerticalStrut(9));
        identity.add(id);
        identity.add(Box.createVerticalStrut(5));
        identity.add(status);
        hero.add(identity, BorderLayout.CENTER);

        JButton editButton = createPrimaryButton("EDIT PROFILE", null);
        editButton.setPreferredSize(new Dimension(125, 38));
        editButton.addActionListener(e -> showEditProfileDialog(username, content, contentLayout));
        JPanel editHolder = new JPanel(new GridBagLayout());
        editHolder.setOpaque(false);
        editHolder.add(editButton);
        hero.add(editHolder, BorderLayout.EAST);
        body.add(hero);
        body.add(Box.createVerticalStrut(16));

        // Snapshot cards connect the profile to actual portal data.
        int totalUnits = getStudyLoadTotalUnits();
        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        stats.add(createProfileStat("ENROLLED SUBJECTS", String.valueOf(scheduleItems.size()), "from Class Schedule"));
        stats.add(createProfileStat("TOTAL UNITS", String.valueOf(totalUnits), "from Study Load"));
        stats.add(createProfileStat("CLASS ENTRIES", String.valueOf(scheduleItems.size()), "current term schedule"));
        body.add(stats);
        body.add(Box.createVerticalStrut(16));

        JPanel detailsGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        detailsGrid.setOpaque(false);
        detailsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedPanel enrollment = createProfileSectionCard("Enrollment details", "Your current academic identity");
        enrollment.add(createProfileInfo("Student ID", profile.studentId));
        enrollment.add(createProfileInfo("Program", profile.program));
        enrollment.add(createProfileInfo("Year level", profile.yearLevel));
        enrollment.add(createProfileInfo("Section", profile.section));
        enrollment.add(createProfileInfo("Gender", profile.gender));
        enrollment.add(createProfileInfo("Semester", "1st Semester"));
        enrollment.add(createProfileInfo("School year", "2026 – 2027"));
        detailsGrid.add(enrollment);

        RoundedPanel contact = createProfileSectionCard("Contact & account", "Information you can update from Edit Profile");
        contact.add(createProfileInfo("Username", username));
        contact.add(createProfileInfo("Email", profile.email.isEmpty() ? "Not set" : profile.email));
        contact.add(createProfileInfo("Mobile", profile.mobile.isEmpty() ? "Not set" : profile.mobile));
        contact.add(createProfileInfo("Address", profile.address.isEmpty() ? "Not set" : profile.address));
        contact.add(createProfileInfo("Portal role", "Student"));
        contact.add(createProfileInfo("Account status", "Active"));
        detailsGrid.add(contact);

        body.add(detailsGrid);
        body.add(Box.createVerticalStrut(16));

        JLabel note = new JLabel("Profile information is saved locally on this computer. Login credentials remain separate from editable student details.");
        note.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        note.setForeground(MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(note);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    private RoundedPanel createProfileSectionCard(String title, String subtitle) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));
        card.setShadow(true);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(createCardTitle(title));
        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        sub.setForeground(MUTED);
        card.add(Box.createVerticalStrut(3));
        card.add(sub);
        card.add(Box.createVerticalStrut(12));
        return card;
    }

    private JPanel createProfileStat(String label, String value, String hint) {
        RoundedPanel card = new RoundedPanel(16, WHITE);
        card.setLayout(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(15, 17, 15, 17));
        card.setShadow(true);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font(FONT_NAME, Font.BOLD, 24));
        valueLabel.setForeground(PRIMARY_DARK);
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        labelLabel.setForeground(TEXT);
        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 9));
        hintLabel.setForeground(MUTED);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(valueLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(labelLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(hintLabel);
        card.add(text, BorderLayout.CENTER);
        return card;
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
        dialog.setSize(600, 700);
        dialog.setMinimumSize(new Dimension(560, 640));
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(0, 14));
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
        root.add(header, BorderLayout.NORTH);

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
        formCard.add(Box.createVerticalStrut(15));

        JTextField fullNameField = createProfileInput(profile.fullName);
        JTextField studentIdField = createProfileInput(profile.studentId);
        JTextField programField = createProfileInput(profile.program);
        JTextField yearField = createProfileInput(profile.yearLevel);
        JTextField sectionField = createProfileInput(profile.section);
        JTextField emailField = createProfileInput(profile.email);
        JTextField mobileField = createProfileInput(profile.mobile);
        JTextField addressField = createProfileInput(profile.address);

        JPanel picturePanel = new JPanel(new BorderLayout(14, 0));
        picturePanel.setOpaque(false);
        picturePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel picturePreview = createProfileAvatar(profile.profilePicture, 74, getInitial(profile.fullName));
        JPanel previewHolder = new JPanel(new GridBagLayout());
        previewHolder.setOpaque(false);
        previewHolder.setPreferredSize(new Dimension(78, 78));
        previewHolder.add(picturePreview);
        picturePanel.add(previewHolder, BorderLayout.WEST);
        final JPanel[] previewHolderRef = {previewHolder};
        JPanel pictureText = new JPanel();
        pictureText.setOpaque(false);
        pictureText.setLayout(new BoxLayout(pictureText, BoxLayout.Y_AXIS));
        JLabel pictureTitle = new JLabel("Profile picture");
        pictureTitle.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        pictureTitle.setForeground(TEXT);
        JLabel pictureHint = new JLabel("Choose from 8 avatars or keep the default.");
        pictureHint.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        pictureHint.setForeground(MUTED);
        JButton changePicture = createSecondaryButton("Choose Picture");
        changePicture.setPreferredSize(new Dimension(125, 34));
        final String[] selectedPicture = {profile.profilePicture};
        changePicture.addActionListener(e -> {
            selectedPicture[0] = chooseProfilePicture(dialog, selectedPicture[0]);
            JLabel replacement = createProfileAvatar(selectedPicture[0], 74, getInitial(fullNameField.getText().trim()));
            picturePanel.remove(previewHolderRef[0]);
            JPanel newHolder = new JPanel(new GridBagLayout());
            newHolder.setOpaque(false);
            newHolder.setPreferredSize(new Dimension(78, 78));
            newHolder.add(replacement);
            previewHolderRef[0] = newHolder;
            picturePanel.add(newHolder, BorderLayout.WEST);
            picturePanel.revalidate();
            picturePanel.repaint();
        });
        pictureText.add(pictureTitle);
        pictureText.add(Box.createVerticalStrut(3));
        pictureText.add(pictureHint);
        pictureText.add(Box.createVerticalStrut(8));
        pictureText.add(changePicture);
        picturePanel.add(pictureText, BorderLayout.CENTER);
        formCard.add(picturePanel);
        formCard.add(Box.createVerticalStrut(14));

        JComboBox<String> genderField = new JComboBox<>(new String[]{"Prefer not to say", "Male", "Female", "Other"});
        genderField.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        genderField.setPreferredSize(new Dimension(0, 36));
        genderField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        styleComboBox(genderField);
        int genderIndex = 0;
        for (int i = 0; i < genderField.getItemCount(); i++) {
            if (genderField.getItemAt(i).equals(profile.gender)) { genderIndex = i; break; }
        }
        genderField.setSelectedIndex(genderIndex);

        formCard.add(createDialogField("Full Name", fullNameField));
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(createDialogField("Student ID", studentIdField));
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(createDialogField("Program", programField));
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(createDialogField("Year Level", yearField));
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(createDialogField("Section", sectionField));
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(createDialogField("Gender", genderField));
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(createDialogField("Email", emailField));
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(createDialogField("Mobile", mobileField));
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(createDialogField("Address", addressField));

        JScrollPane formScroll = new JScrollPane(formCard);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.getVerticalScrollBar().setUnitIncrement(12);
        root.add(formScroll, BorderLayout.CENTER);

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
            String gender = String.valueOf(genderField.getSelectedItem());
            String email = emailField.getText().trim();
            String mobile = mobileField.getText().trim();
            String address = addressField.getText().trim();

            if (fullName.isEmpty() || studentId.isEmpty() || program.isEmpty() || year.isEmpty() || section.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please complete all required profile fields.",
                        "Incomplete Profile", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (containsProfileDelimiter(fullName, studentId, program, year, section, gender, email, mobile, address)) {
                JOptionPane.showMessageDialog(dialog,
                        "The character '|' cannot be used in profile information.",
                        "Invalid Character", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ProfileData updated = new ProfileData(fullName, studentId, program, year, section, email, mobile, address, gender, selectedPicture[0]);
            if (!saveProfile(username, updated)) {
                JOptionPane.showMessageDialog(dialog,
                        "The profile could not be saved.",
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            refreshDisplayedProfile(fullName, selectedPicture[0], content);
            Component profileCard = findNamedComponent(content, "profilePage");
            if (profileCard != null) content.remove(profileCard);
            JPanel rebuiltProfile = createProfilePage(username, content, contentLayout);
            rebuiltProfile.setName("profilePage");
            content.add(rebuiltProfile, "profile");
            contentLayout.show(content, "profile");
            content.revalidate();
            content.repaint();
            refreshDashboard();
            dialog.dispose();
        });
        buttons.add(cancel);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private boolean containsProfileDelimiter(String... values) {
        for (String value : values) {
            if (value != null && value.contains("|")) return true;
        }
        return false;
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

    private void refreshDisplayedProfile(String fullName, String profilePicture, JPanel content) {
        Container root = content.getParent();
        if (root == null) return;

        updateNamedLabel(root, "sidebarProfileName", fullName);
        updateNamedAvatar(root, "sidebarProfileAvatar", profilePicture, getInitial(fullName));
        updateNamedLabel(root, "dashboardGreeting", "Good day, " + fullName + "!");
    }

    private boolean updateNamedAvatar(Container parent, String componentName, String profilePicture, String fallbackInitial) {
        for (Component component : parent.getComponents()) {
            if (componentName.equals(component.getName()) && component instanceof JLabel) {
                JLabel label = (JLabel) component;
                ImageIcon icon = loadAvatarIcon(profilePicture, label.getWidth() > 0 ? label.getWidth() : 38);
                if (icon != null) {
                    label.setText("");
                    label.setIcon(icon);
                } else {
                    label.setIcon(null);
                    label.setText(fallbackInitial);
                    label.setForeground(PRIMARY_DARK);
                    label.setFont(new Font(FONT_NAME, Font.BOLD, 15));
                }
                label.revalidate();
                label.repaint();
                return true;
            }
            if (component instanceof Container && updateNamedAvatar((Container) component, componentName, profilePicture, fallbackInitial)) {
                return true;
            }
        }
        return false;
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

    private JPanel createDialogField(String labelText, JComponent field) {
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
                "Build your prospectus by year and semester, then enter grades and attendance for each term.");

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);

        JPanel termBar = new JPanel(new BorderLayout(12, 0));
        termBar.setOpaque(false);

        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selectors.setOpaque(false);
        JLabel yearLabel = new JLabel("Year");
        yearLabel.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        yearLabel.setForeground(MUTED);
        JComboBox<String> yearSelector = new JComboBox<>(new String[]{
                "1st Year", "2nd Year", "3rd Year", "4th Year"
        });
        JComboBox<String> semesterSelector = new JComboBox<>(new String[]{
                "1st Semester", "2nd Semester"
        });
        yearSelector.setSelectedIndex(Math.max(0, Math.min(3, academicYear - 1)));
        semesterSelector.setSelectedIndex(Math.max(0, Math.min(1, academicSemester - 1)));
        styleComboBox(yearSelector);
        styleComboBox(semesterSelector);
        selectors.add(yearLabel);
        selectors.add(yearSelector);
        selectors.add(semesterSelector);

        JLabel termHint = new JLabel("Select a term to manage its subjects.");
        termHint.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        termHint.setForeground(MUTED);
        selectors.add(termHint);
        termBar.add(selectors, BorderLayout.WEST);

        JButton addSubject = createPrimaryButton("ADD SUBJECT", null);
        addSubject.setPreferredSize(new Dimension(140, 38));
        addSubject.setMaximumSize(new Dimension(140, 38));
        termBar.add(addSubject, BorderLayout.EAST);

        RoundedPanel curriculumCard = new RoundedPanel(18, WHITE);
        curriculumCard.setLayout(new BorderLayout(0, 10));
        curriculumCard.setBorder(new EmptyBorder(18, 20, 18, 20));
        curriculumCard.setShadow(true);

        JPanel curriculumHeader = new JPanel(new BorderLayout());
        curriculumHeader.setOpaque(false);
        JPanel curriculumTitle = new JPanel();
        curriculumTitle.setOpaque(false);
        curriculumTitle.setLayout(new BoxLayout(curriculumTitle, BoxLayout.Y_AXIS));
        curriculumTitle.add(createCardTitle("Curriculum subjects"));
        JLabel curriculumHint = new JLabel("Add, edit or remove the subjects from your prospectus. Changes are saved locally.");
        curriculumHint.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        curriculumHint.setForeground(MUTED);
        curriculumTitle.add(Box.createVerticalStrut(3));
        curriculumTitle.add(curriculumHint);
        curriculumHeader.add(curriculumTitle, BorderLayout.WEST);
        curriculumCard.add(curriculumHeader, BorderLayout.NORTH);

        JPanel subjectList = new JPanel();
        subjectList.setOpaque(false);
        subjectList.setLayout(new BoxLayout(subjectList, BoxLayout.Y_AXIS));
        curriculumCard.add(subjectList, BorderLayout.CENTER);

        JPanel gradeHost = new JPanel(new BorderLayout());
        gradeHost.setOpaque(false);
        JPanel attendanceHost = new JPanel(new BorderLayout());
        attendanceHost.setOpaque(false);

        JPanel academicStack = new JPanel();
        academicStack.setOpaque(false);
        academicStack.setLayout(new BoxLayout(academicStack, BoxLayout.Y_AXIS));
        academicStack.add(curriculumCard);
        academicStack.add(Box.createVerticalStrut(14));
        academicStack.add(gradeHost);
        academicStack.add(Box.createVerticalStrut(14));
        academicStack.add(attendanceHost);

        JScrollPane pageScroll = new JScrollPane(academicStack);
        pageScroll.setBorder(BorderFactory.createEmptyBorder());
        pageScroll.setOpaque(false);
        pageScroll.getViewport().setOpaque(false);

        // Keep the summary cards in their own host so they can be rebuilt immediately
        // whenever the selected year/semester changes. Previously these cards were
        // created only once, which made the attendance summary appear stuck on the
        // first term even though the detailed attendance card had changed.
        JPanel overviewHost = new JPanel(new BorderLayout());
        overviewHost.setOpaque(false);
        overviewHost.add(createOverviewCards(username), BorderLayout.CENTER);

        // Put the long academic content below the selectors without forcing the window to grow.
        JPanel contentArea = new JPanel(new BorderLayout(0, 12));
        contentArea.setOpaque(false);
        contentArea.add(termBar, BorderLayout.NORTH);
        contentArea.add(pageScroll, BorderLayout.CENTER);
        body.removeAll();
        body.add(overviewHost, BorderLayout.NORTH);
        body.add(contentArea, BorderLayout.CENTER);

        Runnable[] refreshHolder = new Runnable[1];
        refreshHolder[0] = () -> {
            int year = yearSelector.getSelectedIndex() + 1;
            int semester = semesterSelector.getSelectedIndex() + 1;
            List<CurriculumSubject> subjects = getSubjectsForTerm(username, year, semester);

            refreshCurriculumSubjectList(username, year, semester, subjects, subjectList, refreshHolder[0]);
            gradeHost.removeAll();
            gradeHost.add(createGradeCard(username, subjects), BorderLayout.CENTER);
            attendanceHost.removeAll();
            attendanceHost.add(createAttendanceCard(username, subjects), BorderLayout.CENTER);

            // Refresh the term summary cards at the same time as the detailed cards.
            // This makes the attendance percentage/count immediately follow the
            // selected year and semester without requiring SAVE ATTENDANCE.
            overviewHost.removeAll();
            overviewHost.add(createOverviewCards(username), BorderLayout.CENTER);

            overviewHost.revalidate();
            overviewHost.repaint();
            gradeHost.revalidate();
            attendanceHost.revalidate();
            gradeHost.repaint();
            attendanceHost.repaint();
            pageScroll.revalidate();
            pageScroll.repaint();
            contentArea.revalidate();
            contentArea.repaint();
        };

        addSubject.addActionListener(e -> showSubjectDialog(username, yearSelector.getSelectedIndex() + 1,
                semesterSelector.getSelectedIndex() + 1, null, refreshHolder[0]));
        yearSelector.addActionListener(e -> {
            academicYear = yearSelector.getSelectedIndex() + 1;
            refreshHolder[0].run();
        });
        semesterSelector.addActionListener(e -> {
            academicSemester = semesterSelector.getSelectedIndex() + 1;
            refreshHolder[0].run();
        });

        refreshHolder[0].run();
        main.add(body, BorderLayout.CENTER);
        return main;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        combo.setForeground(TEXT);
        combo.setBackground(WHITE);
        combo.setPreferredSize(new Dimension(130, 36));
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JPanel createGradeCard(String username, List<CurriculumSubject> subjects) {
        RoundedPanel gradeCard = new RoundedPanel(18, WHITE);
        gradeCard.setLayout(new BorderLayout(0, 12));
        gradeCard.setBorder(new EmptyBorder(18, 20, 18, 20));
        gradeCard.setShadow(true);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(createCardTitle("Grades and GPA calculator"), BorderLayout.WEST);
        JLabel helper = new JLabel(subjects.isEmpty() ? "Add subjects above before entering grades." : "Enter grades using your school's grading scale.");
        helper.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        helper.setForeground(MUTED);
        titleRow.add(helper, BorderLayout.EAST);
        gradeCard.add(titleRow, BorderLayout.NORTH);

        JPanel gradeRows = new JPanel();
        gradeRows.setOpaque(false);
        gradeRows.setLayout(new BoxLayout(gradeRows, BoxLayout.Y_AXIS));
        List<JTextField> gradeFields = new ArrayList<>();

        for (CurriculumSubject subject : subjects) {
            gradeRows.add(createGradeInputRow(subject.name, subject.code, subject.units,
                    loadGradeForSubject(username, subject.code), gradeFields));
        }
        if (subjects.isEmpty()) {
            JLabel empty = new JLabel("No subjects have been added for this term yet.");
            empty.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
            empty.setForeground(MUTED);
            empty.setBorder(new EmptyBorder(22, 0, 22, 0));
            gradeRows.add(empty);
        }

        gradeCard.add(gradeRows, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        JLabel result = new JLabel("GPA: " + getGpaForTerm(username, academicYear, academicSemester));
        result.setFont(new Font(FONT_NAME, Font.BOLD, 17));
        result.setForeground(PRIMARY_DARK);
        JButton calculate = createPrimaryButton("SAVE GRADES & CALCULATE", null);
        calculate.setPreferredSize(new Dimension(205, 40));
        calculate.setMaximumSize(new Dimension(205, 40));
        calculate.addActionListener(e -> {
            if (subjects.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Add at least one subject to this term first.",
                        "No Subjects", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            double weightedTotal = 0;
            double totalUnits = 0;
            int entered = 0;
            for (int i = 0; i < gradeFields.size(); i++) {
                String raw = gradeFields.get(i).getText().trim();
                if (raw.isEmpty()) continue;
                try {
                    double grade = Double.parseDouble(raw);
                    if (grade < 1.00 || grade > 5.00) throw new NumberFormatException();
                    weightedTotal += grade * subjects.get(i).units;
                    totalUnits += subjects.get(i).units;
                    entered++;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter a valid grade from 1.00 to 5.00 for " + subjects.get(i).code + ".",
                            "Invalid Grade", JOptionPane.WARNING_MESSAGE);
                    gradeFields.get(i).requestFocusInWindow();
                    return;
                }
            }
            if (entered == 0) {
                JOptionPane.showMessageDialog(this, "Enter at least one grade before calculating.",
                        "No Grades Entered", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!saveGrades(username, subjects, gradeFields)) {
                JOptionPane.showMessageDialog(this, "The grades could not be saved.",
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double gpa = weightedTotal / totalUnits;
            result.setText(String.format("GPA: %.2f", gpa));
            JOptionPane.showMessageDialog(this,
                    String.format("Weighted GPA calculated: %.2f%nGrades entered: %d of %d", gpa, entered, subjects.size()),
                    "GPA Result", JOptionPane.INFORMATION_MESSAGE);
            refreshDashboard();
        });
        footer.add(result, BorderLayout.WEST);
        footer.add(calculate, BorderLayout.EAST);
        gradeCard.add(footer, BorderLayout.SOUTH);
        return gradeCard;
    }

    private JPanel createGradeInputRow(String subject, String code, double units, String savedGrade, List<JTextField> gradeFields) {
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

    private JPanel createAttendanceCard(String username, List<CurriculumSubject> subjects) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setShadow(true);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(createCardTitle("Attendance record"), BorderLayout.WEST);
        JLabel helper = new JLabel(subjects.isEmpty() ? "No subjects in this term." : "Enter present, absent and unmarked session counts.");
        helper.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        helper.setForeground(MUTED);
        header.add(helper, BorderLayout.EAST);

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        List<JTextField> presentFields = new ArrayList<>();
        List<JTextField> absentFields = new ArrayList<>();
        List<JTextField> unmarkedFields = new ArrayList<>();
        for (CurriculumSubject subject : subjects) {
            String[] saved = loadAttendanceForSubject(username, subject.year, subject.semester, subject.code);
            rows.add(createAttendanceInputRow(subject.name, subject.code, saved[0], saved[1], saved[2],
                    presentFields, absentFields, unmarkedFields));
        }
        if (subjects.isEmpty()) {
            JLabel empty = new JLabel("Add subjects above to record attendance for this term.");
            empty.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
            empty.setForeground(MUTED);
            empty.setBorder(new EmptyBorder(22, 0, 22, 0));
            rows.add(empty);
        }
        card.add(rows, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel note = new JLabel("Use 0 when there are no sessions in a category.");
        note.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        note.setForeground(MUTED);
        JButton save = createPrimaryButton("SAVE ATTENDANCE", new CheckIcon());
        save.setPreferredSize(new Dimension(170, 40));
        save.setMaximumSize(new Dimension(170, 40));
        save.setEnabled(!subjects.isEmpty());
        save.addActionListener(e -> {
            for (int i = 0; i < subjects.size(); i++) {
                if (!isValidCount(presentFields.get(i).getText())
                        || !isValidCount(absentFields.get(i).getText())
                        || !isValidCount(unmarkedFields.get(i).getText())) {
                    JOptionPane.showMessageDialog(this,
                            "Attendance values must be whole numbers 0 or greater for " + subjects.get(i).code + ".",
                            "Invalid Attendance", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            if (!saveAttendance(username, subjects, presentFields, absentFields, unmarkedFields)) {
                JOptionPane.showMessageDialog(this, "The attendance record could not be saved.",
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Attendance record saved.",
                    "Attendance Saved", JOptionPane.INFORMATION_MESSAGE);
            refreshAcademicPage();
            refreshDashboard();
        });
        footer.add(note, BorderLayout.WEST);
        footer.add(save, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createAttendanceInputRow(String subject, String code, String present, String absent, String unmarked,
            List<JTextField> presentFields, List<JTextField> absentFields, List<JTextField> unmarkedFields) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(8, 0, 8, 0)));

        JPanel subjectPanel = new JPanel();
        subjectPanel.setOpaque(false);
        subjectPanel.setLayout(new BoxLayout(subjectPanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(subject);
        title.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        title.setForeground(TEXT);
        JLabel details = new JLabel(code);
        details.setFont(new Font(FONT_NAME, Font.PLAIN, 9));
        details.setForeground(MUTED);
        subjectPanel.add(title);
        subjectPanel.add(Box.createVerticalStrut(2));
        subjectPanel.add(details);
        row.add(subjectPanel, BorderLayout.CENTER);

        JPanel fields = new JPanel(new GridLayout(1, 3, 6, 0));
        fields.setOpaque(false);
        JTextField p = createSmallNumberField(present);
        JTextField a = createSmallNumberField(absent);
        JTextField u = createSmallNumberField(unmarked);
        p.setToolTipText("Present sessions");
        a.setToolTipText("Absent sessions");
        u.setToolTipText("Unmarked sessions");
        presentFields.add(p);
        absentFields.add(a);
        unmarkedFields.add(u);
        fields.add(wrapAttendanceField(p, "Present"));
        fields.add(wrapAttendanceField(a, "Absent"));
        fields.add(wrapAttendanceField(u, "Unmarked"));
        row.add(fields, BorderLayout.EAST);
        return row;
    }

    private JPanel wrapAttendanceField(JTextField field, String labelText) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(new Font(FONT_NAME, Font.PLAIN, 8));
        label.setForeground(MUTED);
        panel.add(label);
        panel.add(Box.createVerticalStrut(2));
        panel.add(field);
        return panel;
    }

    private JTextField createSmallNumberField(String value) {
        JTextField field = new JTextField(value == null || value.trim().isEmpty() ? "0" : value);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        field.setPreferredSize(new Dimension(48, 30));
        field.setMinimumSize(new Dimension(48, 30));
        field.setMaximumSize(new Dimension(48, 30));
        return field;
    }

    private boolean isValidCount(String raw) {
        try {
            return Integer.parseInt(raw.trim()) >= 0;
        } catch (Exception e) {
            return false;
        }
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
        JPanel main = createPageShell("Academic calendar", "Class Schedule", "Manage your classes by academic year and semester.");

        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(22, 22, 22, 22));
        card.setShadow(true);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(createCardTitle("Your class schedule"));
        JLabel termHint = new JLabel("Select the academic term, then add classes for that term.");
        termHint.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        termHint.setForeground(MUTED);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(termHint);
        header.add(titleBlock, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);
        JComboBox<String> yearSelector = new JComboBox<>(new String[]{"1st Year", "2nd Year", "3rd Year", "4th Year"});
        JComboBox<String> semesterSelector = new JComboBox<>(new String[]{"1st Semester", "2nd Semester"});
        yearSelector.setSelectedIndex(scheduleYear - 1);
        semesterSelector.setSelectedIndex(scheduleSemester - 1);
        styleComboBox(yearSelector);
        styleComboBox(semesterSelector);
        yearSelector.addActionListener(e -> {
            scheduleYear = yearSelector.getSelectedIndex() + 1;
            refreshScheduleList();
        });
        semesterSelector.addActionListener(e -> {
            scheduleSemester = semesterSelector.getSelectedIndex() + 1;
            refreshScheduleList();
        });
        controls.add(yearSelector);
        controls.add(semesterSelector);

        JButton studyLoadButton = createSecondaryButton("STUDY LOAD");
        studyLoadButton.setPreferredSize(new Dimension(120, 38));
        studyLoadButton.addActionListener(e -> {
            studyLoadYear = scheduleYear;
            studyLoadSemester = scheduleSemester;
            refreshStudyLoadPage();
            if (dashboardContentLayout != null && dashboardContent != null) {
                dashboardContentLayout.show(dashboardContent, "studyLoad");
            }
        });
        JButton addButton = createPrimaryButton("ADD CLASS", new CalendarIcon());
        addButton.setPreferredSize(new Dimension(130, 38));
        addButton.addActionListener(e -> showAddScheduleDialog());
        controls.add(studyLoadButton);
        controls.add(addButton);
        header.add(controls, BorderLayout.EAST);

        JPanel columnHeader = new JPanel(new BorderLayout(16, 0));
        columnHeader.setOpaque(false);
        columnHeader.setBorder(new EmptyBorder(0, 14, 0, 14));
        JLabel dayTimeHeader = new JLabel("DAY & TIME");
        JLabel subjectHeader = new JLabel("SUBJECT");
        JLabel detailsHeader = new JLabel("ROOM / TYPE / TEACHER");
        for (JLabel headerLabel : new JLabel[]{dayTimeHeader, subjectHeader, detailsHeader}) {
            headerLabel.setFont(new Font(FONT_NAME, Font.BOLD, 9));
            headerLabel.setForeground(MUTED);
        }
        dayTimeHeader.setPreferredSize(new Dimension(145, 18));
        columnHeader.add(dayTimeHeader, BorderLayout.WEST);
        columnHeader.add(subjectHeader, BorderLayout.CENTER);
        columnHeader.add(detailsHeader, BorderLayout.EAST);

        JPanel scheduleHeader = new JPanel(new BorderLayout(0, 8));
        scheduleHeader.setOpaque(false);
        scheduleHeader.add(header, BorderLayout.NORTH);
        scheduleHeader.add(columnHeader, BorderLayout.CENTER);
        card.add(scheduleHeader, BorderLayout.NORTH);

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

        List<ScheduleItem> visible = new ArrayList<>();
        for (ScheduleItem item : scheduleItems) {
            if (item.year == scheduleYear && item.semester == scheduleSemester) visible.add(item);
        }
        visible.sort((a, b) -> {
            int day = Integer.compare(dayOrder(a.day), dayOrder(b.day));
            if (day != 0) return day;
            return a.startTime.compareToIgnoreCase(b.startTime);
        });

        if (visible.isEmpty()) {
            JLabel empty = new JLabel("No classes for " + formatTerm(scheduleYear, scheduleSemester) + ". Click ADD CLASS to create one.");
            empty.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
            empty.setForeground(MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            scheduleListPanel.add(Box.createVerticalStrut(20));
            scheduleListPanel.add(empty);
        } else {
            for (int i = 0; i < visible.size(); i++) {
                ScheduleItem item = visible.get(i);
                scheduleListPanel.add(createScheduleManagerRow(item, scheduleItems.indexOf(item)));
                if (i < visible.size() - 1) scheduleListPanel.add(Box.createVerticalStrut(10));
            }
        }

        scheduleListPanel.revalidate();
        scheduleListPanel.repaint();
    }

    private JPanel createScheduleManagerRow(ScheduleItem item, int index) {
        RoundedPanel row = new RoundedPanel(14, new Color(248, 250, 249));
        row.setLayout(new BorderLayout(16, 0));
        row.setBorder(new EmptyBorder(13, 14, 13, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        JPanel timePanel = new JPanel();
        timePanel.setOpaque(false);
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
        JLabel day = new JLabel(item.day);
        day.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        day.setForeground(PRIMARY);
        JLabel time = new JLabel(item.getTimeRange());
        time.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        time.setForeground(TEXT);
        timePanel.add(day);
        timePanel.add(Box.createVerticalStrut(3));
        timePanel.add(time);
        timePanel.setPreferredSize(new Dimension(145, 60));
        row.add(timePanel, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel subject = new JLabel(item.subject);
        subject.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        subject.setForeground(TEXT);
        JLabel details = new JLabel(item.room + " • " + item.type);
        details.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        details.setForeground(MUTED);
        JLabel teacher = new JLabel("Teacher: " + (item.teacher.isEmpty() ? "Not assigned" : item.teacher));
        teacher.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        teacher.setForeground(PRIMARY_DARK);
        info.add(subject);
        info.add(Box.createVerticalStrut(4));
        info.add(details);
        info.add(Box.createVerticalStrut(3));
        info.add(teacher);
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
            refreshStudyLoadPage();
            refreshDashboard();
        });
        row.add(delete, BorderLayout.EAST);
        return row;
    }

    private void showAddScheduleDialog() {
        JTextField dayField = new JTextField();
        JTextField startTimeField = new JTextField();
        JTextField endTimeField = new JTextField();
        JTextField subjectField = new JTextField();
        JTextField roomField = new JTextField();
        JTextField typeField = new JTextField();
        JTextField teacherField = new JTextField();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(8, 8, 4, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(4, 0, 4, 0);

        addFormField(form, gbc, 0, "Day (example: Monday):", dayField);
        addFormField(form, gbc, 1, "Start time (example: 2:00 PM):", startTimeField);
        addFormField(form, gbc, 2, "End time (example: 3:30 PM):", endTimeField);
        addFormField(form, gbc, 3, "Subject:", subjectField);
        addFormField(form, gbc, 4, "Room / Location:", roomField);
        addFormField(form, gbc, 5, "Class type (example: Lecture):", typeField);
        addFormField(form, gbc, 6, "Teacher / Instructor:", teacherField);

        JLabel hint = new JLabel("Adding this class to " + formatTerm(scheduleYear, scheduleSemester) + ".");
        hint.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        hint.setForeground(MUTED);
        gbc.gridy = 14;
        gbc.insets = new Insets(6, 0, 0, 0);
        form.add(hint, gbc);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Class", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String day = dayField.getText().trim();
        String startTime = startTimeField.getText().trim();
        String endTime = endTimeField.getText().trim();
        String subject = subjectField.getText().trim();
        String room = roomField.getText().trim();
        String type = typeField.getText().trim();
        String teacher = teacherField.getText().trim();

        if (day.isEmpty() || startTime.isEmpty() || endTime.isEmpty()
                || subject.isEmpty() || room.isEmpty() || type.isEmpty() || teacher.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please complete all class fields, including the teacher / instructor.", "Incomplete Class", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (containsScheduleDelimiter(day, startTime, endTime, subject, room, type, teacher)) {
            JOptionPane.showMessageDialog(this, "The character '|' cannot be used in schedule information.", "Invalid Character", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isValidTimeRange(startTime, endTime)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid times such as 2:00 PM and 3:30 PM, with the end time after the start time.",
                    "Invalid Time", JOptionPane.WARNING_MESSAGE);
            return;
        }

        scheduleItems.add(new ScheduleItem(scheduleYear, scheduleSemester, day, startTime, endTime, subject, room, type, teacher));
        saveSchedule(activeUsername);
        refreshScheduleList();
        refreshStudyLoadPage();
        refreshDashboard();
    }

    private void addFormField(JPanel form, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        label.setForeground(TEXT);

        gbc.gridy = row * 2;
        gbc.insets = new Insets(3, 0, 2, 0);
        form.add(label, gbc);

        gbc.gridy = row * 2 + 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        field.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        form.add(field, gbc);
    }

    private boolean containsScheduleDelimiter(String... values) {
        for (String value : values) {
            if (value.contains("|")) return true;
        }
        return false;
    }

    private boolean isValidTimeRange(String startTime, String endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        try {
            LocalTime start = LocalTime.parse(startTime.toUpperCase(Locale.ENGLISH), formatter);
            LocalTime end = LocalTime.parse(endTime.toUpperCase(Locale.ENGLISH), formatter);
            return end.isAfter(start);
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    // ============================================================
    // STUDY LOAD — derived directly from the editable Class Schedule.
    // This keeps the two pages connected: add/delete a class in Class Schedule,
    // and the Study Load updates automatically.
    // ============================================================
    private JPanel createStudyLoadPage(String username) {
        ProfileData profile = loadProfile(username);
        JPanel main = createPageShell(
                "Academic services",
                "Study Load",
                "View your enrolled subjects by academic year and semester.");

        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(22, 22, 22, 22));
        card.setShadow(true);

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = createCardTitle(formatTerm(scheduleOrStudyLoadYear(), scheduleOrStudyLoadSemester()));
        JLabel subtitle = new JLabel(profile.fullName + "  •  " + profile.program + "  •  Section " + profile.section);
        subtitle.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        subtitle.setForeground(MUTED);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(subtitle);
        top.add(titleBlock, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);
        JComboBox<String> yearSelector = new JComboBox<>(new String[]{"1st Year", "2nd Year", "3rd Year", "4th Year"});
        JComboBox<String> semesterSelector = new JComboBox<>(new String[]{"1st Semester", "2nd Semester"});
        yearSelector.setSelectedIndex(studyLoadYear - 1);
        semesterSelector.setSelectedIndex(studyLoadSemester - 1);
        styleComboBox(yearSelector);
        styleComboBox(semesterSelector);
        JButton schedule = createSecondaryButton("CLASS SCHEDULE");
        schedule.setPreferredSize(new Dimension(130, 36));
        yearSelector.addActionListener(e -> {
            studyLoadYear = yearSelector.getSelectedIndex() + 1;
            refreshStudyLoadPage();
        });
        semesterSelector.addActionListener(e -> {
            studyLoadSemester = semesterSelector.getSelectedIndex() + 1;
            refreshStudyLoadPage();
        });
        schedule.addActionListener(e -> {
            scheduleYear = studyLoadYear;
            scheduleSemester = studyLoadSemester;
            if (dashboardContentLayout != null && dashboardContent != null) {
                dashboardContentLayout.show(dashboardContent, "schedule");
                refreshScheduleList();
            }
        });
        controls.add(yearSelector);
        controls.add(semesterSelector);
        controls.add(schedule);
        top.add(controls, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        int selectedYear = studyLoadYear;
        int selectedSemester = studyLoadSemester;

        List<ScheduleItem> ordered = new ArrayList<>();
        for (ScheduleItem item : scheduleItems) {
            if (item.year == selectedYear && item.semester == selectedSemester) ordered.add(item);
        }
        ordered.sort((a, b) -> {
            int day = Integer.compare(dayOrder(a.day), dayOrder(b.day));
            if (day != 0) return day;
            return a.startTime.compareToIgnoreCase(b.startTime);
        });

        String[] columns = {"CODE", "SUBJECT", "UNITS", "DAYS", "TIME", "ROOM", "TYPE", "INSTRUCTOR"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        int totalUnits = 0;
        for (ScheduleItem item : ordered) {
            StudyLoadDetails details = getStudyLoadDetails(item);
            totalUnits += details.units;
            model.addRow(new Object[]{
                    details.code,
                    details.subject,
                    details.units,
                    formatStudyLoadDays(item.day),
                    item.getTimeRange(),
                    item.room,
                    item.type,
                    details.instructor
            });
        }

        JTable table = new JTable(model);
        table.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        table.setForeground(MUTED);
        table.setBackground(WHITE);
        table.setGridColor(new Color(232, 236, 233));
        table.setShowVerticalLines(false);
        table.setRowHeight(34);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(PRIMARY_SOFT);
        table.setSelectionForeground(TEXT);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font(FONT_NAME, Font.BOLD, 9));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(new Color(247, 249, 248));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));

        int[] preferredWidths = {72, 230, 52, 58, 125, 115, 85, 125};
        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(preferredWidths[i]);
        }
        table.getColumnModel().getColumn(2).setMinWidth(48);
        table.getColumnModel().getColumn(2).setMaxWidth(58);

        // Keep the table compact and pinned to the top of the card. The old
        // GridBagLayout + viewport combination allowed a large vertical gap
        // between the term header and the first subject row.
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(232, 236, 233), 1));
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.getHorizontalScrollBar().setUnitIncrement(12);
        scroll.setPreferredSize(new Dimension(0, Math.max(220, Math.min(520, 42 + ordered.size() * 35))));
        card.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel note = new JLabel("Study Load is generated from Class Schedule for the selected term.");
        note.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        note.setForeground(MUTED);
        JLabel units = new JLabel("TOTAL UNITS  " + totalUnits);
        units.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        units.setForeground(PRIMARY_DARK);
        footer.add(note, BorderLayout.WEST);
        footer.add(units, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        main.add(card, BorderLayout.CENTER);
        return main;
    }

    private int scheduleOrStudyLoadYear() {
        return studyLoadYear;
    }

    private int scheduleOrStudyLoadSemester() {
        return studyLoadSemester;
    }

    private String formatTerm(int year, int semester) {
        String yearLabel = year == 1 ? "1st Year" : year == 2 ? "2nd Year" : year == 3 ? "3rd Year" : year + "th Year";
        String semesterLabel = semester == 1 ? "1st Semester" : "2nd Semester";
        return yearLabel + " • " + semesterLabel + " • A.Y. 2026 – 2027";
    }

    private int getStudyLoadTotalUnits() {
        int total = 0;
        for (ScheduleItem item : scheduleItems) total += getStudyLoadDetails(item).units;
        return total;
    }

    private int dayOrder(String day) {
        String d = day == null ? "" : day.toLowerCase(Locale.ENGLISH);
        if (d.contains("monday")) return 1;
        if (d.contains("tuesday")) return 2;
        if (d.contains("wednesday")) return 3;
        if (d.contains("thursday")) return 4;
        if (d.contains("friday")) return 5;
        if (d.contains("saturday")) return 6;
        if (d.contains("sunday")) return 7;
        return 8;
    }

    private String formatStudyLoadDays(String day) {
        String d = day == null ? "" : day.toLowerCase(Locale.ENGLISH);
        List<String> parts = new ArrayList<>();
        if (d.contains("monday")) parts.add("M");
        if (d.contains("tuesday")) parts.add("T");
        if (d.contains("wednesday")) parts.add("W");
        if (d.contains("thursday")) parts.add("TH");
        if (d.contains("friday")) parts.add("F");
        if (d.contains("saturday")) parts.add("SAT");
        if (d.contains("sunday")) parts.add("SUN");
        return parts.isEmpty() ? day : String.join(" ", parts);
    }

    private StudyLoadDetails getStudyLoadDetails(ScheduleItem item) {
        String subject = item.subject == null ? "" : item.subject;
        String lower = subject.toLowerCase(Locale.ENGLISH);
        String code = "";
        String name = subject;
        int units = 0;
        String fallbackInstructor = "";
        if (lower.contains("purposive communication")) { code = "GE 1"; name = "Purposive Communication"; units = 3; fallbackInstructor = "J. Dano"; }
        else if (lower.contains("mathematics in the modern world")) { code = "GE 100"; name = "Mathematics in the Modern World"; units = 3; fallbackInstructor = "J. Bahian"; }
        else if (lower.contains("introduction to computing")) { code = "CIC 111"; name = "Introduction to Computing"; units = 3; fallbackInstructor = "N. Oujimar"; }
        else if (lower.contains("understanding the self")) { code = "GE 7"; name = "Understanding the Self"; units = 3; fallbackInstructor = "V. Sagarino"; }
        else if (lower.contains("fundamentals in programming")) { code = "CFP 110"; name = "Fundamentals in Programming"; units = 3; fallbackInstructor = "N. Oujimar"; }
        else if (lower.contains("professional issues in computing")) { code = "CPC 121"; name = "Professional Issues in Computing"; units = 3; fallbackInstructor = "J. Labrada"; }
        else if (lower.contains("national service training")) { code = "NSTP 1"; name = "National Service Training Program 1"; units = 3; fallbackInstructor = "H. Esteller"; }
        else if (lower.contains("pathfit") || lower.contains("movement enhancement")) { code = "Pathfit 101"; name = "Movement Enhancement"; units = 2; fallbackInstructor = "J. Gumban"; }
        else {
            int separator = subject.indexOf(" - ");
            if (separator > 0) {
                code = subject.substring(0, separator).trim();
                name = subject.substring(separator + 3).trim();
            }
        }
        String instructor = item.teacher == null || item.teacher.trim().isEmpty() ? fallbackInstructor : item.teacher.trim();
        return new StudyLoadDetails(code.isEmpty() ? "—" : code, name, units, instructor.isEmpty() ? "—" : instructor);
    }

    private static class StudyLoadDetails {
        String code;
        String subject;
        int units;
        String instructor;

        StudyLoadDetails(String code, String subject, int units, String instructor) {
            this.code = code;
            this.subject = subject;
            this.units = units;
            this.instructor = instructor;
        }
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
            refreshDashboard();
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
            refreshDashboard();
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
        refreshDashboard();
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
            // New accounts start with an empty task list. Nothing is pre-populated.
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
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 9) {
                    int year = parseIntOrDefault(parts[0], 1);
                    int semester = parseIntOrDefault(parts[1], 1);
                    scheduleItems.add(new ScheduleItem(year, semester, parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8]));
                } else if (parts.length == 6) {
                    // Previous v2 format: day|start|end|subject|room|type.
                    // Keep those classes in the first-year, first-semester study load and
                    // recover the sample instructor from the subject when possible.
                    String teacher = getLegacyTeacher(parts[3]);
                    scheduleItems.add(new ScheduleItem(1, 1, parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], teacher));
                } else if (parts.length == 4) {
                    ScheduleItem legacy = ScheduleItem.fromLegacy(parts[0], parts[1], parts[2], parts[3]);
                    legacy.teacher = getLegacyTeacher(legacy.subject);
                    scheduleItems.add(legacy);
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
                writer.write(item.year + "|" + item.semester + "|" + item.day + "|" + item.startTime + "|" + item.endTime + "|"
                        + item.subject + "|" + item.room + "|" + item.type + "|" + item.teacher);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private int parseIntOrDefault(String value, int fallback) {
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return fallback; }
    }

    private String getLegacyTeacher(String subject) {
        String lower = subject == null ? "" : subject.toLowerCase(Locale.ENGLISH);
        if (lower.contains("purposive communication")) return "J. Dano";
        if (lower.contains("mathematics in the modern world")) return "J. Bahian";
        if (lower.contains("introduction to computing")) return "N. Oujimar";
        if (lower.contains("understanding the self")) return "V. Sagarino";
        if (lower.contains("fundamentals in programming")) return "N. Oujimar";
        if (lower.contains("professional issues in computing")) return "J. Labrada";
        if (lower.contains("national service training")) return "H. Esteller";
        if (lower.contains("pathfit") || lower.contains("movement enhancement")) return "J. Gumban";
        return "";
    }

    // ============================================================
    // PHASE 6 — ATTENDANCE DATA
    // Stores attendance counts per subject for the current account.
    // ============================================================
    private File getAttendanceFile(String username) {
        return getAccountFile("attendance_", username);
    }

    private String[] loadAttendanceForSubject(String username, int year, int semester, String code) {
        File file = getAttendanceFile(username);
        if (!file.exists()) return new String[]{"0", "0", "0"};

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 6);
                if (parts.length == 6) {
                    try {
                        int savedYear = Integer.parseInt(parts[0].trim());
                        int savedSemester = Integer.parseInt(parts[1].trim());
                        if (savedYear == year && savedSemester == semester
                                && parts[2].trim().equalsIgnoreCase(code)) {
                            return new String[]{parts[3].trim(), parts[4].trim(), parts[5].trim()};
                        }
                    } catch (NumberFormatException ignored) {
                    }
                } else if (parts.length == 4 && parts[0].trim().equalsIgnoreCase(code)) {
                    // Backward compatibility with attendance files created by the previous version.
                    return new String[]{parts[1].trim(), parts[2].trim(), parts[3].trim()};
                }
            }
        } catch (IOException ignored) {
        }
        return new String[]{"0", "0", "0"};
    }

    private boolean saveAttendance(String username, List<CurriculumSubject> subjects,
            List<JTextField> presentFields, List<JTextField> absentFields, List<JTextField> unmarkedFields) {
        File file = getAttendanceFile(username);
        List<String> existing = new ArrayList<>();
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) existing.add(line);
                }
            } catch (IOException e) {
                return false;
            }
        }

        for (int i = 0; i < subjects.size(); i++) {
            CurriculumSubject subject = subjects.get(i);
            String replacement = subject.year + "|" + subject.semester + "|" + subject.code + "|"
                    + presentFields.get(i).getText().trim() + "|"
                    + absentFields.get(i).getText().trim() + "|"
                    + unmarkedFields.get(i).getText().trim();
            boolean found = false;
            for (int j = 0; j < existing.size(); j++) {
                String[] parts = existing.get(j).split("\\|", 6);
                if (parts.length == 6) {
                    try {
                        int savedYear = Integer.parseInt(parts[0].trim());
                        int savedSemester = Integer.parseInt(parts[1].trim());
                        if (savedYear == subject.year && savedSemester == subject.semester
                                && parts[2].trim().equalsIgnoreCase(subject.code)) {
                            existing.set(j, replacement);
                            found = true;
                            break;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                } else if (parts.length == 4 && parts[0].trim().equalsIgnoreCase(subject.code)) {
                    // Upgrade an old record to the new year/semester-aware format.
                    existing.set(j, replacement);
                    found = true;
                    break;
                }
            }
            if (!found) existing.add(replacement);
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String line : existing) {
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private AttendanceSummary getAttendanceSummary(String username) {
        // Legacy/global summary retained for callers that do not need a selected term.
        int present = 0, absent = 0, unmarked = 0;
        File file = getAttendanceFile(username);
        if (!file.exists()) return new AttendanceSummary(0, 0, 0);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 6);
                try {
                    if (parts.length == 6) {
                        present += Integer.parseInt(parts[3].trim());
                        absent += Integer.parseInt(parts[4].trim());
                        unmarked += Integer.parseInt(parts[5].trim());
                    } else if (parts.length == 4) {
                        present += Integer.parseInt(parts[1].trim());
                        absent += Integer.parseInt(parts[2].trim());
                        unmarked += Integer.parseInt(parts[3].trim());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException ignored) {
        }
        return new AttendanceSummary(present, absent, unmarked);
    }

    private AttendanceSummary getAttendanceSummary(String username, int year, int semester) {
        int present = 0, absent = 0, unmarked = 0;
        File file = getAttendanceFile(username);
        if (!file.exists()) return new AttendanceSummary(0, 0, 0);

        List<CurriculumSubject> curriculum = loadCurriculum(username);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 6);
                try {
                    if (parts.length == 6) {
                        int savedYear = Integer.parseInt(parts[0].trim());
                        int savedSemester = Integer.parseInt(parts[1].trim());
                        if (savedYear != year || savedSemester != semester) continue;
                        present += Integer.parseInt(parts[3].trim());
                        absent += Integer.parseInt(parts[4].trim());
                        unmarked += Integer.parseInt(parts[5].trim());
                    } else if (parts.length == 4) {
                        CurriculumSubject subject = findSubjectByCode(curriculum, parts[0].trim());
                        if (subject == null || subject.year != year || subject.semester != semester) continue;
                        present += Integer.parseInt(parts[1].trim());
                        absent += Integer.parseInt(parts[2].trim());
                        unmarked += Integer.parseInt(parts[3].trim());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException ignored) {
        }
        return new AttendanceSummary(present, absent, unmarked);
    }

    private static class AttendanceSummary {
        int present;
        int absent;
        int unmarked;
        int markedSessions;

        AttendanceSummary(int present, int absent, int unmarked) {
            this.present = present;
            this.absent = absent;
            this.unmarked = unmarked;
            this.markedSessions = present + absent;
        }

        String getPercentText() {
            if (markedSessions == 0) return "--";
            return String.format("%.0f%%", (present * 100.0) / markedSessions);
        }
    }

    // ============================================================
    // PHASE 6 — ANNOUNCEMENT DATA
    // Announcements are account-specific and persist after logout.
    // ============================================================
    private File getAnnouncementsFile(String username) {
        return getAccountFile("announcements_", username);
    }

    private void loadAnnouncements(String username) {
        announcementItems.clear();
        File file = getAnnouncementsFile(username);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 3);
                if (parts.length == 3) {
                    announcementItems.add(new AnnouncementItem(parts[0], parts[1], parts[2]));
                }
            }
        } catch (IOException ignored) {
            announcementItems.clear();
        }
    }

    private boolean saveAnnouncements(String username) {
        File file = getAnnouncementsFile(username);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (AnnouncementItem item : announcementItems) {
                writer.write(item.title + "|" + item.message + "|" + item.tag);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ============================================================
    // EDITABLE 4-YEAR CURRICULUM
    // ============================================================
    private File getCurriculumFile(String username) {
        return getAccountFile("curriculum_", username);
    }

    private static class CurriculumSubject {
        int year;
        int semester;
        String code;
        String name;
        double units;

        CurriculumSubject(int year, int semester, String code, String name, double units) {
            this.year = year;
            this.semester = semester;
            this.code = code;
            this.name = name;
            this.units = units;
        }
    }

    private List<CurriculumSubject> loadCurriculum(String username) {
        List<CurriculumSubject> result = new ArrayList<>();
        File file = getCurriculumFile(username);
        if (!file.exists()) {
            for (int i = 0; i < DEFAULT_SUBJECT_CODES.length; i++) {
                result.add(new CurriculumSubject(1, 1, DEFAULT_SUBJECT_CODES[i],
                        DEFAULT_SUBJECT_NAMES[i], DEFAULT_SUBJECT_UNITS[i]));
            }
            saveCurriculum(username, result);
            return result;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 5);
                if (parts.length != 5) continue;
                try {
                    int year = Integer.parseInt(parts[0].trim());
                    int semester = Integer.parseInt(parts[1].trim());
                    double units = Double.parseDouble(parts[4].trim());
                    if (year >= 1 && year <= 4 && (semester == 1 || semester == 2)
                            && !parts[2].trim().isEmpty() && !parts[3].trim().isEmpty() && units > 0) {
                        result.add(new CurriculumSubject(year, semester, parts[2].trim(), parts[3].trim(), units));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException ignored) {
            return new ArrayList<>();
        }
        return result;
    }

    private boolean saveCurriculum(String username, List<CurriculumSubject> subjects) {
        if (username == null || username.trim().isEmpty()) return false;
        File file = getCurriculumFile(username);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (CurriculumSubject subject : subjects) {
                writer.write(subject.year + "|" + subject.semester + "|"
                        + subject.code + "|" + subject.name + "|" + formatUnits(subject.units));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private List<CurriculumSubject> getSubjectsForTerm(String username, int year, int semester) {
        List<CurriculumSubject> result = new ArrayList<>();
        for (CurriculumSubject subject : loadCurriculum(username)) {
            if (subject.year == year && subject.semester == semester) result.add(subject);
        }
        return result;
    }

    private CurriculumSubject findSubjectByCode(List<CurriculumSubject> subjects, String code) {
        for (CurriculumSubject subject : subjects) {
            if (subject.code.equalsIgnoreCase(code)) return subject;
        }
        return null;
    }

    private void refreshCurriculumSubjectList(String username, int year, int semester,
            List<CurriculumSubject> subjects, JPanel listPanel, Runnable refresh) {
        listPanel.removeAll();
        if (subjects.isEmpty()) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setOpaque(false);
            empty.setBorder(new EmptyBorder(22, 0, 22, 0));
            JLabel message = new JLabel("No subjects added for this term yet. Use ADD SUBJECT to build this semester.");
            message.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
            message.setForeground(MUTED);
            empty.add(message, BorderLayout.WEST);
            listPanel.add(empty);
        } else {
            for (CurriculumSubject subject : subjects) {
                listPanel.add(createCurriculumSubjectRow(username, year, semester, subject, refresh));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createCurriculumSubjectRow(String username, int year, int semester,
            CurriculumSubject subject, Runnable refresh) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(8, 0, 8, 0)));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(subject.name);
        name.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        name.setForeground(TEXT);
        JLabel details = new JLabel(subject.code + "  •  " + formatUnits(subject.units) + " units");
        details.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        details.setForeground(MUTED);
        info.add(name);
        info.add(Box.createVerticalStrut(2));
        info.add(details);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JButton edit = createSecondaryButton("EDIT");
        edit.setPreferredSize(new Dimension(65, 32));
        edit.setMaximumSize(new Dimension(65, 32));
        edit.addActionListener(e -> showSubjectDialog(username, year, semester, subject, refresh));
        JButton remove = createSecondaryButton("REMOVE");
        remove.setPreferredSize(new Dimension(82, 32));
        remove.setMaximumSize(new Dimension(82, 32));
        remove.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Remove " + subject.code + " — " + subject.name + " from this term?",
                    "Remove Subject", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
            List<CurriculumSubject> all = loadCurriculum(username);
            all.removeIf(item -> item.year == year && item.semester == semester && item.code.equalsIgnoreCase(subject.code));
            if (!saveCurriculum(username, all)) {
                JOptionPane.showMessageDialog(this, "The subject could not be removed.",
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refresh.run();
            refreshAcademicPage();
            refreshDashboard();
        });
        actions.add(edit);
        actions.add(remove);
        row.add(info, BorderLayout.CENTER);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private void showSubjectDialog(String username, int year, int semester, CurriculumSubject editing, Runnable refresh) {
        JDialog dialog = new JDialog(this, editing == null ? "Add Subject" : "Edit Subject", true);
        dialog.setSize(500, 430);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(PAGE);
        root.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel((yearName(year) + "  •  " + semesterName(semester)).toUpperCase());
        eyebrow.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        eyebrow.setForeground(PRIMARY);
        JLabel title = new JLabel(editing == null ? "Add Subject" : "Edit Subject");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 25));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Enter the subject details from your prospectus.");
        subtitle.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        subtitle.setForeground(MUTED);
        header.add(eyebrow);
        header.add(Box.createVerticalStrut(5));
        header.add(title);
        header.add(Box.createVerticalStrut(3));
        header.add(subtitle);

        RoundedPanel form = new RoundedPanel(16, WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        form.setShadow(true);
        JTextField codeField = createProfileInput(editing == null ? "" : editing.code);
        JTextField nameField = createProfileInput(editing == null ? "" : editing.name);
        JTextField unitsField = createProfileInput(editing == null ? "3" : formatUnits(editing.units));
        form.add(createDialogField("Subject Code", codeField));
        form.add(Box.createVerticalStrut(12));
        form.add(createDialogField("Subject Name", nameField));
        form.add(Box.createVerticalStrut(12));
        form.add(createDialogField("Units", unitsField));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton cancel = createSecondaryButton("Cancel");
        cancel.setPreferredSize(new Dimension(95, 38));
        cancel.addActionListener(e -> dialog.dispose());
        JButton save = createPrimaryButton("Save Subject", null);
        save.setPreferredSize(new Dimension(125, 38));
        save.setMaximumSize(new Dimension(125, 38));
        save.addActionListener(e -> {
            String code = codeField.getText().trim();
            String nameValue = nameField.getText().trim();
            String unitsText = unitsField.getText().trim();
            if (code.isEmpty() || nameValue.isEmpty() || unitsText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Complete all subject fields.",
                        "Incomplete Subject", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (code.contains("|") || nameValue.contains("|") || unitsText.contains("|")) {
                JOptionPane.showMessageDialog(dialog, "The character '|' cannot be used.",
                        "Invalid Character", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double units;
            try {
                units = Double.parseDouble(unitsText);
                if (units <= 0 || units > 20) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Units must be a positive number (for example, 3 or 3.0).",
                        "Invalid Units", JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<CurriculumSubject> all = loadCurriculum(username);
            for (CurriculumSubject item : all) {
                boolean sameRecord = editing != null && item.year == editing.year && item.semester == editing.semester
                        && item.code.equalsIgnoreCase(editing.code);
                if (!sameRecord && item.code.equalsIgnoreCase(code)) {
                    JOptionPane.showMessageDialog(dialog, "That subject code is already used in your curriculum.",
                            "Duplicate Subject Code", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            if (editing == null) {
                all.add(new CurriculumSubject(year, semester, code, nameValue, units));
            } else {
                String oldCode = editing.code;
                boolean replaced = false;
                for (int i = 0; i < all.size(); i++) {
                    CurriculumSubject item = all.get(i);
                    if (item.year == editing.year && item.semester == editing.semester
                            && item.code.equalsIgnoreCase(oldCode)) {
                        all.set(i, new CurriculumSubject(year, semester, code, nameValue, units));
                        replaced = true;
                        break;
                    }
                }
                if (!replaced) {
                    all.add(new CurriculumSubject(year, semester, code, nameValue, units));
                }
            }
            if (!saveCurriculum(username, all)) {
                JOptionPane.showMessageDialog(dialog, "The subject could not be saved.",
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dialog.dispose();
            refresh.run();
            refreshAcademicPage();
            refreshDashboard();
        });
        buttons.add(cancel);
        buttons.add(save);

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private String yearName(int year) {
        return year + (year == 1 ? "st" : year == 2 ? "nd" : year == 3 ? "rd" : "th") + " Year";
    }

    private String semesterName(int semester) {
        return semester == 1 ? "1st Semester" : "2nd Semester";
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
        String email;
        String mobile;
        String address;
        String gender;
        String profilePicture;

        ProfileData(String fullName, String studentId, String program, String yearLevel, String section) {
            this(fullName, studentId, program, yearLevel, section, "", "", "", "Prefer not to say", DEFAULT_AVATAR);
        }

        ProfileData(String fullName, String studentId, String program, String yearLevel, String section,
                    String email, String mobile, String address) {
            this(fullName, studentId, program, yearLevel, section, email, mobile, address, "Prefer not to say", DEFAULT_AVATAR);
        }

        ProfileData(String fullName, String studentId, String program, String yearLevel, String section,
                    String email, String mobile, String address, String gender, String profilePicture) {
            this.fullName = fullName;
            this.studentId = studentId;
            this.program = program;
            this.yearLevel = yearLevel;
            this.section = section;
            this.email = email == null ? "" : email;
            this.mobile = mobile == null ? "" : mobile;
            this.address = address == null ? "" : address;
            this.gender = gender == null || gender.trim().isEmpty() ? "Prefer not to say" : gender;
            this.profilePicture = profilePicture == null || profilePicture.trim().isEmpty() ? DEFAULT_AVATAR : profilePicture;
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

        String[] values = new String[10];
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
                        case "email": values[5] = parts[1]; break;
                        case "mobile": values[6] = parts[1]; break;
                        case "address": values[7] = parts[1]; break;
                        case "gender": values[8] = parts[1]; break;
                        case "profilePicture": values[9] = parts[1]; break;
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
                values[4] == null || values[4].trim().isEmpty() ? DEFAULT_SECTION : values[4],
                values[5] == null ? "" : values[5],
                values[6] == null ? "" : values[6],
                values[7] == null ? "" : values[7],
                values[8] == null ? "Prefer not to say" : values[8],
                values[9] == null ? DEFAULT_AVATAR : values[9]
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
            writer.write("email|" + profile.email); writer.newLine();
            writer.write("mobile|" + profile.mobile); writer.newLine();
            writer.write("address|" + profile.address); writer.newLine();
            writer.write("gender|" + profile.gender); writer.newLine();
            writer.write("profilePicture|" + profile.profilePicture); writer.newLine();
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

    private boolean saveGrades(String username, List<CurriculumSubject> subjects, List<JTextField> gradeFields) {
        if (username == null || username.trim().isEmpty()) return false;
        File file = getGradesFile(username);
        List<String> existing = new ArrayList<>();
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) existing.add(line);
            } catch (IOException e) {
                return false;
            }
        }
        for (int i = 0; i < subjects.size(); i++) {
            String code = subjects.get(i).code;
            String replacement = code + "|" + gradeFields.get(i).getText().trim();
            boolean found = false;
            for (int j = 0; j < existing.size(); j++) {
                String[] parts = existing.get(j).split("\\|", 2);
                if (parts.length == 2 && parts[0].trim().equals(code)) {
                    existing.set(j, replacement);
                    found = true;
                    break;
                }
            }
            if (!found) existing.add(replacement);
        }
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String line : existing) {
                writer.write(line);
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
        int year;
        int semester;
        String day;
        String startTime;
        String endTime;
        String subject;
        String room;
        String type;
        String teacher;

        ScheduleItem(int year, int semester, String day, String startTime, String endTime,
                     String subject, String room, String type, String teacher) {
            this.year = year;
            this.semester = semester;
            this.day = day;
            this.startTime = startTime;
            this.endTime = endTime;
            this.subject = subject;
            this.room = room;
            this.type = type;
            this.teacher = teacher == null ? "" : teacher;
        }

        String getTimeRange() {
            return startTime + " - " + endTime;
        }

        static ScheduleItem fromLegacy(String legacyTime, String subject, String room, String type) {
            String day = "Schedule";
            String start = legacyTime.trim();
            String end = "";
            int firstSpace = start.indexOf(' ');
            int separator = start.indexOf(" - ");
            if (firstSpace > 0 && separator > firstSpace) {
                day = start.substring(0, firstSpace).trim();
                String range = start.substring(firstSpace + 1).trim();
                int rangeSeparator = range.indexOf(" - ");
                if (rangeSeparator >= 0) {
                    start = range.substring(0, rangeSeparator).trim();
                    end = range.substring(rangeSeparator + 3).trim();
                } else start = range;
            } else {
                int rangeSeparator = start.indexOf(" - ");
                if (rangeSeparator >= 0) {
                    end = start.substring(rangeSeparator + 3).trim();
                    start = start.substring(0, rangeSeparator).trim();
                }
            }
            if (end.isEmpty()) end = start;
            return new ScheduleItem(1, 1, day, start, end, subject, room, type, "");
        }
    }

    private static class AnnouncementItem {
        String title;
        String message;
        String tag;

        AnnouncementItem(String title, String message, String tag) {
            this.title = title;
            this.message = message;
            this.tag = tag;
        }
    }

    private JPanel createAnnouncementsPage() {
        JPanel main = createPageShell("Campus communication", "Announcements", "Create and manage the notices shown on your student dashboard.");

        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(22, 22, 22, 22));
        card.setShadow(true);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(createCardTitle("Your announcements"), BorderLayout.WEST);
        JButton add = createPrimaryButton("ADD ANNOUNCEMENT", new BellIcon());
        add.setPreferredSize(new Dimension(185, 38));
        add.addActionListener(e -> showAddAnnouncementDialog());
        header.add(add, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        announcementListPanel = new JPanel();
        announcementListPanel.setOpaque(false);
        announcementListPanel.setLayout(new BoxLayout(announcementListPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(announcementListPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scroll, BorderLayout.CENTER);

        refreshAnnouncementList();
        main.add(card, BorderLayout.CENTER);
        return main;
    }

    private void refreshAnnouncementList() {
        if (announcementListPanel == null) return;
        announcementListPanel.removeAll();
        if (announcementItems.isEmpty()) {
            JLabel empty = new JLabel("No announcements yet. Click ADD ANNOUNCEMENT to create one.");
            empty.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
            empty.setForeground(MUTED);
            announcementListPanel.add(Box.createVerticalStrut(20));
            announcementListPanel.add(empty);
        } else {
            for (int i = 0; i < announcementItems.size(); i++) {
                announcementListPanel.add(createAnnouncementManagerRow(announcementItems.get(i), i));
                if (i < announcementItems.size() - 1) announcementListPanel.add(Box.createVerticalStrut(10));
            }
        }
        announcementListPanel.revalidate();
        announcementListPanel.repaint();
    }

    private JPanel createAnnouncementManagerRow(AnnouncementItem item, int index) {
        RoundedPanel row = new RoundedPanel(14, new Color(248, 250, 249));
        row.setLayout(new BorderLayout(14, 0));
        row.setBorder(new EmptyBorder(13, 14, 13, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(item.title);
        title.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        title.setForeground(TEXT);
        JLabel tag = new JLabel(item.tag);
        tag.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        tag.setForeground(PRIMARY);
        JLabel message = new JLabel(item.message);
        message.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        message.setForeground(MUTED);
        info.add(title);
        info.add(Box.createVerticalStrut(3));
        info.add(tag);
        info.add(Box.createVerticalStrut(4));
        info.add(message);
        row.add(info, BorderLayout.CENTER);

        JButton delete = new JButton("DELETE");
        delete.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        delete.setForeground(DANGER);
        delete.setFocusPainted(false);
        delete.setBorderPainted(false);
        delete.setContentAreaFilled(false);
        delete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        delete.addActionListener(e -> {
            announcementItems.remove(index);
            saveAnnouncements(activeUsername);
            refreshAnnouncementList();
            refreshDashboard();
        });
        row.add(delete, BorderLayout.EAST);
        return row;
    }

    private JScrollPane createCompactTextAreaScroll(JTextArea area, int height) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(7, 8, 7, 8));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(340, height));
        scroll.setMinimumSize(new Dimension(340, height));
        scroll.setMaximumSize(new Dimension(340, height));
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private void showAddAnnouncementDialog() {
        JTextArea titleField = new JTextArea(2, 30);
        titleField.setLineWrap(true);
        titleField.setWrapStyleWord(true);
        titleField.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        JScrollPane titleScroll = createCompactTextAreaScroll(titleField, 54);

        JTextArea tagField = new JTextArea("Student notice", 2, 30);
        tagField.setLineWrap(true);
        tagField.setWrapStyleWord(true);
        tagField.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        JScrollPane tagScroll = createCompactTextAreaScroll(tagField, 54);

        JTextArea messageArea = new JTextArea(4, 30);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        JScrollPane messageScroll = createCompactTextAreaScroll(messageArea, 92);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(8, 4, 4, 4));
        form.add(new JLabel("Announcement title:"));
        form.add(Box.createVerticalStrut(5));
        form.add(titleScroll);
        form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("Category / tag:"));
        form.add(Box.createVerticalStrut(5));
        form.add(tagScroll);
        form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("Message:"));
        form.add(Box.createVerticalStrut(5));
        form.add(messageScroll);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Announcement",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String title = titleField.getText().trim();
        String tag = tagField.getText().trim();
        String message = messageArea.getText().trim();
        if (title.isEmpty() || tag.isEmpty() || message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please complete all announcement fields.",
                    "Incomplete Announcement", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (title.contains("|") || tag.contains("|") || message.contains("|")) {
            JOptionPane.showMessageDialog(this, "The character '|' cannot be used in announcement information.",
                    "Invalid Character", JOptionPane.WARNING_MESSAGE);
            return;
        }

        announcementItems.add(0, new AnnouncementItem(title, message, tag));
        saveAnnouncements(activeUsername);
        refreshAnnouncementList();
        refreshDashboard();
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

        JPanel cards = new JPanel(new GridBagLayout());
        cards.setOpaque(false);
        cards.setBorder(new EmptyBorder(0, 0, 0, 0));

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

        RoundedPanel[] settingCards = {appearance, security, session, about};
        for (int i = 0; i < settingCards.length; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 1.0;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.insets = new Insets(0, 0, i == settingCards.length - 1 ? 0 : 14, 0);
            cards.add(settingCards[i], c);
        }
        GridBagConstraints glue = new GridBagConstraints();
        glue.gridx = 0;
        glue.gridy = settingCards.length;
        glue.weightx = 1.0;
        glue.weighty = 1.0;
        glue.fill = GridBagConstraints.BOTH;
        cards.add(Box.createGlue(), glue);

        JLabel footer = new JLabel("Academia One v1.0  •  Java Swing", SwingConstants.LEFT);
        footer.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        footer.setForeground(MUTED);

        main.add(cards, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);
        return main;
    }

    private RoundedPanel createSettingCard(String title, String subtitle) {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        row.setBorder(new EmptyBorder(5, 0, 5, 0));
        row.setPreferredSize(new Dimension(0, 30));
        row.setMinimumSize(new Dimension(0, 30));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
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

    private JPanel createOverviewCards(String username) {
        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));

        AttendanceSummary summary = getAttendanceSummary(username, academicYear, academicSemester);
        String attendanceValue = summary.markedSessions == 0 ? "--" : summary.getPercentText();
        String attendanceDetail = summary.markedSessions == 0 ? "No attendance recorded for this term" : "Based on saved record";
        int totalSubjects = loadCurriculum(username).size();

        cards.add(createStatCard("SUBJECTS LISTED", String.valueOf(totalSubjects),
                totalSubjects == 1 ? "Across all 8 terms • 1 subject" : "Across all 8 terms • " + totalSubjects + " subjects", new BookIcon(), PRIMARY));
        cards.add(createStatCard("ATTENDANCE", attendanceValue, attendanceDetail, new CalendarIcon(), PRIMARY));
        cards.add(createStatCard("ACCOUNT STATUS", "Active", "Local student account", new CheckIcon(), new Color(34, 119, 73)));
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
        JLabel avatar = createProfileAvatar(profile.profilePicture, 52, getInitial(profile.fullName));

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

    // Question 9 — Clear Button Does Not Clear the Password &
    // Question 10 — Cursor Should Return to Username
    // requestFocusInWindow() puts the keyboard cursor back in the username
    // field so the user can immediately type after pressing CLEAR.
    private void clearFields() {
        usernameField.setText("");
        passwordField.setText(""); // Clears both fields
        setPasswordVisible(false);
        setStatus(" ", DANGER);
        usernameField.requestFocusInWindow(); // Returns focus to username
    }

    private Icon whiteIcon(SimpleIcon icon) {
        icon.setColor(Color.WHITE);
        return icon;
    }

    // ============================================================
    // CUSTOM COMPONENTS
    // ============================================================
    private static class CampusBrandPanel extends JPanel {
        private final int radius;
        private final Image image;

        CampusBrandPanel(int radius, Image image) {
            this.radius = radius;
            this.image = image;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));

            if (image != null) {
                double scale = Math.max(
                        (double) getWidth() / image.getWidth(null),
                        (double) getHeight() / image.getHeight(null));
                int w = (int) Math.ceil(image.getWidth(null) * scale);
                int h = (int) Math.ceil(image.getHeight(null) * scale);
                int x = (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;
                g2.drawImage(image, x, y, w, h, this);
            } else {
                g2.setColor(PRIMARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            // Green veil keeps the supplied campus photo visible while maintaining
            // the Academia One brand contrast shown in the reference UI.
            g2.setColor(new Color(5, 79, 38, 188));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

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