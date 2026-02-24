package com.example.pi_dev.venue;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class VenueMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database schema
        com.example.pi_dev.venue.utils.SchemaInitializer.initialize();

        FXMLLoader fxmlLoader = new FXMLLoader(
                VenueMain.class.getResource("/com/example/pi_dev/venue/views/home-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setTitle("Venue Renting - Find Your Perfect Space");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
