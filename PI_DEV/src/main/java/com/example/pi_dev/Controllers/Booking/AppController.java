package com.example.pi_dev.Controllers.Booking;

import com.example.pi_dev.enums.RoleEnum;
import com.example.pi_dev.Entities.Users.User;
import com.example.pi_dev.Utils.Users.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AppController {

    @FXML
    private StackPane contentPane;

    @FXML
    private Button browseBtn;

    @FXML
    private Button listPlaceBtn;

    @FXML
    private Button myBookingsBtn;

    @FXML
    private Button myPlacesBtn;

    @FXML
    private Button adminBtn;

    @FXML
    public void initialize() {
        checkUserRoles();
        loadView("/com/example/pi_dev/booking/views/front/PlaceBrowse.fxml");
    }

    private void checkUserRoles() {
        if (UserSession.getInstance().isLoggedIn()) {
            User user = UserSession.getInstance().getCurrentUser();
            boolean isAdmin = user.getRole() == RoleEnum.ADMIN;
            boolean isHost = user.getRole() == RoleEnum.HOST;

            // Show admin tab only for admins
            if (adminBtn != null) {
                adminBtn.setVisible(isAdmin);
                adminBtn.setManaged(isAdmin);
            }

            // Host features
            if (myPlacesBtn != null) {
                myPlacesBtn.setVisible(isHost || isAdmin);
                myPlacesBtn.setManaged(isHost || isAdmin);
            }
            
            if (listPlaceBtn != null) {
                listPlaceBtn.setVisible(isHost || isAdmin || user.getRole() == RoleEnum.PARTICIPANT);
                listPlaceBtn.setManaged(isHost || isAdmin || user.getRole() == RoleEnum.PARTICIPANT);
            }
        }
    }

    @FXML
    private void handleHome() {
        loadView("/com/example/pi_dev/booking/views/front/PlaceBrowse.fxml");
    }

    @FXML
    private void handleListYourPlace() {
        loadView("/com/example/pi_dev/booking/views/host/PlaceForm.fxml");
    }

    @FXML
    private void handleMyBookings() {
        loadView("/com/example/pi_dev/booking/views/front/MyBookings.fxml");
    }

    @FXML
    private void handleMyPlaces() {
        loadView("/com/example/pi_dev/booking/views/host/MyPlaces.fxml");
    }

    @FXML
    private void handleAdminDashboard() {
        loadView("/com/example/pi_dev/booking/views/admin/AdminDashboard.fxml");
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load view: " + fxmlPath, e);
        }
    }

    public StackPane getContentPane() {
        return contentPane;
    }
}
