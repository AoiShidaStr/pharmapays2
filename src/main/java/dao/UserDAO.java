
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.DatabaseConnection;
import models.User;

public class UserDAO {

    public User findByUsername(String username) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT username,password,role FROM users WHERE username=?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("username"), rs.getString("password"), rs.getString("role"));
            }
        } catch (Exception e) { 
            System.err.println("Error finding user: " + e.getMessage());
        }
        return null;
    }

    public boolean save(User user) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO users(username,password,role) VALUES(?,?,?)")) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { 
            System.err.println("Error saving user: " + e.getMessage());
        }
        return false;
    }
}
