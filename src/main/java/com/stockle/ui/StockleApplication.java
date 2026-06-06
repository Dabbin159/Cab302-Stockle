package com.stockle.ui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
/**
 * The StockleApplication class is the main entry point for the Stockle application. It extends the JavaFX Application class and is responsible for initializing the application with the launcher.
 */
public class StockleApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.init(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(StockleApplication.class.getResource("auth/auth-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        SceneManager.applyTheme(scene.getRoot());
        stage.setTitle("Stockle");
        stage.setScene(scene);
        stage.setWidth(1400);
        stage.setHeight(900);
        stage.show();
    }
}