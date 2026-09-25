module com.codecanvas {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires java.net.http;
    requires org.json;

    opens com.codecanvas to javafx.fxml;
    opens com.codecanvas.controller to javafx.fxml;
    opens com.codecanvas.model to javafx.base;

    exports com.codecanvas;
    exports com.codecanvas.model;
    exports com.codecanvas.controller;
    exports com.codecanvas.db;
    exports com.codecanvas.api;
    exports com.codecanvas.navigation;
    exports com.codecanvas.util;
}
