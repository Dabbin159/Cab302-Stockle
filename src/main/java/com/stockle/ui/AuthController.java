package com.stockle.ui;

import java.io.IOException;
import java.time.LocalDate;

import com.stockle.SessionManager;
import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TabPane;


/**
 * Controller for the authentication screen
 * Handles both the login and signup functionality
 */
public class AuthController {
    // UI Fields
    @FXML private StackPane authRoot;
    @FXML private ImageView darkModeIcon;

    // Login Fields
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPasswordField;
    @FXML private TextField loginPasswordText;
    @FXML private Label loginErrorLabel;

    // Sign up Fields
    @FXML private TextField signupUsername;
    @FXML private TextField signupName;
    @FXML private TextField signupEmail;
    @FXML private PasswordField signupPasswordField;
    @FXML private PasswordField signupConfirmPassword;
    @FXML private TextField signupPasswordText;
    @FXML private DatePicker signupDateOfBirth;
    @FXML private Label signupErrorLabel;

    // The Tab Pane containing login and sing up tabs
    @FXML private TabPane authTabPane;

    /* Database access for user operations */
    private final SQLUserDAO userDAO = SQLUserDAO.getInstance();

    @FXML private void initialize() {
        SceneManager.applyTheme(authRoot);
        syncThemeButton();
    }

    @FXML private void toggleDarkMode() {
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setDarkModeEnabled(!sessionManager.isDarkModeEnabled());
        SceneManager.applyTheme(authRoot);
        syncThemeButton();
    /**
     * Called Automatically by JavaFX when Authentication screen loads.
     * Attaches a listener to clear stale error messages when the user switches tabs.
     */
    @FXML
    public void initialize() {

        authTabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldTab, newTab) -> {
                    // Clears both the error labels when tabs switch
                    loginErrorLabel.setText("");
                    signupErrorLabel.setText("");
                }
        );
    }

    /** Handles user login: validates fields, checks credentials
     * against the database then navigates to the dashboard if
     * successful
     */
    @FXML
    protected void handleLogin() throws IOException {
        String username =
                loginEmail.getText().trim();
        String password =
                loginPasswordField.getText();
        if (username.isEmpty() || password.isEmpty()){
            loginErrorLabel.setText("Please fill in all sections");
            return;
        }

        User user = userDAO.login(username, password);

        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            SceneManager.applyTheme(authRoot);
            SceneManager.switchTo("dashboard/dashboard-" +
                    "view.fxml");
        }
            else {
            loginErrorLabel.setText("Invalid username or password");
            loginPasswordField.clear();
        }
    }

    /** Handles Signup: validates fields, checks credentials
     * against the database then navigates to the dashboard if
     * successful NOT COMPLETE YET
     */
    @FXML
    protected void handleSignup() throws IOException {
        String fullName =
                signupName.getText().trim();
        String email =
                signupEmail.getText().trim();
        String username =
                signupUsername.getText().trim();
        String password =
                signupPasswordField.getText();
        String confirmPassword =
                signupConfirmPassword.getText();
        LocalDate dateOfBirth =
                signupDateOfBirth.getValue();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty())
        {
            signupErrorLabel.setText("Please fill in all sections");
            return;
        }
        else
        {
            boolean success = userDAO.signup(username, password,
                    email, fullName, dateOfBirth);

            if (success)
            {
                User user = userDAO.login(username, password);
                SessionManager.getInstance().setCurrentUser(user);
                SceneManager.applyTheme(authRoot);
                SceneManager.switchTo("dashboard/dashboard-view.fxml");
            }
            else
            {
                signupErrorLabel.setText("Signup failed. Username may be taken. ");
            }
        }
    }

    /**
     * Toggles the login password field between hidden and visible text
     */
    @FXML
    private void toggleLoginPassword() {
        toggle(loginPasswordField, loginPasswordText);
    }

    /**
     * Toggles the sign up password field between hidden and visible text
     */
    @FXML
    private void toggleSignupPassword() {
        toggle(signupPasswordField, signupPasswordText);
    }

    /**
     * Placeholder for forgotten password functionality
     */
    @FXML
    private void forgotPassword() {
    }

    private void toggle(PasswordField masked, TextField plain) {
        if (masked.isVisible()) {
            plain.setText(masked.getText());
            masked.setVisible(false);
            masked.setManaged(false);
            plain.setVisible(true);
            plain.setManaged(true);
            plain.requestFocus();
            plain.end();
        } else {
            masked.setText(plain.getText());
            plain.setVisible(false);
            plain.setManaged(false);
            masked.setVisible(true);
            masked.setManaged(true);
            masked.requestFocus();
            masked.end();
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

        darkModeIcon.setStyle(darkModeEnabled ? "-fx-effect: coloradjust(0, 0, 0.8, 0);" : "");
    }
}
