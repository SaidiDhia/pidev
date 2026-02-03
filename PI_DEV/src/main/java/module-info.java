module com.example.pi_dev {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;


    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    //requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.base;  // Contient java.util

    opens com.example.pi_dev to javafx.fxml;
    opens com.example.pi_dev.messagecontroller to javafx.fxml;
    opens com.example.pi_dev.model to javafx.base;
    exports com.example.pi_dev;

}