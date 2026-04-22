package com.stockle.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthController {

    @FXML private PasswordField loginPasswordField;
    @FXML private TextField loginPasswordText;
    @FXML private PasswordField signupPasswordField;
    @FXML private TextField signupPasswordText;

    @FXML
    private void handleLogin() {
        try {
            SceneManager.switchTo("dashboard-view.fxml");
        } catch (IOException e) {
            showAlert("Could not load dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignup() {
        try {
            SceneManager.switchTo("dashboard-view.fxml");
        } catch (IOException e) {
            showAlert("Could not load dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void toggleLoginPassword() {
        toggle(loginPasswordField, loginPasswordText);
    }

    @FXML
    private void toggleSignupPassword() {
        toggle(signupPasswordField, signupPasswordText);
    }

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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Stockle");
        alert.showAndWait();
    }
}
