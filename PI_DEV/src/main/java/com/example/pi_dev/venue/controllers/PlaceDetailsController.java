package com.example.pi_dev.venue.controllers;

import com.example.pi_dev.venue.services.BookingService;
import com.example.pi_dev.venue.entities.Booking;
import com.example.pi_dev.venue.entities.Place;
import com.example.pi_dev.venue.entities.Amenity;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.common.services.ActivityLogService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javafx.scene.web.WebView;

public class PlaceDetailsController extends BaseController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ImageView mainImageView;

    @FXML
    private javafx.scene.control.Button prevImageBtn;

    @FXML
    private javafx.scene.control.Button nextImageBtn;

    @FXML
    private DatePicker checkInDate;
    @FXML
    private DatePicker checkOutDate;
    @FXML
    private javafx.scene.control.TextField guestsField;
    @FXML
    private Label totalPriceLabel;

    @FXML
    private WebView mapWebView;

    @FXML
    private FlowPane amenitiesFlowPane;
    
    private Place currentPlace;
    private User currentUser;
    private BookingService bookingService;
    private final ActivityLogService activityLogService = new ActivityLogService();
    private java.util.List<LocalDate> blockedDates = new java.util.ArrayList<>();
    private java.util.List<String> placeImages = new java.util.ArrayList<>();
    private int currentImageIndex = 0;

    public void setPlaceData(Place place, User user) {
        this.currentPlace = place;
        this.currentUser = user;
        this.bookingService = new BookingService();

        titleLabel.setText(place.getTitle());
        locationLabel.setText(place.getCity() + ", " + place.getAddress());
        priceLabel.setText("$" + place.getPricePerDay());
        descriptionArea.setText(place.getDescription());

        // Load Images
        loadPlaceImages();

        // Load Blocked Dates
        try {
            blockedDates = bookingService.getBookedDates(place.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Setup DatePickers
        setupDatePickers();

        // Load Map
        loadMap();

        // Display Amenities
        displayAmenities();
    }

    private void displayAmenities() {
        amenitiesFlowPane.getChildren().clear();
        if (currentPlace.getAmenities() != null) {
            for (Amenity amenity : currentPlace.getAmenities()) {
                HBox amenityBox = new HBox(10);
                amenityBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                amenityBox.setStyle("-fx-background-color: #F3F4F6; -fx-padding: 8 15; -fx-background-radius: 20;");

                // Icon (using a simple circle/label for now, could be replaced with FontAwesome if available)
                Label iconLabel = new Label("•");
                iconLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #4F46E5; -fx-font-weight: bold;");

                Label nameLabel = new Label(amenity.getName());
                nameLabel.getStyleClass().add("text-p");
                nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500;");

                amenityBox.getChildren().addAll(iconLabel, nameLabel);
                amenitiesFlowPane.getChildren().add(amenityBox);
            }
        }
    }

    private void setupDatePickers() {
        javafx.util.Callback<DatePicker, javafx.scene.control.DateCell> dayCellFactory = new javafx.util.Callback<DatePicker, javafx.scene.control.DateCell>() {
            @Override
            public javafx.scene.control.DateCell call(final DatePicker datePicker) {
                return new javafx.scene.control.DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        // Disable past dates
                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #f3f4f6;"); 
                        }
                        
                        // Disable blocked dates
                        if (blockedDates.contains(item)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #9ca3af;"); 
                        }

                        // For check-out, disable dates before check-in
                        if (datePicker == checkOutDate && checkInDate.getValue() != null) {
                            if (item.isBefore(checkInDate.getValue().plusDays(1))) {
                                setDisable(true);
                                setStyle("-fx-background-color: #f3f4f6;");
                            }
                        }
                    }
                };
            }
        };

        checkInDate.setDayCellFactory(dayCellFactory);
        checkOutDate.setDayCellFactory(dayCellFactory);

        // Update check-out constraints when check-in changes
        checkInDate.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (checkOutDate.getValue() != null && checkOutDate.getValue().isBefore(newVal.plusDays(1))) {
                    checkOutDate.setValue(newVal.plusDays(1));
                }
                // Refresh check-out cell factory to apply new constraints
                checkOutDate.setDayCellFactory(dayCellFactory);
            }
            handleCalculateTotal();
        });

        checkOutDate.valueProperty().addListener((obs, oldVal, newVal) -> handleCalculateTotal());
    }

    private void loadPlaceImages() {
        try {
            placeImages = bookingService.getPlaceImages(currentPlace.getId());
            // If no images in place_images, use the main image from places table
            if (placeImages.isEmpty() && currentPlace.getImageUrl() != null && !currentPlace.getImageUrl().isEmpty()) {
                placeImages.add(currentPlace.getImageUrl());
            }
            
            if (!placeImages.isEmpty()) {
                currentImageIndex = 0;
                displayImage(placeImages.get(0));
                
                boolean hasMultiple = placeImages.size() > 1;
                prevImageBtn.setVisible(hasMultiple);
                nextImageBtn.setVisible(hasMultiple);
            } else {
                prevImageBtn.setVisible(false);
                nextImageBtn.setVisible(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayImage(String imageUrl) {
        try {
            // Handle relative local paths
            if (!imageUrl.startsWith("http") && !imageUrl.startsWith("file:") && !imageUrl.startsWith("jar:")) {
                java.io.File file = new java.io.File(imageUrl);
                if (!file.exists()) {
                    file = new java.io.File("PI_DEV/" + imageUrl);
                }
                if (!file.exists()) {
                    file = new java.io.File("uploads/places/" + imageUrl);
                }
                
                if (file.exists()) {
                    imageUrl = file.toURI().toString();
                }
            }
            Image image = new Image(imageUrl, 700, 450, true, true, true);
            mainImageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Failed to load image: " + imageUrl);
        }
    }

    @FXML
    private void handlePrevImage() {
        if (placeImages.size() > 1) {
            currentImageIndex = (currentImageIndex - 1 + placeImages.size()) % placeImages.size();
            displayImage(placeImages.get(currentImageIndex));
        }
    }

    @FXML
    private void handleNextImage() {
        if (placeImages.size() > 1) {
            currentImageIndex = (currentImageIndex + 1) % placeImages.size();
            displayImage(placeImages.get(currentImageIndex));
        }
    }

    private void loadMap() {
        if (mapWebView != null) {
            javafx.scene.web.WebEngine webEngine = mapWebView.getEngine();
            
            // Console logging bridge
            webEngine.setOnAlert(event -> System.out.println("[WebView Alert] " + event.getData()));
            webEngine.getLoadWorker().exceptionProperty().addListener((obs, old, ex) -> {
                if (ex != null) System.err.println("[WebView Error] " + ex.getMessage());
            });

            // Force refresh on resize
            mapWebView.widthProperty().addListener((obs, old, newVal) -> {
                webEngine.executeScript("if(typeof forceRefresh === 'function') { forceRefresh(); }");
            });
            mapWebView.heightProperty().addListener((obs, old, newVal) -> {
                webEngine.executeScript("if(typeof forceRefresh === 'function') { forceRefresh(); }");
            });

            java.net.URL url = getClass().getResource("/com/example/pi_dev/venue/views/place_map.html");
            if (url != null) {
                webEngine.load(url.toExternalForm());
                webEngine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
                    if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                        double lat = currentPlace.getLatitude() != 0 ? currentPlace.getLatitude() : 36.8065;
                        double lon = currentPlace.getLongitude() != 0 ? currentPlace.getLongitude() : 10.1815;
                        String title = currentPlace.getTitle().replace("'", "\\'");
                        String city = currentPlace.getCity().replace("'", "\\'");
                        webEngine.executeScript(String.format(java.util.Locale.US, "initMap(%f, %f, 13);", lat, lon));
                        webEngine.executeScript(String.format(java.util.Locale.US, "addMarker(%f, %f, '%s', true, '%s');", lat, lon, title, city));

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
    }

    @FXML
    private void handleCalculateTotal() {
        if (checkInDate.getValue() != null && checkOutDate.getValue() != null) {
            long days = ChronoUnit.DAYS.between(checkInDate.getValue(), checkOutDate.getValue());
            if (days > 0) {
                double total = days * currentPlace.getPricePerDay();
                totalPriceLabel.setText("Total: $" + total);
            } else {
                totalPriceLabel.setText("Invalid dates");
            }
        }
    }

    @FXML
    private void handleBooking() {
        // Check if user passed in or get from session
        if (currentUser == null) {
            if (com.example.pi_dev.user.utils.UserSession.getInstance().isLoggedIn()) {
                currentUser = com.example.pi_dev.user.utils.UserSession.getInstance().getCurrentUser();
            }
        }

        if (currentUser == null) {
            showAlert("Login Required", "Please login to book a venue.");
            // Optional: Navigate to login
            // navigateToLogin(titleLabel);
            return;
        }

        LocalDate start = checkInDate.getValue();
        LocalDate end = checkOutDate.getValue();

        if (start == null || end == null || !end.isAfter(start)) {
            showAlert("Invalid Dates", "Please select valid check-in and check-out dates.");
            return;
        }

        int guests;
        try {
            guests = Integer.parseInt(guestsField.getText());
            if (guests <= 0 || guests > currentPlace.getMaxGuests()) {
                showAlert("Invalid Guests", "Please enter a guest count between 1 and " + currentPlace.getMaxGuests());
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for guests.");
            return;
        }

        try {
            if (bookingService.isAvailable(currentPlace.getId(), java.sql.Date.valueOf(start),
                    java.sql.Date.valueOf(end))) {
                double total = ChronoUnit.DAYS.between(start, end) * currentPlace.getPricePerDay();
                // User ID is UUID, converted to String for Booking entity
                Booking booking = new Booking(0, currentPlace.getId(), currentUser.getUserId().toString(), start, end,
                        total, Booking.Status.PENDING);
                booking.setGuestsCount(guests);
                bookingService.create(booking);
                activityLogService.log(currentUser.getEmail(), "RESERVATION_CREATE", 
                    "New reservation request for: " + currentPlace.getTitle() + " (" + start + " to " + end + ")");
                showAlert("Success", "Booking request sent successfully!");
            } else {
                showAlert("Unavailable", "The selected dates are already booked.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Booking failed due to system error.");
        }
    }

    @FXML
    private void handleBackToSearch() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/pi_dev/venue/views/home-view.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) titleLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
