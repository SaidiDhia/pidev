package com.example.pi_dev.booking.Controllers.Admin;

import com.example.pi_dev.booking.Entities.Booking;
import com.example.pi_dev.booking.Services.BookingService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

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
            private final Button confirmBtn = new Button("✔ Confirm");
            private final Button rejectBtn = new Button("✖ Reject");
            private final Button cancelBtn = new Button("⊘ Cancel");
            private final HBox box = new HBox(6, confirmBtn, rejectBtn, cancelBtn);

            {
                confirmBtn.getStyleClass().addAll("btn", "btn-sm", "btn-success");
                rejectBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
                cancelBtn.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-padding: 3 10 3 10; -fx-cursor: hand; -fx-font-size: 11px;");

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
                cancelBtn.setOnAction(e -> {
                    BookingService.BookingView bv = getTableView().getItems().get(getIndex());
                    handleAdminCancel(bv);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    BookingService.BookingView bv = getTableView().getItems().get(getIndex());
                    Booking.Status status = bv.booking.getStatus();
                    boolean isPending = status == Booking.Status.PENDING;
                    boolean isConfirmed = status == Booking.Status.CONFIRMED;

                    // Confirm/Reject only on PENDING
                    confirmBtn.setDisable(!isPending);
                    rejectBtn.setDisable(!isPending);

                    // Admin Cancel available for PENDING or CONFIRMED
                    boolean canAdminCancel = isPending || isConfirmed;
                    cancelBtn.setVisible(canAdminCancel);
                    cancelBtn.setManaged(canAdminCancel);

                    setGraphic(box);
                }
            }
        });
    }

    private void handleAdminCancel(BookingService.BookingView bv) {
        Booking.Status status = bv.booking.getStatus();

        if (status != Booking.Status.PENDING && status != Booking.Status.CONFIRMED) {
            new Alert(Alert.AlertType.WARNING,
                    "Annulation admin impossible pour le statut : " + status.name(), ButtonType.OK).showAndWait();
            return;
        }

        // Ask for a reason
        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Annulation Admin");
        reasonDialog.setHeaderText("Annuler la réservation #" + bv.booking.getId() + " [" + bv.placeTitle + "]");
        reasonDialog.setContentText("Raison (optionnelle) :");

        Optional<String> result = reasonDialog.showAndWait();
        if (result.isEmpty()) {
            return; // Admin cancelled the dialog
        }

        String reason = result.get().trim().isEmpty() ? null : result.get().trim();

        try {
            double refund = bookingService.cancelByAdmin(bv.booking.getId(), reason);
            loadData();

            String successMsg;
            if (refund > 0) {
                double percent = (refund / bv.booking.getTotalPrice()) * 100;
                successMsg = String.format(
                        "Réservation #%d annulée par l'admin.%nRemboursement : %.0f%% = %.2f$",
                        bv.booking.getId(), percent, refund);
            } else {
                successMsg = "Réservation #" + bv.booking.getId() + " annulée par l'admin.\nRemboursement : 0$";
            }
            new Alert(Alert.AlertType.INFORMATION, successMsg, ButtonType.OK).showAndWait();

        } catch (RuntimeException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private void loadData() {
        // Show ALL bookings (not just PENDING) so admin can manage any status
        List<BookingService.BookingView> views = bookingService.findAllWithTitle();
        bookingsTable.setItems(FXCollections.observableArrayList(views));
    }
}
