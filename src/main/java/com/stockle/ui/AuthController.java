package com.stockle.ui;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.SessionManager;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.Asset;
import com.stockle.api.service.AssetService;
import com.stockle.api.service.MarketDataService;
import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;


/**
 * Controller for the authentication screen
 * Handles both the login and signup functionality
 */
public class AuthController {
    // UI Fields
    @FXML private StackPane authRoot;
    @FXML private ImageView darkModeIcon;
    @FXML private ImageView stockleIcon;
 
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
 
    // Loading fields
    @FXML private StackPane loadingOverlay;
    @FXML private VBox spinnerContainer;
    @FXML private Label loadingLabel;
 
    // The Tab Pane containing login and sign up tabs
    @FXML private TabPane authTabPane;
 
    /* Database access for user operations */
    private final SQLUserDAO userDAO = SQLUserDAO.getInstance();
 
    private ApiClient apiClient;
    private ObjectMapper objectMapper;
    private AssetService assetService;
    private MFXProgressSpinner progressSpinner;

    /**
     * Called Automatically by JavaFX when Authentication screen loads.
     * Attaches a listener to clear stale error messages when the user switches tabs.
     */
    @FXML
    public void initialize() {
        syncThemeButton();
        loginEmail.setText("admin@admin.admin");
        loginPasswordField.setText("Admin123");
        loginPasswordText.setText("Admin123");

        this.apiClient = new ApiClient();
        this.objectMapper = new ObjectMapper();
        MarketDataService marketDataService = new MarketDataService(apiClient, objectMapper);
        this.assetService = new AssetService(apiClient, objectMapper, marketDataService);

        authTabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldTab, newTab) -> {
                    // Clears both the error labels when tabs switch
                    loginErrorLabel.setText("");
                    signupErrorLabel.setText("");
                }
        );
    }

    @FXML private void toggleDarkMode() {
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setDarkModeEnabled(!sessionManager.isDarkModeEnabled());
        SceneManager.applyTheme(authRoot);
        syncThemeButton();
    }

    /** Handles user login: validates fields, checks credentials
     * against the database then navigates to the dashboard if
     * successful
     */
    @FXML
    protected void handleLogin() throws IOException {
        String username = loginEmail.getText().trim();
        String password = loginPasswordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            loginErrorLabel.setText("Please fill in all sections");
            return;
        }

        // Show loading screen
        showLoading();

        // Check credentials in background thread
        new Thread(() -> {
            User user = userDAO.login(username, password);

            if (user == null) {
                // Credentials failed - hide loading and show error
                Platform.runLater(() -> {
                    hideLoading();
                    loginErrorLabel.setText("Invalid username or password");
                    loginPasswordField.clear();
                });
            } else {
                // Credentials correct - continue loading for asset preload
                Platform.runLater(() -> {
                    loginErrorLabel.setText(""); // Clear any previous errors
                });

                // Preload assets in background
                SessionManager.getInstance().setCurrentUser(user);
                    
                if (assetService != null) {
                    List<Asset> assets = assetService.getAllAssets();
                    SessionManager.getInstance().setCachedAssets(assets);
                }
                
                // Asset loading complete - navigate to dashboard
                Platform.runLater(() -> {
                    try {
                        hideLoading();
                        SceneManager.applyTheme(authRoot);
                        SceneManager.switchTo("dashboard/dashboard-view.fxml");
                    } catch (IOException e) {
                        System.err.println("Failed to navigate to dashboard: " + e.getMessage());
                        hideLoading();
                    }
                });
            }
        }).start();
    }

    private void showLoading() {

        loadingOverlay.setVisible(true);
        loadingOverlay.setManaged(true);

        progressSpinner = new MFXProgressSpinner();

        progressSpinner.setPrefSize(70, 70);
        progressSpinner.setRadius(28);

        progressSpinner.setColor1(javafx.scene.paint.Color.web("#FFFFFF"));
        progressSpinner.setColor2(javafx.scene.paint.Color.web("#FFFFFF"));
        progressSpinner.setColor3(javafx.scene.paint.Color.web("#FFFFFF"));
        progressSpinner.setColor4(javafx.scene.paint.Color.web("#FFFFFF"));

        spinnerContainer.getChildren().clear();
        spinnerContainer.getChildren().add(progressSpinner);
    }

    private void hideLoading() {
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);
        
        if (spinnerContainer != null) {
            spinnerContainer.getChildren().clear();
        }
        progressSpinner = null;
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
            signupErrorLabel.setText("Signup failed. Please fill all the sections. ");
            return;
        }
        else
        {
            boolean success = userDAO.signup(username, password,
                    email, fullName, dateOfBirth);

            if (success) {
                User user = userDAO.login(email, password);
                SessionManager.getInstance().setCurrentUser(user);
                try {
                    SceneManager.applyTheme(authRoot);
                    SceneManager.switchTo("dashboard/dashboard-view.fxml");
                } catch (IOException e) {
                    signupErrorLabel.setText("Error navigating to dashboard");
                }
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

        // Update the main Stockle logo to match the active theme
        if (stockleIcon != null) {
            String logoPath = darkModeEnabled
                ? "/com/stockle/ui/images/stockle-icon-dark-mode.png"
                : "/com/stockle/ui/images/stockle-icon-light-mode.png";
            java.net.URL logoUrl = getClass().getResource(logoPath);
            if (logoUrl != null) {
                stockleIcon.setImage(new Image(logoUrl.toExternalForm()));
            }
        }
    }
}
