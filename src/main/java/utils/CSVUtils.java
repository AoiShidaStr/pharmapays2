
package utils;

import java.io.FileWriter;
import java.util.List;
import java.util.logging.Logger;

import models.Commande;
public class CSVUtils {
    private static final Logger logger = Logger.getLogger(CSVUtils.class.getName());
    
    public static boolean exportCommandes(List<Commande> commandes, String path) {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write("id,client,medicament,quantite,date\n");
            for (Commande c : commandes) {
                fw.write(c.getId() + "," + c.getClient() + "," + c.getMedicament().getNom() + "," + c.getQuantite() + "," + c.getDate() + "\n");
            }
            return true;
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Error exporting commandes: {0}", e.getMessage());
        }
        return false;
    }
}
