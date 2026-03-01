package com.example.pi_dev.booking.Controllers.Front;

import com.example.pi_dev.booking.Entities.Booking;
import com.example.pi_dev.booking.Entities.Place;
import com.example.pi_dev.booking.Services.BookingService;
import com.example.pi_dev.booking.Services.PlaceService;
import com.example.pi_dev.booking.Services.ReceiptService;
import com.example.pi_dev.booking.Utils.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class MyBookingsController {

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
    private TableColumn<BookingService.BookingView, Void> colPdf;
    @FXML
    private TableColumn<BookingService.BookingView, Void> colActions;

    private final BookingService bookingService = new BookingService();
    private final PlaceService placeService = new PlaceService();
    private final ReceiptService receiptService = new ReceiptService();

    @FXML
    public void initialize() {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        colProperty.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().placeTitle));

        colDates.setCellValueFactory(data -> {
            Booking b = data.getValue().booking;
            return new SimpleStringProperty(b.getStartDate() + " → " + b.getEndDate());
        });

        colGuests.setCellValueFactory(
                data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().booking.getGuestsCount())
                        .asObject());

        colTotal.setCellValueFactory(
                data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().booking.getTotalPrice())
                        .asObject());

        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().booking.getStatus().name()));

        // Receipt download column
        colPdf.setCellFactory(col -> new TableCell<>() {
            private final Button downloadBtn = new Button("📄 Receipt");

            {
                downloadBtn.setStyle("-fx-background-color: #0EA5E9; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-padding: 3 10 3 10; -fx-cursor: hand; -fx-font-size: 11px;");
                downloadBtn.setOnAction(e -> {
                    BookingService.BookingView bv = getTableView().getItems().get(getIndex());
                    handleDownloadReceipt(bv);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : downloadBtn);
            }
        });

        // Actions column
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().addAll("btn", "btn-sm");
                deleteBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");

                editBtn.setOnAction(e -> {
                    BookingService.BookingView bv = getTableView().getItems().get(getIndex());
                    handleEdit(bv);
                });
                deleteBtn.setOnAction(e -> {
                    BookingService.BookingView bv = getTableView().getItems().get(getIndex());
                    handleDelete(bv);
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
                    editBtn.setDisable(!isPending);
                    deleteBtn.setDisable(!isPending && bv.booking.getStatus() != Booking.Status.CANCELLED);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadData() {
        List<BookingService.BookingView> views = bookingService.findByUserWithTitle(Session.currentUserId);
        bookingsTable.setItems(FXCollections.observableArrayList(views));
    }

    private void handleDownloadReceipt(BookingService.BookingView bv) {
        try {
            Place place = placeService.getPlaceById(bv.booking.getPlaceId());
            Path receiptFile = receiptService.generateReceipt(bv.booking, place);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(receiptFile.toFile().toURI());
            } else {
                new Alert(Alert.AlertType.INFORMATION,
                        "Receipt saved:\n" + receiptFile.toAbsolutePath(), ButtonType.OK).showAndWait();
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Could not generate receipt: " + e.getMessage(), ButtonType.OK)
                    .showAndWait();
        }
    }

    private void handleEdit(BookingService.BookingView bv) {
        if (bv.booking.getStatus() != Booking.Status.PENDING) {
            new Alert(Alert.AlertType.WARNING, "Only PENDING bookings can be edited.", ButtonType.OK).showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pi_dev/booking/views/front/BookingDialog.fxml"));
            Node root = loader.load();
            BookingDialogController ctrl = loader.getController();
            Place place = placeService.getPlaceById(bv.booking.getPlaceId());
            ctrl.setEditingBooking(bv.booking, place);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Edit Booking");
            dialog.setScene(new Scene((javafx.scene.Parent) root));
            dialog.showAndWait();
            loadData();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Cannot open edit dialog: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private void handleDelete(BookingService.BookingView bv) {
        Booking.Status status = bv.booking.getStatus();
        if (status != Booking.Status.PENDING && status != Booking.Status.CANCELLED) {
            new Alert(Alert.AlertType.WARNING, "Only PENDING or CANCELLED bookings can be deleted.", ButtonType.OK)
                    .showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this booking?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                bookingService.supprimerBooking(bv.booking.getId());
                loadData();
            }
        });
    }
}
