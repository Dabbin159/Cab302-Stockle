package com.stockle.ui;

import com.stockle.SessionManager;
import com.stockle.database.PasswordUtils;
import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

/**
 * Controller for the profile/account settings
 * Allows a logged-in User to update their username, email, and password.
 */
public class ProfileController {

    // Input fields for editable user information
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // Label to display success or error message
    @FXML private Label feedbackLabel;
    @FXML private javafx.scene.image.ImageView darkModeIcon;

    // Database Access
    private final SQLUserDAO userDAO = SQLUserDAO.getInstance();

    /**
     * Called Automatically when the profile screen loads
     * Pre fills the fields with the current user's information
     */
    @FXML
    public void initialize() {
        syncThemeButton();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            usernameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
        }
    }

    @FXML
    private void toggleDarkMode() {
        if (usernameField == null || usernameField.getScene() == null) return;
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setDarkModeEnabled(!sessionManager.isDarkModeEnabled());
        SceneManager.applyTheme(usernameField.getScene().getRoot());
        syncThemeButton();
    }

    private void syncThemeButton() {
        if (darkModeIcon == null) return;
        boolean dark = SessionManager.getInstance().isDarkModeEnabled();
        String iconPath = dark
            ? "/com/stockle/ui/images/light-mode-button.png"
            : "/com/stockle/ui/images/dark-mode-button.png";
        java.net.URL url = getClass().getResource(iconPath);
        if (url != null) darkModeIcon.setImage(new javafx.scene.image.Image(url.toExternalForm()));
    }

    /**
     * Handles saving the updated information on the profile
     * Validates the inputs, Hashes the new password if its provided
     * updates the database and refreshes the screen
     */
    @FXML
    protected void handleSave() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            feedbackLabel.setText("No user is currently logged in");
            return;
        }

        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validating that the username and password are not empty
        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            feedbackLabel.getStyleClass().setAll("feedback-label", "feedback-label-error");
            feedbackLabel.setText("Username and email cannot be empty.");
            return;
        }

        // If a new password is entered it validates to see if passwords match
        if (!newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                feedbackLabel.getStyleClass().setAll("feedback-label", "feedback-label-error");
                feedbackLabel.setText("The passwords entered do not match");
                return;
            }
            // Hashing the new password before it saves
            String hashedPassword = PasswordUtils.hashPassword(newPassword);
            currentUser.setPassword((hashedPassword));
        }

        // Updating the User object with the new values
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);

        // Sends the changes to the database
        userDAO.updateUser(currentUser);

        // Update the session so the rest of the application reflects the changes
        SessionManager.getInstance().setCurrentUser(currentUser);

        feedbackLabel.getStyleClass().setAll("feedback-label");
        feedbackLabel.setText("Account updated successfully.");
    }

    /**
     * Navigates back to the dashboard screen
     */
    @FXML
    protected void handleBack() throws IOException {
        SceneManager.switchTo("dashboard/dashboard-view.fxml");
    }

    @FXML private void navTrading()  throws IOException { SceneManager.switchTo("trading/trading-view.fxml"); }
    @FXML private void navAI()       throws IOException { SceneManager.switchTo("ai/ai-view.fxml"); }
    @FXML private void navNews()     throws IOException { SceneManager.switchTo("news/news-view.fxml"); }

    @FXML
    private void handleSignOut() throws IOException {
        SessionManager.getInstance().logout();
        SceneManager.switchTo("auth/auth-view.fxml");
    }
}
