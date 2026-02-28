package Controllers.Host;

import Entities.Place;
import Services.PlaceService;
import Utils.Session;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;

public class MyPlacesController {

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
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().addAll("btn", "btn-sm");
                deleteBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");

                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadData() {
        List<Place> places = placeService.findByHost(Session.currentUserId);
        placesTable.setItems(FXCollections.observableArrayList(places));
    }

    private void handleEdit(Place place) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/host/PlaceForm.fxml"));
            Node view = loader.load();
            PlaceFormController ctrl = loader.getController();
            ctrl.setEditingPlace(place);

            StackPane parent = (StackPane) placesTable.getScene().lookup("#contentPane");
            if (parent != null) {
                parent.getChildren().setAll(view);
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Cannot open edit form: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private void handleDelete(Place place) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + place.getTitle() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                placeService.supprimerPlace(place.getId());
                loadData();
            }
        });
    }
}
