
package models;

public class Commande {
    private int id;
    private String client;
    private Medicament medicament;
    private int quantite;
    private String date;

    public Commande(int id, String client, Medicament medicament, int quantite, String date) {
        this.id = id; this.client = client; this.medicament = medicament; this.quantite = quantite; this.date = date;
    }
    public Commande(String client, Medicament medicament, int quantite, String date) {
        this(-1, client, medicament, quantite, date);
    }
    public int getId(){return id;}
    public String getClient(){return client;}
    public Medicament getMedicament(){return medicament;}
    public int getQuantite(){return quantite;}
    public String getDate(){return date;}
}
