package com.stockle.ui;

import com.stockle.SessionManager;
import com.stockle.database.PasswordUtils;
import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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

    // Database Access
    private final SQLUserDAO userDAO = SQLUserDAO.getInstance();

    /**
     * Called Automatically when the profile screen loads
     * Pre fills the fields with the current user's information
     */
    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            usernameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
        }
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
            feedbackLabel.setText("Username and email cannot be empty.");
            return;
        }

        // If a new password is entered it validates to see if passwords match
        if (!newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
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

        feedbackLabel.setText("Account updated successfully.");
    }

    /**
     * Navigates back to the dashboard screen
     */
    @FXML
    protected void handleBack() throws java.io.IOException {
        SceneManager.switchTo("dashboard/dashboard-view.fxml");
    }
}
