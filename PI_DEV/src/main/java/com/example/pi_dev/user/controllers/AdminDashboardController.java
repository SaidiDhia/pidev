package com.example.pi_dev.user.controllers;

import com.example.pi_dev.user.enums.RoleEnum;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.user.services.UserService;
import com.example.pi_dev.user.utils.UserSession;
import com.example.pi_dev.venue.dao.PlaceDAO;
import com.example.pi_dev.venue.entities.Place;
import com.example.pi_dev.venue.entities.Booking;
import com.example.pi_dev.venue.dao.BookingDAO;
import com.example.pi_dev.common.dao.ActivityLogDAO;
import com.example.pi_dev.common.models.ActivityLog;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class AdminDashboardController {

    @FXML private TextField searchField;
    @FXML private ComboBox<RoleEnum> roleFilter;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, User> colAvatar;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, Void> colActions;

    // Venue Requests Fields
    @FXML private FlowPane venueRequestsFlowPane;

    // All Places Fields
    @FXML private FlowPane allPlacesFlowPane;

    @FXML private FlowPane reservationsFlowPane;
    @FXML private TableView<ActivityLog> activityLogTable;
    @FXML private TableColumn<ActivityLog, String> colTimestamp;
    @FXML private TableColumn<ActivityLog, String> colUser;
    @FXML private TableColumn<ActivityLog, String> colAction;
    @FXML private TableColumn<ActivityLog, String> colDetails;

    private final UserService userService = new UserService();
    private final PlaceDAO placeDAO = new PlaceDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    
    private ObservableList<User> masterData = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;
    private ObservableList<Place> pendingPlaces = FXCollections.observableArrayList();
    private ObservableList<Place> allPlaces = FXCollections.observableArrayList();
    private ObservableList<ActivityLog> activityLogs = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadData();
        
        loadVenueData();

        setupActivityLogTable();
        loadActivityLogs();
        loadReservationsAsCards();
    }

    private void setupActivityLogTable() {
        colTimestamp.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimestamp().toString()));
        colUser.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserEmail()));
        colAction.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAction()));
        colDetails.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDetails()));
        activityLogTable.setItems(activityLogs);
    }

    private void loadActivityLogs() {
        try {
            activityLogs.setAll(activityLogDAO.findAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadReservationsAsCards() {
        try {
            java.util.List<Booking> allBookings = bookingDAO.findAll();
            reservationsFlowPane.getChildren().clear();
            for (Booking booking : allBookings) {
                reservationsFlowPane.getChildren().add(createReservationCard(booking));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createReservationCard(Booking booking) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(350);
        card.setStyle("-fx-padding: 20; -fx-background-radius: 12; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // Fetch User and Place details
        User renter = userService.getUserById(java.util.UUID.fromString(booking.getRenterId()));
        Place place = null;
        try {
            place = placeDAO.findById(booking.getPlaceId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Profile Picture
        Circle profileCircle = new Circle(25);
        Image avatarImage = null;
        if (renter != null && renter.getProfilePicture() != null && !renter.getProfilePicture().isEmpty()) {
            File file = new File("uploads/profiles/" + renter.getProfilePicture());
            if (file.exists()) {
                avatarImage = new Image(file.toURI().toString(), 50, 50, true, true);
            }
        }
        if (avatarImage == null) {
            java.io.InputStream stream = getClass().getResourceAsStream("/com/example/pi_dev/user/default-avatar.png");
            if (stream != null) {
                avatarImage = new Image(stream, 50, 50, true, true);
            }
        }
        
        if (avatarImage != null) {
            profileCircle.setFill(new ImagePattern(avatarImage));
        } else {
            profileCircle.setFill(javafx.scene.paint.Color.web("#D1D5DB"));
        }

        VBox userDetails = new VBox(2);
        Label nameLabel = new Label(renter != null ? renter.getFullName() : "Unknown User");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label emailLabel = new Label(renter != null ? renter.getEmail() : booking.getRenterId());
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        Label phoneLabel = new Label(renter != null ? renter.getPhoneNumber() : "No phone");
        phoneLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        
        userDetails.getChildren().addAll(nameLabel, emailLabel, phoneLabel);
        header.getChildren().addAll(profileCircle, userDetails);

        Separator separator = new Separator();

        VBox bookingInfo = new VBox(8);
        bookingInfo.getChildren().addAll(
            createDetailRow("Venue:", place != null ? place.getTitle() : "Place #" + booking.getPlaceId()),
            createDetailRow("Check-in:", booking.getStartDate().toString()),
            createDetailRow("Check-out:", booking.getEndDate().toString()),
            createDetailRow("Guests:", String.valueOf(booking.getGuestsCount())),
            createDetailRow("Total:", String.format("$%.2f", booking.getTotalPrice()))
        );

        Label statusBadge = new Label(booking.getStatus().name());
        String statusColor = switch (booking.getStatus()) {
            case PENDING -> "#FBBF24";
            case CONFIRMED -> "#10B981";
            case REJECTED, CANCELLED -> "#EF4444";
            case COMPLETED -> "#6366F1";
            default -> "#6B7280";
        };
        statusBadge.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        HBox footer = new HBox(statusBadge);
        footer.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(header, separator, bookingInfo, footer);
        return card;
    }

    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(10);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B7280; -fx-min-width: 70;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #374151;");
        row.getChildren().addAll(lbl, val);
        return row;
    }

    private void loadVenueData() {
        try {
            pendingPlaces.setAll(placeDAO.findPending());
            allPlaces.setAll(placeDAO.findAll());

            venueRequestsFlowPane.getChildren().clear();
            for (Place place : pendingPlaces) {
                venueRequestsFlowPane.getChildren().add(createPlaceCard(place, true));
            }

            allPlacesFlowPane.getChildren().clear();
            for (Place place : allPlaces) {
                allPlacesFlowPane.getChildren().add(createPlaceCard(place, false));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load places.");
        }
    }

    private VBox createPlaceCard(Place place, boolean isRequest) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 0; -fx-background-radius: 12; -fx-pref-width: 380; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-background-color: white; -fx-overflow: hidden;");

        // Place Image Header
        VBox imageContainer = new VBox();
        imageContainer.setStyle("-fx-background-radius: 12 12 0 0; -fx-overflow: hidden;");
        ImageView placeImageView = new ImageView();
        placeImageView.setFitWidth(380);
        placeImageView.setFitHeight(180);
        placeImageView.setPreserveRatio(false);
        
        Image placeImage = null;
        if (place.getImageUrl() != null && !place.getImageUrl().isEmpty()) {
            File file = new File("uploads/places/" + place.getImageUrl());
            if (file.exists()) {
                placeImage = new Image(file.toURI().toString(), 380, 180, false, true);
            }
        }
        if (placeImage == null) {
            java.io.InputStream stream = getClass().getResourceAsStream("/com/example/pi_dev/user/default-place.png");
            if (stream != null) {
                placeImage = new Image(stream, 380, 180, false, true);
            }
        }
        if (placeImage != null) {
            placeImageView.setImage(placeImage);
        } else {
            // If no image, show a placeholder background
            imageContainer.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 12 12 0 0; -fx-min-height: 180;");
        }
        
        // Rounded corners for the image header
        Rectangle clip = new Rectangle(380, 180);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        placeImageView.setClip(clip);
        imageContainer.getChildren().add(placeImageView);

        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 20;");

        // Place Title
        Label titleLabel = new Label(place.getTitle());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
        titleLabel.setWrapText(true);

        // Category Badge
        Label categoryBadge = new Label(place.getCategory());
        categoryBadge.setStyle("-fx-background-color: #EEF2FF; -fx-text-fill: #4F46E5; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(titleLabel, categoryBadge);

        // Fetch Host details
        User host = null;
        try {
            host = userService.getUserById(java.util.UUID.fromString(place.getHostId()));
        } catch (Exception e) {
            System.err.println("Error fetching host: " + e.getMessage());
        }

        // Host Info Section
        HBox hostInfo = new HBox(12);
        hostInfo.setAlignment(Pos.CENTER_LEFT);
        hostInfo.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 12; -fx-background-radius: 10; -fx-border-color: #E5E7EB; -fx-border-radius: 10;");

        // Host Profile Picture
        Circle profileCircle = new Circle(22);
        Image avatarImage = null;
        if (host != null && host.getProfilePicture() != null && !host.getProfilePicture().isEmpty()) {
            File file = new File("uploads/profiles/" + host.getProfilePicture());
            if (file.exists()) {
                avatarImage = new Image(file.toURI().toString(), 44, 44, true, true);
            }
        }
        if (avatarImage == null) {
            java.io.InputStream stream = getClass().getResourceAsStream("/com/example/pi_dev/user/default-avatar.png");
            if (stream != null) {
                avatarImage = new Image(stream, 44, 44, true, true);
            }
        }
        
        if (avatarImage != null) {
            profileCircle.setFill(new ImagePattern(avatarImage));
        } else {
            // Fallback: Gray circle with initials or just color
            profileCircle.setFill(javafx.scene.paint.Color.web("#D1D5DB"));
        }

        VBox hostDetails = new VBox(2);
        Label hostNameLabel = new Label(host != null ? host.getFullName() : "Unknown Host");
        hostNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #111827;");
        Label hostEmailLabel = new Label(host != null ? host.getEmail() : "No email");
        hostEmailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        Label hostPhoneLabel = new Label(host != null ? host.getPhoneNumber() : "No phone");
        hostPhoneLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        
        hostDetails.getChildren().addAll(hostNameLabel, hostEmailLabel, hostPhoneLabel);
        hostInfo.getChildren().addAll(profileCircle, hostDetails);

        // Description
        Label descLabel = new Label(place.getDescription());
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4B5563; -fx-line-spacing: 2;");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(60);

        // Grid-like details
        VBox details = new VBox(10);
        details.setStyle("-fx-background-color: #FFFFFF;");
        
        HBox locationRow = createDetailRow("Address:", place.getAddress() + ", " + place.getCity());
        HBox priceRow = createDetailRow("Price:", String.format("$%.2f / day", place.getPricePerDay()));
        
        HBox capacityRow = new HBox(20);
        capacityRow.getChildren().addAll(
            createDetailRow("Capacity:", String.valueOf(place.getCapacity())),
            createDetailRow("Max Guests:", String.valueOf(place.getMaxGuests()))
        );

        details.getChildren().addAll(locationRow, priceRow, capacityRow);

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setStyle("-fx-padding: 10 0 0 0;");

        if (isRequest) {
            Button approveBtn = new Button("Approve Request");
            approveBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: bold;");
            approveBtn.setOnAction(e -> handleApprovePlace(place));

            Button denyBtn = new Button("Reject");
            denyBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: bold;");
            denyBtn.setOnAction(e -> handleDenyPlace(place));

            actions.getChildren().addAll(denyBtn, approveBtn);
        } else {
            Button deleteBtn = new Button("Remove Venue");
            deleteBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: bold;");
            deleteBtn.setOnAction(e -> handleDeletePlace(place));
            actions.getChildren().add(deleteBtn);
        }

        content.getChildren().addAll(titleRow, hostInfo, descLabel, details, actions);
        card.getChildren().addAll(imageContainer, content);
        return card;
    }

    private void handleDeletePlace(Place place) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Place");
        alert.setHeaderText("Delete " + place.getTitle() + "?");
        alert.setContentText("Are you sure you want to delete this place? This will also delete all associated images and bookings.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                placeDAO.delete(place.getId());
                loadVenueData();
                activityLogDAO.log(UserSession.getInstance().getCurrentUser().getEmail(), "PLACE_DELETE", "Deleted place: " + place.getTitle());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to delete place.");
            }
        }
    }

    private void handleApprovePlace(Place place) {
        try {
            placeDAO.updateStatus(place.getId(), Place.Status.APPROVED);
            loadVenueData();
            showAlert("Success", "Place approved successfully.");
            activityLogDAO.log(UserSession.getInstance().getCurrentUser().getEmail(), "PLACE_APPROVE", "Approved place: " + place.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to approve place.");
        }
    }

    private void handleDenyPlace(Place place) {
        try {
            placeDAO.updateStatus(place.getId(), Place.Status.DENIED);
            loadVenueData();
            showAlert("Success", "Place denied.");
            activityLogDAO.log(UserSession.getInstance().getCurrentUser().getEmail(), "PLACE_DENY", "Denied place: " + place.getTitle());
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
        colAvatar.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        colAvatar.setCellFactory(param -> new TableCell<>() {
            private final Circle circle = new Circle(20);
            
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    String photoPath = user.getProfilePicture();
                    Image image = null;
                    if (photoPath != null && !photoPath.isEmpty()) {
                        File file = new File("uploads/profiles/" + photoPath);
                        if (file.exists()) {
                            image = new Image(file.toURI().toString(), 40, 40, true, true);
                        }
                    }
                    
                    if (image == null) {
                        java.io.InputStream stream = getClass().getResourceAsStream("/com/example/pi_dev/user/default-avatar.png");
                        if (stream != null) {
                            image = new Image(stream, 40, 40, true, true);
                        }
                    }
                    
                    if (image != null) {
                        circle.setFill(new ImagePattern(image));
                    } else {
                        circle.setFill(javafx.scene.paint.Color.web("#D1D5DB"));
                    }
                    setGraphic(circle);
                    setAlignment(Pos.CENTER);
                }
            }
        });

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