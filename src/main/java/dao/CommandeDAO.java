
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;
import models.Commande;
import models.Medicament;

public class CommandeDAO {

    public boolean save(String client, int medicamentId, int quantite, String date) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO commandes(client,medicament_id,quantite,date) VALUES(?,?,?,?)")) {
            ps.setString(1, client);
            ps.setInt(2, medicamentId);
            ps.setInt(3, quantite);
            ps.setString(4, date);
            return ps.executeUpdate()>0;
        } catch (Exception e) { 
            System.err.println("Error saving commande: " + e.getMessage());
        }
        return false;
    }

    public List<Commande> findByClient(String client) {
        List<Commande> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT co.id, co.client, co.quantite, co.date, m.id as mid, m.nom, m.forme, m.voie, m.date_autorisation, m.generique, m.prix " +
                 "FROM commandes co JOIN medicaments m ON co.medicament_id = m.id WHERE co.client=? ORDER BY co.date DESC")) {
            ps.setString(1, client);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Medicament m = new Medicament(
                    rs.getInt("mid"),
                    rs.getString("nom"),
                    rs.getString("forme"),
                    rs.getString("voie"),
                    rs.getString("date_autorisation"),
                    rs.getBoolean("generique"),
                    rs.getDouble("prix")
                );
                list.add(new Commande(rs.getInt("id"), rs.getString("client"), m, rs.getInt("quantite"), rs.getString("date")));
            }
        } catch (Exception e) { 
            System.err.println("Error finding commandes by client: " + e.getMessage());
        }
        return list;
    }
}
