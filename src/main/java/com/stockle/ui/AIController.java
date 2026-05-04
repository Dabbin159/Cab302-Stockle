package com.stockle.ui;

import java.io.IOException;

import com.stockle.SessionManager;
import com.stockle.api.GroqService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Handles the AI chat screen.
 *
 * Takes care of sending messages, showing replies, and wiring up the simple
 * navigation buttons on the page.
 */
public class AIController {

    @FXML
    private VBox chatBox;

    @FXML
    private TextField userInput;

    @FXML
    private ImageView darkModeIcon;

    private final GroqService groqService = new GroqService();

    @FXML
    private void initialize() {
        syncThemeButton();
    }

    /**
     * Sends the user's message to the AI and shows both messages in the chat.
     *
     * If the input box is empty, nothing is sent.
     */
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

    /**
     * Fills the input box with one of the suggested prompts.
     *
     * This lets the user click a prompt instead of typing it out.
     *
     * @param event the button press event from the suggestion chip
     */
    @FXML
    protected void handleSuggest(ActionEvent event) {
        userInput.setText(((Button) event.getSource()).getText());
        userInput.requestFocus();
    }

    /**
     * Shows the placeholder analysis message for now.
     *
     * The full analysis flow can be swapped in later.
     */
    @FXML
    protected void handleAnalyse() {
        addMessage("AI: Analysis feature coming soon!");
    }

    @FXML
    private void toggleDarkMode() {
        if (chatBox == null || chatBox.getScene() == null) {
            return;
        }

        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setDarkModeEnabled(!sessionManager.isDarkModeEnabled());
        SceneManager.applyTheme(chatBox.getScene().getRoot());
        syncThemeButton();
    }

    /**
     * Adds a chat bubble to the conversation area.
     *
     * Messages that start with "You: " are styled as user messages. Everything
     * else is treated as an AI reply.
     *
     * @param text the message text to display
     */
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

    private void syncThemeButton() {
        if (darkModeIcon == null) {
            return;
        }

        boolean darkModeEnabled = SessionManager.getInstance().isDarkModeEnabled();
        String iconPath = darkModeEnabled
            ? "/com/stockle/ui/images/light-mode-button.png"
            : "/com/stockle/ui/images/dark-mode-button.png";

        java.net.URL iconUrl = getClass().getResource(iconPath);
        if (iconUrl != null) {
            darkModeIcon.setImage(new Image(iconUrl.toExternalForm()));
        }
    }

    // Navigation

    /**
     * Opens the dashboard screen.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    @FXML private void navDashboard() throws IOException {
        SceneManager.switchTo("dashboard/dashboard-view.fxml");
    }

    /**
     * Opens the trading screen.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    @FXML private void navTrading() throws IOException {
        SceneManager.switchTo("trading/trading-view.fxml");
    }

    /**
     * Sends the user back to the sign-in screen.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    @FXML private void handleSignOut() throws IOException {
        SceneManager.switchTo("auth/auth-view.fxml");
    }
}
