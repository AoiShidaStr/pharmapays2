package pharmapays;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe de connexion JDBC à la base de données MySQL pharmapays.
 * Utilise le pattern Singleton pour partager une connexion unique.
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/pharmapays?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    private DatabaseConnection() {}

    /**
     * Retourne la connexion unique à la base de données.
     * Crée la connexion si elle n'existe pas ou si elle est fermée.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                // Force l'encodage UTF-8 pour les caractères accentués
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("SET NAMES utf8mb4");
                }
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL introuvable : " + e.getMessage());
            }
        }
        return connection;
    }

    /**
     * Ferme proprement la connexion.
     */
    public static void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Erreur fermeture connexion : " + e.getMessage());
            }
        }
    }
}
