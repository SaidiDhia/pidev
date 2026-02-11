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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

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
    private TextField maxGuestsField;
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
    @FXML
    private Label imagePathLabel;
    @FXML
    private ImageView imagePreview;

    private PlaceDAO placeDAO;
    private Place editingPlace;
    private File selectedFile;
    private String selectedImageUrl;

    public AddPlaceController() {
        placeDAO = new PlaceDAO();
    }

    @FXML
    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Place Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        this.selectedFile = fileChooser.showOpenDialog(imagePreview.getScene().getWindow());
        if (this.selectedFile != null) {
            selectedImageUrl = this.selectedFile.toURI().toString();
            imagePreview.setImage(new Image(selectedImageUrl));
            imagePathLabel.setText(this.selectedFile.getName());
            imagePreview.setVisible(true);
            imagePreview.setManaged(true);
        }
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
            int maxGuests = Integer.parseInt(maxGuestsField.getText());
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
                newPlace.setMaxGuests(maxGuests);
                newPlace.setAddress(addressField.getText());
                newPlace.setCity(cityField.getText());
                newPlace.setCategory(categoryField.getText());
                newPlace.setStatus(Place.Status.PENDING);
                newPlace.setLatitude(lat);
                newPlace.setLongitude(lon);

                // Handle image upload if selected
                if (selectedFile != null) {
                    String savedPath = saveImage(selectedFile);
                    if (savedPath != null) {
                        newPlace.setImageUrl(savedPath);
                    }
                }

                placeDAO.create(newPlace);
                showAlert("Success", "Your place has been submitted for approval!");
            } else {
                // Update existing place
                editingPlace.setTitle(titleField.getText());
                editingPlace.setDescription(descriptionArea.getText());
                editingPlace.setPricePerDay(price);
                editingPlace.setCapacity(capacity);
                editingPlace.setMaxGuests(maxGuests);
                editingPlace.setAddress(addressField.getText());
                editingPlace.setCity(cityField.getText());
                editingPlace.setCategory(categoryField.getText());
                editingPlace.setLatitude(lat);
                editingPlace.setLongitude(lon);

                // Handle image upload if selected
                if (selectedFile != null) {
                    String savedPath = saveImage(selectedFile);
                    if (savedPath != null) {
                        editingPlace.setImageUrl(savedPath);
                    }
                }

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

    private String saveImage(File file) {
        try {
            // Create uploads directory if it doesn't exist
            File uploadDir = new File("uploads/venues");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate unique filename
            String extension = "";
            String fileName = file.getName();
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i);
            }
            String newFileName = UUID.randomUUID().toString() + extension;
            File destFile = new File(uploadDir, newFileName);

            // Copy file
            Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Return relative path for storage in DB
            return "uploads/venues/" + newFileName;
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save image: " + e.getMessage());
            return null;
        }
    }

    @FXML
    private void handleBack() {
        closeWindow();
    }

    private boolean validateInput() {
        if (titleField.getText().isEmpty() || priceField.getText().isEmpty() || 
            capacityField.getText().isEmpty() || maxGuestsField.getText().isEmpty() || 
            cityField.getText().isEmpty()) {
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
        maxGuestsField.setText(String.valueOf(place.getMaxGuests()));
        addressField.setText(place.getAddress());
        cityField.setText(place.getCity());
        categoryField.setText(place.getCategory());
        latitudeField.setText(String.valueOf(place.getLatitude()));
        longitudeField.setText(String.valueOf(place.getLongitude()));
        this.selectedImageUrl = place.getImageUrl();
        if (selectedImageUrl != null && !selectedImageUrl.isEmpty()) {
            String imagePath = selectedImageUrl;
            if (!imagePath.startsWith("http") && !imagePath.startsWith("file:") && !imagePath.startsWith("jar:")) {
                File file = new File(imagePath);
                if (file.exists()) {
                    imagePath = file.toURI().toString();
                }
            }
            try {
                imagePreview.setImage(new Image(imagePath));
                imagePreview.setVisible(true);
                imagePreview.setManaged(true);
                imagePathLabel.setText("Current Image");
            } catch (Exception e) {
                System.err.println("Failed to load image: " + imagePath);
                e.printStackTrace();
            }
        }

        // Update map to show selected location
        if (mapWebView != null) {
            initializeMap(place.getLatitude(), place.getLongitude());
        }
    }

    @FXML
    private void initialize() {
        if (mapWebView != null) {
            mapWebView.getEngine().setJavaScriptEnabled(true);
            // Optional: prevent right-click menu in webview
            mapWebView.setContextMenuEnabled(false);
        }
        initializeMap(36.8065, 10.1815); // Default to Tunisia
    }

    private void initializeMap(double initialLat, double initialLon) {
        if (mapWebView == null)
            return;

        javafx.scene.web.WebEngine webEngine = mapWebView.getEngine();
  
          // JavaScript bridge to get coordinates from map - set BEFORE loading content
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaApp", new JavaBridge());
                    
                    // Enable edit mode after load
                    webEngine.executeScript(String.format(java.util.Locale.US, "enableEditMode(%f, %f);", initialLat, initialLon));
                } catch (Exception e) {
                    System.err.println("Failed to initialize map bridge: " + e.getMessage());
                }
            }
        });

        java.net.URL url = getClass().getResource("/com/example/pi_dev/venue/views/place_map.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
        } else {
            System.err.println("Could not find place_map.html for AddPlaceView");
        }
    }

    public class JavaBridge {
        public void updateCoordinates(double lat, double lng) {
            javafx.application.Platform.runLater(() -> {
                latitudeField.setText(String.format(java.util.Locale.US, "%.6f", lat));
                longitudeField.setText(String.format(java.util.Locale.US, "%.6f", lng));
            });
        }
    }
}
