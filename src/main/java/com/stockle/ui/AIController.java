package com.stockle.ui;

import java.io.IOException;

import com.stockle.api.GroqService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class AIController {

    @FXML
    private VBox chatBox;
    
    @FXML
    private TextField userInput;

    private GroqService groqService = new GroqService();

    @FXML
    protected void handleSend()  {
        String input = userInput.getText();
        if (input.isEmpty()) {
            return;
        }
        
        addMessage("You: " + input);

        String response = groqService.askChatbot(input);
        addMessage("AI: " + response);

        userInput.clear();
    }

    @FXML
    protected void handleAnalyse() {
        addMessage("AI: Analysis feature coming soon!");
    }

    private void addMessage(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        chatBox.getChildren().add(label);
    }

    // Navigation

    @FXML private void navDashboard() throws IOException {
        SceneManager.switchTo("dashboard/dashboard-view.fxml");
    }

    @FXML private void navTrading() throws IOException {
        SceneManager.switchTo("trading/trading-view.fxml");
    }

    @FXML private void handleSignOut() throws IOException {
        SceneManager.switchTo("auth/auth-view.fxml");
    }
}
