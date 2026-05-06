package com.stockle.ui;

import java.io.IOException;

import com.stockle.SessionManager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage stage;

    static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void switchTo(String fxmlFile) throws IOException {
        double width = stage.getWidth();
        double height = stage.getHeight();
        double x = stage.getX();
        double y = stage.getY();

        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlFile));
        Parent root = loader.load();
        Scene scene = new Scene(root, width, height);

        applyTheme(root);

        stage.setScene(scene);

        javafx.application.Platform.runLater(() -> {
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setX(x);
            stage.setY(y);
        });
    }

    static void applyTheme(Parent root) {
        if (root == null) {
            return;
        }

        boolean darkModeEnabled = SessionManager.getInstance().isDarkModeEnabled();
        root.getStyleClass().remove("dark-theme");
        root.getStyleClass().remove("dark-mode");

        if (darkModeEnabled) {
            if (root.getStyleClass().contains("auth-root")) {
                root.getStyleClass().add("dark-mode");
            } else {
                root.getStyleClass().add("dark-theme");
            }
        }
    }
}
