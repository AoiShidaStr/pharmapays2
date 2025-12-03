
package gui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Medicament;
import services.MedicamentService;
import utils.AlertUtils;

public class ProfessionnelController {
    @FXML private ListView<String> medsList;
    @FXML private TextField nomField;
    @FXML private TextField formeField;
    @FXML private TextField voieField;
    @FXML private TextField dateAutField;
    @FXML private TextField prixField;
    @FXML private javafx.scene.control.CheckBox generiqueCheckBox;

    private final MedicamentService ms = new MedicamentService();

    @FXML public void initialize() { refresh(); }

    private void refresh() {
        medsList.getItems().clear();
        for (Medicament m : ms.getAll()) {
            medsList.getItems().add(m.getId() + ": " + m.getNom() + " (" + m.getForme() + "/" + m.getVoie() + ") - " + m.getPrix() + "€");
        }
    }

    @FXML public void handleAddMedicament() {
        try {
            String nom = nomField.getText().trim();
            String forme = formeField.getText().trim();
            String voie = voieField.getText().trim();
            String dateAut = dateAutField.getText().trim();
            double prix = Double.parseDouble(prixField.getText().trim());
            boolean generique = generiqueCheckBox.isSelected();
            
            boolean ok = ms.ajouter(new Medicament(nom, forme, voie, dateAut, generique, prix));
            if (ok) { 
                AlertUtils.showInfo("Succès","Médicament ajouté"); 
                refresh(); 
                nomField.clear();
                formeField.clear();
                voieField.clear();
                dateAutField.clear();
                prixField.clear();
                generiqueCheckBox.setSelected(false);
            } else {
                AlertUtils.showError("Erreur","Ajout impossible");
            }
        } catch (NumberFormatException e) { 
            AlertUtils.showError("Erreur","Veuillez vérifier le champ prix (nombre)");
        } catch (Exception e) { 
            AlertUtils.showError("Erreur","Erreur lors de l'ajout du médicament");
        }
    }

    @FXML public void handleModifyStock() {
        AlertUtils.showInfo("Information","Fonction de modification de stock via UI simplifiée"); 
    }

    @FXML public void handleVendre() {
        AlertUtils.showInfo("Information","Fonction de vente à implémenter via interface spécialisée"); 
    }

    @FXML public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Stage stage = (Stage) medsList.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) { 
            System.err.println("Error loading LoginView: " + e.getMessage());
        }
    }
}
