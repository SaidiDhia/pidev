package com.example.pi_dev.events.Controllers;

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

    public void setActiviteData(Object activite) {
        this.currentActivite = activite;

        try {
            String titre = (String) activite.getClass().getMethod("getTitre").invoke(activite);
            String description = (String) activite.getClass().getMethod("getDescription").invoke(activite);

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
            String selectedTitre = activiteCombo.getValue();
            String nomEvent = nomorgField.getText().trim();
            String description = descriptionField.getText().trim();
            String lieu = lieuField.getText().trim();
            String prix = prixField.getText().trim();
            String capacite = capaciteField.getText().trim();

            if (selectedTitre == null || selectedTitre.isEmpty()) {
                showAlert("Veuillez sélectionner une activité");
                return;
            }

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

            Object selectedActivite = null;
            for (Object activite : activitesList) {
                try {
                    Method getTitreMethod = activite.getClass().getMethod("getTitre");
                    String titre = (String) getTitreMethod.invoke(activite);
                    if (titre.equals(activiteCombo.getValue())) {
                        selectedActivite = activite;
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            if (selectedActivite == null) {
                showAlert("Activité non trouvée");
                return;
            }

            String sql = "INSERT INTO events (id_activite, organisateur, materiels_necessaires, lieu, prix, capacite_max, places_disponibles, date_debut, date_fin, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);

            int activiteId = (Integer) selectedActivite.getClass().getMethod("getId").invoke(selectedActivite);
            pstmt.setInt(1, activiteId);
            pstmt.setString(2, nomEvent);
            pstmt.setString(3, description);
            pstmt.setString(4, lieu);
            pstmt.setDouble(5, prixValue);
            pstmt.setInt(6, capaciteValue);
            pstmt.setInt(7, capaciteValue);
            pstmt.setDate(8, dateDebutPicker.getValue() != null ? java.sql.Date.valueOf(dateDebutPicker.getValue()) : null);
            pstmt.setDate(9, dateFinPicker.getValue() != null ? java.sql.Date.valueOf(dateFinPicker.getValue()) : null);

            // Image non nécessaire pour les événements
            pstmt.setString(10, null);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Événement créé avec succès!");
                clearFields();
                // Revenir automatiquement au catalogue
                goToCatalogue(event);
            } else {
                showAlert("Erreur lors de la création de l'événement");
            }

        } catch (Exception e) {
            System.err.println("ERREUR lors de la création de l'événement:");
            e.printStackTrace();
            showAlert("Erreur lors de la création de l'événement: " + e.getMessage());
        }
    }

    private void clearFields() {
        nomorgField.clear();
        descriptionField.clear();
        lieuField.clear();
        prixField.clear();
        capaciteField.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        activiteCombo.setValue(null);
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
