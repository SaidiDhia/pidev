package com.example.pi_dev.venue.controllers;

import com.example.pi_dev.venue.dao.PlaceDAO;
import com.example.pi_dev.venue.entities.Place;
import com.example.pi_dev.user.models.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController extends BaseController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private FlowPane placesContainer;
    @FXML
    private Button signInButton;
    @FXML
    private Button settingsButton;

    @FXML
    private javafx.scene.control.ToggleButton mapViewToggle;
    @FXML
    private javafx.scene.web.WebView mapWebView;
    @FXML
    private javafx.scene.control.ScrollPane scrollPane;

    private PlaceDAO placeDAO;
    private List<Place> allPlaces;
    private List<Place> currentFilteredPlaces;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        placeDAO = new PlaceDAO();

        // Update UI based on Login State
        updateAuthState();

        loadPlaces();

        // Search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterPlaces(newValue);
        });
    }

    @FXML
    private Button adminButton;

    @FXML
    private Button myPlacesButton;

    @FXML
    private Button myReservationsButton;

    @FXML
    private Button bookingRequestsButton;

    @FXML
    private void handleMyPlaces() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/pi_dev/venue/views/my-places-view.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) searchField.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load My Places view.");
        }
    }

    @FXML
    private void handleMyReservations() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/pi_dev/venue/views/user-reservations.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("My Reservations");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load User Reservations view.");
        }
    }

    @FXML
    private void handleBookingRequests() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/pi_dev/venue/views/owner-reservations.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Booking Requests");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load Owner Reservations view.");
        }
    }

    @FXML
    private void handleAdminDashboard() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/pi_dev/user/admin_dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) searchField.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load Admin view.");
        }
    }

    private void updateAuthState() {
        boolean isLoggedIn = com.example.pi_dev.user.utils.UserSession.getInstance().isLoggedIn();
        if (signInButton != null) {
            signInButton.setVisible(!isLoggedIn);
            signInButton.setManaged(!isLoggedIn);
        }
        if (settingsButton != null) {
            settingsButton.setVisible(isLoggedIn);
            settingsButton.setManaged(isLoggedIn);
        }

        // Admin Button Logic
        if (adminButton != null) {
            boolean isAdmin = false;
            if (isLoggedIn) {
                User currentUser = com.example.pi_dev.user.utils.UserSession.getInstance().getCurrentUser();
                // Check role string directly if enum comparison is tricky due to imports, or
                // use the Enum
                // Assuming RoleEnum is available and User has getRole() returning RoleEnum
                isAdmin = currentUser.getRole() == com.example.pi_dev.user.enums.RoleEnum.ADMIN;
            }
            adminButton.setVisible(isAdmin);
            adminButton.setManaged(isAdmin);
        }

        // My Places Button Logic
        if (myPlacesButton != null) {
            myPlacesButton.setVisible(isLoggedIn);
            myPlacesButton.setManaged(isLoggedIn);
        }

        if (myReservationsButton != null) {
            myReservationsButton.setVisible(isLoggedIn);
            myReservationsButton.setManaged(isLoggedIn);
        }

        if (bookingRequestsButton != null) {
            bookingRequestsButton.setVisible(isLoggedIn);
            bookingRequestsButton.setManaged(isLoggedIn);
        }
    }

    private void loadPlaces() {
        try {
            allPlaces = placeDAO.findAllApproved();
            displayPlaces(allPlaces);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayPlaces(List<Place> places) {
        this.currentFilteredPlaces = places;
        placesContainer.getChildren().clear();
        if (places.isEmpty()) {
            Label emptyLabel = new Label("No venues found.");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
            placesContainer.getChildren().add(emptyLabel);
        }
        for (Place place : places) {
            placesContainer.getChildren().add(createPlaceCard(place));
        }

        // Update map if visible
        if (mapViewToggle != null && mapViewToggle.isSelected()) {
            updateMap(places);
        }
    }

    private void filterPlaces(String query) {
        if (query == null || query.isEmpty()) {
            displayPlaces(allPlaces);
            return;
        }
        String lowerQuery = query.toLowerCase();
        List<Place> filtered = new java.util.ArrayList<>();
        for (Place p : allPlaces) {
            if (p.getTitle().toLowerCase().contains(lowerQuery) ||
                    p.getCity().toLowerCase().contains(lowerQuery)) {
                filtered.add(p);
            }
        }
        displayPlaces(filtered);
    }

    @FXML
    private void handleSearch() {
        filterPlaces(searchField.getText());
    }

    @FXML
    private void handleMapViewToggle() {
        boolean showMap = mapViewToggle.isSelected();
        scrollPane.setVisible(!showMap);
        scrollPane.setManaged(!showMap);
        mapWebView.setVisible(showMap);
        mapWebView.setManaged(showMap);

        if (showMap) {
            String location = mapWebView.getEngine().getLocation();
            if (location == null || location.isEmpty()) {
                initMap();
            } else {
                updateMap(currentFilteredPlaces != null ? currentFilteredPlaces : allPlaces);
                // Force map resize check when shown - multiple attempts for layout stability
                try {
                    String resizeScript = "setTimeout(function(){ if(typeof map !== 'undefined') map.invalidateSize(); }, 100);" +
                                         "setTimeout(function(){ if(typeof map !== 'undefined') map.invalidateSize(); }, 500);" +
                                         "setTimeout(function(){ if(typeof map !== 'undefined') map.invalidateSize(); }, 1000);";
                    mapWebView.getEngine().executeScript(resizeScript);
                } catch (Exception e) {}
            }
        }
    }

    private void initMap() {
        javafx.scene.web.WebEngine webEngine = mapWebView.getEngine();
        java.net.URL url = getClass().getResource("/com/example/pi_dev/venue/views/place_map.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
            webEngine.getLoadWorker().stateProperty().addListener((obs, old, newValue) -> {
                if (newValue == javafx.concurrent.Worker.State.SUCCEEDED) {
                    // Ensure Locale is US for decimal points in JS
                    javafx.application.Platform.runLater(() -> {
                        updateMap(currentFilteredPlaces != null ? currentFilteredPlaces : allPlaces);
                    });
                }
            });
        }
    }

    private void updateMap(List<Place> places) {
        if (places == null) return;
        
        javafx.scene.web.WebEngine webEngine = mapWebView.getEngine();

        // Use a small delay or check if the function exists to avoid "function not found" errors
        String script = "if (typeof clearMarkers === 'function') { clearMarkers(); } " +
                       "if (typeof initMap === 'function') { initMap(); } ";
        
        for (Place p : places) {
            double lat = p.getLatitude() != 0 ? p.getLatitude() : 36.8065;
            double lon = p.getLongitude() != 0 ? p.getLongitude() : 10.1815;
            String title = p.getTitle().replace("'", "\\'");
            script += String.format(java.util.Locale.US, "if (typeof addMarker === 'function') { addMarker(%f, %f, '%s'); } ", lat, lon, title);
        }

        if (!places.isEmpty()) {
            Place first = places.get(0);
            double lat = first.getLatitude() != 0 ? first.getLatitude() : 36.8065;
            double lon = first.getLongitude() != 0 ? first.getLongitude() : 10.1815;
            script += String.format(java.util.Locale.US, "if (typeof setView === 'function') { setView(%f, %f, 10); } ", lat, lon);
        }

        final String finalScript = script;
        javafx.application.Platform.runLater(() -> {
            try {
                webEngine.executeScript(finalScript);
            } catch (Exception e) {
                System.err.println("Map script execution failed: " + e.getMessage());
            }
        });
    }

    // Creating cards programmatically to ensure custom styling without complex
    // listview cells
    private VBox createPlaceCard(Place place) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-background-radius: 12; -fx-cursor: hand;");
        card.setPrefWidth(280);

        // Click event to focus map or go to details
        card.setOnMouseClicked(e -> {
            if (mapViewToggle != null && mapViewToggle.isSelected()) {
                // Focus this place on map
                double lat = place.getLatitude() != 0 ? place.getLatitude() : 36.8065;
                double lon = place.getLongitude() != 0 ? place.getLongitude() : 10.1815;
                String script = String.format(java.util.Locale.US, "if (typeof setView === 'function') { setView(%f, %f, 15); }", lat, lon);
                try {
                    mapWebView.getEngine().executeScript(script);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                navigateToDetails(place);
            }
        });

        // Image Loading
        ImageView imageView = new ImageView();
        imageView.setFitWidth(260);
        imageView.setFitHeight(180);
        imageView.setStyle("-fx-background-color: #eee;"); // Placeholder color
        
        if (place.getImageUrl() != null && !place.getImageUrl().isEmpty()) {
            try {
                String imageUrl = place.getImageUrl();
                // Handle relative local paths from DB
                if (!imageUrl.startsWith("http") && !imageUrl.startsWith("file:") && !imageUrl.startsWith("jar:")) {
                    java.io.File file = new java.io.File(imageUrl);
                    if (!file.exists()) {
                        // Try with PI_DEV prefix if working dir is parent
                        file = new java.io.File("PI_DEV/" + imageUrl);
                    }
                    
                    if (file.exists()) {
                        imageUrl = file.toURI().toString();
                    } else {
                        // Try as resource
                        URL resource = getClass().getResource(imageUrl);
                        if (resource != null) {
                            imageUrl = resource.toExternalForm();
                        }
                    }
                }
                
                final String finalUrl = imageUrl;
                Image image = new Image(finalUrl, 260, 180, true, true, true); // Load in background
                image.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        System.err.println("Error loading image: " + finalUrl);
                    }
                });
                
                imageView.setImage(image);
                
                // Add a clip for rounded corners on the image
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(260, 180);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                imageView.setClip(clip);
            } catch (Exception e) {
                System.err.println("Exception loading image: " + place.getImageUrl() + " - " + e.getMessage());
            }
        }

        Label title = new Label(place.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        title.setWrapText(true);

        Label price = new Label("$" + place.getPricePerDay() + " / day");
        price.setStyle("-fx-font-size: 14px; -fx-text-fill: #4F46E5; -fx-font-weight: bold;");

        Label location = new Label(place.getCity());
        location.setStyle("-fx-text-fill: #666;");

        card.getChildren().addAll(imageView, title, location, price);

        return card;
    }

    @FXML
    private void handleSignIn() {
        navigateToLogin(searchField);
    }

    @FXML
    private void handleSettings() {
        // Navigate to User Settings from existing User module
        // Assuming path /com/example/pi_dev/user/settings.fxml
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/pi_dev/user/settings.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) searchField.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load Settings view.");
        }
    }

    public void refresh() {
        updateAuthState();
        loadPlaces();
    }

    @FXML
    private void handleBecomeHost() {
        if (!com.example.pi_dev.user.utils.UserSession.getInstance().isLoggedIn()) {
            // Redirect to login if not logged in
            navigateToLogin(searchField);
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/pi_dev/venue/views/add-place-view.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("List Your Space");
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            // Refresh places after adding (optional, if approved immediately)
            refresh();

        } catch (java.io.IOException e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load Add Place view");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void navigateToDetails(Place place) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/pi_dev/venue/views/place-details-view.fxml"));
            javafx.scene.Parent root = loader.load();

            PlaceDetailsController controller = loader.getController();
            User currentUser = null;
            if (com.example.pi_dev.user.utils.UserSession.getInstance().isLoggedIn()) {
                currentUser = com.example.pi_dev.user.utils.UserSession.getInstance().getCurrentUser();
            }
            controller.setPlaceData(place, currentUser);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);

            // Get current stage from a node
            javafx.stage.Stage stage = (javafx.stage.Stage) placesContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load Place Details view: " + e.getMessage());
        }
    }
}
