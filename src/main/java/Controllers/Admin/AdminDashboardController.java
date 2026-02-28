package Controllers.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab rentingsTab;
    @FXML
    private Tab reservationsTab;
    @FXML
    private Tab allPlacesTab;

    @FXML
    public void initialize() {
        loadTab(rentingsTab, "/views/admin/AdminRentings.fxml");
        loadTab(reservationsTab, "/views/admin/AdminReservations.fxml");
        loadTab(allPlacesTab, "/views/admin/AdminAllPlaces.fxml");
    }

    private void loadTab(Tab tab, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            tab.setContent(content);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                    "Cannot load tab: " + fxmlPath + "\n" + e.getMessage(), ButtonType.OK)
                    .showAndWait();
        }
    }
}
