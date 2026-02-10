package com.example.pi_dev.venue.controllers;

import com.example.pi_dev.venue.dao.BookingDAO;
import com.example.pi_dev.venue.entities.Booking;
import com.example.pi_dev.venue.entities.Place;
import com.example.pi_dev.user.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
    private DatePicker checkInDate;
    @FXML
    private DatePicker checkOutDate;
    @FXML
    private Label totalPriceLabel;

    @FXML
    private javafx.scene.web.WebView mapWebView;

    private Place currentPlace;
    private User currentUser;
    private BookingDAO bookingDAO;
    private java.util.List<LocalDate> blockedDates = new java.util.ArrayList<>();

    public void setPlaceData(Place place, User user) {
        this.currentPlace = place;
        this.currentUser = user;
        this.bookingDAO = new BookingDAO();

        titleLabel.setText(place.getTitle());
        locationLabel.setText(place.getCity() + ", " + place.getAddress());
        priceLabel.setText("$" + place.getPricePerDay() + " / day");
        descriptionArea.setText(place.getDescription());

        // Load Image
        if (place.getImageUrl() != null && !place.getImageUrl().isEmpty()) {
            try {
                Image image = new Image(place.getImageUrl(), 700, 400, true, true, true);
                mainImageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Failed to load image: " + place.getImageUrl());
            }
        }

        // Load Blocked Dates
        try {
            blockedDates = bookingDAO.getBookedDates(place.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Setup DatePickers
        setupDatePickers();

        // Load Map
        loadMap();
    }

    private void setupDatePickers() {
        javafx.util.Callback<DatePicker, javafx.scene.control.DateCell> dayCellFactory = new javafx.util.Callback<DatePicker, javafx.scene.control.DateCell>() {
            @Override
            public javafx.scene.control.DateCell call(final DatePicker datePicker) {
                return new javafx.scene.control.DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;"); // Pink for past
                        }
                        if (blockedDates.contains(item)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffcccc; -fx-text-fill: gray;"); // Blocked dates
                        }
                    }
                };
            }
        };

        checkInDate.setDayCellFactory(dayCellFactory);
        checkOutDate.setDayCellFactory(dayCellFactory);
    }

    private void loadMap() {
        if (mapWebView != null) {
            javafx.scene.web.WebEngine webEngine = mapWebView.getEngine();
            java.net.URL url = getClass().getResource("/com/example/pi_dev/venue/views/place_map.html");
            if (url != null) {
                webEngine.load(url.toExternalForm());

                // Wait for page to load then set coordinates
                webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                    System.out.println("WebView State: " + newValue);
                    if (newValue == javafx.concurrent.Worker.State.SUCCEEDED) {
                        try {
                            double lat = currentPlace.getLatitude();
                            double lon = currentPlace.getLongitude();
                            // If 0, use default
                            if (lat == 0 && lon == 0) {
                                lat = 36.8065;
                                lon = 10.1815;
                            }
                            String title = currentPlace.getTitle().replace("'", "\\'");

                            webEngine.executeScript("initMap(" + lat + ", " + lon + ", '" + title + "')");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                System.err.println("Could not find place_map.html");
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

        try {
            if (bookingDAO.isAvailable(currentPlace.getId(), java.sql.Date.valueOf(start),
                    java.sql.Date.valueOf(end))) {
                double total = ChronoUnit.DAYS.between(start, end) * currentPlace.getPricePerDay();
                // User ID is UUID, converted to String for Booking entity
                Booking booking = new Booking(0, currentPlace.getId(), currentUser.getUserId().toString(), start, end,
                        total, Booking.Status.PENDING);
                bookingDAO.create(booking);
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
