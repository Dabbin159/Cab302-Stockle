package com.stockle.ui;

import java.io.IOException;

import javafx.fxml.FXML;

public class DashboardController {

    @FXML
    private void handleSignOut() throws IOException {
        SceneManager.switchTo("auth/auth-view.fxml");
    }
}
