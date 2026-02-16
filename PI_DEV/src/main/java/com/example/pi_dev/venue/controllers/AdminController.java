package com.example.pi_dev.venue.controllers;

import com.example.pi_dev.venue.services.PlaceService;
import com.example.pi_dev.venue.entities.Place;
import com.example.pi_dev.user.utils.UserSession;
import com.example.pi_dev.user.enums.RoleEnum;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML
    private TableView<Place> pendingPlacesTable;

    @FXML
    private TableColumn<Place, String> titleColumn;

    @FXML
    private TableColumn<Place, String> hostColumn;

    @FXML
    private TableColumn<Place, Double> priceColumn;

    @FXML
    private TableColumn<Place, Void> actionColumn;

    private PlaceService placeService;
    private ObservableList<Place> pendingPlaces;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        placeService = new PlaceService();
        pendingPlaces = FXCollections.observableArrayList();

        // Security Check
        if (!UserSession.getInstance().isLoggedIn() ||
                UserSession.getInstance().getCurrentUser().getRole() != RoleEnum.ADMIN) {
            // Ideally redirect or show error. For now, just disabling.
            System.err.println("Access Denied: Not an Admin");
            return;
        }

        setupTable();
        loadPendingPlaces();
    }

    private void setupTable() {
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        hostColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHostId())); // Ideally
                                                                                                               // fetch
                                                                                                               // Host
                                                                                                               // Name
        priceColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPricePerDay()));

        // Action Buttons
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button denyBtn = new Button("Deny");
            private final HBox pane = new HBox(10, approveBtn, denyBtn);

            {
                approveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                denyBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                approveBtn.setOnAction(event -> {
                    Place place = getTableView().getItems().get(getIndex());
                    handleApprove(place);
                });

                denyBtn.setOnAction(event -> {
                    Place place = getTableView().getItems().get(getIndex());
                    handleDeny(place);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        pendingPlacesTable.setItems(pendingPlaces);
    }

    private void loadPendingPlaces() {
        try {
            pendingPlaces.setAll(placeService.findPending());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load pending places.");
        }
    }

    private void handleApprove(Place place) {
        try {
            placeService.updateStatus(place.getId(), Place.Status.APPROVED);
            pendingPlaces.remove(place);
            showAlert("Success", "Place approved successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to approve place.");
        }
    }

    private void handleDeny(Place place) {
        try {
            placeService.updateStatus(place.getId(), Place.Status.DENIED);
            pendingPlaces.remove(place);
            showAlert("Success", "Place denied.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to deny place.");
        }
    }

    @FXML
    private void handleBackToHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/pi_dev/venue/views/home-view.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) pendingPlacesTable.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
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
