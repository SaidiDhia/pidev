package com.example.pi_dev.Controllers.Booking.Admin;

import com.example.pi_dev.Entities.Booking.Booking;
import com.example.pi_dev.Services.Booking.BookingService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;

public class AdminReservationsController {

    @FXML
    private TableView<BookingService.BookingView> bookingsTable;
    @FXML
    private TableColumn<BookingService.BookingView, String> colProperty;
    @FXML
    private TableColumn<BookingService.BookingView, String> colDates;
    @FXML
    private TableColumn<BookingService.BookingView, Integer> colGuests;
    @FXML
    private TableColumn<BookingService.BookingView, Double> colTotal;
    @FXML
    private TableColumn<BookingService.BookingView, String> colStatus;
    @FXML
    private TableColumn<BookingService.BookingView, Void> colActions;

    private final BookingService bookingService = new BookingService();

    @FXML
    public void initialize() {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        colProperty.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().placeTitle));

        colDates.setCellValueFactory(d -> {
            Booking b = d.getValue().booking;
            return new SimpleStringProperty(b.getStartDate() + " → " + b.getEndDate());
        });

        colGuests.setCellValueFactory(
                d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().booking.getGuestsCount()).asObject());

        colTotal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().booking.getTotalPrice()).asObject());

        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().booking.getStatus().name()));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button confirmBtn = new Button("Confirm");
            private final Button rejectBtn = new Button("Reject");
            private final HBox box = new HBox(6, confirmBtn, rejectBtn);

            {
                confirmBtn.getStyleClass().addAll("btn", "btn-sm", "btn-success");
                rejectBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");

                confirmBtn.setOnAction(e -> {
                    BookingService.BookingView bv = getTableView().getItems().get(getIndex());
                    bookingService.updateStatus(bv.booking.getId(), Booking.Status.CONFIRMED);
                    loadData();
                });
                rejectBtn.setOnAction(e -> {
                    BookingService.BookingView bv = getTableView().getItems().get(getIndex());
                    bookingService.updateStatus(bv.booking.getId(), Booking.Status.REJECTED);
                    loadData();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadData() {
        List<BookingService.BookingView> views = bookingService.findPendingWithTitle();
        bookingsTable.setItems(FXCollections.observableArrayList(views));
    }
}
