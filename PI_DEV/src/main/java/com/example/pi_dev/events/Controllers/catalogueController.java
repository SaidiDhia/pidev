package com.example.pi_dev.events.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import com.example.pi_dev.events.Entities.Event;
import com.example.pi_dev.events.Entities.Activite;

public class catalogueController {

    private Connection connection;
    private List<Activite> activitesList;
    private List<Event> eventsList;

    @FXML
    private Label activitedesc;

    @FXML
    private ImageView activiteimg;

    @FXML
    private Label activitetitre;

    @FXML
    private Label activitetype;

    @FXML
    private Label capaciteevent;

    @FXML
    private Label datedebut;

    @FXML
    private Label datefin;

    @FXML
    private VBox eventcard;

    @FXML
    private ImageView eventimg;

    @FXML
    private FlowPane flowActivites;

    @FXML
    private FlowPane flowEvents;

    @FXML
    private Button modevent;

    @FXML
    private Button modifieract;

    @FXML
    private Button orgactivite;

    @FXML
    private Button orgevent;

    @FXML
    private Label placesdispoevent;

    @FXML
    private Label prixevent;

    @FXML
    private Button recherche;

    @FXML
    private ScrollPane scrollPaneActivites;

    @FXML
    private ScrollPane scrollPaneEvents;

    @FXML
    private Button suppact;

    @FXML
    private Button suppevent;

    @FXML
    private Tab tabActivites;

    @FXML
    private Tab tabEvents;

    @FXML
    private TextField txtRecherche;

    public void initialize() {
        initializeDatabase();
        refreshData();
        
        // Add window focus listener to refresh when window becomes active
        flowActivites.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs1, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.focusedProperty().addListener((obs2, oldFocused, newFocused) -> {
                            if (newFocused) {
                                refreshData();
                            }
                        });
                    }
                });
            }
        });
        
        // Ajouter un écouteur pour le rafraîchissement manuel
        startRefreshListener();
    }
    
    private void startRefreshListener() {
        Thread refreshThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // Vérifier chaque seconde
                    if (CatalogueRefreshManager.getInstance().isRefreshRequested()) {
                        javafx.application.Platform.runLater(() -> {
                            refreshData();
                            CatalogueRefreshManager.getInstance().resetRefresh();
                        });
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }
    
    public void refreshData() {
        loadActivites();
        loadEvents();
        displayActivites();
        displayEvents();
    }
    
    private void displayActivites() {
        System.out.println("Affichage des activités - Nombre dans la liste: " + activitesList.size());
        flowActivites.getChildren().clear();
        for (Activite activite : activitesList) {
            System.out.println("Ajout de la carte pour: " + activite.getTitre());
            addActiviteCard(activite);
        }
        System.out.println("Nombre de cartes dans le FlowPane: " + flowActivites.getChildren().size());
    }
    
    private void displayEvents() {
        System.out.println("Affichage des événements - Nombre dans la liste: " + eventsList.size());
        flowEvents.getChildren().clear();
        for (Event event : eventsList) {
            System.out.println("Ajout de la carte pour l'événement: " + event.getOrganisateur());
            addEventCard(event);
        }
        System.out.println("Nombre de cartes d'événements dans le FlowPane: " + flowEvents.getChildren().size());
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/wonderlust_db", "root", "");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadActivites() {
        activitesList = new ArrayList<>();
        try {
            System.out.println("Chargement des activités depuis la base de données...");
            Statement stmt = connection.createStatement();
            
            // Essayer d'abord avec typeActivite
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT id, titre, description, type_activite, image FROM activites");
                while (rs.next()) {
                    Activite activite = new Activite();
                    activite.setId(rs.getInt("id"));
                    activite.setTitre(rs.getString("titre"));
                    activite.setDescription(rs.getString("description"));
                    activite.setTypeActivite(rs.getString("type_activite"));
                    activite.setImage(rs.getString("image"));
                    activitesList.add(activite);
                    System.out.println("Activité chargée: " + activite.getTitre());
                }
            } catch (SQLException e) {
                // Si type_activite n'existe pas, utiliser la requête sans cette colonne
                System.out.println("Colonne type_activite non trouvée, utilisation sans type...");
                rs = stmt.executeQuery("SELECT id, titre, description, image FROM activites");
                while (rs.next()) {
                    Activite activite = new Activite();
                    activite.setId(rs.getInt("id"));
                    activite.setTitre(rs.getString("titre"));
                    activite.setDescription(rs.getString("description"));
                    activite.setTypeActivite(null); // Pas de type par défaut
                    activite.setImage(rs.getString("image"));
                    activitesList.add(activite);
                    System.out.println("Activité chargée: " + activite.getTitre());
                }
            }
            
            System.out.println("Total activités chargées: " + activitesList.size());
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des activités: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadEvents() {
        eventsList = new ArrayList<>();
        try {
            System.out.println("Chargement des événements depuis la base de données...");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM events");
            while (rs.next()) {
                Event event = new Event();
                event.setId(rs.getInt("id"));
                event.setIdActivite(rs.getInt("id_activite"));
                event.setLieu(rs.getString("lieu"));
                event.setAdresseComplete(rs.getString("adresse_complete"));
                event.setDateDebut(rs.getTimestamp("date_debut") != null ? rs.getTimestamp("date_debut").toLocalDateTime() : null);
                event.setDateFin(rs.getTimestamp("date_fin") != null ? rs.getTimestamp("date_fin").toLocalDateTime() : null);
                event.setPrix(rs.getBigDecimal("prix"));
                event.setCapaciteMax(rs.getInt("capacite_max"));
                event.setPlacesDisponibles(rs.getInt("places_disponibles"));
                event.setOrganisateur(rs.getString("organisateur"));
                event.setMaterielsNecessaires(rs.getString("materiels_necessaires"));
                event.setStatut(Event.StatutEvent.valueOf(rs.getString("statut")));
                event.setDateCreation(rs.getTimestamp("date_creation"));
                event.setDateModification(rs.getTimestamp("date_modification"));
                
                eventsList.add(event);
                System.out.println("Événement chargé: " + event.getOrganisateur());
            }
            System.out.println("Total événements chargés: " + eventsList.size());
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des événements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void goToEventDetails(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EventDetails.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Détails de l'événement");
            stage.setScene(new Scene(root));
            stage.setWidth(1000);
            stage.setHeight(750);
            stage.setMinWidth(900);
            stage.setMinHeight(650);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleRecherche(ActionEvent event) {
        String searchText = txtRecherche.getText().toLowerCase();
        
        flowActivites.getChildren().clear();
        flowEvents.getChildren().clear();
        
        for (Activite activite : activitesList) {
            if (activite.getTitre().toLowerCase().contains(searchText) ||
                activite.getTypeActivite().toLowerCase().contains(searchText) ||
                activite.getDescription().toLowerCase().contains(searchText)) {
                addActiviteCard(activite);
            }
        }
        
        for (Event eventItem : eventsList) {
            if (eventItem.getOrganisateur().toLowerCase().contains(searchText)) {
                addEventCard(eventItem);
            }
        }
    }

    @FXML
    void modifierActivite(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/modifsuppActivite.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Modifier/Supprimer Activité");
            stage.setScene(new Scene(root));
            stage.setWidth(900);
            stage.setHeight(650);
            stage.setMinWidth(800);
            stage.setMinHeight(550);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void modifierEvent(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/modifierEvent.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Modifier Événement");
            stage.setScene(new Scene(root));
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void ouvrirGestionActivites(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AjoutActivite.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter Activité");
            stage.setScene(new Scene(root));
            stage.setWidth(850);
            stage.setHeight(600);
            stage.setMinWidth(750);
            stage.setMinHeight(500);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void ouvrirOrganisation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Event.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter Événement");
            stage.setScene(new Scene(root));
            stage.setWidth(900);
            stage.setHeight(650);
            stage.setMinWidth(800);
            stage.setMinHeight(550);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void supprimeract(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer une activité");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette activité ?");
        alert.showAndWait();
    }
    
    private void ouvrirModificationActivite(Activite activite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/modifsuppActivite.fxml"));
            Parent root = loader.load();
            
            // Récupérer le contrôleur et passer les données
            modifierActiviteController controller = loader.getController();
            controller.setActiviteData(activite);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier/Supprimer Activité: " + activite.getTitre());
            stage.setScene(new Scene(root));
            stage.setWidth(900);
            stage.setHeight(650);
            stage.setMinWidth(800);
            stage.setMinHeight(550);
            stage.centerOnScreen();
            stage.show();
            
            // Rafraîchir quand la fenêtre se ferme
            stage.setOnHidden(e -> refreshData());
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de l'interface de modification");
        }
    }
    
    private void supprimerActivite(Activite activite) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'activité");
        alert.setContentText("Voulez-vous vraiment supprimer l'activité \"" + activite.getTitre() + "\" ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String sql = "DELETE FROM activites WHERE id = ?";
                    PreparedStatement pstmt = connection.prepareStatement(sql);
                    pstmt.setInt(1, activite.getId());
                    pstmt.executeUpdate();
                    
                    showAlert("Activité supprimée avec succès");
                    refreshData(); // Rafraîchir l'affichage complet
                    
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur lors de la suppression de l'activité");
                }
            }
        });
    }

    @FXML
    void supprimerevent(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'événement");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet événement ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String sql = "DELETE FROM events WHERE id = ?";
                    PreparedStatement pstmt = connection.prepareStatement(sql);
                    pstmt.setInt(1, getCurrentEventId());
                    pstmt.executeUpdate();
                    
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Succès");
                    successAlert.setHeaderText("Événement supprimé");
                    successAlert.setContentText("L'événement a été supprimé avec succès.");
                    successAlert.show();
                    
                    loadEvents();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private int getCurrentActiviteId() {
        return 1;
    }
    
    private int getCurrentEventId() {
        return 1;
    }
    
    private void addActiviteCard(Activite activite) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;");
        card.setPrefWidth(200);
        
        // Image de l'activité
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        
        // Charger l'image depuis le chemin
        try {
            if (activite.getImage() != null && !activite.getImage().isEmpty() && !activite.getImage().equals("default.jpg")) {
                File imageFile = new File(activite.getImage());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imageView.setImage(image);
                } else {
                    // Image par défaut si le fichier n'existe pas
                    Image defaultImage = new Image(getClass().getResourceAsStream("/views/images/default-placeholder.png"));
                    if (defaultImage != null) {
                        imageView.setImage(defaultImage);
                    }
                }
            } else {
                // Image par défaut
                Image defaultImage = new Image(getClass().getResourceAsStream("/views/images/default-placeholder.png"));
                if (defaultImage != null) {
                    imageView.setImage(defaultImage);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + activite.getImage());
            // Image par défaut en cas d'erreur
            imageView.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ddd;");
        }
        
        // Labels pour les informations
        Label titleLabel = new Label(activite.getTitre());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1a5f3f;");
        titleLabel.setWrapText(true);
        
        // Ajouter le type seulement s'il existe
        if (activite.getTypeActivite() != null && !activite.getTypeActivite().isEmpty()) {
            Label typeLabel = new Label("Type: " + activite.getTypeActivite());
            typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            card.getChildren().add(typeLabel);
        }
        
        Label descLabel = new Label(activite.getDescription());
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(60);
        
        // Boutons d'action
        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button modifierButton = new Button("✏️");
        modifierButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 5 10; -fx-font-size: 10px; -fx-cursor: hand;");
        modifierButton.setOnAction(e -> ouvrirModificationActivite(activite));
        
        Button supprimerButton = new Button("🗑️");
        supprimerButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 5 10; -fx-font-size: 10px; -fx-cursor: hand;");
        supprimerButton.setOnAction(e -> supprimerActivite(activite));
        
        buttonBox.getChildren().addAll(modifierButton, supprimerButton);
        
        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(imageView, titleLabel, descLabel, buttonBox);
        
        // Ajouter le gestionnaire de clic pour ouvrir les détails
        card.setOnMouseClicked(e -> ouvrirDetailsActivite(activite));
        
        // Ajouter la carte au flow pane
        flowActivites.getChildren().add(card);
    }
    
    private void ouvrirDetailsActivite(Activite activite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ActiviteCard.fxml"));
            Parent root = loader.load();
            
            // Passer les données au contrôleur
            ActiviteCardController controller = loader.getController();
            controller.setActiviteData(activite);
            
            Stage stage = new Stage();
            stage.setTitle("Détails de l'activité: " + activite.getTitre());
            stage.setScene(new Scene(root));
            stage.setWidth(800);
            stage.setHeight(700);
            stage.setMinWidth(700);
            stage.setMinHeight(600);
            stage.centerOnScreen();
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture des détails de l'activité");
        }
    }
    
    private void addEventCard(Event event) {
        try {
            System.out.println("Création de la carte pour l'événement: " + event.getOrganisateur());
            
            VBox card = new VBox();
            card.setSpacing(10);
            card.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 15; -fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;");
            
            // Afficher seulement les infos essentielles sans image
            Label titleLabel = new Label(event.getOrganisateur());
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a5f3f;");
            
            Label lieuLabel = new Label("📍 " + event.getLieu());
            lieuLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            
            Label dateDebutLabel = new Label("📅 Début: " + 
                (event.getDateDebut() != null ? event.getDateDebut().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy ")) : "Non défini"));
            dateDebutLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
            
            Label dateFinLabel = new Label("📅 Fin: " + 
                (event.getDateFin() != null ? event.getDateFin().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy ")) : "Non défini"));
            dateFinLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
            
            Label prixLabel = new Label("💰 Prix: " + (event.getPrix() != null ? event.getPrix() + " TND" : "0 TND"));
            prixLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d7a2d;");
            
            Label capaciteLabel = new Label("👥 Capacité: " + event.getCapaciteMax());
            capaciteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            
            Label placesLabel = new Label("🎫 Places: " + event.getPlacesDisponibles());
            placesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            
            // Ajouter les détails si disponibles
            if (event.getMaterielsNecessaires() != null && !event.getMaterielsNecessaires().trim().isEmpty()) {
                Label descLabel = new Label("📝 " + event.getMaterielsNecessaires());
                descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-wrap-text: true;");
                descLabel.setMaxWidth(200);
                card.getChildren().add(descLabel);
            }
            
            // Boutons d'action
            HBox buttonBox = new HBox(5);
            buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
            
            Button modifierButton = new Button("✏️ Modifier");
            modifierButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 8 16; -fx-font-size: 12px; -fx-cursor: hand;");
            modifierButton.setOnAction(e -> {
                System.out.println("Clic sur bouton modifier pour: " + event.getOrganisateur());
                ouvrirModificationEvent(event);
            });
            
            buttonBox.getChildren().add(modifierButton);
            
            card.getChildren().addAll(titleLabel, lieuLabel, dateDebutLabel, dateFinLabel, prixLabel, capaciteLabel, placesLabel, buttonBox);
            
            // Ajouter le gestionnaire de clic pour ouvrir la réservation
            card.setOnMouseClicked(e -> {
                System.out.println("Clic sur la carte pour: " + event.getOrganisateur());
                ouvrirReservation(event);
            });
            
            flowEvents.getChildren().add(card);
            System.out.println("Carte ajoutée avec succès pour: " + event.getOrganisateur());
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la carte pour l'événement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ouvrirReservation(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Reservation.fxml"));
            Parent root = loader.load();
            
            // Récupérer le contrôleur et passer les données de l'événement
            ReservationController controller = loader.getController();
            controller.loadEvent(event.getId());
            
            Stage stage = new Stage();
            stage.setTitle("Réservation: " + event.getOrganisateur());
            stage.setScene(new Scene(root));
            stage.setWidth(900);
            stage.setHeight(700);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.centerOnScreen();
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de l'interface de réservation");
        }
    }
    
    private void ouvrirModificationEvent(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/modifierEvent.fxml"));
            Parent root = loader.load();
            
            // Récupérer le contrôleur et passer les données
            modifierEventController controller = loader.getController();
            controller.setEventData(event);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier l'événement: " + event.getOrganisateur());
            stage.setScene(new Scene(root));
            stage.setWidth(900);
            stage.setHeight(700);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.centerOnScreen();
            stage.show();
            
            // Rafraîchir quand la fenêtre se ferme
            stage.setOnHidden(e -> refreshData());
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de l'interface de modification");
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}
