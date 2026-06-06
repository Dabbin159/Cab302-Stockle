module com.stockle {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
   
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.json;
    requires jbcrypt;
    requires MaterialFX;

    opens com.stockle to javafx.fxml;
    opens com.stockle.api to javafx.fxml;
    opens com.stockle.api.client to javafx.fxml;
    opens com.stockle.ui to javafx.fxml;
    opens com.stockle.model to javafx.fxml;
    opens com.stockle.database to javafx.fxml;
    opens com.stockle.updater to javafx.fxml;
    opens com.stockle.api.data to com.fasterxml.jackson.databind;

    exports com.stockle;
    exports com.stockle.ui;
    exports com.stockle.model;
    exports com.stockle.api;
    exports com.stockle.api.client;
    exports com.stockle.api.data;
    exports com.stockle.api.service;
    exports com.stockle.api.apiTests;
    exports com.stockle.database;
    exports com.stockle.updater;
}