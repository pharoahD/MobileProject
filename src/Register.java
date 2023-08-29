import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Register {
    private JFrame registerFrame;
    private static final String DATABASE_URL = "jdbc:sqlite:D:\\mobile\\identifier.sqlite";

    public Register() {
        initializeComponents();
    }
    private void initializeComponents() {
        registerFrame = new JFrame("注册页面");
        registerFrame.setSize(400, 300);
        registerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        registerFrame.setLocationRelativeTo(null); // Center the frame on the screen

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2, 10, 10)); // Add spacing between components

        Font labelFont = new Font("宋体", Font.BOLD, 14);
        Font fieldFont = new Font("宋体", Font.PLAIN, 14);

        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(labelFont);
        JTextField usernameField = new JTextField();
        usernameField.setFont(fieldFont);

        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(labelFont);
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(fieldFont);

        JLabel confirmPasswordLabel = new JLabel("确认密码:");
        confirmPasswordLabel.setFont(labelFont);
        JPasswordField confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(fieldFont);

        JLabel phoneNumberLabel = new JLabel("手机号:");
        phoneNumberLabel.setFont(labelFont);
        JTextField phoneNumberField = new JTextField();
        phoneNumberField.setFont(fieldFont);

        JLabel emailLabel = new JLabel("邮箱:");
        emailLabel.setFont(labelFont);
        JTextField emailField = new JTextField();
        emailField.setFont(fieldFont);

        JButton registerButton = new JButton("注册");
        registerButton.setFont(labelFont);
        registerButton.setBackground(new Color(70, 130, 180));
        registerButton.setForeground(Color.WHITE);

        JButton returnButton = new JButton("返回");
        returnButton.setFont(labelFont);
        returnButton.setBackground(new Color(70, 130, 180));
        returnButton.setForeground(Color.WHITE);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String hashedPassword = hashSHA256(password);
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String phoneNumber = phoneNumberField.getText();
            String email = emailField.getText();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phoneNumber.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(registerFrame, "请填写所有必填信息。", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidPhoneNumber(phoneNumber)) {
                JOptionPane.showMessageDialog(registerFrame, "手机号格式不正确。", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(registerFrame, "邮箱格式不正确。", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isPasswordComplex(password)) {
                JOptionPane.showMessageDialog(registerFrame, "密码不符合复杂性要求。", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(registerFrame, "确认密码与密码不一致。", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (isUsernameTaken(username)) {
                JOptionPane.showMessageDialog(registerFrame, "该用户名已被注册，请选择另一个用户名。", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int uniqueID = generateUniqueID();
            String registrationDate = getCurrentDate();
            String userLevel = "普通用户";
            double totalSpent = 0.0;

            insertUser(uniqueID, username, userLevel, registrationDate, totalSpent, phoneNumber, email);
            Login login = new Login();
            login.insertUser(username, hashedPassword);

            JOptionPane.showMessageDialog(registerFrame, "注册成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            registerFrame.dispose(); // 关闭当前注册页面
            Main mainPage = new Main();
            mainPage.createAndShowGUI(); // 返回到主页面
            // Clear input fields
            usernameField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");
            phoneNumberField.setText("");
            emailField.setText("");
        });


        returnButton.addActionListener(e -> {
            registerFrame.dispose(); // 关闭当前注册页面
            Main mainPage = new Main();
            mainPage.createAndShowGUI(); // 返回到主页面
        });

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);

        panel.add(confirmPasswordLabel);
        panel.add(confirmPasswordField);
        panel.add(phoneNumberLabel);
        panel.add(phoneNumberField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(new JLabel()); // 空标签用于布局
        panel.add(registerButton);
        panel.add(new JLabel()); // 空标签用于布局
        panel.add(returnButton); // 添加返回按钮

        registerFrame.add(panel);
        registerFrame.setVisible(true);
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

    public static boolean checkPassword(String inputPassword, String hashedPassword) {
        String hashedInputPassword = hashSHA256(inputPassword);
        return hashedInputPassword.equals(hashedPassword);
    }
    private boolean isUsernameTaken(String username) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT COUNT(*) FROM UsersData WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    private boolean isPasswordComplex(String password) {
        // 密码长度至少为八位
        if (password.length() < 8) {
            return false;
        }

        // 检查密码是否包含至少一个大写字母、一个小写字母、一个数字和一个标点符号
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
            } else if (isSymbol(c)) { // 自定义方法检查是否为标点符号
                hasSymbol = true;
            }

            if (hasUppercase && hasLowercase && hasDigit && hasSymbol) {
                return true;
            }
        }

        return false;
    }

    private boolean isSymbol(char c) {
        String symbols = "~`!@#$%^&*()-_=+[]{}|;:',.<>?/";
        return symbols.contains(Character.toString(c));
    }


    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{11}");
    }

    private boolean isValidEmail(String email) {
        return email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}");
    }

    private int generateUniqueID() {
        int uniqueID = 0;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT MAX(id) FROM UsersData";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                uniqueID = resultSet.getInt(1) + 1;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return uniqueID;
    }


    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }

    public void insertUser(int id, String username,  String userLevel, String registrationDate,
                           double totalSpent, String phoneNumber, String email) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "INSERT INTO UsersData (id, username, user_level, registration_date, total_spent, phone_number, email) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setString(2, username);
            stmt.setString(3, userLevel);
            stmt.setString(4, registrationDate);
            stmt.setDouble(5, totalSpent);
            stmt.setString(6, phoneNumber);
            stmt.setString(7, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void showRegisterPage() {
        registerFrame.setVisible(true);
    }
}
