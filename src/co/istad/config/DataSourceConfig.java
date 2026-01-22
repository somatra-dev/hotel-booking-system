package co.istad.config;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DataSourceConfig {

    private static Connection conn;
    public static Connection getConnection(){
        return conn;
    }
    public static void init() {
        if (conn == null) {
            String url = "jdbc:postgresql://localhost:5255/hotel";
            Properties info = new Properties();
            info.put("user", "hotel");
            info.put("password", "hotel@168");

            try {
                conn = DriverManager.getConnection(url, info);
            } catch (SQLException e) {
                System.out.println("Connection Failed!" + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}