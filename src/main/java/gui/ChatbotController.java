package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.MedicamentService;

public class ChatbotController {
    @FXML private TextArea chatArea;
    @FXML private TextField userInput;
    private final MedicamentService ms = new MedicamentService();

    @FXML public void initialize() {
        chatArea.appendText("🤖 Chatbot : Bonjour ! Je suis votre assistant PharmaPays.\n");
        chatArea.appendText("Comment puis-je vous aider aujourd’hui ? 💊\n\n");
    }

    @FXML public void handleSend() {
        String message = userInput.getText().trim();
        if (message.isEmpty()) return;
        chatArea.appendText("👤 Vous : " + message + "\n");
        String lower = message.toLowerCase();
        if (lower.contains("liste") || lower.contains("medic")) {
            StringBuilder sb = new StringBuilder();
            for (var m : ms.getAll()) sb.append(m.getNom()).append(" (stock: ").append(m.getStock()).append(")\n");
            chatArea.appendText("🤖 Chatbot : Voici les médicaments disponibles:\n" + sb.toString() + "\n");
        } else {
            chatArea.appendText("🤖 Chatbot : Je n'ai pas compris, essayez 'liste' ou 'commander'.\n\n");
        }
        userInput.clear();
    }

    @FXML public void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ClientView.fxml"));
            Stage stage = (Stage) chatArea.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (java.io.IOException | RuntimeException e) { 
            System.err.println("Error loading ClientView: " + e.getMessage());
        }
    }
}
