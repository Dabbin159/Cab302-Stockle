package com.stockle.ui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage stage;
    private static boolean isMaximized = true;


    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void switchTo(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlFile));

        boolean wasMaximized = stage.isMaximized();
        double width = stage.getWidth();
        double height = stage.getHeight();
        
        Scene scene = new Scene(loader.load(), width, height);

        stage.setScene(scene);

        if (wasMaximized) {
            javafx.application.Platform.runLater(() -> stage.setMaximized(true));
        } else {
            stage.setWidth(width);
            stage.setHeight(height);
        }
    }

    public static Stage getStage() {
        return stage;
    }
}
