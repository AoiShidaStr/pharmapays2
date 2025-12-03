
package models;

public class Medicament {
    private int id;
    private String nom;
    // New fields per DB schema
    private String forme;
    private String voie;
    private String dateAutorisation;
    private boolean generique;
    private double prix;
    // Deprecated/optional legacy field kept for compatibility
    private Integer stock;

    /**
     * New constructor matching the MySQL `medicament` table:
     * (id, nom, forme, voie, date_autorisation, generique, prix)
     */
    public Medicament(int id, String nom, String forme, String voie, String dateAutorisation, boolean generique, double prix) {
        this.id = id;
        this.nom = nom;
        this.forme = forme;
        this.voie = voie;
        this.dateAutorisation = dateAutorisation;
        this.generique = generique;
        this.prix = prix;
        this.stock = null;
    }

    /**
     * Convenience constructor without id (to create new entries).
     */
    public Medicament(String nom, String forme, String voie, String dateAutorisation, boolean generique, double prix) {
        this(-1, nom, forme, voie, dateAutorisation, generique, prix);
    }

    /**
     * Legacy constructor kept for backward compatibility with existing code that
     * still uses (id, nom, prix, stock). Prefer the new constructors above.
     */
    public Medicament(int id, String nom, double prix, int stock) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.stock = stock;
        this.forme = null;
        this.voie = null;
        this.dateAutorisation = null;
        this.generique = false;
    }

    public Medicament(String nom, double prix, int stock) {
        this(-1, nom, prix, stock);
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getForme() { return forme; }
    public String getVoie() { return voie; }
    public String getDateAutorisation() { return dateAutorisation; }
    public boolean isGenerique() { return generique; }
    public double getPrix() { return prix; }
    // stock is optional now; may be null if using new schema
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
