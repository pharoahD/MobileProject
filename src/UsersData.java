import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UsersData {
    private JFrame frame;
    private JTable userTable;
    private DefaultTableModel tableModel;

    private static final String DATABASE_URL = "jdbc:sqlite:D:\\mobile\\identifier.sqlite";

    public UsersData() {
        initializeComponents();
        createTableIfNotExists();
        updateTableData();
    }

    private void initializeComponents() {
        frame = new JFrame("用户信息管理");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        tableModel = new DefaultTableModel();
        userTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        frame.add(panel);
    }

    private void createTableIfNotExists() {
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

    private void updateTableData() {
        tableModel.setColumnIdentifiers(new String[]{
                "ID", "用户名", "用户级别", "注册日期", "累计消费金额", "手机号", "邮箱"
        });

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT * FROM UsersData";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int userId = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String userLevel = resultSet.getString("user_level");
                String registrationDate = resultSet.getString("registration_date");
                double totalSpent = resultSet.getDouble("total_spent");
                String phoneNumber = resultSet.getString("phone_number");
                String email = resultSet.getString("email");

                tableModel.addRow(new Object[]{
                        userId, username, userLevel, registrationDate, totalSpent, phoneNumber, email
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void showUserInfoPage() {
        frame.setVisible(true);
    }
    public void showFunctionMenu() {
        JFrame menuFrame = new JFrame("功能菜单");
        menuFrame.setSize(300, 250); // 稍微增加一点高度以适应标题
        menuFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        // 添加大标题
        JLabel titleLabel = new JLabel("客户管理");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 24)); // 设置大标题字体和大小
        titleLabel.setHorizontalAlignment(JLabel.CENTER); // 居中对齐
        GridBagConstraints titleConstraints = new GridBagConstraints();
        titleConstraints.gridx = 0;
        titleConstraints.gridy = 0;
        titleConstraints.gridwidth = 1;
        titleConstraints.insets = new Insets(20, 0, 30, 0); // 设置上下间距
        panel.add(titleLabel, titleConstraints);

        // 创建并美化按钮
        JButton listAllUsersButton = createStyledButton("列出所有用户信息");
        JButton deleteUserButton = createStyledButton("  删除用户信息  ");
        JButton searchUserButton = createStyledButton("  搜索用户信息  ");
        listAllUsersButton.addActionListener(e -> openListAllUsersPage());
        deleteUserButton.addActionListener(e -> openDeleteUserPage());
        searchUserButton.addActionListener(e -> openSearchUserPage());
        // 添加按钮到面板
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 1;
        buttonConstraints.gridwidth = 1;
        buttonConstraints.insets = new Insets(10, 0, 10, 0); // 设置按钮间距
        panel.add(listAllUsersButton, buttonConstraints);
        buttonConstraints.gridy = 2;
        panel.add(deleteUserButton, buttonConstraints);
        buttonConstraints.gridy = 3;
        panel.add(searchUserButton, buttonConstraints);

        menuFrame.add(panel);
        menuFrame.setVisible(true);
    }

    private JButton createStyledButton(String buttonText) {
        JButton button = new JButton(buttonText);
        button.setFont(new Font("宋体", Font.PLAIN, 16)); // 设置按钮字体和大小
        button.setForeground(Color.WHITE); // 设置文字颜色
        button.setBackground(new Color(70, 130, 180)); // 设置背景颜色
        button.setFocusPainted(false); // 去除按钮点击时的焦点框
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // 设置边框
        button.setPreferredSize(new Dimension(200, 40)); // 设置按钮大小
        return button;
    }

    private void openListAllUsersPage() {
        JFrame userListFrame = new JFrame("所有用户信息");
        userListFrame.setSize(800, 600);

        JTable userTable = new JTable(tableModel); // 使用之前定义的 tableModel
        JScrollPane scrollPane = new JScrollPane(userTable);

        userListFrame.add(scrollPane);
        userListFrame.setVisible(true);
    }


    private void openDeleteUserPage() {
        String userIdInput = JOptionPane.showInputDialog(frame, "请输入要删除的用户ID:");

        if (userIdInput != null) {
            try {
                int userId = Integer.parseInt(userIdInput);

                if (isUserExists(userId)) {
                    int option = JOptionPane.showConfirmDialog(frame, "确认要删除用户ID为 " + userId + " 的用户信息吗？", "确认删除", JOptionPane.YES_NO_OPTION);

                    if (option == JOptionPane.YES_OPTION) {
                        deleteUser(userId);
                        updateTableData();
                        JOptionPane.showMessageDialog(frame, "用户信息已删除。");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "该用户不存在。");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "无效的用户ID。");
            }
        }
    }

    private boolean isUserExists(int userId) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT COUNT(*) FROM UsersData WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void deleteUser(int userId) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "DELETE FROM UsersData WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void openSearchUserPage() {
        String keyword = JOptionPane.showInputDialog(frame, "请输入要搜索的用户名或ID：");
        if (keyword != null) {
            searchUser(keyword);
        }
    }

    private void searchUser(String keyword) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sql = "SELECT * FROM UsersData WHERE id = ? OR username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            // 判断输入的关键字是数字（ID）还是字符串（用户名）
            if (isNumeric(keyword)) {
                stmt.setInt(1, Integer.parseInt(keyword));
                stmt.setString(2, "");
            } else {
                stmt.setInt(1, 0);
                stmt.setString(2, keyword);
            }

            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                int userId = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String userLevel = resultSet.getString("user_level");
                String registrationDate = resultSet.getString("registration_date");
                double totalSpent = resultSet.getDouble("total_spent");
                String phoneNumber = resultSet.getString("phone_number");
                String email = resultSet.getString("email");

                // 创建一个新的窗口来显示搜索结果
                JFrame searchResultFrame = new JFrame("搜索结果");
                searchResultFrame.setSize(400, 300);

                JPanel panel = new JPanel(new GridLayout(7, 2));

                panel.add(new JLabel("ID:"));
                panel.add(new JLabel(String.valueOf(userId)));

                panel.add(new JLabel("用户名:"));
                panel.add(new JLabel(username));

                panel.add(new JLabel("用户级别:"));
                panel.add(new JLabel(userLevel));

                panel.add(new JLabel("注册日期:"));
                panel.add(new JLabel(registrationDate));

                panel.add(new JLabel("累计消费金额:"));
                panel.add(new JLabel(String.valueOf(totalSpent)));

                panel.add(new JLabel("手机号:"));
                panel.add(new JLabel(phoneNumber));

                panel.add(new JLabel("邮箱:"));
                panel.add(new JLabel(email));

                searchResultFrame.add(panel);
                searchResultFrame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(frame, "未找到匹配的用户信息。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }





}
