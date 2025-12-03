
package services;

import dao.MedicamentDAO;
import models.Medicament;
import java.util.List;

public class MedicamentService {
    private final MedicamentDAO dao = new MedicamentDAO();
    public List<Medicament> getAll() { return dao.findAll(); }
    public boolean ajouter(Medicament m) { return dao.save(m); }
    public boolean modifier(int id, Medicament m) { return dao.updateMedicament(id, m); }
    public Medicament findById(int id){ return dao.findById(id); }
}
