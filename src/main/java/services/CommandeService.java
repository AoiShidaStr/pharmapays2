
package services;

import dao.CommandeDAO;
import dao.MedicamentDAO;
import models.Medicament;
import java.time.LocalDate;

public class CommandeService {
    private final CommandeDAO dao = new CommandeDAO();
    private final MedicamentDAO mdao = new MedicamentDAO();

    public boolean passerCommande(String client, int medicamentId, int quantite) {
        Medicament m = mdao.findById(medicamentId);
        if (m==null || m.getStock() < quantite) return false;
        int nouveau = m.getStock() - quantite;
        if (!mdao.updateStock(medicamentId, nouveau)) return false;
        return dao.save(client, medicamentId, quantite, LocalDate.now().toString());
    }
}
