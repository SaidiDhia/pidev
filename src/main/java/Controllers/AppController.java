package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AppController {

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        loadView("/views/front/PlaceBrowse.fxml");
    }

    @FXML
    private void handleHome() {
        loadView("/views/front/PlaceBrowse.fxml");
    }

    @FXML
    private void handleListYourPlace() {
        loadView("/views/host/PlaceForm.fxml");
    }

    @FXML
    private void handleMyBookings() {
        loadView("/views/front/MyBookings.fxml");
    }

    @FXML
    private void handleMyPlaces() {
        loadView("/views/host/MyPlaces.fxml");
    }

    @FXML
    private void handleAdminDashboard() {
        loadView("/views/admin/AdminDashboard.fxml");
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
