module com.example.labs {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    requires java.sql;

    opens com.example.labs.client to javafx.fxml;
    exports com.example.labs.client;

    exports com.example.labs.common;
    exports com.example.labs.common.model;
    exports com.example.labs.common.patterns.command;
    exports com.example.labs.common.patterns.iterator;
    exports com.example.labs.common.patterns.memento;
    exports com.example.labs.common.patterns.visitor;
}