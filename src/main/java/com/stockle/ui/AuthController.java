package com.stockle.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthController {
    @FXML private PasswordField loginPasswordField;
    @FXML private TextField loginPasswordText;
    @FXML private PasswordField signupPasswordField;
    @FXML private TextField signupPasswordText;

    @FXML
    private void handleLogin() throws IOException {
        SceneManager.switchTo("dashboard/dashboard-view.fxml");
    }

    @FXML
    private void handleSignup() throws IOException {
        SceneManager.switchTo("dashboard/dashboard-view.fxml");
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
}
