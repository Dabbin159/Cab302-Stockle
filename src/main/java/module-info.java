module com.stockle {
    requires javafx.controls;
    requires javafx.fxml;
   
    requires java.net.http;

    opens com.stockle.api to javafx.fxml;
    opens com.stockle.ui to javafx.fxml;
    opens com.stockle.model to javafx.fxml;
    
    exports com.stockle.ui;
    exports com.stockle.model;
    exports com.stockle.api;
}
