module com.example.dbcinema {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens com.example.dbcinema to javafx.fxml;
    exports com.example.dbcinema;
    exports com.example.dbcinema.controllers;
    opens com.example.dbcinema.controllers to javafx.fxml;
}