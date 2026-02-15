package Controllers;

import Entities.Event;
import Entities.Reservation;
import Services.EventService;
import Services.ReservationService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class ReservationController {

    @FXML private Label EventTitle;
    @FXML private Label Eventlieu;
    @FXML private Label datedebut;
    @FXML private Label prix;
    @FXML private Label personCountLabel;
    @FXML private Label nbrepersolabel;
    @FXML private Label prixlabel;
    @FXML private ImageView EventImage;

    @FXML private TextField idEventField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField nombrePersonnesField;
    @FXML private TextArea demandesp;

    @FXML private Button reserverButton;
    @FXML private Button annulerRES;
    @FXML private Button cataloguebtn;
    @FXML private Button panierButton;

    private EventService eventService = new EventService();
    private ReservationService reservationService = new ReservationService();

    private Event currentEvent;

    // panier local
    public static ObservableList<Reservation> panier = FXCollections.observableArrayList();

    // ================= INITIALISATION EVENT =================
    public void loadEvent(int idEvent) {

        try {
            currentEvent = eventService.findById(idEvent);

            if (currentEvent != null) {
                if (currentEvent.getActivite() != null) {
                    EventTitle.setText(currentEvent.getActivite().getTitre());
                } else {
                    EventTitle.setText("Événement " + currentEvent.getId());
                }
                Eventlieu.setText(currentEvent.getLieu());
                datedebut.setText(currentEvent.getDateDebut().toString());
                prix.setText(currentEvent.getPrix() + " TND");
                personCountLabel.setText("Places restantes : " + currentEvent.getPlacesDisponibles());
                nbrepersolabel.setText("Nombre de personnes : " + nombrePersonnesField.getText());
                
                try {
                    int nbPersonnes = Integer.parseInt(nombrePersonnesField.getText().isEmpty() ? "1" : nombrePersonnesField.getText());
                    double prixUnitaire = currentEvent.getPrix().doubleValue();
                    double total = prixUnitaire * nbPersonnes;
                    prixlabel.setText("Prix total : " + total + " TND");
                } catch (NumberFormatException e) {
                    prixlabel.setText("Prix total : 0 TND");
                }

                Image img = new Image("file:" + currentEvent.getImage());
                EventImage.setImage(img);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= CONTROLE SAISIE =================
    private boolean validateInput() {

        if (currentEvent == null) {
            showAlert("Veuillez d'abord sélectionner un événement");
            return false;
        }

        if (nomField.getText().isEmpty()) {
            showAlert("Le nom est obligatoire");
            return false;
        }

        if (!Pattern.matches("^[A-Za-z ]+$", nomField.getText())) {
            showAlert("Nom invalide");
            return false;
        }

        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", emailField.getText())) {
            showAlert("Email invalide");
            return false;
        }

        if (!Pattern.matches("^[0-9]{8,15}$", telephoneField.getText())) {
            showAlert("Téléphone invalide");
            return false;
        }

        int nombre;
        try {
            nombre = Integer.parseInt(nombrePersonnesField.getText());
        } catch (NumberFormatException e) {
            showAlert("Nombre invalide");
            return false;
        }

        if (nombre <= 0 || nombre > 12) {
            showAlert("Maximum 12 personnes autorisées");
            return false;
        }

        if (nombre > currentEvent.getPlacesDisponibles()) {
            showAlert("Pas assez de places disponibles");
            return false;
        }

        return true;
    }

    // ================= RESERVER =================
    @FXML
    void reserver(ActionEvent event) {

        if (!validateInput()) return;

        try {

            Reservation r = new Reservation();
            r.setIdEvent(currentEvent.getId());
            r.setNomComplet(nomField.getText());
            r.setEmail(emailField.getText());
            r.setTelephone(telephoneField.getText());
            r.setNombrePersonnes(Integer.parseInt(nombrePersonnesField.getText()));

            reservationService.ajouter(r);

            panier.add(r);

            showAlert("Réservation ajoutée avec succès et ajoutée au panier ✔");
            
            clearFields();
            updateLabels();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur lors de la réservation");
        }
    }

    // ================= ANNULER =================
    @FXML
    void annulerRES(ActionEvent event) {
        goToCatalogue(event);
    }

    // ================= NAVIGATION =================
    @FXML
    void goToCatalogue(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Catalogue.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Catalogue");
            stage.setWidth(1400);
            stage.setHeight(900);
            stage.setMinWidth(1200);
            stage.setMinHeight(700);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement du catalogue");
        }
    }

    @FXML
    void goToPanier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Reservation.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Panier");
            stage.setWidth(1000);
            stage.setHeight(750);
            stage.setMinWidth(900);
            stage.setMinHeight(650);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement du panier");
        }
    }
    
    @FXML
    void nombrePersonnesChanged(ActionEvent event) {
        updateLabels();
    }

    // ================= UTILITY =================
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void clearFields() {
        nomField.clear();
        emailField.clear();
        telephoneField.clear();
        nombrePersonnesField.clear();
        demandesp.clear();
    }
    
    private void updateLabels() {
        if (currentEvent != null && !nombrePersonnesField.getText().isEmpty()) {
            try {
                int nbPersonnes = Integer.parseInt(nombrePersonnesField.getText());
                nbrepersolabel.setText("Nombre de personnes : " + nbPersonnes);
                double prixUnitaire = currentEvent.getPrix().doubleValue();
                double total = prixUnitaire * nbPersonnes;
                prixlabel.setText("Prix total : " + total + " TND");
            } catch (NumberFormatException e) {
                nbrepersolabel.setText("Nombre de personnes : 0");
                prixlabel.setText("Prix total : 0 TND");
            }
        }
    }
}
