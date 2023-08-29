import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class User {
    private static final String DATABASE_URL = "jdbc:sqlite:D:\\mobile\\identifier.sqlite";
    private String username;
    private JFrame userFrame;
    private JFrame shopFrame;
    private JFrame historyFrame;

    public static void closeAllFrames() {
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window instanceof JFrame) {
                window.dispose();
            }
        }
    }
    public User(String username) {
        CartHistory cartHistory = new CartHistory();
        cartHistory.createCartHistoryTable();
        this.username = username;
        initializeComponents();
    }

    private void initializeComponents() {
        userFrame = new JFrame("用户系统");
        userFrame.setSize(400, 300);
        userFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        // 添加大标题
        JLabel titleLabel = new JLabel("用户系统");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        GridBagConstraints titleConstraints = new GridBagConstraints();
        titleConstraints.gridx = 0;
        titleConstraints.gridy = 0;
        titleConstraints.gridwidth = 1;
        titleConstraints.insets = new Insets(20, 0, 30, 0);
        panel.add(titleLabel, titleConstraints);

        // 创建并美化按钮
        JButton changePasswordButton = createStyledButton("修改自身密码");
        JButton shoppingCartButton = createStyledButton(" 购物车管理 ");
        JButton purchaseHistoryButton = createStyledButton("历史消费记录");
        JButton openShopButton = createStyledButton("打开购物商城");
        JButton returnButton = createStyledButton("返回"); // 添加返回按钮

        changePasswordButton.addActionListener(e -> openChangePasswordDialog());
        shoppingCartButton.addActionListener(e -> openShoppingCartPage());
        purchaseHistoryButton.addActionListener(e -> openPurchaseHistoryPage(username));
        openShopButton.addActionListener(e -> openShopPage());
        returnButton.addActionListener(e -> returnToMainPage()); // 返回按钮的动作监听器
        // 添加按钮到面板
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 1;
        buttonConstraints.gridwidth = 1;
        buttonConstraints.insets = new Insets(10, 0, 10, 0);
        panel.add(changePasswordButton, buttonConstraints);
        buttonConstraints.gridy = 2;
        panel.add(shoppingCartButton, buttonConstraints);
        buttonConstraints.gridy = 3;
        panel.add(purchaseHistoryButton, buttonConstraints);
        buttonConstraints.gridy = 4;
        panel.add(openShopButton, buttonConstraints);
        buttonConstraints.gridy = 5;
        panel.add(returnButton, buttonConstraints); // 添加返回按钮到面板

        userFrame.add(panel);
        userFrame.setVisible(true);
    }

    private JButton createStyledButton(String buttonText) {
        JButton button = new JButton(buttonText);
        button.setFont(new Font("宋体", Font.PLAIN, 16));
        button.setBackground(new Color(70, 130, 180)); // 设置背景颜色
        button.setForeground(Color.WHITE); // 设置文字颜色
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        button.setPreferredSize(new Dimension(300, 40)); // 统一设置按钮大小，例如 300x40 像素
        return button;
    }






    private void openShopPage() {
        shopFrame = new JFrame("购物商城");
        shopFrame.setSize(800, 600);
        shopFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultTableModel tableModel = new DefaultTableModel();
        JTable shopTable = new JTable(tableModel);

        // 添加表头
        tableModel.addColumn("商品编号");
        tableModel.addColumn("商品名称");
        tableModel.addColumn("库存数量");
        tableModel.addColumn("价格");

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String goodsQuery = "SELECT * FROM Goods";
            PreparedStatement goodsStmt = conn.prepareStatement(goodsQuery);
            ResultSet goodsResultSet = goodsStmt.executeQuery();

            while (goodsResultSet.next()) {
                int productId = goodsResultSet.getInt("id");
                String productName = goodsResultSet.getString("name");
                int quantity = goodsResultSet.getInt("quantity");
                double price = goodsResultSet.getDouble("retail_price");

                Object[] rowData = {productId, productName, quantity, price};
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(shopTable);
        shopFrame.add(scrollPane);
        shopFrame.setVisible(true);
    }

    private void returnToMainPage() {
        userFrame.dispose(); // 关闭用户系统窗口
        closeAllFrames();
        Main mainPage = new Main();
        mainPage.createAndShowGUI(); // 返回到主页面
    }


    private void openChangePasswordDialog() {


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
            Login login = new Login();
            if (login.isValidUserLogin(username, currentPassword)) {
                if (newPassword.equals(confirmPassword)) {
                    if (isPasswordComplex(newPassword)) {
                        String hashedPassword = hashSHA256(newPassword);
                        updatePassword(username, hashedPassword);
                        JOptionPane.showMessageDialog(null, "密码修改成功。");
                    } else {
                        JOptionPane.showMessageDialog(null, "新密码不符合复杂度要求。");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "新密码和确认密码不匹配。");
                }
            } else {
                JOptionPane.showMessageDialog(null, "当前密码不正确。");
            }
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

    private boolean isSymbol(char c) {
        String symbols = "~`!@#$%^&*()-_=+[]{}|;:',.<>?/";
        return symbols.contains(Character.toString(c));
    }

    private void updatePassword(String username, String newPassword) {
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




    private void openShoppingCartPage() {
        Cart cart = new Cart();
        cart.openShoppingCartPage(username);// 打开购物车管理页面的逻辑
    }

    private void openPurchaseHistoryPage(String username) {
        historyFrame = new JFrame("历史消费记录");
        historyFrame.setSize(600, 400);
        historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        DefaultTableModel tableModel = new DefaultTableModel();
        JTable historyTable = new JTable(tableModel);

        tableModel.addColumn("订单编号");
        tableModel.addColumn("下单日期");
        tableModel.addColumn("商品编号");
        tableModel.addColumn("商品名称");
        tableModel.addColumn("数量");
        tableModel.addColumn("价格");

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String historyQuery = "SELECT * FROM CartHistory WHERE username = ?";
            PreparedStatement historyStmt = conn.prepareStatement(historyQuery);
            historyStmt.setString(1, username);
            ResultSet historyResultSet = historyStmt.executeQuery();

            while (historyResultSet.next()) {
                int orderId = historyResultSet.getInt("order_id");
                String orderDate = historyResultSet.getString("order_date");
                int productId = historyResultSet.getInt("product_id");
                String productName = historyResultSet.getString("product_name");
                int quantity = historyResultSet.getInt("quantity");
                double price = historyResultSet.getDouble("price");

                Object[] rowData = {orderId, orderDate, productId, productName, quantity, price};
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(historyTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        historyFrame.add(panel);
        historyFrame.setVisible(true);
    }






    public void showUserPage() {
        userFrame.setVisible(true);
    }
}
