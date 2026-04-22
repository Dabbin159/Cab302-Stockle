package com.stockle.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class HomeController {
    
    @FXML
    protected void goToAI(javafx.event.ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        
        FXMLLoader fxmlLoader = new FXMLLoader(StockleApplication.class.getResource("ai-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 400);
        stage.setScene(scene);
    }
}
