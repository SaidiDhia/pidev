package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;

public class ActiviteCardController {

    @FXML private Button catalog;
    @FXML private Button organiserEvent;
    @FXML private Button reserver;
    @FXML private ImageView activiteImageView;
    @FXML private Label titreLabel;
    @FXML private Label typeLabel;
    @FXML private Label descriptionLabel;

    private Object currentActivite; // Utiliser Object pour éviter les problèmes d'accès

    public void initialize() {
        // Initialisation si nécessaire
    }

    public void setActiviteData(Object activite) {
        this.currentActivite = activite;
        
        // Utiliser reflection pour accéder aux méthodes de l'activité
        try {
            // Récupérer les données via reflection
            String titre = (String) activite.getClass().getMethod("getTitre").invoke(activite);
            String type = (String) activite.getClass().getMethod("getType").invoke(activite);
            String description = (String) activite.getClass().getMethod("getDescription").invoke(activite);
            String imagePath = (String) activite.getClass().getMethod("getImagePath").invoke(activite);
            
            // Remplir les champs avec les données de l'activité
            titreLabel.setText(titre);
            typeLabel.setText(type);
            descriptionLabel.setText(description);
            
            // Charger l'image
            loadActiviteImage(imagePath);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la récupération des données de l'activité");
        }
    }

    private void loadActiviteImage(String imagePath) {
        try {
            if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("default.jpg")) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    activiteImageView.setImage(image);
                } else {
                    // Image par défaut si le fichier n'existe pas
                    loadDefaultImage();
                }
            } else {
                // Image par défaut
                loadDefaultImage();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + imagePath);
            loadDefaultImage();
        }
    }

    private void loadDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/views/images/default-placeholder.png"));
            if (defaultImage != null) {
                activiteImageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image par défaut");
        }
    }

    @FXML
    void goToCatalogue(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Catalogue.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Catalogue");
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement du catalogue");
        }
    }

    @FXML
    void organiserEvent(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Event.fxml"));
            Parent root = loader.load();
            
            // Passer l'activité au contrôleur d'événement
            Object controller = loader.getController();
            if (controller instanceof EventController) {
                EventController eventController = (EventController) controller;
                eventController.setActiviteData(currentActivite);
            }
            
            Stage stage = new Stage();
            stage.setTitle("Organiser un événement");
            stage.setScene(new Scene(root));
            stage.setWidth(900);
            stage.setHeight(650);
            stage.setMinWidth(800);
            stage.setMinHeight(550);
            stage.centerOnScreen();
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de l'interface d'événement");
        }
    }

    @FXML
    void reserver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Reservation.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Réserver");
            stage.setScene(new Scene(root));
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setMinWidth(700);
            stage.setMinHeight(500);
            stage.centerOnScreen();
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de l'interface de réservation");
        }
    }

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
