package com.example.pi_dev.events.Controllers;

import com.example.pi_dev.events.Entities.Activite;
import com.example.pi_dev.events.Entities.Event;
import com.example.pi_dev.events.Entities.Reservation;
import com.example.pi_dev.events.Services.EventService;
import com.example.pi_dev.events.Services.ReservationService;
import com.example.pi_dev.events.Services.GoogleCalendarService;
import com.example.pi_dev.events.Services.EmailService;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import java.util.regex.Pattern;
import com.example.pi_dev.events.Services.GoogleCalendarService;

public class ReservationController {

    @FXML
    private Label EventDescription;
    @FXML
    private Label Eventlieu;
    @FXML
    private Label datedebut;
    @FXML
    private Label personCountLabel;
    @FXML
    private Label prixlabel;
    @FXML
    private HBox EventImagesContainer;
    @FXML
    private Button prevImageButton;
    @FXML
    private Button nextImageButton;
    @FXML
    private Label imageCounterLabel;
    @FXML
    private VBox videoContainer;
    @FXML
    private WebView videoWebView;

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

    // Variables pour la navigation des images
    private List<javafx.scene.Node> allMediaNodes = new ArrayList<>();
    private int currentImageIndex = 0;

    // panier local
    public static ObservableList<Reservation> panier = FXCollections.observableArrayList();

    // Service Google Calendar
    private GoogleCalendarService googleCalendarService;

    // Service Email
    private EmailService emailService;

    // ================= INITIALISATION =================
    public void initialize() {
        // Initialiser Google Calendar Service
        googleCalendarService = new GoogleCalendarService();
        boolean calendarInitialized = googleCalendarService.initialize();

        if (calendarInitialized) {
            System.out.println("📅 Google Calendar Service prêt");
        } else {
            System.out.println("⚠️ Google Calendar Service non disponible");
        }

        // Initialiser Email Service
        emailService = new EmailService();
        boolean emailInitialized = emailService.initialize();

        if (emailInitialized) {
            System.out.println("📧 Email Service prêt");
        } else {
            System.out.println("⚠️ Email Service non disponible");
        }

        // Ajouter un listener pour mettre à jour le prix total quand le nombre de personnes change
        nombrePersonnesField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateLabels();
        });

        // Valeur par défaut de 1 personne
        nombrePersonnesField.setText("1");
    }

    // ================= INITIALISATION EVENT =================
    public void loadEvent(int idEvent) {
        try {
            currentEvent = eventService.findById(idEvent);

            if (currentEvent != null) {
                System.out.println("DEBUG: Événement chargé - ID: " + currentEvent.getId());
                System.out.println("DEBUG: Événement titre: " + (currentEvent.getActivite() != null ? currentEvent.getActivite().getTitre() : "Pas d'activité"));
                System.out.println("DEBUG: Événement lieu: " + currentEvent.getLieu());
                System.out.println("DEBUG: Événement prix: " + currentEvent.getPrix());

                // Charger les images de l'événement
                loadEventImages();

                // Vérifier que les champs FXML sont initialisés avant de les utiliser
                if (EventDescription != null) {
                    // Afficher la description de l'événement (priorité sur la description de l'activité)
                    String eventDescription = currentEvent.getDescription();
                    String activiteDescription = (currentEvent.getActivite() != null) ? currentEvent.getActivite().getDescription() : null;

                    String descriptionToShow = eventDescription;
                    if (descriptionToShow == null || descriptionToShow.trim().isEmpty()) {
                        descriptionToShow = activiteDescription;
                    }

                    if (descriptionToShow != null && !descriptionToShow.trim().isEmpty()) {
                        EventDescription.setText(descriptionToShow);
                    } else {
                        EventDescription.setText("Aucune description disponible");
                    }
                }

                if (Eventlieu != null) {
                    Eventlieu.setText(currentEvent.getLieu() != null ? currentEvent.getLieu() : "Lieu non spécifié");
                }

                if (datedebut != null) {
                    // Formater les dates correctement
                    if (currentEvent.getDateDebut() != null) {
                        datedebut.setText(currentEvent.getDateDebut().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    } else {
                        datedebut.setText("Date non définie");
                    }
                }

                // Charger la vidéo YouTube directement dans l'interface
                loadYouTubeVideo();

                if (personCountLabel != null) {
                    // Afficher les places restantes au lieu du nombre total
                    int placesRestantes = currentEvent.getPlacesDisponibles();
                    personCountLabel.setText(" " + placesRestantes + " places restantes");
                }

                if (demandesp != null) {
                    // Afficher les matériels nécessaires de l'événement (lecture seule)
                    if (currentEvent.getMaterielsNecessaires() != null && !currentEvent.getMaterielsNecessaires().trim().isEmpty()) {
                        demandesp.setText(currentEvent.getMaterielsNecessaires());
                        demandesp.setEditable(false); // Rendre le champ non modifiable
                        demandesp.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd;");
                    } else {
                        demandesp.setText("Aucun équipement requis");
                        demandesp.setEditable(false);
                        demandesp.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd;");
                    }
                }

                if (prixlabel != null) {
                    try {
                        int nbPersonnes = Integer.parseInt(nombrePersonnesField.getText().isEmpty() ? "1" : nombrePersonnesField.getText());
                        double prixUnitaire = currentEvent.getPrix().doubleValue();
                        double total = prixUnitaire * nbPersonnes;
                        prixlabel.setText(" " + total + " TND");
                    } catch (NumberFormatException e) {
                        prixlabel.setText(" 0 TND");
                    }
                }

                // Charger la vidéo YouTube directement dans l'interface
                loadYouTubeVideo();

                // Afficher les photos supplémentaires si disponibles
                loadEventPhotos();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadEventImages() {
        try {
            allMediaNodes.clear();
            currentImageIndex = 0;

            // Ajouter l'image principale de l'événement
            if (currentEvent.getImage() != null && !currentEvent.getImage().trim().isEmpty()) {
                try {
                    java.io.File imageFile = new java.io.File(currentEvent.getImage());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        ImageView imageView = new ImageView(image);
                        imageView.setFitHeight(180);
                        imageView.setFitWidth(180);
                        imageView.setPreserveRatio(true);
                        imageView.setStyle("-fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: #2D70B3; -fx-background-color: white; -fx-padding: 5;");
                        allMediaNodes.add(imageView);
                        System.out.println("DEBUG: Image principale chargée: " + currentEvent.getImage());
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement de l'image principale: " + e.getMessage());
                }
            }

            // Ajouter l'image de l'activité si différente
            if (currentEvent.getActivite() != null && currentEvent.getActivite().getImage() != null) {
                String activityImagePath = currentEvent.getActivite().getImage();
                if (!activityImagePath.isEmpty() && !activityImagePath.equals(currentEvent.getImage())) {
                    try {
                        java.io.File imageFile = new java.io.File(activityImagePath);
                        if (imageFile.exists()) {
                            Image image = new Image(imageFile.toURI().toString());
                            ImageView imageView = new ImageView(image);
                            imageView.setFitHeight(180);
                            imageView.setFitWidth(180);
                            imageView.setPreserveRatio(true);
                            imageView.setStyle("-fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: #2D70B3; -fx-background-color: white; -fx-padding: 5;");
                            allMediaNodes.add(imageView);
                            System.out.println("DEBUG: Image activité chargée: " + activityImagePath);
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors du chargement de l'image activité: " + e.getMessage());
                    }
                }
            }

            // Ajouter les photos supplémentaires
            loadEventPhotos();

            // Afficher la première image par défaut
            if (!allMediaNodes.isEmpty()) {
                System.out.println("DEBUG: Nombre total de médias chargés: " + allMediaNodes.size());
                updateImageDisplay();
            } else {
                System.out.println("DEBUG: Aucun média à afficher");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des images: " + e.getMessage());
        }
    }

    private void loadEventPhotos() {
        try {
            // Récupérer les photos de l'événement depuis la base de données
            String sql = "SELECT chemin_photo FROM event_photos WHERE id_event = ?";
            java.sql.PreparedStatement ps = com.example.pi_dev.events.Utils.Mydatabase.getInstance().getConnextion().prepareStatement(sql);
            ps.setInt(1, currentEvent.getId());
            java.sql.ResultSet rs = ps.executeQuery();

            List<String> photoPaths = new ArrayList<>();
            while (rs.next()) {
                photoPaths.add(rs.getString("chemin_photo"));
            }
            rs.close();
            ps.close();

            for (String photoPath : photoPaths) {
                try {
                    java.io.File photoFile = new java.io.File(photoPath);
                    if (photoFile.exists()) {
                        Image image = new Image(photoFile.toURI().toString());

                        // Ajouter au conteneur principal d'images
                        ImageView imageView = new ImageView(image);
                        imageView.setFitHeight(180);
                        imageView.setFitWidth(180);
                        imageView.setPreserveRatio(true);
                        imageView.setStyle("-fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: #2D70B3; -fx-background-color: white; -fx-padding: 5;");
                        allMediaNodes.add(imageView);

                        System.out.println("DEBUG: Photo supplémentaire chargée: " + photoPath);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement de la photo: " + photoPath + " - " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des photos: " + e.getMessage());
        }
    }

    // ================= NAVIGATION IMAGES =================
    @FXML
    void previousImage(ActionEvent event) {
        if (currentImageIndex > 0) {
            currentImageIndex--;
            updateImageDisplay();
        }
    }

    @FXML
    void nextImage(ActionEvent event) {
        if (currentImageIndex < allMediaNodes.size() - 1) {
            currentImageIndex++;
            updateImageDisplay();
        }
    }

    private void updateImageDisplay() {
        if (allMediaNodes.isEmpty() || EventImagesContainer == null) {
            return;
        }

        // Vider le conteneur
        EventImagesContainer.getChildren().clear();

        // Ajouter seulement l'image actuelle
        javafx.scene.Node currentNode = allMediaNodes.get(currentImageIndex);
        EventImagesContainer.getChildren().add(currentNode);

        // Mettre à jour le compteur
        if (imageCounterLabel != null) {
            imageCounterLabel.setText((currentImageIndex + 1) + " / " + allMediaNodes.size());
        }

        // Activer/désactiver les boutons
        if (prevImageButton != null) {
            prevImageButton.setDisable(currentImageIndex == 0);
        }
        if (nextImageButton != null) {
            nextImageButton.setDisable(currentImageIndex == allMediaNodes.size() - 1);
        }

        System.out.println("DEBUG: Affichage image " + (currentImageIndex + 1) + " / " + allMediaNodes.size());
    }

    private void loadYouTubeVideo() {
        System.out.println("DEBUG: Début loadYouTubeVideo()");
        System.out.println("DEBUG: currentEvent = " + (currentEvent != null ? "NON NULL" : "NULL"));

        if (currentEvent != null) {
            System.out.println("DEBUG: currentEvent.getVideoYoutube() = " + currentEvent.getVideoYoutube());
            System.out.println("DEBUG: videoWebView = " + (videoWebView != null ? "NON NULL" : "NULL"));
            System.out.println("DEBUG: videoContainer = " + (videoContainer != null ? "NON NULL" : "NULL"));
        }

        if (currentEvent != null && currentEvent.getVideoYoutube() != null && !currentEvent.getVideoYoutube().trim().isEmpty() && videoWebView != null && videoContainer != null) {
            try {
                String videoUrl = currentEvent.getVideoYoutube().trim();
                System.out.println("DEBUG: Chargement de la vidéo YouTube dans WebView: " + videoUrl);

                // Convertir l'URL YouTube en URL embed
                String embedUrl = convertToEmbedUrl(videoUrl);
                System.out.println("DEBUG: URL embed: " + embedUrl);

                // Vérifier si l'URL embed est valide
                if (embedUrl == null || embedUrl.trim().isEmpty()) {
                    System.err.println("DEBUG: URL embed invalide, masquage du conteneur vidéo");
                    videoContainer.setVisible(false);
                    return;
                }

                // Créer le HTML pour l'intégration de la vidéo avec configuration améliorée
                String htmlContent = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <style>\n" +
                        "        body { margin: 0; padding: 0; background-color: white; }\n" +
                        "        .video-container { position: relative; width: 100%; height: 180px; }\n" +
                        "        iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none; }\n" +
                        "        .error-message { text-align: center; padding: 20px; color: #666; font-family: Arial; }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div class=\"video-container\">\n" +
                        "        <iframe src=\"" + embedUrl + "?rel=0&showinfo=0&modestbranding=1\" \n" +
                        "                frameborder=\"0\" \n" +
                        "                allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" \n" +
                        "                allowfullscreen>\n" +
                        "        </iframe>\n" +
                        "    </div>\n" +
                        "    <script>\n" +
                        "        // Gestion des erreurs YouTube\n" +
                        "        window.addEventListener('message', function(event) {\n" +
                        "            try {\n" +
                        "                var data = JSON.parse(event.data);\n" +
                        "                if (data.event === 'onError') {\n" +
                        "                    document.querySelector('.video-container').innerHTML = \n" +
                        "                        '<div class=\"error-message\">Vidéo non disponible. Cliquez pour ouvrir dans YouTube.</div>';\n" +
                        "                }\n" +
                        "            } catch(e) {}\n" +
                        "        });\n" +
                        "        \n" +
                        "        // Fallback: si la vidéo ne charge pas\n" +
                        "        setTimeout(function() {\n" +
                        "            var iframe = document.querySelector('iframe');\n" +
                        "            if (iframe && !iframe.contentWindow) {\n" +
                        "                document.querySelector('.video-container').innerHTML = \n" +
                        "                    '<div class=\"error-message\"><a href=\"" + embedUrl + "\" target=\"_blank\">Cliquez pour regarder sur YouTube</a></div>';\n" +
                        "            }\n" +
                        "        }, 5000);\n" +
                        "    </script>\n" +
                        "</body>\n" +
                        "</html>";

                // Charger le contenu HTML dans le WebView
                WebEngine webEngine = videoWebView.getEngine();
                webEngine.loadContent(htmlContent);

                // Rendre le conteneur vidéo visible
                videoContainer.setVisible(true);

                System.out.println("DEBUG: Vidéo YouTube chargée avec succès dans WebView");

            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de la vidéo YouTube: " + e.getMessage());
                e.printStackTrace();
                // Cacher le conteneur vidéo en cas d'erreur
                if (videoContainer != null) {
                    videoContainer.setVisible(false);
                }
            }
        } else {
            System.out.println("DEBUG: Aucune vidéo YouTube à charger ou WebView non initialisé");
            // Cacher le conteneur vidéo s'il n'y a pas de vidéo
            if (videoContainer != null) {
                videoContainer.setVisible(false);
            }
        }
    }

    private String convertToEmbedUrl(String youtubeUrl) {
        String embedUrl = "";

        try {
            if (youtubeUrl.contains("youtube.com/watch?v=")) {
                String videoId = youtubeUrl.substring(youtubeUrl.indexOf("v=") + 2);
                if (videoId.contains("&")) {
                    videoId = videoId.substring(0, videoId.indexOf("&"));
                }
                if (videoId.contains("#")) {
                    videoId = videoId.substring(0, videoId.indexOf("#"));
                }
                embedUrl = "https://www.youtube.com/embed/" + videoId;
            } else if (youtubeUrl.contains("youtu.be/")) {
                String videoId = youtubeUrl.substring(youtubeUrl.indexOf("youtu.be/") + 9);
                if (videoId.contains("?")) {
                    videoId = videoId.substring(0, videoId.indexOf("?"));
                }
                if (videoId.contains("&")) {
                    videoId = videoId.substring(0, videoId.indexOf("&"));
                }
                embedUrl = "https://www.youtube.com/embed/" + videoId;
            } else if (youtubeUrl.contains("youtube.com/embed/")) {
                embedUrl = youtubeUrl;
            } else if (youtubeUrl.contains("youtube.com/shorts/")) {
                String videoId = youtubeUrl.substring(youtubeUrl.indexOf("shorts/") + 7);
                if (videoId.contains("?")) {
                    videoId = videoId.substring(0, videoId.indexOf("?"));
                }
                embedUrl = "https://www.youtube.com/embed/" + videoId;
            }

            // Validation supplémentaire de l'ID vidéo
            if (!embedUrl.isEmpty()) {
                String videoId = embedUrl.substring(embedUrl.lastIndexOf("/") + 1);
                if (videoId.length() < 11 || videoId.matches(".*[^a-zA-Z0-9_-].*")) {
                    System.err.println("ID vidéo YouTube invalide: " + videoId);
                    return "";
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la conversion de l'URL YouTube: " + e.getMessage());
            return "";
        }

        return embedUrl;
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
        if (!validateInput()) {
            return;
        }

        try {
            // Validation de l'événement
            if (currentEvent == null || currentEvent.getId() == 0) {
                showAlert("Aucun événement sélectionné. Veuillez sélectionner un événement avant de réserver.");
                return;
            }

            // Vérifier que l'événement existe toujours en base
            if (!eventExists(currentEvent.getId())) {
                showAlert("L'événement sélectionné n'est plus disponible. Veuillez rafraîchir la liste des événements et réessayer.");
                return;
            }

            // Créer la réservation
            Reservation reservation = new Reservation();
            reservation.setIdEvent(currentEvent.getId());
            reservation.setNom(nomField.getText());
            reservation.setEmail(emailField.getText());
            reservation.setTelephone(telephoneField.getText());
            reservation.setNombrePersonnes(Integer.parseInt(nombrePersonnesField.getText()));
            reservation.setDemandesSpeciales(""); // Les demandes spéciales sont gérées séparément
            reservation.setEvent(currentEvent);
            reservation.setStatut(Reservation.StatutReservation.CONFIRMEE);

            // Calculer le prix total
            double prixUnitaire = currentEvent.getPrix().doubleValue();
            int nbPersonnes = Integer.parseInt(nombrePersonnesField.getText());
            double prixTotal = prixUnitaire * nbPersonnes;
            reservation.setPrixTotal(prixTotal);

            // Ajouter à la base de données
            reservationService.add(reservation);

            // Ajouter au panier
            panier.add(reservation);

            showAlert("Réservation effectuée avec succès !");

            // Générer et afficher le QR code
            afficherQRCode(reservation);

            // Ajouter automatiquement au calendrier Google
            if (googleCalendarService != null && googleCalendarService.isAvailable()) {
                boolean addedToCalendar = googleCalendarService.addReservationToCalendar(reservation);
                if (addedToCalendar) {
                    System.out.println("📅 Réservation ajoutée automatiquement à Google Calendar");
                    showAlert("Réservation effectuée avec succès !\n📅 L'événement a été ajouté à votre Google Calendar.");
                } else {
                    System.out.println("⚠️ Erreur lors de l'ajout à Google Calendar");
                    showAlert("Réservation effectuée avec succès !\n⚠️ Impossible d'ajouter à Google Calendar.");
                }
            } else {
                showAlert("Réservation effectuée avec succès !");
            }

            // Envoyer un email de confirmation avec lien Google Maps
            if (emailService != null && emailService.isAvailable()) {
                boolean emailSent = emailService.sendReservationConfirmation(reservation);
                if (emailSent) {
                    System.out.println("📧 Email de confirmation envoyé avec lien Google Maps");
                    // Afficher un message informatif
                    Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                    infoAlert.setTitle("📧 Email envoyé");
                    infoAlert.setHeaderText(null);
                    infoAlert.setContentText("Un email de confirmation a été envoyé à " + reservation.getEmail() +
                        "\n\n🗺️ L'email contient un lien Google Maps pour vous rendre au lieu de l'événement.");
                    infoAlert.showAndWait();
                } else {
                    System.out.println("⚠️ Erreur lors de l'envoi de l'email");
                    showAlert("Réservation effectuée avec succès !\n⚠️ Impossible d'envoyer l'email de confirmation.");
                }
            }

            // Fermer la fenêtre de réservation
            fermerFenetre();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la réservation: " + e.getMessage());

            // Gestion spécifique des erreurs de clé étrangère
            if (e.getMessage().contains("foreign key constraint fails")) {
                showAlert("Erreur : L'événement sélectionné n'est plus disponible. Veuillez rafraîchir la liste des événements et réessayer.");
                // Fermer la fenêtre de réservation
                fermerFenetre();
            } else {
                showAlert("Erreur lors de la réservation: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showAlert("Veuillez entrer un nombre valide pour le nombre de personnes");
        }
    }

    private void afficherQRCode(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pi_dev/events/QRCode.fxml"));
            Parent root = loader.load();

            QRCodeController qrController = loader.getController();
            qrController.setReservation(reservation);

            Stage stage = new Stage();
            stage.setTitle("QR Code de Réservation");
            stage.setScene(new Scene(root));
            stage.setWidth(500);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture de l'interface QR Code: " + e.getMessage());
            showAlert("Erreur lors de l'ouverture du QR Code");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pi_dev/events/panier.fxml"));
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
        if (EventDescription != null && EventDescription.getScene() != null) {
            Stage stage = (Stage) EventDescription.getScene().getWindow();
            stage.close();
        }
    }

    private boolean eventExists(int eventId) {
        try {
            String sql = "SELECT COUNT(*) FROM events WHERE id = ?";
            java.sql.PreparedStatement ps = com.example.pi_dev.events.Utils.Mydatabase.getInstance().getConnextion().prepareStatement(sql);
            ps.setInt(1, eventId);
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
