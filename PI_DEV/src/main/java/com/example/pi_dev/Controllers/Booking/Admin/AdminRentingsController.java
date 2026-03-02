package com.example.pi_dev.Controllers.Booking.Admin;

import com.example.pi_dev.Entities.Booking.Place;
import com.example.pi_dev.Services.Booking.PlaceService;
import com.example.pi_dev.enums.RoleEnum;
import com.example.pi_dev.Entities.Users.User;
import com.example.pi_dev.Repositories.Users.UserRepository;
import com.example.pi_dev.Utils.Users.UserSession;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

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
    private final UserRepository userRepository = new UserRepository();

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

                    // Update user role to HOST when place is approved, unless they are already ADMIN or HOST
                    try {
                        UUID hostId = UUID.fromString(p.getHostId());
                        userRepository.findById(hostId).ifPresent(user -> {
                            if (user.getRole() != RoleEnum.ADMIN && user.getRole() != RoleEnum.HOST) {
                                try {
                                    userRepository.updateRole(hostId, RoleEnum.HOST);

                                    // If the user being approved is the current logged-in user,
                                    // update their role in memory so the UI refreshes
                                    User currentUser = UserSession.getInstance().getCurrentUser();
                                    if (currentUser != null && currentUser.getUserId().equals(hostId)) {
                                        currentUser.setRole(RoleEnum.HOST);
                                    com.example.pi_dev.Utils.Booking.Session.update();
                                    }
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                    } catch (SQLException | IllegalArgumentException ex) {
                        ex.printStackTrace();
                    }

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
