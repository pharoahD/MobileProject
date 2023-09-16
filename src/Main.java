import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class Main {
    private static JFrame mainFrame;

    public static void main(String[] args) {
        createTable();
        createUserTable();
        createCartHistoryTable();
        createCartTable();
        createGoodsTable();
        createOrderTable();
        createTableIfNotExists();
        CreateHotGoodsDatabase();
        createRequestTable();
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }
    private static final String DATABASE_URL = "jdbc:sqlite:D:\\mobile\\identifier.sqlite";
    public static void createUserTable() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "CREATE TABLE IF NOT EXISTS Users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL, password TEXT NOT NULL)";
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void createTable() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            // Check if table exists
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet resultSet = meta.getTables(null, null, "Admins", null);
            boolean tableExists = resultSet.next();

            if (!tableExists) {
                // Table doesn't exist, create it
                String createTableSQL = "CREATE TABLE IF NOT EXISTS Admins (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL, password TEXT NOT NULL)";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createTableSQL);

                    // Insert initial admin if the table is newly created
                    insertAdmin("admin", hashSHA256("ynuadmin"));
                    System.out.println("Admin inserted successfully.");
                }
            }
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
    private static final String CREATE_CART_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS CartHistory (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "username TEXT," +
            "order_id INTEGER," +
            "order_date TEXT," + // 添加下单日期列
            "product_id INTEGER," +
            "product_name TEXT," +
            "quantity INTEGER," +
            "price REAL" +
            ");";
    public static void createCartHistoryTable() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            conn.createStatement().execute(CREATE_CART_HISTORY_TABLE);

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

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void createGoodsTable() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "CREATE TABLE IF NOT EXISTS Goods (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "manufacturer TEXT NOT NULL, " +
                    "production_date TEXT NOT NULL, " +
                    "model TEXT NOT NULL, " +
                    "purchase_price REAL NOT NULL, " +
                    "retail_price REAL NOT NULL, " +
                    "quantity INTEGER NOT NULL)";
            conn.createStatement().execute(sql);
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
    public static void createTableIfNotExists() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "CREATE TABLE IF NOT EXISTS UsersData (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL, " +
                    "user_level TEXT NOT NULL, " +
                    "registration_date TEXT NOT NULL, " +
                    "total_spent REAL NOT NULL, " +
                    "phone_number TEXT NOT NULL, " +
                    "email TEXT NOT NULL)";
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void CreateHotGoodsDatabase() {
        Connection conn = null;

        try {
            // 连接到数据库
            conn = DriverManager.getConnection(DATABASE_URL);

            if (conn != null) {


                // 创建商品表
                Statement statement = conn.createStatement();
                String createTableSQL = "CREATE TABLE IF NOT EXISTS HotGoods (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "productName TEXT NOT NULL," +
                        "purchaseCount INTEGER DEFAULT 0" +
                        ");";
                statement.executeUpdate(createTableSQL);


            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public static void createRequestTable() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS Request (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL)";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createAndShowGUI() {

        mainFrame = new JFrame("购物商城页面");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(300, 250); // 增加高度以容纳标题

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("YNU购物商城"); // 添加标题标签
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24)); // 标题字体样式
        titleLabel.setForeground(new Color(36, 95, 220)); // 设置标题标签的前景颜色
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel); // 将标题标签添加到面板中

        JButton loginButton = createStyledButton("登录");
        JButton registerButton = createStyledButton("注册");
        JButton exitButton = createStyledButton("退出");

        loginButton.addActionListener(e -> openLoginPage());
        registerButton.addActionListener(e -> openRegisterPage());
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(Box.createVerticalStrut(20));
        panel.add(loginButton);
        panel.add(registerButton);
        panel.add(Box.createVerticalStrut(20));
        panel.add(exitButton);

        mainFrame.getContentPane().add(panel, BorderLayout.CENTER);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }



    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 16)); // 使用微软雅黑字体
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 152, 219));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private static void openLoginPage() {
        mainFrame.dispose(); // Close the main frame
        Login loginPage = new Login();
        loginPage.showLoginPage();
    }

    private static void openRegisterPage() {
        mainFrame.dispose(); // 关闭当前主页面
        Register registerPage = new Register();
        registerPage.showRegisterPage();

    }

}
