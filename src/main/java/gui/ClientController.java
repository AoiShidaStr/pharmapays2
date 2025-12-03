
package gui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import models.Medicament;
import services.MedicamentService;
import utils.AlertUtils;
import utils.SessionManager;

public class ClientController {
    @FXML private ListView<String> medsList;
    @FXML private Label welcomeLabel;
    private final MedicamentService ms = new MedicamentService();

    @FXML public void initialize() {
        if (SessionManager.getCurrentUser()!=null) welcomeLabel.setText("Bienvenue, " + SessionManager.getCurrentUser().getUsername());
        refreshList();
    }

    private void refreshList() {
        medsList.getItems().clear();
        for (Medicament m : ms.getAll()) medsList.getItems().add(m.getId() + ": " + m.getNom() + " - " + m.getPrix() + "€ (" + m.getStock() + ")");
    }

    @FXML public void handleChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Chatbot.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Chatbot PharmaPays");
            stage.show();
        } catch (IOException e) { 
            System.err.println("Error loading Chatbot: " + e.getMessage());
        }
    }

    @FXML public void handleCommander() {
        AlertUtils.showInfo("Commande","Fonction de commande (UI complète à implémenter)"); 
    }

    @FXML public void handleLogout() {
        try {
            SessionManager.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException | NullPointerException e) { 
            System.err.println("Error loading LoginView: " + e.getMessage());
        }
    }
}
