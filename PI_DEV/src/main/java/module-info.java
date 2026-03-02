module com.example.pi_dev {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;

    // Java standard modules
    requires java.sql;
    requires java.desktop;
    requires java.net.http;
    requires java.mail;
    requires java.prefs;
    requires java.management;
    requires java.logging;
    requires java.naming;
    requires java.xml;

    // JavaFX UI libraries
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    // QR Code libraries
    requires com.google.zxing;
    requires com.google.zxing.javase;

    // JSON processing
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;

// JWT libraries – the JAR names are jjwt-api, jjwt-impl, jjwt-jackson
    requires jjwt.api;
    requires jjwt.impl;
    requires jjwt.jackson;

// Webcam capture – the JAR is webcam-capture
    requires webcam.capture;

// Audio/Media libraries
    requires jaudiotagger;        // JAR: jaudiotagger-2.0.3.jar
    requires mp3spi;
    // Cloudinary (if used)
    requires cloudinary.http44;
    requires cloudinary.core;

    // iText PDF (if used)
    requires kernel;
    requires layout;

    // Test dependencies
    requires static org.junit.jupiter.api;
    requires static org.testng;
    requires googleauth;
    requires jdk.jsobject;
    requires itextpdf;
    requires io;
    requires freetts;

    // Open the entire module for FXGL and other reflective libraries
    opens com.example.pi_dev to javafx.fxml, com.almasb.fxgl.all;

    // Messaging module - FULLY ADDED from your original
    opens com.example.pi_dev.messaging.messagecontroller to javafx.fxml;
    opens com.example.pi_dev.messaging.messagingmodel to javafx.base;
    opens com.example.pi_dev.messaging.messagingrepository to javafx.base;
    opens com.example.pi_dev.messaging.messagingservice to javafx.base;
    opens com.example.pi_dev.messaging.messagingsession to javafx.base;

    exports com.example.pi_dev.messaging.messagecontroller;
    exports com.example.pi_dev.messaging.messagingmodel;
    exports com.example.pi_dev.messaging.messagingrepository;
    exports com.example.pi_dev.messaging.messagingservice;
    exports com.example.pi_dev.messaging.messagingsession;

    // User module
    opens com.example.pi_dev.user.controllers to javafx.fxml;
    opens com.example.pi_dev.user.models to javafx.base;
    opens com.example.pi_dev.user.database to javafx.base;
    opens com.example.pi_dev.user.repositories to javafx.base;
    opens com.example.pi_dev.user.services to javafx.base;
    opens com.example.pi_dev.user.enums to javafx.base;
    opens com.example.pi_dev.user.utils to javafx.base;

    // Main layout module
    opens com.example.pi_dev.main.controllers to javafx.fxml;
    exports com.example.pi_dev.main.controllers;




    // Common module
    opens com.example.pi_dev.common.services to javafx.base;
    opens com.example.pi_dev.common.models to javafx.base;

    // Exports
    exports com.example.pi_dev;

    // User exports
    exports com.example.pi_dev.user.models;
    exports com.example.pi_dev.user.services;
    exports com.example.pi_dev.user.enums;
    exports com.example.pi_dev.user.controllers;
    exports com.example.pi_dev.user.database;
    exports com.example.pi_dev.user.repositories;
    exports com.example.pi_dev.user.utils;



    // Events exports
    opens com.example.pi_dev.events.Controllers to javafx.fxml;
    exports com.example.pi_dev.events.Controllers;
    exports com.example.pi_dev.events.Entities;
    exports com.example.pi_dev.events.Services;
    exports com.example.pi_dev.events.Iservices;
    exports com.example.pi_dev.events.Utils;

    // Blog exports
    opens com.example.pi_dev.blog.Controllers to javafx.fxml;
    exports com.example.pi_dev.blog.Controllers;
    exports com.example.pi_dev.blog.Entities;
    exports com.example.pi_dev.blog.Services;
    exports com.example.pi_dev.blog.GUI;

    //booking exports
    exports com.example.pi_dev.booking.test to javafx.graphics;
    opens   com.example.pi_dev.booking.test to javafx.fxml;

    // Booking module exports and opens
    opens com.example.pi_dev.booking.Controllers to javafx.fxml;
    exports com.example.pi_dev.booking.Controllers;

    // ADDED: Host package opens and exports
    opens com.example.pi_dev.booking.Controllers.Host to javafx.fxml;
    exports com.example.pi_dev.booking.Controllers.Host;

    // Booking controllers - open ALL subpackages
    opens com.example.pi_dev.booking.Controllers.Front to javafx.fxml;
    opens com.example.pi_dev.booking.Controllers.Admin to javafx.fxml; // if exists

    // Duplicate opens entry removed: com.example.pi_dev.events.Controllers

// Export them as needed
    exports com.example.pi_dev.booking.Controllers.Front;
    exports com.example.pi_dev.common.services;
    exports com.example.pi_dev.common.models;

    //marketplace
    exports com.example.pi_dev.marketplace.test to javafx.graphics;
    opens   com.example.pi_dev.marketplace.test to javafx.fxml;
    opens   com.example.pi_dev.marketplace.Controllers to javafx.fxml;
    exports com.example.pi_dev.marketplace.Controllers;
}