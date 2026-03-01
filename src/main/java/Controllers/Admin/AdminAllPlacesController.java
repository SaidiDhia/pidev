package Controllers.Admin;

import Entities.Place;
import Services.PlaceService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;

public class AdminAllPlacesController {

    @FXML
    private TableView<Place> placesTable;
    @FXML
    private TableColumn<Place, Integer> colId;
    @FXML
    private TableColumn<Place, String> colTitle;
    @FXML
    private TableColumn<Place, String> colCity;
    @FXML
    private TableColumn<Place, Double> colPrice;
    @FXML
    private TableColumn<Place, String> colStatus;
    @FXML
    private TableColumn<Place, String> colHost;
    @FXML
    private TableColumn<Place, Void> colActions;

    private final PlaceService placeService = new PlaceService();

    @FXML
    public void initialize() {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colCity.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCity()));
        colPrice.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPricePerDay()).asObject());
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        colHost.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHostId()));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button denyBtn = new Button("Deny");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, approveBtn, denyBtn, deleteBtn);

            {
                approveBtn.getStyleClass().addAll("btn", "btn-sm");
                approveBtn.setStyle(
                        "-fx-background-color: #16a34a; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 8 4 8;");
                denyBtn.getStyleClass().add("btn-sm");
                denyBtn.setStyle(
                        "-fx-background-color: #d97706; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 8 4 8;");
                deleteBtn.getStyleClass().add("btn-sm");
                deleteBtn.setStyle(
                        "-fx-background-color: #DC2626; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 8 4 8;");

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
                deleteBtn.setOnAction(e -> {
                    Place p = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete \"" + p.getTitle() + "\"?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.YES) {
                            placeService.supprimerPlace(p.getId());
                            loadData();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Place p = getTableView().getItems().get(getIndex());
                    // Show only relevant actions based on status
                    approveBtn.setVisible(p.getStatus() != Place.Status.APPROVED);
                    denyBtn.setVisible(p.getStatus() != Place.Status.DENIED);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadData() {
        List<Place> all = placeService.afficherPlaces();
        placesTable.setItems(FXCollections.observableArrayList(all));
    }
}
