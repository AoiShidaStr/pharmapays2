package pharmapays;

/**
 * Classe métier représentant un médicament.
 * Correspond à la table 'medicaments' de la base pharmapays.
 */
public class Medicament {

    private int id;
    private String nom;
    private String description;
    private double prix;
    private int stock;
    private boolean ordonnanceRequise;
    private String categorie;
    private int categorieId;

    public Medicament() {}

    public Medicament(int id, String nom, String description, double prix,
                      int stock, boolean ordonnanceRequise, String categorie, int categorieId) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.ordonnanceRequise = ordonnanceRequise;
        this.categorie = categorie;
        this.categorieId = categorieId;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public double getPrix() { return prix; }
    public int getStock() { return stock; }
    public boolean isOrdonnanceRequise() { return ordonnanceRequise; }
    public String getCategorie() { return categorie; }
    public int getCategorieId() { return categorieId; }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setDescription(String description) { this.description = description; }
    public void setPrix(double prix) { this.prix = prix; }
    public void setStock(int stock) { this.stock = stock; }
    public void setOrdonnanceRequise(boolean ordonnanceRequise) { this.ordonnanceRequise = ordonnanceRequise; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }

    @Override
    public String toString() {
        return nom + " (" + prix + " EUR) - Stock: " + stock;
    }
}
