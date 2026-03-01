module com.example.pi_dev {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    // requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires transitive java.sql;
    requires java.base;
    requires java.desktop; // For ImageIO, AWT
    requires javafx.swing; // For SwingFXUtils
    requires jjwt.api; // JWT API
    requires com.fasterxml.jackson.databind;
    requires java.mail;
    requires webcam.capture;
    requires googleauth;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires jdk.jsobject; // For JavaScript interop in WebView
    requires itextpdf;     // For PDF Export

    opens com.example.pi_dev to javafx.fxml;


    // User Module Opens/Exports
    opens com.example.pi_dev.user.models to javafx.base;

    exports com.example.pi_dev.user.models;
    exports com.example.pi_dev.user.services;
    exports com.example.pi_dev.user.enums;
    exports com.example.pi_dev.user.controllers;
    exports com.example.pi_dev.user.database;

    opens com.example.pi_dev.user.controllers to javafx.fxml;

    exports com.example.pi_dev.common.services;




    exports com.example.pi_dev;

}
