import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CartHistory {
    private static final String DATABASE_URL = "jdbc:sqlite:D:\\mobile\\identifier.sqlite";

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
            System.out.println("CartHistory table created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
