package com.example.pi_dev.venue.controllers;

import com.example.pi_dev.venue.services.BookingService;
import com.example.pi_dev.venue.entities.Booking;
import com.example.pi_dev.venue.entities.Place;
import com.example.pi_dev.venue.services.PlaceService;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.user.services.UserService;
import com.example.pi_dev.user.utils.UserSession;
import com.example.pi_dev.common.services.ActivityLogService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.sql.SQLException;
import java.util.List;

import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.geometry.Pos;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import java.io.File;
import java.io.InputStream;

public class OwnerReservationsController {

    @FXML private FlowPane reservationsFlowPane;

    private BookingService bookingService;
    private PlaceService placeService;
    private UserService userService;
    private ActivityLogService activityLogService;
    private ObservableList<Booking> reservationsList = FXCollections.observableArrayList();

    public OwnerReservationsController() {
        bookingService = new BookingService();
        placeService = new PlaceService();
        userService = new UserService();
        activityLogService = new ActivityLogService();
    }

    @FXML
    public void initialize() {
        loadReservations();
    }

    private void loadReservations() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        try {
            List<Booking> bookings = bookingService.findByOwner(currentUser.getUserId());
            reservationsList.setAll(bookings);
            
            reservationsFlowPane.getChildren().clear();
            for (Booking booking : reservationsList) {
                reservationsFlowPane.getChildren().add(createReservationCard(booking));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load reservations: " + e.getMessage());
        }
    }

    private VBox createReservationCard(Booking booking) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 20; -fx-background-radius: 12; -fx-pref-width: 350; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 5); -fx-background-color: white;");

        // Place Title
        String placeTitle = "Unknown Place";
        try {
            Place place = placeService.findById(booking.getPlaceId());
            if (place != null) placeTitle = place.getTitle();
        } catch (SQLException e) {
            placeTitle = "Place #" + booking.getPlaceId();
        }

        Label titleLabel = new Label(placeTitle);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
        titleLabel.setWrapText(true);

        // Renter Info Section
        User renter = null;
        try {
            renter = userService.getUserById(booking.getRenterId());
        } catch (Exception e) {
            System.err.println("Error fetching renter: " + e.getMessage());
        }

        HBox renterInfo = new HBox(12);
        renterInfo.setAlignment(Pos.CENTER_LEFT);
        renterInfo.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 12; -fx-background-radius: 10; -fx-border-color: #E5E7EB; -fx-border-radius: 10;");

        Circle profileCircle = new Circle(22);
        Image avatarImage = null;
        if (renter != null && renter.getProfilePicture() != null && !renter.getProfilePicture().isEmpty()) {
            File file = new File("uploads/profiles/" + renter.getProfilePicture());
            if (file.exists()) {
                avatarImage = new Image(file.toURI().toString(), 44, 44, true, true);
            }
        }
        
        if (avatarImage == null) {
            InputStream stream = getClass().getResourceAsStream("/com/example/pi_dev/user/default-avatar.png");
            if (stream != null) {
                avatarImage = new Image(stream, 44, 44, true, true);
            }
        }
        
        if (avatarImage != null) {
            profileCircle.setFill(new ImagePattern(avatarImage));
        } else {
            profileCircle.setFill(javafx.scene.paint.Color.web("#D1D5DB"));
        }

        VBox renterDetails = new VBox(2);
        Label renterNameLabel = new Label(renter != null ? renter.getFullName() : "Unknown Renter");
        renterNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #111827;");
        Label renterEmailLabel = new Label(renter != null ? renter.getEmail() : "No email");
        renterEmailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        Label renterPhoneLabel = new Label(renter != null ? renter.getPhoneNumber() : "No phone");
        renterPhoneLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        
        renterDetails.getChildren().addAll(renterNameLabel, renterEmailLabel, renterPhoneLabel);
        renterInfo.getChildren().addAll(profileCircle, renterDetails);

        // Booking Details
        VBox details = new VBox(8);
        details.getChildren().addAll(
            createDetailRow("Dates:", booking.getStartDate() + " to " + booking.getEndDate()),
            createDetailRow("Guests:", String.valueOf(booking.getGuestsCount())),
            createDetailRow("Total:", String.format("$%.2f", booking.getTotalPrice())),
            createStatusLabel(booking.getStatus())
        );

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setStyle("-fx-padding: 10 0 0 0;");

        if (booking.getStatus() == Booking.Status.PENDING) {
            Button approveBtn = new Button("Approve");
            approveBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-weight: bold;");
            approveBtn.setOnAction(e -> handleApprove(booking));

            Button rejectBtn = new Button("Reject");
            rejectBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-weight: bold;");
            rejectBtn.setOnAction(e -> handleReject(booking));

            actions.getChildren().addAll(rejectBtn, approveBtn);
        }

        card.getChildren().addAll(titleLabel, renterInfo, details, actions);
        return card;
    }

    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B7280; -fx-min-width: 60;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #374151;");
        row.getChildren().addAll(lbl, val);
        return row;
    }

    private Label createStatusLabel(Booking.Status status) {
        Label lbl = new Label(status.name());
        String color = switch (status) {
            case PENDING -> "#F59E0B";
            case CONFIRMED -> "#10B981";
            case REJECTED, CANCELLED -> "#EF4444";
            case COMPLETED -> "#6366F1";
            default -> "#6B7280";
        };
        lbl.setStyle("-fx-text-fill: white; -fx-background-color: " + color + "; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
        return lbl;
    }

    private void handleApprove(Booking booking) {
        try {
            bookingService.updateStatus(booking.getId(), Booking.Status.CONFIRMED);
            
            String placeName = "Unknown Place";
            try {
                Place place = placeService.findById(booking.getPlaceId());
                if (place != null) placeName = place.getTitle();
            } catch (SQLException e) {
                // Keep default
            }
            
            String logMsg = String.format("Approved booking of %s from %s to %s", 
                placeName, booking.getStartDate(), booking.getEndDate());
                
            activityLogService.log(UserSession.getInstance().getCurrentUser().getEmail(), "BOOKING_APPROVE", logMsg);
            loadReservations();
        } catch (SQLException e) {
            showAlert("Error", "Failed to approve booking: " + e.getMessage());
        }
    }

    private void handleReject(Booking booking) {
        try {
            bookingService.updateStatus(booking.getId(), Booking.Status.REJECTED);
            
            String placeName = "Unknown Place";
            try {
                Place place = placeService.findById(booking.getPlaceId());
                if (place != null) placeName = place.getTitle();
            } catch (SQLException e) {
                // Keep default
            }
            
            String logMsg = String.format("Rejected booking of %s from %s to %s", 
                placeName, booking.getStartDate(), booking.getEndDate());
                
            activityLogService.log(UserSession.getInstance().getCurrentUser().getEmail(), "BOOKING_REJECT", logMsg);
            loadReservations();
        } catch (SQLException e) {
            showAlert("Error", "Failed to reject booking: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadReservations();
    }

    @FXML
    private void handleBack() {
        reservationsFlowPane.getScene().getWindow().hide();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
