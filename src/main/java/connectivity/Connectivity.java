package connectivity;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connectivity {
    private Connection connection;

    public Connection getConnection() {
//        String dbName = "expenses_tracker_db";
//        String dbUserName = "root";
//        String dbPassWord = System.getenv("MYSQL_DB_PASSWORD");
        try {
//            Class.forName("com.mysql.jdbc.Driver");
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite::resource:db.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }

//    public static void main(String[] args) {
//        Connectivity connectivity = new Connectivity();
//        Connection connection = connectivity.getConnection();
//        System.out.println(connection);
//    }
}
