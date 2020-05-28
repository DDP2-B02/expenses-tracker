package connectivity;

import java.sql.Connection;
import java.sql.DriverManager;

public class Connectivity {
    private Connection connection;
    public Connection getConnection() {
        String dbName = "expenses_tracker_db";
        String dbUserName = "root";
        String dbPassWord = "";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/"+dbName,dbUserName,dbPassWord);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }
}
