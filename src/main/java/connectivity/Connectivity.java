package connectivity;

import java.sql.Connection;
import java.sql.DriverManager;

public class Connectivity {
    private Connection connection;

    /*
    * Method to get connection to sqlite database
    * */
    public Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite::resource:db.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }
}
