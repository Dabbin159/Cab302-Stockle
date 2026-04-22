package com.stockle.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import com.stockle.api.GeminiService;
import com.stockle.model.TradeContext;

public class AIController {
    @FXML
    private VBox chatBox;
    
    @FXML
    private TextField userInput;

    private GeminiService geminiService = new GeminiService();

    @FXML
    protected void handleSend()  {
        String input = userInput.getText();
        if (input.isEmpty()) {
            return;
        }
        
        addMessage("You: " + input);

        if (!isStockRelated(input)) {
            addMessage("AI: Please ask about stock-related topics.");
            return;
        }

        String response = geminiService.askChatbot(input);
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

    private boolean isStockRelated(String input) {
        String lowerInput = input.toLowerCase();
        return lowerInput.contains("stock") || lowerInput.contains("market") || lowerInput.contains("trade");
    }

    @FXML
    protected void goHome(javafx.event.ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        
        FXMLLoader fxmlLoader = new FXMLLoader(StockleApplication.class.getResource("home-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 400);
        stage.setScene(scene);
    }
}
