
package gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.UserService;
import utils.AlertUtils;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    private final UserService userService = new UserService();

    @FXML public void initialize() { roleComboBox.getItems().addAll("client","professionnel"); }

    @FXML public void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();
        if (username.isEmpty() || password.isEmpty() || role == null) { AlertUtils.showError("Erreur","Veuillez remplir tous les champs !"); return; }
        if (role.equals("professionnel") && !userService.hasProof(username)) role = "client";
        boolean ok = userService.inscrire(username,password,role);
        if (ok) AlertUtils.showInfo("Succès","Compte créé avec succès !"); else AlertUtils.showError("Erreur","Impossible de créer le compte.");
    }
}
