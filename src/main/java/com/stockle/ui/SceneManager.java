package com.stockle.ui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage stage;

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void switchTo(String fxmlFile) throws IOException {
        double width = stage.getWidth();
        double height = stage.getHeight();
        double x = stage.getX();
        double y = stage.getY();

        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlFile));
        
        Scene scene = new Scene(loader.load(), width, height);

        stage.setScene(scene);

        javafx.application.Platform.runLater(() -> {
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setX(x);
            stage.setY(y);
        });
    }

    public static Stage getStage() {
        return stage;
    }
}
