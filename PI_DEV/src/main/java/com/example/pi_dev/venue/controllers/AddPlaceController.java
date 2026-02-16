package com.example.pi_dev.venue.controllers;

import com.example.pi_dev.venue.services.PlaceService;
import com.example.pi_dev.venue.services.AmenityService;
import com.example.pi_dev.venue.entities.Place;
import com.example.pi_dev.venue.entities.Amenity;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.user.utils.UserSession;
import com.example.pi_dev.common.services.ActivityLogService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.sql.SQLException;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.CheckBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
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
    private HBox imagePreviewContainer;
    @FXML
    private Label imagePathLabel;
    @FXML
    private javafx.scene.web.WebView mapWebView;
    @FXML
    private FlowPane amenitiesFlowPane;
    
    private PlaceService placeService;
    private AmenityService amenityService;
    private final ActivityLogService activityLogService = new ActivityLogService();
    private Place editingPlace;
    private List<File> selectedFiles = new ArrayList<>();
    private List<String> existingImageUrls = new ArrayList<>();
    private List<CheckBox> amenityCheckBoxes = new ArrayList<>();
    
    // Bridge for JS communication
    public class JavaBridge {
        public void updateCoordinates(double lat, double lon) {
            javafx.application.Platform.runLater(() -> {
                latitudeField.setText(String.format(java.util.Locale.US, "%.6f", lat));
                longitudeField.setText(String.format(java.util.Locale.US, "%.6f", lon));
                mapWebView.getEngine().executeScript(String.format(java.util.Locale.US, "addMarker(%f, %f, 'Selected Location', true, '');", lat, lon));
            });
        }
    }

    public AddPlaceController() {
        placeService = new PlaceService();
        amenityService = new AmenityService();
    }
    
    @FXML
    public void initialize() {
        if (mapWebView != null) {
             initMap();
        }
        loadAmenities();
    }

    private void loadAmenities() {
        try {
            List<Amenity> allAmenities = amenityService.findAll();
            amenitiesFlowPane.getChildren().clear();
            amenityCheckBoxes.clear();

            for (Amenity amenity : allAmenities) {
                CheckBox cb = new CheckBox(amenity.getName());
                cb.setUserData(amenity);
                cb.getStyleClass().add("text-p");
                cb.setStyle("-fx-font-size: 14px;");
                
                amenityCheckBoxes.add(cb);
                amenitiesFlowPane.getChildren().add(cb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void initMap() {
        javafx.scene.web.WebEngine webEngine = mapWebView.getEngine();
        
        // Console logging bridge
        webEngine.setOnAlert(event -> System.out.println("[WebView Alert] " + event.getData()));
        webEngine.getLoadWorker().exceptionProperty().addListener((obs, old, ex) -> {
            if (ex != null) System.err.println("[WebView Error] " + ex.getMessage());
        });

        java.net.URL url = getClass().getResource("/com/example/pi_dev/venue/views/place_map.html");
        
        // Force refresh on resize and visibility changes
        mapWebView.widthProperty().addListener((obs, old, newVal) -> {
            webEngine.executeScript("if(typeof forceRefresh === 'function') { forceRefresh(); }");
        });
        mapWebView.heightProperty().addListener((obs, old, newVal) -> {
            webEngine.executeScript("if(typeof forceRefresh === 'function') { forceRefresh(); }");
        });

        if (url != null) {
            webEngine.load(url.toExternalForm());
            webEngine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    netscape.javascript.JSObject window = (netscape.javascript.JSObject) webEngine.executeScript("window");
                    window.setMember("javaApp", new JavaBridge());
                    
                    if (editingPlace != null) {
                        double lat = editingPlace.getLatitude() != 0 ? editingPlace.getLatitude() : 36.8065;
                        double lon = editingPlace.getLongitude() != 0 ? editingPlace.getLongitude() : 10.1815;
                        webEngine.executeScript(String.format(java.util.Locale.US, "initMap(%f, %f, 13);", lat, lon));
                        webEngine.executeScript(String.format(java.util.Locale.US, "addMarker(%f, %f, 'Selected Location', true, '');", lat, lon));
                    }

                    // Series of refreshes to handle JavaFX layout timing
                    for (int delay : new int[]{200, 500, 1000, 2000}) {
                        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(delay));
                        pause.setOnFinished(e -> {
                            try {
                                webEngine.executeScript("if(typeof forceRefresh === 'function') { forceRefresh(); }");
                            } catch (Exception ex) {}
                        });
                        pause.play();
                    }
                }
            });
        }
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
        
        // Pre-select amenities
        if (place.getAmenities() != null) {
            for (CheckBox cb : amenityCheckBoxes) {
                Amenity amenity = (Amenity) cb.getUserData();
                boolean hasAmenity = place.getAmenities().stream()
                        .anyMatch(a -> a.getId() == amenity.getId());
                cb.setSelected(hasAmenity);
            }
        }

        try {
            existingImageUrls = placeService.getImages(place.getId());
            refreshImagePreviews();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshImagePreviews() {
        imagePreviewContainer.getChildren().clear();
        
        // Show existing images
        for (String url : existingImageUrls) {
            addImagePreview(url, false);
        }
        
        // Show newly selected images
        for (File file : selectedFiles) {
            addImagePreview(file.toURI().toString(), true);
        }
        
        int total = existingImageUrls.size() + selectedFiles.size();
        imagePathLabel.setText(total + " photo(s) selected");
    }

    private void addImagePreview(String imageUrl, boolean isNew) {
        VBox container = new VBox(5);
        container.setAlignment(javafx.geometry.Pos.CENTER);
        
        ImageView imageView = new ImageView();
        imageView.setFitHeight(120);
        imageView.setFitWidth(150);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("card");

        try {
            String finalUrl = imageUrl;
            if (!imageUrl.startsWith("http") && !imageUrl.startsWith("file:") && !imageUrl.startsWith("jar:")) {
                File file = new File(imageUrl);
                if (file.exists()) finalUrl = file.toURI().toString();
            }
            imageView.setImage(new Image(finalUrl, 150, 120, true, true));
        } catch (Exception e) {
            System.err.println("Failed to load preview: " + imageUrl);
        }

        Button removeBtn = new Button("Remove");
        removeBtn.getStyleClass().addAll("btn", "btn-danger");
        removeBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 5;");
        removeBtn.setOnAction(e -> {
            if (isNew) {
                selectedFiles.removeIf(f -> f.toURI().toString().equals(imageUrl));
            } else {
                existingImageUrls.remove(imageUrl);
            }
            refreshImagePreviews();
        });

        container.getChildren().addAll(imageView, removeBtn);
        imagePreviewContainer.getChildren().add(container);
    }

    @FXML
    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Place Images");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        List<File> files = fileChooser.showOpenMultipleDialog(titleField.getScene().getWindow());
        if (files != null) {
            selectedFiles.addAll(files);
            refreshImagePreviews();
        }
    }

    @FXML
    private void handleSubmit() {
        if (!validateInput()) return;

        Place place = (editingPlace != null) ? editingPlace : new Place();
        place.setTitle(titleField.getText());
        place.setDescription(descriptionArea.getText());
        place.setPricePerDay(Double.parseDouble(priceField.getText()));
        place.setCapacity(Integer.parseInt(capacityField.getText()));
        place.setMaxGuests(Integer.parseInt(maxGuestsField.getText()));
        place.setAddress(addressField.getText());
        place.setCity(cityField.getText());
        place.setCategory(categoryField.getText());
        place.setLatitude(Double.parseDouble(latitudeField.getText()));
        place.setLongitude(Double.parseDouble(longitudeField.getText()));
        place.setHostId(UserSession.getInstance().getCurrentUser().getUserId().toString());
        place.setStatus(Place.Status.PENDING);

        // Collect selected amenities
        List<Amenity> selectedAmenities = new ArrayList<>();
        for (CheckBox cb : amenityCheckBoxes) {
            if (cb.isSelected()) {
                selectedAmenities.add((Amenity) cb.getUserData());
            }
        }
        place.setAmenities(selectedAmenities);

        try {
            if (editingPlace == null) {
                placeService.create(place);
                activityLogService.log(UserSession.getInstance().getCurrentUser().getEmail(), "PLACE_CREATE", "Created new place: " + place.getTitle());
            } else {
                placeService.update(place);
                activityLogService.log(UserSession.getInstance().getCurrentUser().getEmail(), "PLACE_UPDATE", "Updated place: " + place.getTitle());
                
                // Handle multiple images for update: clear and re-add all current + new
                placeService.clearImages(place.getId());
                for (String url : existingImageUrls) {
                    placeService.saveImage(place.getId(), url);
                }
            }

            // Save new images
            File destDir = new File("uploads/places");
            if (!destDir.exists()) destDir.mkdirs();

            for (File selectedFile : selectedFiles) {
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                File destFile = new File(destDir, fileName);
                try {
                    Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    placeService.saveImage(place.getId(), destFile.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            showAlert("Success", "Listing submitted for approval!");
            handleBack();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save listing: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (titleField.getText().isEmpty() || priceField.getText().isEmpty() || 
            addressField.getText().isEmpty() || cityField.getText().isEmpty()) {
            showAlert("Error", "Please fill in all required fields.");
            return false;
        }
        try {
            Double.parseDouble(priceField.getText());
            Integer.parseInt(capacityField.getText());
            Integer.parseInt(maxGuestsField.getText());
            Double.parseDouble(latitudeField.getText());
            Double.parseDouble(longitudeField.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid numeric values.");
            return false;
        }
        return true;
    }

    @FXML
    private void handleBack() {
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
}
