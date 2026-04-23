package com.stockle.ui;

import com.stockle.SessionManager;
import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    //Storing
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final SQLUserDAO userDAO = SQLUserDAO.getInstance();

    @FXML
    protected void onLoginButtonClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()){
            errorLabel.setText("Please fill in all sections");
            return;
        }

        User user = userDAO.login(username, password);

        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            navigateTo("dashboard-view.fxml", 1280, 720);
        }
        else {
            errorLabel.setText("Inavlid username or password. ");
            passwordField.clear();
        }
    }

    @FXML
    protected void onSignUpLinkClick() {
        navigateTo("Signup-view.fxml", 480, 600);
    }

    private void navigateTo(String fxml, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), width, height));
        } catch (IOException) {
            e.printStackTrace();
        }
    }
}
