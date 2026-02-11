package com.example.pi_dev.user.controllers;

import com.example.pi_dev.user.enums.RoleEnum;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.user.services.UserService;
import com.example.pi_dev.user.utils.UserSession;
import com.example.pi_dev.venue.dao.PlaceDAO;
import com.example.pi_dev.venue.entities.Place;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class AdminDashboardController {

    @FXML private TextField searchField;
    @FXML private ComboBox<RoleEnum> roleFilter;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, Void> colActions;

    // Venue Requests Fields
    @FXML private TableView<Place> pendingPlacesTable;
    @FXML private TableColumn<Place, String> titleColumn;
    @FXML private TableColumn<Place, String> hostColumn;
    @FXML private TableColumn<Place, Double> priceColumn;
    @FXML private TableColumn<Place, Void> actionColumn;

    private final UserService userService = new UserService();
    private final PlaceDAO placeDAO = new PlaceDAO();
    
    private ObservableList<User> masterData = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;
    private ObservableList<Place> pendingPlaces = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadData();
        
        setupVenueTable();
        loadVenueData();
    }

    private void setupVenueTable() {
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        hostColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHostId()));
        priceColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPricePerDay()));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button denyBtn = new Button("Deny");
            private final HBox pane = new HBox(10, approveBtn, denyBtn);

            {
                approveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 10;");
                denyBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 10;");

                approveBtn.setOnAction(event -> {
                    Place place = getTableView().getItems().get(getIndex());
                    handleApprovePlace(place);
                });

                denyBtn.setOnAction(event -> {
                    Place place = getTableView().getItems().get(getIndex());
                    handleDenyPlace(place);
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

    private void loadVenueData() {
        try {
            pendingPlaces.setAll(placeDAO.findPending());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load pending places.");
        }
    }

    private void handleApprovePlace(Place place) {
        try {
            placeDAO.updateStatus(place.getId(), Place.Status.APPROVED);
            pendingPlaces.remove(place);
            showAlert("Success", "Place approved successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to approve place.");
        }
    }

    private void handleDenyPlace(Place place) {
        try {
            placeDAO.updateStatus(place.getId(), Place.Status.DENIED);
            pendingPlaces.remove(place);
            showAlert("Success", "Place denied.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to deny place.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setupTable() {
        colName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
        colEmail.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        colPhone.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhoneNumber()));
        colRole.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole().name()));
        colStatus.setCellValueFactory(cellData -> {
            boolean isActive = cellData.getValue().getIsActive() != null && cellData.getValue().getIsActive();
            return new SimpleStringProperty(isActive ? "Active" : "Inactive");
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("button-primary");
                editBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 10;");
                deleteBtn.getStyleClass().add("button-secondary");
                deleteBtn.setStyle("-fx-text-fill: red; -fx-padding: 5 10; -fx-font-size: 10;");

                editBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });

                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
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
    }

    private void setupFilters() {
        roleFilter.setItems(FXCollections.observableArrayList(RoleEnum.values()));
        
        filteredData = new FilteredList<>(masterData, p -> true);
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        roleFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        
        userTable.setItems(filteredData);
    }

    private void updateFilter() {
        filteredData.setPredicate(user -> {
            String searchText = searchField.getText().toLowerCase();
            RoleEnum selectedRole = roleFilter.getValue();

            boolean matchesSearch = searchText.isEmpty() || 
                                    (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchText)) ||
                                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText));
            
            boolean matchesRole = selectedRole == null || user.getRole() == selectedRole;

            return matchesSearch && matchesRole;
        });
    }

    private void loadData() {
        masterData.setAll(userService.getAllUsers());
    }

    @FXML
    void handleResetFilters(ActionEvent event) {
        searchField.clear();
        roleFilter.setValue(null);
    }

    @FXML
    void handleAddUser(ActionEvent event) {
        openUserForm(null);
    }

    private void handleEditUser(User user) {
        openUserForm(user);
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete " + user.getFullName() + "?");
        alert.setContentText("Are you sure you want to delete this user? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userService.deleteUser(user.getUserId());
            loadData();
        }
    }

    private void openUserForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pi_dev/user/user_form.fxml"));
            Parent root = loader.load();
            
            UserFormController controller = loader.getController();
            controller.setService(userService);
            controller.setUser(user);
            controller.setOnSaveCallback(this::loadData);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(user == null ? "Add User" : "Edit User");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSettings(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pi_dev/user/settings.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleBackToHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pi_dev/venue/views/home-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        UserSession.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pi_dev/user/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}