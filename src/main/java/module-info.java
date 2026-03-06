module com.stockle.stockle {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.stockle.stockle to javafx.fxml;
    exports com.stockle.stockle;
}