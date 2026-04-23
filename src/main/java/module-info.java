module com.stockle {
    requires javafx.controls;
    requires javafx.fxml;
   
    requires java.net.http;
    requires okhttp3;
    requires com.fasterxml.jackson.databind;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.json;
    requires jbcrypt;

    opens com.stockle.api to javafx.fxml;
    opens com.stockle.ui to javafx.fxml;
    opens com.stockle.model to javafx.fxml;
    
    exports com.stockle.ui;
    exports com.stockle.model;
    exports com.stockle.api;
}