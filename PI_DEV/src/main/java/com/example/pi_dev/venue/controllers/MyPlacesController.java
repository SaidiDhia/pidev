package com.example.pi_dev.venue.controllers;

import com.example.pi_dev.venue.dao.PlaceDAO;
import com.example.pi_dev.venue.entities.Place;
import com.example.pi_dev.user.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class MyPlacesController implements Initializable {

    @FXML
    private FlowPane placesContainer;

    private PlaceDAO placeDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        placeDAO = new PlaceDAO();
        loadMyPlaces();
    }

    private void loadMyPlaces() {
        if (!UserSession.getInstance().isLoggedIn()) {
            showEmptyMessage("Please log in to view your places.");
            return;
        }

        String hostId = UserSession.getInstance().getCurrentUser().getUserId().toString();

        try {
            List<Place> myPlaces = placeDAO.findByHost(hostId);
            displayPlaces(myPlaces);
        } catch (SQLException e) {
            e.printStackTrace();
            showEmptyMessage("Error loading your places.");
        }
    }

    private void displayPlaces(List<Place> places) {
        placesContainer.getChildren().clear();

        if (places.isEmpty()) {
            showEmptyMessage("You haven't created any places yet. Click 'Become a Host' to get started!");
            return;
        }

        for (Place place : places) {
            placesContainer.getChildren().add(createPlaceCard(place));
        }
    }

    private void showEmptyMessage(String message) {
        placesContainer.getChildren().clear();
        Label emptyLabel = new Label(message);
        emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
        placesContainer.getChildren().add(emptyLabel);
    }

    private VBox createPlaceCard(Place place) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-background-radius: 12;");
        card.setPrefWidth(280);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(260);
        imageView.setFitHeight(180);
        imageView.setStyle("-fx-background-color: #eee;");

        if (place.getImageUrl() != null && !place.getImageUrl().isEmpty()) {
            try {
                String imageUrl = place.getImageUrl();
                // Handle relative local paths from DB
                if (!imageUrl.startsWith("http") && !imageUrl.startsWith("file:") && !imageUrl.startsWith("jar:")) {
                    java.io.File file = new java.io.File(imageUrl);
                    if (file.exists()) {
                        imageUrl = file.toURI().toString();
                    } else {
                        // Try as resource
                        java.net.URL resource = getClass().getResource(imageUrl);
                        if (resource != null) {
                            imageUrl = resource.toExternalForm();
                        }
                    }
                }
                Image image = new Image(imageUrl, 260, 180, true, true, true);
                imageView.setImage(image);
            } catch (Exception e) {
                // Keep placeholder
            }
        }

        Label title = new Label(place.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        title.setWrapText(true);

        Label price = new Label("$" + place.getPricePerDay() + " / day");
        price.setStyle("-fx-font-size: 14px; -fx-text-fill: #4F46E5; -fx-font-weight: bold;");

        Label status = new Label("Status: " + place.getStatus());
        status.setStyle("-fx-text-fill: " + getStatusColor(place.getStatus()) + "; -fx-font-weight: bold;");

        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-padding: 8 16;");
        editButton.setOnAction(e -> handleEdit(place));

        card.getChildren().addAll(imageView, title, price, status, editButton);
        return card;
    }

    private String getStatusColor(Place.Status status) {
        switch (status) {
            case APPROVED:
                return "#22c55e";
            case PENDING:
                return "#f59e0b";
            case DENIED:
                return "#ef4444";
            default:
                return "#666";
        }
    }

    private void handleEdit(Place place) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/pi_dev/venue/views/add-place-view.fxml"));
            javafx.scene.Parent root = loader.load();

            AddPlaceController controller = loader.getController();
            controller.setPlace(place); // This method needs to be implemented

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Edit Place");
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            loadMyPlaces(); // Refresh
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/pi_dev/venue/views/home-view.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) placesContainer.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
