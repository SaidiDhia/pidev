package Controllers;

import Entities.Activite;
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

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class ReservationController {

    @FXML
    private Label EventTitle;
    @FXML
    private Label Eventlieu;
    @FXML
    private Label datedebut;
    @FXML
    private Label personCountLabel;
    @FXML
    private Label prixlabel;

    @FXML
    private TextField idEventField;
    @FXML
    private TextField nomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField nombrePersonnesField;
    @FXML
    private TextArea demandesp;

    @FXML
    private Button reserverButton;
    @FXML
    private Button annulerRES;
    @FXML
    private Button cataloguebtn;
    @FXML
    private Button panierButton;

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
                System.out.println("DEBUG: Événement chargé - ID: " + currentEvent.getId());
                System.out.println("DEBUG: Événement titre: " + (currentEvent.getActivite() != null ? currentEvent.getActivite().getTitre() : "Pas d'activité"));
                System.out.println("DEBUG: Événement lieu: " + currentEvent.getLieu());
                System.out.println("DEBUG: Événement prix: " + currentEvent.getPrix());
                
                if (currentEvent.getActivite() != null) {
                    EventTitle.setText(currentEvent.getActivite().getTitre());
                    // Afficher le nom de l'activité sous le nom de l'événement
                    if (currentEvent.getActivite().getTitre() != null) {
                        Eventlieu.setText(currentEvent.getActivite().getTitre());
                    }
                } else {
                    EventTitle.setText(currentEvent.getLieu() != null ? currentEvent.getLieu() : "Événement");
                    Eventlieu.setText("");
                }

                // Formater les dates correctement
                if (currentEvent.getDateDebut() != null) {
                    datedebut.setText(currentEvent.getDateDebut().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else {
                    datedebut.setText("Date non définie");
                }

                personCountLabel.setText(" " + currentEvent.getPlacesDisponibles());

                // Afficher les matériels nécessaires au lieu du nombre de personnes
                if (currentEvent.getMaterielsNecessaires() != null && !currentEvent.getMaterielsNecessaires().trim().isEmpty()) {
                    demandesp.setText(currentEvent.getMaterielsNecessaires());
                }

                try {
                    int nbPersonnes = Integer.parseInt(nombrePersonnesField.getText().isEmpty() ? "6" : nombrePersonnesField.getText());
                    double prixUnitaire = currentEvent.getPrix().doubleValue();
                    double total = prixUnitaire * nbPersonnes;
                    prixlabel.setText(" " + total + " TND");
                } catch (NumberFormatException e) {
                    prixlabel.setText("  0 TND");
                }
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

            // Ouvrir automatiquement le panier après ajout
            ouvrirPanierAutomatiquement();

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
        fermerFenetre();
    }

    @FXML
    void goToPanier(ActionEvent event) {
        fermerFenetre();
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
        System.out.println("=== DEBUG: updateLabels() appelé ===");
        System.out.println("currentEvent: " + (currentEvent != null ? "NON NULL" : "NULL"));
        System.out.println("nombrePersonnesField.getText(): '" + nombrePersonnesField.getText() + "'");

        if (currentEvent != null && !nombrePersonnesField.getText().isEmpty()) {
            try {
                int nbPersonnes = Integer.parseInt(nombrePersonnesField.getText());

                System.out.println("currentEvent.getPrix(): " + (currentEvent.getPrix() != null ? currentEvent.getPrix() : "NULL"));

                double prixUnitaire = currentEvent.getPrix().doubleValue();
                double total = prixUnitaire * nbPersonnes;

                System.out.println("prixUnitaire: " + prixUnitaire);
                System.out.println("total calculé: " + total);

                prixlabel.setText(" " + total + " TND");
            } catch (NumberFormatException e) {
                prixlabel.setText("  0 TND");
            }
        } else {
            System.out.println("DEBUG: currentEvent est NULL ou nombrePersonnesField est vide");
        }
    }

    private void ouvrirPanierAutomatiquement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/panier.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("🛒 Mon Panier");
            stage.setScene(new Scene(root));
            stage.setWidth(1000);
            stage.setHeight(750);
            stage.setMinWidth(900);
            stage.setMinHeight(650);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture du panier");
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) EventTitle.getScene().getWindow();
        stage.close();
    }
}
