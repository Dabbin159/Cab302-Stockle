package com.stockle.ui;

import java.io.IOException;

import com.stockle.SessionManager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Manages scene transitions and themes
 */
public class SceneManager {

    private static Stage stage;

    /**
     * Initialises scene manager
     * @param primaryStage the primary stage of the application
     */
    static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    /**
     * Switches to a new scene defined by the given FXML file
     * @param fxmlFile the path to the FXML file
     * @throws IOException if an I/O error occurs
     */
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

    /**
     * Applies the current theme
     * @param root the root node of the scene to apply the theme to
     */
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
