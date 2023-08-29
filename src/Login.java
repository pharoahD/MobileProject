import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Properties;
import java.util.Random;
public class Login {
    private JFrame loginFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton forgotPasswordButton;
    private JButton backToMainButton;
    private String loggedInUsername;
    private static final String DATABASE_URL = "jdbc:sqlite:D:\\mobile\\identifier.sqlite";

    private void createUserTable() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "CREATE TABLE IF NOT EXISTS Users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL, password TEXT NOT NULL)";
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public  void createTable() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "CREATE TABLE IF NOT EXISTS Admins (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL, password TEXT NOT NULL)";
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void insertUser(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "INSERT INTO Users (username, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void insertAdmin(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "INSERT INTO Admins (username, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isValidAdmin(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT * FROM Admins WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next(); // If there's a matching record, login is valid
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public Login() {
        initializeComponents();
    }

    private void initializeComponents() {
        loginFrame = new JFrame("登录");
        loginFrame.setSize(300, 250);
        loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null); // 居中显示

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 10, 10)); // 设置面板布局和间距

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10)); // 设置输入框面板布局和间距

        JLabel usernameLabel = new JLabel("用户名:");
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("密码:");
        passwordField = new JPasswordField();

        inputPanel.add(usernameLabel);
        inputPanel.add(usernameField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // 设置按钮面板布局和间距

        loginButton = new JButton("登录");
        forgotPasswordButton = new JButton("忘记密码");
        backToMainButton = new JButton("返回主页面");

        loginButton.addActionListener(e -> performLogin());
        forgotPasswordButton.addActionListener(e -> showForgotPasswordDialog());
        backToMainButton.addActionListener(e -> backToMainPage());

        buttonPanel.add(loginButton);
        buttonPanel.add(forgotPasswordButton);
        buttonPanel.add(backToMainButton);

        panel.add(inputPanel);
        panel.add(buttonPanel);

        loginFrame.add(panel);
    }


    public void showLoginPage() {
        loginFrame.setVisible(true);
    }

    private int loginAttempts = 0;
    private final int MAX_LOGIN_ATTEMPTS = 5;

    private void performLogin() {
        String username = usernameField.getText();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);

        if (isValidAdminLogin(username, password)) {
            loginFrame.dispose();
            // 进入管理员系统
            JOptionPane.showMessageDialog(null, "欢迎管理员 " + username);
            Manager manager = new Manager();
            manager.showManagerPage();
        } else if (isValidUserLogin(username, password)) {
            loginFrame.dispose();
            // 进入普通用户系统
            JOptionPane.showMessageDialog(null, "欢迎用户 " + username);

            User user = new User(username);

        } else {
            loginAttempts++;
            if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                JOptionPane.showMessageDialog(loginFrame, "登录失败次数过多，系统将关闭。");
                System.exit(0);
            } else {
                int remainingAttempts = MAX_LOGIN_ATTEMPTS - loginAttempts;
                JOptionPane.showMessageDialog(loginFrame, "登录失败，请检查用户名和密码。还剩 " + remainingAttempts + " 次尝试。");
            }
        }
    }



    private boolean isValidAdminLogin(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT * FROM Admins WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isValidUserLogin(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT password FROM Users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                String hashedPassword = resultSet.getString("password");
                String hashedInputPassword = hashSHA256(password); // Hash the input password
                return hashedInputPassword.equals(hashedPassword); // Compare hashed passwords
            }

            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String hashSHA256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showForgotPasswordDialog() {
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();

        Object[] message = {
                "用户名:", usernameField,
                "注册邮箱:", emailField
        };

        int option = JOptionPane.showConfirmDialog(
                loginFrame,
                message,
                "忘记密码",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String email = emailField.getText();
            if (isUsernameAndEmailMatch(username, email)) {
                // 用户名和邮箱匹配，执行发送验证码和重置密码逻辑
                sendVerificationCodeAndResetPassword(username, email);
            } else {
                JOptionPane.showMessageDialog(
                        loginFrame,
                        "用户名和邮箱不匹配，请重新输入正确的用户名和邮箱。"
                );
            }
        }
    }

    private boolean isUsernameAndEmailMatch(String username, String email) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT * FROM UserData WHERE username = ? AND email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, email);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next(); // 如果有匹配记录，返回 true
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void sendVerificationCodeAndResetPassword(String username, String email) {
        // 生成随机验证码（示例：6位数字）
        String verificationCode = generateRandomVerificationCode();

        // 发送验证码到用户的邮箱
        boolean sentSuccessfully = sendVerificationCodeToEmail(email, verificationCode);

        if (sentSuccessfully) {
            // 要求用户输入收到的验证码
            String userInputCode = JOptionPane.showInputDialog(
                    loginFrame,
                    "请输入收到的验证码:",
                    "验证码确认",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // 验证用户输入的验证码是否正确
            if (verificationCode.equals(userInputCode)) {
                // 允许用户输入新密码
                JPasswordField newPasswordField = new JPasswordField();
                JPasswordField confirmPasswordField = new JPasswordField();

                Object[] newPasswordMessage = {
                        "新密码:", newPasswordField,
                        "确认新密码:", confirmPasswordField
                };

                int newPasswordOption = JOptionPane.showConfirmDialog(
                        loginFrame,
                        newPasswordMessage,
                        "设置新密码",
                        JOptionPane.OK_CANCEL_OPTION
                );

                if (newPasswordOption == JOptionPane.OK_OPTION) {
                    while (true) {
                        char[] newPasswordChars = newPasswordField.getPassword();
                        char[] confirmPasswordChars = confirmPasswordField.getPassword();
                        String newPassword = new String(newPasswordChars);
                        String confirmPassword = new String(confirmPasswordChars);

                        if (newPassword.equals(confirmPassword) && isPasswordComplex(newPassword)) {
                            // 更新密码
                            updatePassword(username, newPassword);
                            JOptionPane.showMessageDialog(loginFrame, "密码已重置成功。");
                            break; // 密码符合要求，跳出循环
                        } else {
                            int retryOption = JOptionPane.showConfirmDialog(
                                    loginFrame,
                                    "密码不符合要求或确认密码不匹配。\n是否要重新输入密码？",
                                    "重试",
                                    JOptionPane.YES_NO_OPTION
                            );

                            if (retryOption == JOptionPane.NO_OPTION) {
                                break; // 用户选择不重试，跳出循环
                            }
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(loginFrame, "验证码不正确，请重新尝试。");
            }
        } else {
            JOptionPane.showMessageDialog(loginFrame, "验证码发送失败，请稍后再试。");
        }
    }

    private String generateRandomVerificationCode() {
        // 实现生成随机验证码的逻辑，返回一个随机验证码字符串
        // 例如，使用 Random 类生成一串数字
        Random random = new Random();
        int code = random.nextInt(900000) + 100000; // 生成6位数字验证码
        return String.valueOf(code);
    }

    private boolean sendVerificationCodeToEmail(String email, String verificationCode) {
        try {
            // 使用 JavaMail 发送验证码邮件
            String host = "smtp.qq.com"; // 邮件服务器主机名
            String username = "1239217122@qq.com"; // 邮箱用户名
            String password = "aegcolpfvaqtjfbh"; // 邮箱密码

            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.auth", "true");

            // 创建邮件会话
            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            // 创建邮件内容
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("重置密码验证码");
            message.setText("您的验证码是: " + verificationCode);

            // 发送邮件
            Transport.send(message);
            return true; // 邮件发送成功
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 邮件发送失败
        }

    }

    private boolean isSymbol(char c) {
        String symbols = "~`!@#$%^&*()-_=+[]{}|;:',.<>?/";
        return symbols.contains(Character.toString(c));
    }

    private boolean isPasswordComplex(String password) {
        // 密码长度至少为八位，包含至少一个大写字母、一个小写字母、一个数字和一个标点符号
        if (password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (isSymbol(c)) {
                hasSymbol = true;
            }

            if (hasUppercase && hasLowercase && hasDigit && hasSymbol) {
                return true;
            }
        }

        return false;
    }
    private void updatePassword(String username, String newPassword) {
        // 实现更新用户密码的逻辑，与您之前在 Login 类中的 updatePassword 方法类似
        // 更新 Users 表中指定用户的密码
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "UPDATE Users SET password = ? WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newPassword);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void backToMainPage() {
        // Implement your logic to go back to the main page
        loginFrame.dispose();
        Main.main(null);
    }
}
