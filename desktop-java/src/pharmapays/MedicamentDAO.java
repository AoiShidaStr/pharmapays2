package pharmapays;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Couche d'accès aux données (DAO) pour les médicaments.
 * Utilise des requêtes préparées (PreparedStatement) pour la sécurité SQL.
 * Appelle les procédures stockées de la base pharmapays quand disponibles.
 */
public class MedicamentDAO {

    /**
     * Récupère tous les médicaments avec leur catégorie (jointure).
     */
    public List<Medicament> findAll() throws SQLException {
        List<Medicament> liste = new ArrayList<>();
        String sql = "SELECT m.id, m.nom, m.description, m.prix, m.stock, "
                   + "m.ordonnance_requise, m.categorie_id, c.nom AS categorie_nom "
                   + "FROM medicaments m "
                   + "LEFT JOIN categories c ON m.categorie_id = c.id "
                   + "ORDER BY m.nom";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    /**
     * Recherche des médicaments par nom (LIKE).
     */
    public List<Medicament> search(String terme) throws SQLException {
        List<Medicament> liste = new ArrayList<>();
        String sql = "SELECT m.id, m.nom, m.description, m.prix, m.stock, "
                   + "m.ordonnance_requise, m.categorie_id, c.nom AS categorie_nom "
                   + "FROM medicaments m "
                   + "LEFT JOIN categories c ON m.categorie_id = c.id "
                   + "WHERE m.nom LIKE ? OR m.description LIKE ? "
                   + "ORDER BY m.nom";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + terme + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapRow(rs));
                }
            }
        }
        return liste;
    }

    /**
     * Récupère un médicament par son ID.
     */
    public Medicament findById(int id) throws SQLException {
        String sql = "SELECT m.id, m.nom, m.description, m.prix, m.stock, "
                   + "m.ordonnance_requise, m.categorie_id, c.nom AS categorie_nom "
                   + "FROM medicaments m "
                   + "LEFT JOIN categories c ON m.categorie_id = c.id "
                   + "WHERE m.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Ajoute un nouveau médicament dans la base.
     */
    public boolean create(Medicament med) throws SQLException {
        String sql = "INSERT INTO medicaments (nom, description, prix, stock, ordonnance_requise, categorie_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, med.getNom());
            ps.setString(2, med.getDescription());
            ps.setDouble(3, med.getPrix());
            ps.setInt(4, med.getStock());
            ps.setBoolean(5, med.isOrdonnanceRequise());
            ps.setInt(6, med.getCategorieId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        med.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Met à jour un médicament existant.
     * Utilise la procédure stockée sp_maj_stock pour le stock.
     */
    public boolean update(Medicament med) throws SQLException {
        String sql = "UPDATE medicaments SET nom = ?, description = ?, prix = ?, "
                   + "stock = ?, ordonnance_requise = ?, categorie_id = ? "
                   + "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, med.getNom());
            ps.setString(2, med.getDescription());
            ps.setDouble(3, med.getPrix());
            ps.setInt(4, med.getStock());
            ps.setBoolean(5, med.isOrdonnanceRequise());
            ps.setInt(6, med.getCategorieId());
            ps.setInt(7, med.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Supprime un médicament par son ID.
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM medicaments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Récupère toutes les catégories pour le formulaire.
     */
    public List<String[]> findAllCategories() throws SQLException {
        List<String[]> categories = new ArrayList<>();
        String sql = "SELECT id, nom FROM categories ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("nom")
                });
            }
        }
        return categories;
    }

    /**
     * Appelle la procédure stockée sp_maj_stock.
     */
    public boolean majStock(int medicamentId, int nouvelleQuantite) throws SQLException {
        String sql = "{CALL sp_maj_stock(?, ?, ?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, medicamentId);
            cs.setInt(2, nouvelleQuantite);
            cs.setString(3, "Mise à jour manuelle (desktop)");
            cs.execute();
            return true;
        }
    }

    /**
     * Utilise la vue v_catalogue pour afficher le catalogue.
     */
    public List<Medicament> findFromCatalogue() throws SQLException {
        List<Medicament> liste = new ArrayList<>();
        String sql = "SELECT * FROM v_catalogue";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Medicament med = new Medicament();
                med.setId(rs.getInt("id"));
                med.setNom(rs.getString("nom"));
                med.setDescription(rs.getString("description"));
                med.setPrix(rs.getDouble("prix"));
                med.setStock(rs.getInt("stock"));
                med.setOrdonnanceRequise(rs.getBoolean("ordonnance_requise"));
                // La vue peut avoir un nom de colonne différent pour la catégorie
                try {
                    med.setCategorie(rs.getString("categorie"));
                } catch (SQLException e) {
                    med.setCategorie("N/A");
                }
                liste.add(med);
            }
        }
        return liste;
    }

    /**
     * Mappe une ligne ResultSet vers un objet Medicament.
     */
    private Medicament mapRow(ResultSet rs) throws SQLException {
        return new Medicament(
            rs.getInt("id"),
            rs.getString("nom"),
            rs.getString("description"),
            rs.getDouble("prix"),
            rs.getInt("stock"),
            rs.getBoolean("ordonnance_requise"),
            rs.getString("categorie_nom"),
            rs.getInt("categorie_id")
        );
    }
}
