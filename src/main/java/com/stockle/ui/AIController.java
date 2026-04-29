package com.stockle.ui;

import java.io.IOException;

import com.stockle.api.GroqService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AIController {

    @FXML
    private VBox chatBox;

    @FXML
    private TextField userInput;

    private GroqService groqService = new GroqService();

    @FXML
    protected void handleSend() {
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
    protected void handleSuggest(ActionEvent event) {
        userInput.setText(((Button) event.getSource()).getText());
        userInput.requestFocus();
    }

    @FXML
    protected void handleAnalyse() {
        addMessage("AI: Analysis feature coming soon!");
    }

    private void addMessage(String text) {
        boolean isUser = text.startsWith("You: ");
        String body = isUser ? text.substring(5) : text.substring(4);

        Label msg = new Label(body);
        msg.setWrapText(true);
        msg.getStyleClass().add(isUser ? "user-msg-text" : "ai-msg-text");

        VBox bubble = new VBox(msg);
        bubble.getStyleClass().add(isUser ? "user-bubble" : "ai-bubble");

        if (isUser) {
            StackPane avatar = new StackPane(new Label("U"));
            avatar.getStyleClass().add("user-avatar");
            avatar.getChildren().get(0).getStyleClass().add("user-avatar-lbl");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(8, spacer, bubble, avatar);
            row.setAlignment(Pos.TOP_RIGHT);
            chatBox.getChildren().add(row);
        } else {
            StackPane avatar = new StackPane(new Label("AI"));
            avatar.getStyleClass().add("bot-avatar");
            avatar.getChildren().get(0).getStyleClass().add("bot-avatar-lbl");

            HBox row = new HBox(8, avatar, bubble);
            row.setAlignment(Pos.TOP_LEFT);
            chatBox.getChildren().add(row);
        }
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
