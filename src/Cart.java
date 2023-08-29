import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class Cart {
    private JFrame cartFrame; // 定义 cartFrame 成员变量
    private static final String DATABASE_URL = "jdbc:sqlite:D:\\mobile\\identifier.sqlite";
    private DefaultTableModel tableModel;
    private JTable productListTable;

    public void openShoppingCartPage(String username) {
        cartFrame = new JFrame("购物车管理");
        cartFrame.setSize(600, 400);
        cartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        DefaultTableModel tableModel = new DefaultTableModel();
        JTable cartTable = new JTable(tableModel);

        tableModel.addColumn("商品编号");
        tableModel.addColumn("商品名称");
        tableModel.addColumn("数量");
        tableModel.addColumn("价格");

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT product_id, product_name, quantity, price FROM Cart WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int productId = resultSet.getInt("product_id");
                String productName = resultSet.getString("product_name");
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");

                Object[] rowData = {productId, productName, quantity, price};
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(cartTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton addButton = new JButton("添加商品");
        JButton removeButton = new JButton("移除选中商品");
        JButton updateButton = new JButton("更新选中数量");
        JButton checkoutButton = new JButton("下单"); // 新增的下单按钮
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(checkoutButton); // 将下单按钮添加到按钮面板中
        panel.add(buttonPanel, BorderLayout.SOUTH);


        addButton.addActionListener(e -> {
            // 打开添加商品对话框并实现添加商品到购物车的逻辑
            openAddProductDialog(tableModel, username);
        });



        removeButton.addActionListener(e -> {
            // 实现移除选中商品的逻辑
            removeSelectedProducts(cartTable, username);
            refreshCartTable(tableModel, username);
        });

        updateButton.addActionListener(e -> {
            // 实现更新选中商品数量的逻辑
            updateSelectedProductQuantities(cartTable, username);
            refreshCartTable(tableModel, username);
        });

        checkoutButton.addActionListener(e -> {
            // 实现下单逻辑
            performCheckout(username);
        });

        cartFrame.add(panel);
        cartFrame.setVisible(true);
    }

    private void openPaymentPage(int orderId, Connection conn) {
        JFrame paymentFrame = new JFrame("支付页面");
        paymentFrame.setSize(400, 300);
        paymentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 计算总金额，可以从订单历史记录中查询
        double totalAmount = calculateTotalAmount(orderId, conn);

        // 创建支付方式选择组件，这里假设有一个下拉框来选择支付方式
        JComboBox<String> paymentMethodComboBox = new JComboBox<>(new String[]{"支付宝", "微信支付", "信用卡"});

        JButton payButton = new JButton("支付");
        payButton.addActionListener(e -> {
            String selectedPaymentMethod = (String) paymentMethodComboBox.getSelectedItem();
            // 模拟支付操作，显示支付成功提示
            JOptionPane.showMessageDialog(null, "支付成功！");

            // 关闭支付页面
            paymentFrame.dispose();
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));
        panel.add(new JLabel("总金额:"));
        panel.add(new JLabel(String.valueOf(totalAmount)));
        panel.add(new JLabel("选择支付方式:"));
        panel.add(paymentMethodComboBox);
        panel.add(new JLabel()); // Empty label for spacing
        panel.add(payButton);

        paymentFrame.add(panel);
        paymentFrame.setVisible(true);
    }

    private double calculateTotalAmount(int orderId, Connection conn) {
        double totalAmount = 0.0;

        try {
            String query = "SELECT price * quantity AS itemTotal FROM CartHistory WHERE order_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, orderId);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                totalAmount += resultSet.getDouble("itemTotal");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalAmount;
    }

    private void updateTotalSpent(String username, double totalAmount, Connection conn) {
        try {
            String updateTotalSpentSQL = "UPDATE UsersData SET total_spent = total_spent + ? WHERE username = ?";
            PreparedStatement updateTotalSpentStmt = conn.prepareStatement(updateTotalSpentSQL);
            updateTotalSpentStmt.setDouble(1, totalAmount);
            updateTotalSpentStmt.setString(2, username);
            updateTotalSpentStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void performCheckout(String username) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            // 获取购物车中的商品信息
            String cartQuery = "SELECT * FROM Cart WHERE username = ?";
            PreparedStatement cartStmt = conn.prepareStatement(cartQuery);
            cartStmt.setString(1, username);
            ResultSet cartResultSet = cartStmt.executeQuery();
            boolean allProductsAvailable = true;

            // 验证购物车中的商品是否存在并库存是否足够
            while (cartResultSet.next()) {
                int productId = cartResultSet.getInt("product_id");
                int quantity = cartResultSet.getInt("quantity");

                // 查询Goods数据库获取商品信息
                Product product = getProductFromGoodsDatabase(productId);

                if (product == null || quantity > product.getQuantity()) {
                    allProductsAvailable = false;
                    break;
                }
            }

            if (allProductsAvailable) {
            // 创建订单记录并插入订单数据库
            String orderInsertQuery = "INSERT INTO Orders (username, order_date) VALUES (?, ?)";
            PreparedStatement orderInsertStmt = conn.prepareStatement(orderInsertQuery, Statement.RETURN_GENERATED_KEYS);
            orderInsertStmt.setString(1, username);
            orderInsertStmt.setString(2, getCurrentDate()); // 获取当前日期
            int affectedRows = orderInsertStmt.executeUpdate();
            ResultSet cartResultSet1 = cartStmt.executeQuery();

            if (affectedRows > 0) {
                ResultSet generatedKeys = orderInsertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);
                    double totalAmount = calculateTotalAmount(orderId, conn);
                    updateTotalSpent(username, totalAmount, conn);
                    // 将购物车中的商品信息插入购物车历史数据库
                    while (cartResultSet1.next()) {
                        int productId = cartResultSet.getInt("product_id");
                        String productName = cartResultSet.getString("product_name");
                        int quantity = cartResultSet.getInt("quantity");
                        double price = cartResultSet.getDouble("price");

                        insertCartItemHistory(orderId, username, getCurrentDate(), productId, productName, quantity, price, conn);
                        // 更新Goods数据库中的商品数量
                        updateProductQuantity(productId, quantity, conn);
                    }

                    // 清空购物车
                    String clearCartQuery = "DELETE FROM Cart WHERE username = ?";
                    PreparedStatement clearCartStmt = conn.prepareStatement(clearCartQuery);
                    clearCartStmt.setString(1, username);
                    clearCartStmt.executeUpdate();

                    JOptionPane.showMessageDialog(null, "订单已生成，购物车已清空。");
                    // 关闭购物车管理界面
                    cartFrame.dispose();

                    // 打开支付页面
                    openPaymentPage(orderId, conn);
                }
            }
            }
            else {
                    JOptionPane.showMessageDialog(null, "部分商品不存在或库存不足，请修改购物车中的商品。");
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateProductQuantity(int productId, int quantity, Connection conn) {
        try {
            // 查询Goods数据库获取商品信息
            Product product = getProductFromGoodsDatabase(productId);

            if (product != null) {
                int currentQuantity = product.getQuantity();
                int newQuantity = currentQuantity - quantity;

                if (newQuantity >= 0) {
                    String updateQuery = "UPDATE Goods SET quantity = ? WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setInt(1, newQuantity);
                    updateStmt.setInt(2, productId);
                    updateStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void insertCartItemHistory(int orderId, String username, String orderDate, int productId, String productName, int quantity, double price, Connection conn) {
        try {
            String insertQuery = "INSERT INTO CartHistory (order_id, username, order_date, product_id, product_name, quantity, price) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, orderId);
            insertStmt.setString(2, username);
            insertStmt.setString(3, orderDate); // 下单日期
            insertStmt.setInt(4, productId);
            insertStmt.setString(5, productName);
            insertStmt.setInt(6, quantity);
            insertStmt.setDouble(7, price);
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date(System.currentTimeMillis());
        return dateFormat.format(currentDate);
    }


    private boolean isValidInteger(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private void openAddProductDialog(DefaultTableModel tableModel, String username){
        JFrame addProductFrame = new JFrame("添加商品到购物车");
        addProductFrame.setSize(300, 200);
        addProductFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 2));

        JLabel productIdLabel = new JLabel("商品编号:");
        JTextField productIdField = new JTextField();

        JLabel quantityLabel = new JLabel("数量:");
        JTextField quantityField = new JTextField();

        JButton addButton = new JButton("添加");
        addButton.addActionListener(e -> {
            String productIdText = productIdField.getText();
            String quantityText = quantityField.getText();

            if (!isValidInteger(productIdText) || !isValidInteger(quantityText)) {
                JOptionPane.showMessageDialog(null, "请输入有效的整数。");
                return;
            }

            int productId = Integer.parseInt(productIdText);
            int requestedQuantity = Integer.parseInt(quantityText);
            // 查询Goods数据库获取商品信息
            Product product = getProductFromGoodsDatabase(productId);

            if (product != null) {
                int availableQuantity = product.getQuantity();

                if (requestedQuantity > availableQuantity) {
                    JOptionPane.showMessageDialog(null, "所需数量超过库存最大数量。");
                } else {
                    // 实现添加商品到购物车的逻辑
                    addToCart(username, productId, requestedQuantity);
                    JOptionPane.showMessageDialog(null, "商品已添加到购物车。");

                    // 刷新购物车表格
                    refreshCartTable(tableModel, username);

                    addProductFrame.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(null, "商品编号无效。");
            }
        });

        panel.add(productIdLabel);
        panel.add(productIdField);
        panel.add(quantityLabel);
        panel.add(quantityField);
        panel.add(new JLabel()); // Empty label for spacing
        panel.add(addButton);

        addProductFrame.add(panel);
        addProductFrame.setVisible(true);
    }

    private Product getProductFromGoodsDatabase(int productId) {
        // 假设您的数据库连接代码已经实现
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT * FROM Goods WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                // 从查询结果构建商品对象
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double price = resultSet.getDouble("retail_price");
                int quantity = resultSet.getInt("quantity");

                return new Product(id, name, price, quantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // 返回 null 表示未找到对应商品
    }


    private void addToCart(String username, int productId, int quantity) {
        // 查询购物车中是否已存在相同商品
        CartItem existingCartItem = getCartItem(username, productId);

        if (existingCartItem != null) {
            // 已存在相同商品，更新数量
            int newQuantity = existingCartItem.getQuantity() + quantity;
            updateCartItemQuantity(username, productId, newQuantity);
        } else {
            // 不存在相同商品，插入新购物车项
            Product product = getProductFromGoodsDatabase(productId);
            if (product != null) {
                insertCartItem(username, product.getId(), product.getName(), quantity, product.getPrice());
            }
        }
    }

    private CartItem getCartItem(String username, int productId) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT * FROM Cart WHERE username = ? AND product_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, productId);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                int cartId = resultSet.getInt("cart_id");
                int quantity = resultSet.getInt("quantity");
                // 根据结果创建购物车项对象
                return new CartItem(cartId, productId, quantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // 如果不存在相同的商品项，返回null
    }

    private void updateCartItemQuantity(String username, int productId, int newQuantity) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String updateSql = "UPDATE Cart SET quantity = ? WHERE username = ? AND product_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, newQuantity);
            updateStmt.setString(2, username);
            updateStmt.setInt(3, productId);
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertCartItem(String username, int productId, String productName, int quantity, double price) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "INSERT INTO Cart (username, product_id, product_name, quantity, price) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, productId);
            stmt.setString(3, productName);
            stmt.setInt(4, quantity);
            stmt.setDouble(5, price);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteCartItem(String username, int productId) {
        String sql = "DELETE FROM Cart WHERE username = ? AND product_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
            System.out.println("商品已从购物车中移除：商品ID = " + productId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeSelectedProducts(JTable cartTable, String username) {
        DefaultTableModel model = (DefaultTableModel) cartTable.getModel();
        int[] selectedRows = cartTable.getSelectedRows();

        int confirmResult = JOptionPane.showConfirmDialog(
                null,
                "确认要删除选中的商品吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmResult == JOptionPane.YES_OPTION) {
            for (int rowIndex : selectedRows) {
                int productId = (int) model.getValueAt(rowIndex, 0); // Assuming product ID is in the first column

                deleteCartItem(username, productId); // Implement this method to delete the cart item
            }

            // Update the table to reflect the changes
            refreshCartTable(model, username); // Implement this method to refresh the cart table
        }
    }



    private void updateSelectedProductQuantities(JTable cartTable, String username) {
        DefaultTableModel model = (DefaultTableModel) cartTable.getModel();
        int[] selectedRows = cartTable.getSelectedRows();

        for (int rowIndex : selectedRows) {
            int productId = (int) model.getValueAt(rowIndex, 0); // Assuming product ID is in the second column
            int currentQuantity = (int) model.getValueAt(rowIndex, 2); // Assuming quantity is in the fourth column

            // 查询Goods数据库获取商品信息
            Product product = getProductFromGoodsDatabase(productId);

            if (product != null) {
                int availableQuantity = product.getQuantity();

                // 弹出对话框，让用户重新输入数量
                String input = JOptionPane.showInputDialog(null, "请输入新的数量:", currentQuantity);

                if (input != null) {
                    try {
                        int newQuantity = Integer.parseInt(input);
                        if (newQuantity <= 0) {
                            // 将商品移除
                            deleteCartItem(username, productId);
                        } else if (newQuantity > availableQuantity) {
                            JOptionPane.showMessageDialog(null, "所需数量超过库存最大数量。");
                        } else {
                            // 更新购物车中已存在商品的数量
                            updateCartItemQuantity(username, productId, newQuantity);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "请输入有效的数字。");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "商品编号无效。");
            }
        }

        // 刷新购物车表格
        refreshCartTable(model, username);
    }



    private void refreshCartTable(DefaultTableModel tableModel, String username) {
        tableModel.setRowCount(0); // 清空表格内容

        String sql = "SELECT product_id, product_name, quantity, price FROM Cart WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int productId = resultSet.getInt("product_id");
                String productName = resultSet.getString("product_name");
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");

                // 添加数据到表格模型
                Object[] rowData = {productId, productName, quantity, price};
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createOrderTable() {
        String ORDERS_TABLE_CREATE_SQL =
                "CREATE TABLE IF NOT EXISTS Orders (" +
                        "username TEXT NOT NULL," +
                        "order_date TEXT NOT NULL)";

// 在数据库初始化时执行该语句来创建 Orders 表
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(ORDERS_TABLE_CREATE_SQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public static void createCartTable() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS Cart (" +
                    "cart_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL, " +
                    "product_id INTEGER NOT NULL, " +
                    "product_name TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "price REAL NOT NULL)";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
                System.out.println("Cart table created successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}




