package com.stockle.ui;

import java.io.IOException;

import com.stockle.api.GroqService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

    @FXML
    protected void goHome(javafx.event.ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        
        FXMLLoader fxmlLoader = new FXMLLoader(StockleApplication.class.getResource("home-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 400);
        stage.setScene(scene);
    }
}
