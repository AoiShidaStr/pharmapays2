
package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.User;
import services.UserService;
import utils.AlertUtils;
import utils.SessionManager;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        User user = userService.authentifier(username, password);
        if (user == null) { AlertUtils.showError("Connexion échouée","Identifiants invalides !"); return; }
        SessionManager.setCurrentUser(user);
        try {
            FXMLLoader loader;
            Stage stage = (Stage) usernameField.getScene().getWindow();
            if (user.getRole().equalsIgnoreCase("professionnel")) {
                loader = new FXMLLoader(getClass().getResource("/views/ProfessionnelView.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/views/ClientView.fxml"));
            }
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (java.io.IOException | NullPointerException e) {
            AlertUtils.showError("Erreur","Impossible de charger la vue : " + e.getMessage());
        }
    }

    @FXML public void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegisterView.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (java.io.IOException | NullPointerException e) {
            AlertUtils.showError("Erreur","Impossible d’ouvrir la page d’inscription");
        }
    }
}
