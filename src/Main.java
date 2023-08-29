import javax.swing.*;
import java.awt.*;

public class Main {
    private static JFrame mainFrame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
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
