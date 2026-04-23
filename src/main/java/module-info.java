module com.stockle {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.json;
    requires jbcrypt;

    opens com.stockle to javafx.fxml;
    opens com.stockle.ui to javafx.fxml;
    opens com.stockle.model to javafx.fxml;
    
    exports com.stockle;
    exports com.stockle.ui;
    exports com.stockle.model;
    exports com.stockle.api;
}