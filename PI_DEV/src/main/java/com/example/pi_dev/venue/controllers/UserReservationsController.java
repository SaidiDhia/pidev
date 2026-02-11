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

public class UserReservationsController {

    @FXML private TableView<Booking> reservationsTable;
    @FXML private TableColumn<Booking, String> placeColumn;
    @FXML private TableColumn<Booking, String> datesColumn;
    @FXML private TableColumn<Booking, String> guestsColumn;
    @FXML private TableColumn<Booking, String> priceColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, Void> actionsColumn;

    private BookingDAO bookingDAO;
    private ObservableList<Booking> reservationsList = FXCollections.observableArrayList();

    public UserReservationsController() {
        bookingDAO = new BookingDAO();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadReservations();
    }

    private void setupTable() {
        placeColumn.setCellValueFactory(data -> new SimpleStringProperty("Place #" + data.getValue().getPlaceId()));
        datesColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStartDate() + " to " + data.getValue().getEndDate()));
        guestsColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getGuestsCount())));
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%.2f", data.getValue().getTotalPrice())));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox container = new HBox(10, editBtn, cancelBtn);

            {
                editBtn.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-size: 11px;");
                cancelBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 11px;");
                
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                cancelBtn.setOnAction(e -> handleCancel(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Booking b = getTableView().getItems().get(getIndex());
                    if (b.getStatus() == Booking.Status.PENDING) {
                        setGraphic(container);
                    } else if (b.getStatus() == Booking.Status.CONFIRMED) {
                        setGraphic(cancelBtn); // Can only cancel confirmed ones, not edit
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        reservationsTable.setItems(reservationsList);
    }

    private void loadReservations() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        try {
            List<Booking> bookings = bookingDAO.findByRenter(currentUser.getUserId().toString());
            reservationsList.setAll(bookings);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load your reservations: " + e.getMessage());
        }
    }

    private void handleEdit(Booking booking) {
        // Show a dialog to edit guests or dates
        TextInputDialog dialog = new TextInputDialog(String.valueOf(booking.getGuestsCount()));
        dialog.setTitle("Edit Reservation");
        dialog.setHeaderText("Change Number of Guests");
        dialog.setContentText("Enter new guest count:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(countStr -> {
            try {
                int newCount = Integer.parseInt(countStr);
                booking.setGuestsCount(newCount);
                bookingDAO.update(booking);
                loadReservations();
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number.");
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
        reservationsTable.getScene().getWindow().hide();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
