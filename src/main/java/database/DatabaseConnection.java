
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Connect to the 'PharmaPays' schema on the MySQL server.
    private static final String URL = "jdbc:mysql://172.16.0.101:3306/PharmaPays?useSSL=false&serverTimezone=UTC";
    private static final String USER = "PharmaPays";
    private static final String PASSWORD = "user";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données MySQL : " + e.getMessage());
            return null;
        }
    }
}