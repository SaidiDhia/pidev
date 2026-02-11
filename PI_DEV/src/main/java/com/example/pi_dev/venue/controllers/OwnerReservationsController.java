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

public class OwnerReservationsController {

    @FXML private TableView<Booking> reservationsTable;
    @FXML private TableColumn<Booking, String> placeColumn;
    @FXML private TableColumn<Booking, String> renterColumn;
    @FXML private TableColumn<Booking, String> datesColumn;
    @FXML private TableColumn<Booking, String> guestsColumn;
    @FXML private TableColumn<Booking, String> priceColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, Void> actionsColumn;

    private BookingDAO bookingDAO;
    private ObservableList<Booking> reservationsList = FXCollections.observableArrayList();

    public OwnerReservationsController() {
        bookingDAO = new BookingDAO();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadReservations();
    }

    private void setupTable() {
        placeColumn.setCellValueFactory(data -> new SimpleStringProperty("Place #" + data.getValue().getPlaceId()));
        renterColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRenterId()));
        datesColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStartDate() + " to " + data.getValue().getEndDate()));
        guestsColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getGuestsCount())));
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%.2f", data.getValue().getTotalPrice())));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox container = new HBox(10, approveBtn, rejectBtn);

            {
                approveBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-size: 11px;");
                rejectBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 11px;");
                
                approveBtn.setOnAction(e -> handleApprove(getTableView().getItems().get(getIndex())));
                rejectBtn.setOnAction(e -> handleReject(getTableView().getItems().get(getIndex())));
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
            List<Booking> bookings = bookingDAO.findByOwner(currentUser.getUserId().toString());
            reservationsList.setAll(bookings);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load reservations: " + e.getMessage());
        }
    }

    private void handleApprove(Booking booking) {
        try {
            bookingDAO.updateStatus(booking.getId(), Booking.Status.CONFIRMED);
            loadReservations();
        } catch (SQLException e) {
            showAlert("Error", "Failed to approve booking: " + e.getMessage());
        }
    }

    private void handleReject(Booking booking) {
        try {
            bookingDAO.updateStatus(booking.getId(), Booking.Status.REJECTED);
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
        // Implementation depends on how navigation is handled in the project
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
