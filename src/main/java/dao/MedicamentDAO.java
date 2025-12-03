package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;
import models.Medicament;

public class MedicamentDAO {

    public List<Medicament> findAll() {
        List<Medicament> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT id, nom, forme, voie, date_autorisation, generique, prix FROM medicaments")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Medicament(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("forme"),
                    rs.getString("voie"),
                    rs.getString("date_autorisation"),
                    rs.getBoolean("generique"),
                    rs.getDouble("prix")
                ));
            }
        } catch (Exception e) { 
            System.err.println("Error finding all medicaments: " + e.getMessage());
        }
        return list;
    }

    public Medicament findById(int id) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT id, nom, forme, voie, date_autorisation, generique, prix FROM medicaments WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Medicament(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("forme"),
                    rs.getString("voie"),
                    rs.getString("date_autorisation"),
                    rs.getBoolean("generique"),
                    rs.getDouble("prix")
                );
            }
        } catch (Exception e) { 
            System.err.println("Error finding medicament by id: " + e.getMessage());
        }
        return null;
    }

    public boolean save(Medicament m) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO medicaments(nom, forme, voie, date_autorisation, generique, prix) VALUES(?,?,?,?,?,?)")) {
            ps.setString(1, m.getNom());
            ps.setString(2, m.getForme());
            ps.setString(3, m.getVoie());
            ps.setString(4, m.getDateAutorisation());
            ps.setBoolean(5, m.isGenerique());
            ps.setDouble(6, m.getPrix());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { 
            System.err.println("Error saving medicament: " + e.getMessage());
        }
        return false;
    }

    public boolean updateMedicament(int id, Medicament m) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE medicaments SET nom=?, forme=?, voie=?, date_autorisation=?, generique=?, prix=? WHERE id=?")) {
            ps.setString(1, m.getNom());
            ps.setString(2, m.getForme());
            ps.setString(3, m.getVoie());
            ps.setString(4, m.getDateAutorisation());
            ps.setBoolean(5, m.isGenerique());
            ps.setDouble(6, m.getPrix());
            ps.setInt(7, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { 
            System.err.println("Error updating medicament: " + e.getMessage());
        }
        return false;
    }
    public boolean updateStock(int medicamentId, int newStock) {
        // Implementation to update the stock of a medicament
        // Return true if update was successful, false otherwise
        return true;
    }
}
