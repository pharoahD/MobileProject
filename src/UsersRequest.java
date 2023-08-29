import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class UsersRequest{
    private static final String DATABASE_URL = "jdbc:sqlite:D:\\mobile\\identifier.sqlite";
    private JFrame frame;
    private DefaultListModel<String> requestListModel;
    private JList<String> requestList;

    public UsersRequest() {
        initializeComponents();
        populateRequestList();
    }

    private void initializeComponents() {
        frame = new JFrame("请求处理");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        requestListModel = new DefaultListModel<>();
        requestList = new JList<>(requestListModel);
        JScrollPane scrollPane = new JScrollPane(requestList);

        JButton processButton = new JButton("处理选定请求");
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedUsername = requestList.getSelectedValue();
                if (selectedUsername != null) {
                    processRequest(selectedUsername);
                    requestListModel.removeElement(selectedUsername);
                }
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(processButton, BorderLayout.SOUTH);

        frame.add(panel);
    }

    private void populateRequestList() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String selectSql = "SELECT username FROM Request";
            PreparedStatement stmt = conn.prepareStatement(selectSql);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                requestListModel.addElement(username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processRequest(String username) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String updateSql = "UPDATE Users SET password = ? WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(updateSql);
            String hashedPassword = hashSHA256("ynu123456");
            stmt.setString(1, hashedPassword); // Hash the default password
            stmt.setString(2, username);
            stmt.executeUpdate();

            String deleteSql = "DELETE FROM Request WHERE username = ?";
            stmt = conn.prepareStatement(deleteSql);
            stmt.setString(1, username);
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

    public void insertRequest(String username) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String insertSql = "INSERT INTO Request (username) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(insertSql);
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void showRequestProcessingPage() {
        frame.setVisible(true);
    }
}