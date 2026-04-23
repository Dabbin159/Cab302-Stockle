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
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlFile));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().clear();
        stage.setScene(scene);
    }

    public static Stage getStage() {
        return stage;
    }
}
