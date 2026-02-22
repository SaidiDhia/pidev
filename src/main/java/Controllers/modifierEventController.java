package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Entities.Event;

public class modifierEventController {

    @FXML
    private ComboBox<String> activiteCombo;

    @FXML
    private Button annulerButton;

    @FXML
    private TextField capaciteField;

    @FXML
    private DatePicker dateDebutPicker;

    @FXML
    private DatePicker dateFinPicker;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField lieuField;

    @FXML
    private Button modifierEventButton;

    @FXML
    private TextField nomorgField;

    @FXML
    private Button supprimerEventButton;

    @FXML
    private TextField prixField;

    private Event currentEvent;
    private Connection connection;
    private List<Object> activitesList;

    public void initialize() {
        initializeDatabase();
        loadActivites();
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
            String sql = "SELECT id, titre, description, image FROM activites";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object activite = createActiviteObject(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    "Sport",
                    rs.getString("description"),
                    rs.getString("image")
                );
                activitesList.add(activite);
            }

            activiteCombo.getItems().clear();
            for (Object activite : activitesList) {
                if (activite != null) {
                    String titre = (String) activite.getClass().getMethod("getTitre").invoke(activite);
                    activiteCombo.getItems().add(titre);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object createActiviteObject(int id, String titre, String type, String description, String imagePath) {
        try {
            Class<?> activiteClass = Class.forName("Entities.Activite");
            java.lang.reflect.Constructor<?> constructor = activiteClass.getConstructor(
                int.class, String.class, String.class, String.class, String.class
            );
            return constructor.newInstance(id, titre, type, description, imagePath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setEventData(Event event) {
        this.currentEvent = event;
        
        try {
            // Remplir les champs avec les données de l'événement
            if (nomorgField != null) {
                nomorgField.setText(event.getOrganisateur());
            }
            if (descriptionField != null) {
                descriptionField.setText(event.getMaterielsNecessaires());
            }
            if (lieuField != null) {
                lieuField.setText(event.getLieu());
            }
            if (prixField != null) {
                prixField.setText(String.valueOf(event.getPrix()));
            }
            if (capaciteField != null) {
                capaciteField.setText(String.valueOf(event.getCapaciteMax()));
            }
            if (dateDebutPicker != null && event.getDateDebut() != null) {
                dateDebutPicker.setValue(event.getDateDebut().toLocalDate());
            }
            if (dateFinPicker != null && event.getDateFin() != null) {
                dateFinPicker.setValue(event.getDateFin().toLocalDate());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la récupération des données de l'événement");
        }
    }

    @FXML
    void modifierEvent(ActionEvent event) {
        try {
            String nomEvent = nomorgField.getText().trim();
            String description = descriptionField.getText().trim();
            String lieu = lieuField.getText().trim();
            String prix = prixField.getText().trim();
            String capacite = capaciteField.getText().trim();
            
            // Contrôle de saisie
            if (nomEvent.isEmpty()) {
                showAlert("Le nom de l'événement est obligatoire");
                return;
            }
            
            if (description.isEmpty()) {
                showAlert("La description est obligatoire");
                return;
            }
            
            if (lieu.isEmpty()) {
                showAlert("Le lieu est obligatoire");
                return;
            }
            
            if (dateDebutPicker.getValue() == null) {
                showAlert("La date de début est obligatoire");
                return;
            }
            
            if (dateFinPicker.getValue() == null) {
                showAlert("La date de fin est obligatoire");
                return;
            }
            
            if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
                showAlert("La date de fin doit être après la date de début");
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

            // Mettre à jour l'événement dans la base de données
            String sql = "UPDATE events SET organisateur = ?, materiels_necessaires = ?, lieu = ?, prix = ?, capacite_max = ?, places_disponibles = ?, date_debut = ?, date_fin = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            
            pstmt.setString(1, nomEvent);
            pstmt.setString(2, description);
            pstmt.setString(3, lieu);
            pstmt.setDouble(4, prixValue);
            pstmt.setInt(5, capaciteValue);
            pstmt.setInt(6, capaciteValue);
            pstmt.setDate(7, dateDebutPicker.getValue() != null ? java.sql.Date.valueOf(dateDebutPicker.getValue()) : null);
            pstmt.setDate(8, dateFinPicker.getValue() != null ? java.sql.Date.valueOf(dateFinPicker.getValue()) : null);
            
            // Récupérer l'ID de l'événement actuel
            int eventId = getCurrentEventId();
            pstmt.setInt(9, eventId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showAlert("Événement modifié avec succès!");
                goToCatalogue(event);
            } else {
                showAlert("Erreur lors de la modification de l'événement");
            }
            
        } catch (Exception e) {
            System.err.println("ERREUR lors de la modification de l'événement:");
            e.printStackTrace();
            showAlert("Erreur lors de la modification de l'événement: " + e.getMessage());
        }
    }

    @FXML
    void supprimerEvent(ActionEvent event) {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de suppression");
            confirmation.setHeaderText("Voulez-vous vraiment supprimer cet événement?");
            confirmation.setContentText("Cette action est irréversible.");
            
            if (confirmation.showAndWait().get() == ButtonType.OK) {
                // Supprimer l'événement de la base de données
                String sql = "DELETE FROM events WHERE id = ?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                
                int eventId = getCurrentEventId();
                pstmt.setInt(1, eventId);
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    showAlert("Événement supprimé avec succès!");
                    goToCatalogue(event);
                } else {
                    showAlert("Erreur lors de la suppression de l'événement");
                }
            }
        } catch (Exception e) {
            System.err.println("ERREUR lors de la suppression de l'événement:");
            e.printStackTrace();
            showAlert("Erreur lors de la suppression de l'événement: " + e.getMessage());
        }
    }

    private int getCurrentEventId() {
        // Cette méthode devrait récupérer l'ID de l'événement actuel
        // Vous devrez adapter cette logique selon votre structure de données
        if (currentEvent != null) {
            try {
                // Si vous avez un getId() dans votre classe Event
                Method getIdMethod = currentEvent.getClass().getMethod("getId");
                return (Integer) getIdMethod.invoke(currentEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0; // Valeur par défaut à adapter
    }

    @FXML
    void goToCatalogue(ActionEvent event) {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) activiteCombo.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
