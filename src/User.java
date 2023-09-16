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

    public void openShopPage() {
        shopFrame = new JFrame("购物商城");
        shopFrame.setSize(1200, 800);
        shopFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultTableModel tableModel = new DefaultTableModel();
        JTable shopTable = new JTable(tableModel);

        // 添加表头
        tableModel.addColumn("商品编号");
        tableModel.addColumn("商品名称");
        tableModel.addColumn("生产厂家");
        tableModel.addColumn("生产日期");
        tableModel.addColumn("型号");
        tableModel.addColumn("库存数量");
        tableModel.addColumn("价格");

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String goodsQuery = "SELECT * FROM Goods";
            PreparedStatement goodsStmt = conn.prepareStatement(goodsQuery);
            ResultSet goodsResultSet = goodsStmt.executeQuery();

            while (goodsResultSet.next()) {
                int productId = goodsResultSet.getInt("id");
                String productName = goodsResultSet.getString("name");
                String manufacturer = goodsResultSet.getString("manufacturer");
                String productionDate = goodsResultSet.getString("production_date");
                String model = goodsResultSet.getString("model");
                int quantity = goodsResultSet.getInt("quantity");
                double price = goodsResultSet.getDouble("retail_price");

                Object[] rowData = {productId, productName, manufacturer, productionDate, model, quantity, price};
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(shopTable);
        JTextField searchField = new JTextField(30);

        JButton searchButton = new JButton("搜索");
        JButton refreshButton = new JButton("刷新");
        JButton sortButton = new JButton("价格排序");
        JButton hotSortButton = new JButton("热门排序");

        // 创建购物车表格
        DefaultTableModel cartTableModel = new DefaultTableModel();
        JTable cartTable = new JTable(cartTableModel);
        cartTableModel.addColumn("商品编号");
        cartTableModel.addColumn("商品名称");
        cartTableModel.addColumn("数量");

        // 创建按钮面板，包括添加到购物车按钮和购物车刷新按钮
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(searchField);
        buttonPanel.add(searchButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(sortButton);

        buttonPanel.add(hotSortButton);


        // 添加到购物车按钮
        JButton addToCartButton = new JButton("添加到购物车");
        addToCartButton.addActionListener(e -> addToCart(shopTable, cartTableModel,username));
        buttonPanel.add(addToCartButton);

        // 刷新按钮的点击事件，重新加载购物内容
        refreshButton.addActionListener(e -> {
            refreshShopContent(tableModel);
        });

        // 价格排序按钮的点击事件
        final boolean[] ascending = {true};
        sortButton.addActionListener(e -> {
            ascending[0] = !ascending[0];
            loadSortedShopContent(tableModel, ascending[0]);
        });
        hotSortButton.addActionListener(e -> {
            ascending[0] = !ascending[0];
            loadHotSortedShopContent(tableModel, ascending[0]);
        });
        // 搜索按钮的点击事件
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText();
            performSearch(keyword, tableModel);
        });

        // 创建主面板，包括按钮面板和商品表格
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 创建购物车面板，包括购物车表格
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("购物车"));
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // 将主面板和购物车面板添加到购物商城页面
        shopFrame.add(mainPanel, BorderLayout.CENTER);
        shopFrame.add(cartPanel, BorderLayout.EAST);

        shopFrame.setVisible(true);
    }



        // SQLite数据库连接URL

    private void loadHotSortedShopContent(DefaultTableModel tableModel, boolean ascending) {
        tableModel.setRowCount(0); // Clear the table

        String sortOrder = ascending ? "ASC" : "DESC";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String query = "SELECT g.id, g.name, g.manufacturer, g.production_date, g.model, g.quantity, g.retail_price, h.purchaseCount " +
                    "FROM Goods g " +
                    "LEFT JOIN HotGoods h ON g.id = h.id " +
                    "ORDER BY h.purchaseCount " + sortOrder;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int productId = resultSet.getInt("id");
                String productName = resultSet.getString("name");
                String manufacturer = resultSet.getString("manufacturer");
                String productionDate = resultSet.getString("production_date");
                String model = resultSet.getString("model");
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("retail_price");

                Object[] rowData = {productId, productName, manufacturer, productionDate, model, quantity, price};
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // 添加商品到购物车
    private void addToCart(JTable shopTable, DefaultTableModel cartTableModel, String username) {
        int selectedRow = shopTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(shopFrame, "请选择要添加到购物车的商品。", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int productId = (int) shopTable.getValueAt(selectedRow, 0); // 商品编号
        String productName = (String) shopTable.getValueAt(selectedRow, 1); // 商品名称
        int maxQuantity = (int) shopTable.getValueAt(selectedRow, 5); // 最大库存数量
        double productPrice = (double) shopTable.getValueAt(selectedRow, 6); // 商品价格

        // 弹出输入对话框来让用户选择数量
        String quantityStr = JOptionPane.showInputDialog(shopFrame, "请输入要购买的数量（不超过 " + maxQuantity + "）：", "选择数量", JOptionPane.PLAIN_MESSAGE);

        if (quantityStr != null && !quantityStr.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr);

                if (quantity <= 0 || quantity > maxQuantity) {
                    JOptionPane.showMessageDialog(shopFrame, "数量无效，必须为正整数且不超过 " + maxQuantity + "。", "错误", JOptionPane.ERROR_MESSAGE);
                } else {
                    // 检查购物车中是否已存在相同商品，如果存在则增加数量，否则添加新条目
                    boolean itemExists = false;
                    for (int i = 0; i < cartTableModel.getRowCount(); i++) {
                        if (productId == (int) cartTableModel.getValueAt(i, 0)) {
                            int currentQuantity = (int) cartTableModel.getValueAt(i, 2);
                            cartTableModel.setValueAt(currentQuantity + quantity, i, 2);
                            itemExists = true;
                            break;
                        }
                    }

                    if (!itemExists) {
                        // 如果购物车中不存在相同商品，则添加新的购物车项
                        Object[] rowData = {
                                productId, // 商品编号
                                productName, // 商品名称

                                quantity, // 数量
                                productPrice // 商品价格
                        };
                        cartTableModel.addRow(rowData);
                    }

                    // 更新数据库：将商品添加到购物车表中
                    updateCartInDatabase(username, productId, productName, productPrice, quantity);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(shopFrame, "请输入有效的数字。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    // 更新购物车数据库
    private void updateCartInDatabase(String username, int productId, String productName, double productPrice, int quantity) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            // 检查购物车中是否已存在相同商品，如果存在则增加数量，否则插入新条目
            String checkIfExistsSQL = "SELECT * FROM Cart WHERE username = ? AND product_id = ?";
            PreparedStatement checkIfExistsStmt = conn.prepareStatement(checkIfExistsSQL);
            checkIfExistsStmt.setString(1, username);
            checkIfExistsStmt.setInt(2, productId);
            ResultSet resultSet = checkIfExistsStmt.executeQuery();

            if (resultSet.next()) {
                // 商品已存在于购物车中，增加数量
                int currentQuantity = resultSet.getInt("quantity");
                String updateSQL = "UPDATE Cart SET quantity = ? WHERE username = ? AND product_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSQL);
                updateStmt.setInt(1, currentQuantity + quantity);
                updateStmt.setString(2, username);
                updateStmt.setInt(3, productId);
                updateStmt.executeUpdate();
            } else {
                // 商品不在购物车中，插入新条目
                String insertSQL = "INSERT INTO Cart (username, product_id, product_name,quantity , price) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
                insertStmt.setString(1, username);
                insertStmt.setInt(2, productId);
                insertStmt.setString(3, productName); // 添加商品名称
                insertStmt.setDouble(5, productPrice); // 添加商品价格
                insertStmt.setInt(4, quantity);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }







    private void loadSortedShopContent(DefaultTableModel tableModel, boolean ascending) {
        tableModel.setRowCount(0); // 清空表格模型的数据

        String sortOrder = ascending ? "ASC" : "DESC"; // 根据排序顺序确定 SQL 排序顺序

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String query = "SELECT id, name, manufacturer, production_date, model,quantity , retail_price FROM Goods " +
                    "ORDER BY retail_price " + sortOrder;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int productId = resultSet.getInt("id");
                String productName = resultSet.getString("name");
                String manufacturer = resultSet.getString("manufacturer");
                String productionDate = resultSet.getString("production_date");
                String model = resultSet.getString("model");
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("retail_price");
                Object[] rowData = {productId, productName, manufacturer, productionDate, model,quantity,  price};
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void refreshShopContent(DefaultTableModel tableModel) {
        // 清空表格模型的数据
        tableModel.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String goodsQuery = "SELECT * FROM Goods";
            PreparedStatement goodsStmt = conn.prepareStatement(goodsQuery);
            ResultSet goodsResultSet = goodsStmt.executeQuery();

            while (goodsResultSet.next()) {
                int productId = goodsResultSet.getInt("id");
                String productName = goodsResultSet.getString("name");
                String manufacturer = goodsResultSet.getString("manufacturer");
                String productionDate = goodsResultSet.getString("production_date");
                String model = goodsResultSet.getString("model");
                int quantity = goodsResultSet.getInt("quantity");
                double price = goodsResultSet.getDouble("retail_price");

                Object[] rowData = {productId, productName, manufacturer, productionDate, model, quantity, price};
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void performSearch(String keyword, DefaultTableModel tableModel) {
        tableModel.setRowCount(0); // 清空表格内容

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String goodsQuery = "SELECT * FROM Goods WHERE id LIKE ? OR name LIKE ? OR manufacturer LIKE ? OR production_date LIKE ? OR model LIKE ?";
            PreparedStatement goodsStmt = conn.prepareStatement(goodsQuery);
            String wildcardKeyword = "%" + keyword + "%";
            goodsStmt.setString(1, wildcardKeyword);
            goodsStmt.setString(2, wildcardKeyword);
            goodsStmt.setString(3, wildcardKeyword);
            goodsStmt.setString(4, wildcardKeyword);
            goodsStmt.setString(5, wildcardKeyword);
            ResultSet goodsResultSet = goodsStmt.executeQuery();

            while (goodsResultSet.next()) {
                int productId = goodsResultSet.getInt("id");
                String productName = goodsResultSet.getString("name");
                String manufacturer = goodsResultSet.getString("manufacturer");
                String productionDate = goodsResultSet.getString("production_date");
                String model = goodsResultSet.getString("model");
                int quantity = goodsResultSet.getInt("quantity");
                double price = goodsResultSet.getDouble("retail_price");

                Object[] rowData = {productId, productName, manufacturer, productionDate, model, quantity, price};
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }




    private void returnToMainPage() {
        userFrame.dispose(); // 关闭用户系统窗口
        closeAllFrames();
        Main mainPage = new Main();
        mainPage.createAndShowGUI(); // 返回到主页面
    }


    private void openChangePasswordDialog() {
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

                Login login = new Login();
                if (login.isValidUserLogin(username, currentPassword)) {
                    if (newPassword.equals(confirmPassword)) {
                        if (isPasswordComplex(newPassword)) {
                            String hashedPassword = hashSHA256(newPassword);
                            updatePassword(username, hashedPassword);
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
