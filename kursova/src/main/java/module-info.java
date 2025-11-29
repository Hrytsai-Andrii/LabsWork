module com.example.kursova {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    requires java.sql;

    opens com.example.kursova.client to javafx.fxml;
    exports com.example.kursova.client;

    exports com.example.kursova.common;
    exports com.example.kursova.common.model;
    exports com.example.kursova.common.patterns.command;
    exports com.example.kursova.common.patterns.iterator;
    exports com.example.kursova.common.patterns.memento;
    exports com.example.kursova.common.patterns.visitor;
}