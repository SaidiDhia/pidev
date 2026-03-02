package com.example.pi_dev.Controllers.Booking.Host;

import com.example.pi_dev.Entities.Booking.Booking;
import com.example.pi_dev.Services.Booking.BookingService;
import com.example.pi_dev.Utils.Booking.Session;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.util.List;

public class HostReservationsController {

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
            return new SimpleStringProperty(b.getStartDate() + " to " + b.getEndDate());
        });

        colGuests.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().booking.getGuestsCount()).asObject());

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
                if (empty) {
                    setGraphic(null);
                } else {
                    BookingService.BookingView bv = getTableView().getItems().get(getIndex());
                    boolean isPending = bv.booking.getStatus() == Booking.Status.PENDING;
                    confirmBtn.setVisible(isPending);
                    confirmBtn.setManaged(isPending);
                    rejectBtn.setVisible(isPending);
                    rejectBtn.setManaged(isPending);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadData() {
        if (Session.currentUserId == null) return;
        List<BookingService.BookingView> views = bookingService.findByHostWithTitle(Session.currentUserId);
        bookingsTable.setItems(FXCollections.observableArrayList(views));
    }
}
