package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventController {

    @FXML
    private ComboBox<String> activiteCombo;

    @FXML
    private Button ajouterEventButton;

    @FXML
    private TextField capaciteField;

    @FXML
    private Button catalogue;

    @FXML
    private CheckBox check1;

    @FXML
    private CheckBox check2;

    @FXML
    private CheckBox check3;

    @FXML
    private CheckBox check4;

    @FXML
    private DatePicker dateDebutPicker;

    @FXML
    private DatePicker dateFinPicker;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<String> difficulteCombo;

    @FXML
    private TextField emailorgField;

    @FXML
    private TextArea equipementField;

    @FXML
    private TextField lieuField;

    @FXML
    private TextField nomorgField;

    @FXML
    private TextField prixField;

    @FXML
    private TextField telephoneorgField;

    private Object currentActivite;
    private Connection connection;
    private List<Object> activitesList;

    public void initialize() {
        initializeDatabase();
        loadActivites();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/wonderlust_db", "root", "");
            
            // Vérifier la structure de la table events
            try {
                java.sql.DatabaseMetaData metaData = connection.getMetaData();
                java.sql.ResultSet columns = metaData.getColumns(null, null, "events", null);
                System.err.println("=== STRUCTURE DE LA TABLE EVENTS ===");
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    System.err.println("Champ: " + columnName + " | Type: " + columnType);
                }
                System.err.println("================================");
            } catch (Exception e) {
                System.err.println("Erreur lors de la vérification de la structure: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadActivites() {
        activitesList = new ArrayList<>();
        try {
            String sql = "SELECT id, titre, description, image FROM activites";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Créer un objet Activite temporaire
                Object activite = createActiviteObject(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    "Sport", // type par défaut
                    rs.getString("description"),
                    rs.getString("image")
                );
                activitesList.add(activite);
            }

            // Remplir la ComboBox
            activiteCombo.getItems().clear();
            for (Object activite : activitesList) {
                String titre = (String) activite.getClass().getMethod("getTitre").invoke(activite);
                activiteCombo.getItems().add(titre);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object createActiviteObject(int id, String titre, String type, String description, String imagePath) {
        try {
            // Utiliser reflection pour créer un objet compatible
            Class<?> activiteClass = Class.forName("Controllers.catalogueController$Activite");
            java.lang.reflect.Constructor<?> constructor = activiteClass.getConstructor(
                int.class, String.class, String.class, String.class, String.class
            );
            return constructor.newInstance(id, titre, type, description, imagePath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setActiviteData(Object activite) {
        this.currentActivite = activite;
        
        try {
            // Récupérer les données de l'activité via reflection
            String titre = (String) activite.getClass().getMethod("getTitre").invoke(activite);
            String description = (String) activite.getClass().getMethod("getDescription").invoke(activite);
            
            // Pré-remplir les champs avec les données de l'activité
            if (nomorgField != null) {
                nomorgField.setText("Événement: " + titre);
            }
            if (descriptionField != null) {
                descriptionField.setText(description);
            }
            if (lieuField != null) {
                lieuField.setText("Lieu à définir");
            }
            if (prixField != null) {
                prixField.setText("0");
            }
            if (capaciteField != null) {
                capaciteField.setText("20");
            }
            
            // Pré-sélectionner l'activité dans la ComboBox
            if (activiteCombo != null && titre != null) {
                activiteCombo.setValue(titre);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la récupération des données de l'activité");
        }
    }

    @FXML
    void ajouterEvent(ActionEvent event) {
        try {
            // Validation des champs obligatoires
            String selectedTitre = activiteCombo.getValue();
            String nomEvent = nomorgField.getText().trim();
            String description = descriptionField.getText().trim();
            String lieu = lieuField.getText().trim();
            String prix = prixField.getText().trim();
            String capacite = capaciteField.getText().trim();
            
            // Contrôle de saisie
            if (selectedTitre == null || selectedTitre.isEmpty()) {
                showAlert("Veuillez sélectionner une activité");
                return;
            }
            
            if (nomEvent.isEmpty()) {
                showAlert("Le nom de l'événement est obligatoire");
                return;
            }
            
            if (description.isEmpty()) {
                showAlert("La description de l'événement est obligatoire");
                return;
            }
            
            if (lieu.isEmpty()) {
                showAlert("Le lieu de l'événement est obligatoire");
                return;
            }
            
            // Validation du prix
            double prixValue = 0;
            try {
                prixValue = Double.parseDouble(prix.isEmpty() ? "0" : prix);
                if (prixValue < 0) {
                    showAlert("Le prix ne peut pas être négatif");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Veuillez entrer un prix valide");
                return;
            }
            
            // Validation de la capacité
            int capaciteValue = 20;
            try {
                capaciteValue = Integer.parseInt(capacite.isEmpty() ? "20" : capacite);
                if (capaciteValue <= 0) {
                    showAlert("La capacité doit être supérieure à 0");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Veuillez entrer une capacité valide");
                return;
            }

            // Trouver l'activité sélectionnée
            Object selectedActivite = null;
            for (Object activite : activitesList) {
                String titre = (String) activite.getClass().getMethod("getTitre").invoke(activite);
                if (titre.equals(selectedTitre)) {
                    selectedActivite = activite;
                    break;
                }
            }

            if (selectedActivite == null) {
                showAlert("Activité non trouvée");
                return;
            }

            // Insérer l'événement dans la base de données
            String sql = "INSERT INTO events (organisateur, EVENT_DEFINITION, lieu, prix, capacite_max, places_disponibles, date_debut, date_fin, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, nomEvent);
            pstmt.setString(2, description);
            pstmt.setString(3, lieu);
            pstmt.setDouble(4, prixValue);
            pstmt.setInt(5, capaciteValue);
            pstmt.setInt(6, capaciteValue);
            pstmt.setDate(7, dateDebutPicker.getValue() != null ? java.sql.Date.valueOf(dateDebutPicker.getValue()) : null);
            pstmt.setDate(8, dateFinPicker.getValue() != null ? java.sql.Date.valueOf(dateFinPicker.getValue()) : null);
            
            // Récupérer l'image de l'activité sélectionnée
            String imagePath = (String) selectedActivite.getClass().getMethod("getImagePath").invoke(selectedActivite);
            pstmt.setString(9, imagePath);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showAlert("Événement créé avec succès!");
                
                // Vider les champs
                clearFields();
                
                // Rafraîchir le catalogue
                CatalogueRefreshManager.getInstance().requestRefresh();
            } else {
                showAlert("Erreur lors de la création de l'événement");
            }
            
        } catch (Exception e) {
            System.err.println("ERREUR DÉTAILLÉE lors de la création de l'événement:");
            e.printStackTrace();
            System.err.println("Message d'erreur: " + e.getMessage());
            System.err.println("Cause racine: " + e.getCause());
            showAlert("Erreur lors de la création de l'événement: " + e.getMessage());
        }
    }

    private void clearFields() {
        nomorgField.clear();
        descriptionField.clear();
        lieuField.clear();
        prixField.clear();
        capaciteField.clear();
        equipementField.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        activiteCombo.setValue(null);
    }

    @FXML
    void viderFormulaire(ActionEvent event) {
        clearFields();
        showAlert("Formulaire vidé avec succès");
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
