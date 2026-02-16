package com.example.pi_dev.venue.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class BaseController {

    protected void navigateTo(Node sourceNode, String fxmlPath) {
        try {
            // Handle both relative (to venue) and absolute paths
            String path = fxmlPath;
            if (!fxmlPath.startsWith("/")) {
                path = "/com/example/pi_dev/venue/views/" + fxmlPath;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML: " + fxmlPath);
        }
    }

    protected void navigateToLogin(Node sourceNode) {
        // Navigate to the existing User module's login view
        // Assuming the path based on project structure convention
        navigateTo(sourceNode, "/com/example/pi_dev/user/login.fxml");
    }

    protected void navigateToRegister(Node sourceNode) {
        navigateTo(sourceNode, "/com/example/pi_dev/user/signup.fxml");
    }
}
