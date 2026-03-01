package com.example.pi_dev.booking.Controllers.Admin;

import com.example.pi_dev.booking.Entities.Place;
import com.example.pi_dev.booking.Services.PlaceService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;

public class AdminRentingsController {

    @FXML
    private TableView<Place> placesTable;
    @FXML
    private TableColumn<Place, String> colTitle;
    @FXML
    private TableColumn<Place, String> colCity;
    @FXML
    private TableColumn<Place, Double> colPrice;
    @FXML
    private TableColumn<Place, String> colStatus;
    @FXML
    private TableColumn<Place, Void> colActions;

    private final PlaceService placeService = new PlaceService();

    @FXML
    public void initialize() {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colCity.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCity()));
        colPrice.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPricePerDay()).asObject());
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button denyBtn = new Button("Deny");
            private final HBox box = new HBox(6, approveBtn, denyBtn);

            {
                approveBtn.getStyleClass().addAll("btn", "btn-sm", "btn-success");
                denyBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");

                approveBtn.setOnAction(e -> {
                    Place p = getTableView().getItems().get(getIndex());
                    placeService.updateStatus(p.getId(), Place.Status.APPROVED);
                    loadData();
                });
                denyBtn.setOnAction(e -> {
                    Place p = getTableView().getItems().get(getIndex());
                    placeService.updateStatus(p.getId(), Place.Status.DENIED);
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
        List<Place> pending = placeService.findPending();
        placesTable.setItems(FXCollections.observableArrayList(pending));
    }
}
