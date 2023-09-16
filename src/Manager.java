import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class Manager {
    Login login =new Login();
    private JFrame managerFrame;
    private static final String DATABASE_URL = "jdbc:sqlite:D:\\mobile\\identifier.sqlite";
    public Manager() {
        initializeComponents();
    }

    private void initializeComponents() {
        managerFrame = new JFrame("管理员系统");
        managerFrame.setSize(400, 300);
        managerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout()); // 使用GridBagLayout布局

        // 创建按钮，并设置样式和颜色
        JButton changePasswordButton = createStyledButton("修改自身密码");
        JButton resetUserPasswordButton = createStyledButton("重置用户密码");
        JButton customerManagementButton = createStyledButton("客户信息管理");
        JButton productManagementButton = createStyledButton("商品信息管理");
        JButton returnToLoginButton = createStyledButton("返回登录页面");
        // 添加标题
        JLabel titleLabel = new JLabel("管理员系统");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 20)); // 设置标题字体和大小
        titleLabel.setHorizontalAlignment(JLabel.CENTER); // 居中对齐
        GridBagConstraints titleConstraints = new GridBagConstraints();
        titleConstraints.gridx = 0;
        titleConstraints.gridy = 0;
        titleConstraints.gridwidth = 2; // 跨越两列
        titleConstraints.insets = new Insets(10, 0, 20, 0); // 设置上下间距
        panel.add(titleLabel, titleConstraints);
        // 添加按钮到面板
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.insets = new Insets(5, 10, 5, 10); // 设置按钮之间的间距
        panel.add(changePasswordButton, constraints);
        panel.add(resetUserPasswordButton, constraints);
        panel.add(customerManagementButton, constraints);
        panel.add(productManagementButton, constraints);
        panel.add(returnToLoginButton, constraints);
        changePasswordButton.addActionListener(e -> openChangePasswordDialog());
        resetUserPasswordButton.addActionListener(e -> openResetUserPasswordDialog());
        customerManagementButton.addActionListener(e -> openCustomerManagementPage());
        productManagementButton.addActionListener(e -> openProductManagementPage());
        // 将返回按钮添加到主页面界面
        returnToLoginButton.addActionListener(e -> {
            managerFrame.dispose(); // 关闭当前窗口
            closeAllFrames();
            Main mainPage = new Main();
            mainPage.createAndShowGUI(); // 返回到主页面
        });

        managerFrame.add(panel);
        managerFrame.setVisible(true);
    }
    public static void closeAllFrames() {
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window instanceof JFrame) {
                window.dispose();
            }
        }
    }
    // 创建自定义样式的按钮
    private JButton createStyledButton(String buttonText) {
        JButton button = new JButton(buttonText);
        button.setBackground(new Color(70, 130, 180)); // 设置背景颜色
        button.setForeground(Color.WHITE); // 设置文字颜色
        button.setFont(new Font("宋体", Font.BOLD, 14)); // 设置字体
        button.setPreferredSize(new Dimension(200, 40)); // 设置按钮大小
        button.setFocusPainted(false); // 去除焦点框
        button.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20)); // 设置内边距

        return button;
    }





    public void showManagerPage() {
        managerFrame.setVisible(true);
    }

    private void openChangePasswordDialog() {
        String username = getUsernameOfLoggedInAdmin();

        while (true) {
            JPasswordField currentPasswordField = new JPasswordField();
            JPasswordField newPasswordField = new JPasswordField();
            JPasswordField confirmPasswordField = new JPasswordField();

            Object[] message = {
                    "当前密码:", currentPasswordField,
                    "新密码:", newPasswordField,
                    "确认新密码:", confirmPasswordField
            };

            int option = JOptionPane.showConfirmDialog(
                    null,
                    message,
                    "修改密码",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (option == JOptionPane.OK_OPTION) {
                char[] currentPasswordChars = currentPasswordField.getPassword();
                char[] newPasswordChars = newPasswordField.getPassword();
                char[] confirmPasswordChars = confirmPasswordField.getPassword();

                String currentPassword = new String(currentPasswordChars);
                String newPassword = new String(newPasswordChars);
                String confirmPassword = new String(confirmPasswordChars);

                // 首先，验证输入的当前密码是否正确
                if (login.isValidAdmin(username, hashSHA256(currentPassword))) {
                    // 然后，验证新密码和确认密码是否一致
                    if (newPassword.equals(confirmPassword)) {
                        if (isPasswordComplex(newPassword)) {
                            // 对新密码进行哈希处理并更新数据库中的密码
                            updateAdminPassword(username, newPassword);
                            JOptionPane.showMessageDialog(null, "密码修改成功。");
                            break; // 密码修改成功，跳出循环
                        } else {
                            int retryOption = JOptionPane.showConfirmDialog(
                                    null,
                                    "新密码不符合复杂度要求。\n是否要重新输入新密码？",
                                    "重试",
                                    JOptionPane.YES_NO_OPTION
                            );

                            if (retryOption == JOptionPane.NO_OPTION) {
                                break; // 用户选择不重试，跳出循环
                            }
                        }
                    } else {
                        int retryOption = JOptionPane.showConfirmDialog(
                                null,
                                "新密码和确认密码不匹配。\n是否要重新输入新密码？",
                                "重试",
                                JOptionPane.YES_NO_OPTION
                        );

                        if (retryOption == JOptionPane.NO_OPTION) {
                            break; // 用户选择不重试，跳出循环
                        }
                    }
                } else {
                    int retryOption = JOptionPane.showConfirmDialog(
                            null,
                            "当前密码不正确。\n是否要重新输入当前密码？",
                            "重试",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (retryOption == JOptionPane.NO_OPTION) {
                        break; // 用户选择不重试，跳出循环
                    }
                }
            } else {
                break; // 用户取消修改密码，跳出循环
            }
        }
    }

    private boolean isPasswordComplex(String password) {
        // 密码长度至少为8个字符
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
        }

        return hasUppercase && hasLowercase && hasDigit && hasSymbol;
    }

    private boolean isSymbol(char c) {
        String symbols = "~`!@#$%^&*()-_=+[]{}|;:',.<>?/";
        return symbols.contains(Character.toString(c));
    }

    public String getUsernameOfLoggedInAdmin() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT username FROM Admins LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void updateAdminPassword(String username, String newPassword) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "UPDATE Admins SET password = ? WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, hashSHA256(newPassword));
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private void openResetUserPasswordDialog() {
        SwingUtilities.invokeLater(() -> {
            UsersRequest requestProcessingGUI = new UsersRequest();
            requestProcessingGUI.showRequestProcessingPage();
        });
    }


    private void openCustomerManagementPage() {
        UsersData usersData = new UsersData();
        usersData.showFunctionMenu();
    }



    private void openProductManagementPage() {
        SwingUtilities.invokeLater(() -> {
            GoodsData goodsData = new GoodsData();
            goodsData.showGoodsInfoPage();
        });
    }


}
