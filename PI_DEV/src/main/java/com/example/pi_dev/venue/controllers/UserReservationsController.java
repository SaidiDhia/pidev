package com.example.pi_dev.venue.controllers;

import com.example.pi_dev.venue.dao.BookingDAO;
import com.example.pi_dev.venue.entities.Booking;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.user.utils.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.time.LocalDate;

public class UserReservationsController {

    @FXML private FlowPane reservationsFlowPane;

    private BookingDAO bookingDAO;
    private com.example.pi_dev.common.dao.ActivityLogDAO activityLogDAO;
    private ObservableList<Booking> reservationsList = FXCollections.observableArrayList();

    public UserReservationsController() {
        bookingDAO = new BookingDAO();
        activityLogDAO = new com.example.pi_dev.common.dao.ActivityLogDAO();
    }

    @FXML
    public void initialize() {
        loadReservations();
    }

    private void loadReservations() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        try {
            List<Booking> bookings = bookingDAO.findByRenter(currentUser.getUserId().toString());
            reservationsList.setAll(bookings);
            
            reservationsFlowPane.getChildren().clear();
            for (Booking booking : reservationsList) {
                reservationsFlowPane.getChildren().add(createReservationCard(booking));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load your reservations: " + e.getMessage());
        }
    }

    private VBox createReservationCard(Booking booking) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 20; -fx-background-radius: 12; -fx-pref-width: 300; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 5); -fx-background-color: white;");

        String placeTitle = "Unknown Place";
        try {
            com.example.pi_dev.venue.dao.PlaceDAO placeDAO = new com.example.pi_dev.venue.dao.PlaceDAO();
            com.example.pi_dev.venue.entities.Place place = placeDAO.findById(booking.getPlaceId());
            if (place != null) placeTitle = place.getTitle();
        } catch (Exception e) {
            placeTitle = "Place #" + booking.getPlaceId();
        }

        Label titleLabel = new Label(placeTitle);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
        titleLabel.setWrapText(true);

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
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
            editBtn.setOnAction(e -> handleEdit(booking));

            Button cancelBtn = new Button("Cancel");
            cancelBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
            cancelBtn.setOnAction(e -> handleCancel(booking));

            actions.getChildren().addAll(editBtn, cancelBtn);
        } else if (booking.getStatus() == Booking.Status.CONFIRMED) {
            Button cancelBtn = new Button("Cancel");
            cancelBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
            cancelBtn.setOnAction(e -> handleCancel(booking));
            actions.getChildren().add(cancelBtn);
        }

        card.getChildren().addAll(titleLabel, details, actions);
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
        lbl.setStyle("-fx-text-fill: white; -fx-background-color: " + color + "; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px;");
        return lbl;
    }

    private void handleEdit(Booking booking) {
        Dialog<Booking> dialog = new Dialog<>();
        dialog.setTitle("Edit Reservation");
        dialog.setHeaderText("Modify your booking details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        DatePicker startDatePicker = new DatePicker(booking.getStartDate());
        DatePicker endDatePicker = new DatePicker(booking.getEndDate());
        Spinner<Integer> guestsSpinner = new Spinner<>(1, 20, booking.getGuestsCount());
        guestsSpinner.setEditable(true);

        content.getChildren().addAll(
            new Label("Start Date:"), startDatePicker,
            new Label("End Date:"), endDatePicker,
            new Label("Number of Guests:"), guestsSpinner
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                booking.setStartDate(startDatePicker.getValue());
                booking.setEndDate(endDatePicker.getValue());
                booking.setGuestsCount(guestsSpinner.getValue());
                return booking;
            }
            return null;
        });

        Optional<Booking> result = dialog.showAndWait();
        result.ifPresent(updatedBooking -> {
            try {
                // Recalculate price if dates changed
                com.example.pi_dev.venue.dao.PlaceDAO placeDAO = new com.example.pi_dev.venue.dao.PlaceDAO();
                com.example.pi_dev.venue.entities.Place place = placeDAO.findById(updatedBooking.getPlaceId());
                if (place != null) {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(startDatePicker.getValue(), endDatePicker.getValue());
                    if (days <= 0) days = 1;
                    updatedBooking.setTotalPrice(place.getPricePerDay() * days);
                }

                bookingDAO.update(updatedBooking);
                activityLogDAO.log(UserSession.getInstance().getCurrentUser().getEmail(), "BOOKING_UPDATE", "Updated booking #" + updatedBooking.getId());
                loadReservations();
            } catch (SQLException e) {
                showAlert("Error", "Failed to update booking: " + e.getMessage());
            }
        });
    }

    private void handleCancel(Booking booking) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Reservation");
        alert.setHeaderText("Are you sure you want to cancel this reservation?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                bookingDAO.delete(booking.getId());
                activityLogDAO.log(UserSession.getInstance().getCurrentUser().getEmail(), "BOOKING_CANCEL", "Cancelled booking #" + booking.getId());
                loadReservations();
            } catch (SQLException e) {
                showAlert("Error", "Failed to cancel booking: " + e.getMessage());
            }
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
