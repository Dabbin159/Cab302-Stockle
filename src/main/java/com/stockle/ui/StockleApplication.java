package com.stockle.ui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
// random test comment
public class StockleApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.init(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(StockleApplication.class.getResource("auth/auth-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Stockle");
        stage.setScene(scene);
        stage.setWidth(1400);
        stage.setHeight(900);
        stage.show();
    }
}