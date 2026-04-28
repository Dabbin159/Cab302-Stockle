package com.stockle.ui;

import java.io.IOException;
import java.time.LocalDate;

import com.stockle.SessionManager;
import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;

public class AuthController {

    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPasswordField;
    @FXML private TextField loginPasswordText;
    @FXML private TextField signupUsername;
    @FXML private TextField signupName;
    @FXML private TextField signupEmail;
    @FXML private PasswordField signupPasswordField;
    @FXML private PasswordField signupConfirmPassword;
    @FXML private TextField signupPasswordText;
    @FXML private DatePicker signupDateOfBirth;
    @FXML private Label loginErrorLabel;
    private final SQLUserDAO userDAO = SQLUserDAO.getInstance();


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

        // Splitting the fullName Field
        String[] nameParts = fullName.split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty())
        {
            loginErrorLabel.setText("Please fill in all sections");
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
                SceneManager.switchTo("dashboard/dashboard-view.fxml");
            }
            else
            {
                loginErrorLabel.setText("Signup failed. Username may be taken. ");
            }
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
}
