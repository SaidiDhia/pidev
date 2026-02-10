package com.example.pi_dev.venue.controllers;

import com.example.pi_dev.venue.dao.PlaceDAO;
import com.example.pi_dev.venue.entities.Place;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.user.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.sql.SQLException;

public class AddPlaceController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField priceField;
    @FXML
    private TextField capacityField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField latitudeField;
    @FXML
    private TextField longitudeField;
    @FXML
    private javafx.scene.web.WebView mapWebView;

    private PlaceDAO placeDAO;
    private Place editingPlace; // null if creating new, populated if editing

    public AddPlaceController() {
        placeDAO = new PlaceDAO();
    }

    @FXML
    private void handleSubmit() {
        if (!validateInput()) {
            return;
        }

        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert("Error", "You must be logged in to add a place.");
            return;
        }

        try {
            double price = Double.parseDouble(priceField.getText());
            int capacity = Integer.parseInt(capacityField.getText());
            double lat = latitudeField.getText().isEmpty() ? 36.8065 : Double.parseDouble(latitudeField.getText());
            double lon = longitudeField.getText().isEmpty() ? 10.1815 : Double.parseDouble(longitudeField.getText());

            if (editingPlace == null) {
                // Create new place
                Place newPlace = new Place();
                newPlace.setHostId(currentUser.getUserId().toString());
                newPlace.setTitle(titleField.getText());
                newPlace.setDescription(descriptionArea.getText());
                newPlace.setPricePerDay(price);
                newPlace.setCapacity(capacity);
                newPlace.setAddress(addressField.getText());
                newPlace.setCity(cityField.getText());
                newPlace.setCategory(categoryField.getText());
                newPlace.setStatus(Place.Status.PENDING);
                newPlace.setLatitude(lat);
                newPlace.setLongitude(lon);

                placeDAO.create(newPlace);
                showAlert("Success", "Your place has been submitted for approval!");
            } else {
                // Update existing place
                editingPlace.setTitle(titleField.getText());
                editingPlace.setDescription(descriptionArea.getText());
                editingPlace.setPricePerDay(price);
                editingPlace.setCapacity(capacity);
                editingPlace.setAddress(addressField.getText());
                editingPlace.setCity(cityField.getText());
                editingPlace.setCategory(categoryField.getText());
                editingPlace.setLatitude(lat);
                editingPlace.setLongitude(lon);

                placeDAO.update(editingPlace);
                showAlert("Success", "Your place has been updated!");
            }

            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Price, Capacity, Latitude, and Longitude must be valid numbers.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to save place: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        closeWindow();
    }

    private boolean validateInput() {
        if (titleField.getText().isEmpty() || priceField.getText().isEmpty() || cityField.getText().isEmpty()) {
            showAlert("Validation Error", "Please fill in all required fields.");
            return false;
        }
        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setPlace(Place place) {
        this.editingPlace = place;
        titleField.setText(place.getTitle());
        descriptionArea.setText(place.getDescription());
        priceField.setText(String.valueOf(place.getPricePerDay()));
        capacityField.setText(String.valueOf(place.getCapacity()));
        addressField.setText(place.getAddress());
        cityField.setText(place.getCity());
        categoryField.setText(place.getCategory());
        latitudeField.setText(String.valueOf(place.getLatitude()));
        longitudeField.setText(String.valueOf(place.getLongitude()));

        // Update map to show selected location
        if (mapWebView != null) {
            initializeMap(place.getLatitude(), place.getLongitude());
        }
    }

    @FXML
    private void initialize() {
        initializeMap(36.8065, 10.1815); // Default to Tunisia
    }

    private void initializeMap(double lat, double lon) {
        if (mapWebView == null)
            return;

        javafx.scene.web.WebEngine webEngine = mapWebView.getEngine();
        String mapHtml = String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta charset='utf-8'>" +
                        "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                        "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                        "<style>body{margin:0;padding:0}#map{height:100vh;width:100vw}</style>" +
                        "</head>" +
                        "<body>" +
                        "<div id='map'></div>" +
                        "<script>" +
                        "var map=L.map('map').setView([%f,%f],8);" +
                        "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{attribution:'OpenStreetMap'}).addTo(map);"
                        +
                        "var marker=L.marker([%f,%f],{draggable:true}).addTo(map);" +
                        "function updateCoords(lat,lng){" +
                        "  window.javaApp.updateCoordinates(lat,lng);" +
                        "}" +
                        "marker.on('dragend',function(e){" +
                        "  var pos=marker.getLatLng();" +
                        "  updateCoords(pos.lat,pos.lng);" +
                        "});" +
                        "map.on('click',function(e){" +
                        "  marker.setLatLng(e.latlng);" +
                        "  updateCoords(e.latlng.lat,e.latlng.lng);" +
                        "});" +
                        "</script>" +
                        "</body>" +
                        "</html>",
                lat, lon, lat, lon);

        webEngine.loadContent(mapHtml);

        // JavaScript bridge to get coordinates from map
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaApp", new JavaBridge());
            }
        });
    }

    public class JavaBridge {
        public void updateCoordinates(double lat, double lng) {
            javafx.application.Platform.runLater(() -> {
                latitudeField.setText(String.format("%.6f", lat));
                longitudeField.setText(String.format("%.6f", lng));
            });
        }
    }
}
