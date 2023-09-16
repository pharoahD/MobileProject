import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GoodsData {
    private JFrame frame;

    private static final String DATABASE_URL = "jdbc:sqlite:D:\\mobile\\identifier.sqlite";

    public GoodsData() {
        initializeComponents();

    }


    private void initializeComponents() {
        frame = new JFrame("商品信息管理");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        // 添加大标题
        JLabel titleLabel = new JLabel("商品信息管理");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        GridBagConstraints titleConstraints = new GridBagConstraints();
        titleConstraints.gridx = 0;
        titleConstraints.gridy = 0;
        titleConstraints.gridwidth = 1;
        titleConstraints.insets = new Insets(20, 0, 30, 0);
        panel.add(titleLabel, titleConstraints);

        // 创建并美化按钮
        JButton listGoodsButton = createStyledButton("列出所有商品信息");
        JButton addGoodsButton = createStyledButton("添加商品信息");
        JButton modifyGoodsButton = createStyledButton("修改商品信息");
        JButton deleteGoodsButton = createStyledButton("删除商品信息");

        listGoodsButton.addActionListener(e -> listAllGoods());
        addGoodsButton.addActionListener(e -> addGoods());
        modifyGoodsButton.addActionListener(e -> modifyGoods());
        deleteGoodsButton.addActionListener(e -> deleteGoods());
        // 添加按钮到面板
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 1;
        buttonConstraints.gridwidth = 1;
        buttonConstraints.insets = new Insets(10, 0, 10, 0);
        panel.add(listGoodsButton, buttonConstraints);
        buttonConstraints.gridy = 2;
        panel.add(addGoodsButton, buttonConstraints);
        buttonConstraints.gridy = 3;
        panel.add(modifyGoodsButton, buttonConstraints);
        buttonConstraints.gridy = 4;
        panel.add(deleteGoodsButton, buttonConstraints);

        frame.add(panel);
    }

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


    private void listAllGoods() {
        JFrame listFrame = new JFrame("所有商品信息");
        listFrame.setSize(800, 600);
        listFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultTableModel tableModel = new DefaultTableModel();
        JTable goodsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(goodsTable);

        tableModel.setColumnIdentifiers(new String[]{
                "商品编号", "商品名称", "生产厂家", "生产日期", "型号", "进货价", "零售价格", "数量"
        });

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT * FROM Goods";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int goodsId = resultSet.getInt("id");
                String goodsName = resultSet.getString("name");
                String manufacturer = resultSet.getString("manufacturer");
                String productionDate = resultSet.getString("production_date");
                String model = resultSet.getString("model");
                double purchasePrice = resultSet.getDouble("purchase_price");
                double retailPrice = resultSet.getDouble("retail_price");
                int quantity = resultSet.getInt("quantity");

                tableModel.addRow(new Object[]{
                        goodsId, goodsName, manufacturer, productionDate,
                        model, purchasePrice, retailPrice, quantity
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        listFrame.add(scrollPane);
        listFrame.setVisible(true);
    }

    private void insertGoods(int id,String name, String manufacturer, String productionDate,
                             String model, double purchasePrice, double retailPrice, int quantity) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "INSERT INTO Goods (id, name, manufacturer, production_date, model, purchase_price, retail_price, quantity) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setString(2, name);
            stmt.setString(3, manufacturer);
            stmt.setString(4, productionDate);
            stmt.setString(5, model);
            stmt.setDouble(6, purchasePrice);
            stmt.setDouble(7, retailPrice);
            stmt.setInt(8, quantity);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidInteger(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void addGoods() {
        JFrame addFrame = new JFrame("添加商品信息");
        addFrame.setSize(400, 300);
        addFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(9, 2));
        JLabel idLabel = new JLabel("商品编号:");
        JTextField idField = new JTextField();


        JLabel nameLabel = new JLabel("商品名称:");
        JTextField nameField = new JTextField();

        JLabel manufacturerLabel = new JLabel("生产厂家:");
        JTextField manufacturerField = new JTextField();

        JLabel productionDateLabel = new JLabel("生产日期:");
        JTextField productionDateField = new JTextField();

        JLabel modelLabel = new JLabel("型号:");
        JTextField modelField = new JTextField();

        JLabel purchasePriceLabel = new JLabel("进货价:");
        JTextField purchasePriceField = new JTextField();

        JLabel retailPriceLabel = new JLabel("零售价格:");
        JTextField retailPriceField = new JTextField();

        JLabel quantityLabel = new JLabel("数量:");
        JTextField quantityField = new JTextField();

        JButton addButton = new JButton("添加");
        addButton.addActionListener(e -> {
            String quantityText = quantityField.getText();

            if (!isValidInteger(quantityText)) {
                JOptionPane.showMessageDialog(null, "请输入有效的整数数量。");
                return;
            }
            int id = Integer.parseInt(idField.getText());
            String name = nameField.getText();
            String manufacturer = manufacturerField.getText();
            String productionDate = productionDateField.getText();
            String model = modelField.getText();
            double purchasePrice = Double.parseDouble(purchasePriceField.getText());
            double retailPrice = Double.parseDouble(retailPriceField.getText());
            int quantity = Integer.parseInt(quantityText);

            insertGoods(id,name, manufacturer, productionDate, model, purchasePrice, retailPrice, quantity);

            insertPurchaseCount(name,0);
            addFrame.dispose();
        });
        panel.add(idLabel);
        panel.add(idField);
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(manufacturerLabel);
        panel.add(manufacturerField);
        panel.add(productionDateLabel);
        panel.add(productionDateField);
        panel.add(modelLabel);
        panel.add(modelField);
        panel.add(purchasePriceLabel);
        panel.add(purchasePriceField);
        panel.add(retailPriceLabel);
        panel.add(retailPriceField);
        panel.add(quantityLabel);
        panel.add(quantityField);
        panel.add(new JLabel()); // Empty label for spacing
        panel.add(addButton);

        addFrame.add(panel);
        addFrame.setVisible(true);
    }

    private static void insertPurchaseCount(String productName, int purchaseCount) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                System.out.println("成功连接到数据库");

                // 检查产品是否已存在
                String checkIfExistsSQL = "SELECT purchaseCount FROM HotGoods WHERE productName = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkIfExistsSQL)) {
                    checkStmt.setString(1, productName);
                    ResultSet resultSet = checkStmt.executeQuery();

                    if (resultSet.next()) {
                        // 产品已存在，获取当前购买次数并叠加
                        int currentCount = resultSet.getInt("purchaseCount");
                        purchaseCount += currentCount;

                        // 更新购买次数
                        String updateSQL = "UPDATE HotGoods SET purchaseCount = ? WHERE productName = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                            updateStmt.setInt(1, purchaseCount);
                            updateStmt.setString(2, productName);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        // 产品不存在，插入新行
                        String insertSQL = "INSERT INTO HotGoods (productName, purchaseCount) VALUES (?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                            insertStmt.setString(1, productName);
                            insertStmt.setInt(2, purchaseCount);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public GoodsInfo getGoodsInfoById(int goodsId) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String selectSql = "SELECT * FROM Goods WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(selectSql);
            stmt.setInt(1, goodsId);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String manufacturer = resultSet.getString("manufacturer");
                String productionDate = resultSet.getString("production_date");
                String model = resultSet.getString("model");
                double purchasePrice = resultSet.getDouble("purchase_price");
                double retailPrice = resultSet.getDouble("retail_price");
                int quantity = resultSet.getInt("quantity");

                return new GoodsInfo(goodsId, name, manufacturer, productionDate, model, purchasePrice, retailPrice, quantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Return null if goods with specified ID are not found
    }


    private void modifyGoods() {
        String input = JOptionPane.showInputDialog(null, "请输入要修改的商品编号:", "修改商品信息", JOptionPane.PLAIN_MESSAGE);

        if (input != null && !input.isEmpty()) {
            int goodsId = Integer.parseInt(input);
            GoodsInfo goodsInfo = getGoodsInfoById(goodsId);

            if (goodsInfo != null) {
                // 弹出一个对话框显示商品信息并允许用户修改
                JPanel panel = new JPanel();
                panel.setLayout(new GridLayout(7, 2));

                JTextField nameField = new JTextField(goodsInfo.getName());
                JTextField manufacturerField = new JTextField(goodsInfo.getManufacturer());
                JTextField productionDateField = new JTextField(goodsInfo.getProductionDate());
                JTextField modelField = new JTextField(goodsInfo.getModel());
                JTextField purchasePriceField = new JTextField(Double.toString(goodsInfo.getPurchasePrice()));
                JTextField retailPriceField = new JTextField(Double.toString(goodsInfo.getRetailPrice()));
                JTextField quantityField = new JTextField(Integer.toString(goodsInfo.getQuantity()));

                panel.add(new JLabel("商品名称:"));
                panel.add(nameField);
                panel.add(new JLabel("生产厂家:"));
                panel.add(manufacturerField);
                panel.add(new JLabel("生产日期:"));
                panel.add(productionDateField);
                panel.add(new JLabel("型号:"));
                panel.add(modelField);
                panel.add(new JLabel("进货价:"));
                panel.add(purchasePriceField);
                panel.add(new JLabel("零售价格:"));
                panel.add(retailPriceField);
                panel.add(new JLabel("数量:"));
                panel.add(quantityField);

                int option = JOptionPane.showConfirmDialog(null, panel, "修改商品信息", JOptionPane.OK_CANCEL_OPTION);

                if (option == JOptionPane.OK_OPTION) {
                    // 更新数据库中的商品信息
                    String quantityText = quantityField.getText();

                    if (!isValidInteger(quantityText)) {
                        JOptionPane.showMessageDialog(null, "请输入有效的整数数量。");
                        return;
                    }
                    updateGoodsInfo(goodsId, nameField.getText(), manufacturerField.getText(), productionDateField.getText(),
                            modelField.getText(), Double.parseDouble(purchasePriceField.getText()),
                            Double.parseDouble(retailPriceField.getText()), Integer.parseInt(quantityText));
                }
            } else {
                JOptionPane.showMessageDialog(null, "找不到该商品编号的商品信息。");
            }
        }
    }
    private void updateGoodsInfo(int goodsId, String name, String manufacturer, String productionDate,
                                 String model, double purchasePrice, double retailPrice, int quantity) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "UPDATE Goods SET name = ?, manufacturer = ?, production_date = ?, model = ?, " +
                    "purchase_price = ?, retail_price = ?, quantity = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, name);
            stmt.setString(2, manufacturer);
            stmt.setString(3, productionDate);
            stmt.setString(4, model);
            stmt.setDouble(5, purchasePrice);
            stmt.setDouble(6, retailPrice);
            stmt.setInt(7, quantity);
            stmt.setInt(8, goodsId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void deleteGoods() {
        String input = JOptionPane.showInputDialog(
                null,
                "请输入要删除的商品编号：",
                "删除商品信息",
                JOptionPane.QUESTION_MESSAGE
        );

        if (input != null && !input.isEmpty()) {
            int goodsId = Integer.parseInt(input);
            int result = JOptionPane.showConfirmDialog(
                    null,
                    "确定要删除该商品信息吗？",
                    "删除商品信息",
                    JOptionPane.YES_NO_OPTION
            );

            if (result == JOptionPane.YES_OPTION) {
                try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
                    String sql = "DELETE FROM Goods WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, goodsId);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(
                            null,
                            "商品信息删除成功。",
                            "删除商品信息",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            null,
                            "删除商品信息时出现错误。",
                            "删除商品信息",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }



    public void showGoodsInfoPage() {
        frame.setVisible(true);

    }


}