package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AjoutActiviteController {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField typeactField;
    @FXML private TextField imageField1;
    @FXML private Button ajouterButton;
    @FXML private Button annulerButton;

    private Connection connection;

    public void initialize() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/wonderlust_db", "root", "");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void ajouter(ActionEvent event) {
        String titre = titreField.getText().trim();
        String description = descriptionArea.getText().trim();
        String type = typeactField.getText().trim();
        String imagePath = imageField1.getText().trim();

        // Validation des champs
        if (titre.isEmpty()) {
            showAlert("Le titre est obligatoire");
            titreField.requestFocus();
            return;
        }
        
        if (titre.length() < 3 || titre.length() > 100) {
            showAlert("Le titre doit contenir entre 3 et 100 caractères");
            titreField.requestFocus();
            return;
        }
        
        if (description.isEmpty()) {
            showAlert("La description est obligatoire");
            descriptionArea.requestFocus();
            return;
        }
        
        if (description.length() < 10 || description.length() > 500) {
            showAlert("La description doit contenir entre 10 et 500 caractères");
            descriptionArea.requestFocus();
            return;
        }
        
        if (type.isEmpty()) {
            showAlert("Le type est obligatoire");
            typeactField.requestFocus();
            return;
        }
        
        if (!type.matches("^[A-Za-zÀ-ÿ\\s]{2,30}$")) {
            showAlert("Le type doit contenir uniquement des lettres (2-30 caractères)");
            typeactField.requestFocus();
            return;
        }

        try {
            String sql = "INSERT INTO activites (titre, description, image) VALUES (?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, titre);
            pstmt.setString(2, description);
            pstmt.setString(3, imagePath.isEmpty() ? "default.jpg" : imagePath);
            pstmt.executeUpdate();

            showAlert("Activité ajoutée avec succès");
            fermerFenetre();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ajout de l'activité");
        }
    }

    @FXML
    void goToCatalogue(ActionEvent event) {
        fermerFenetre();
    }

    @FXML
    void annuler(ActionEvent event) {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
        
        // Rafraîchir le catalogue automatiquement
        CatalogueRefreshManager.getInstance().requestRefresh();
        refreshCatalogue();
    }
    
    private void refreshCatalogue() {
        try {
            // Trouver toutes les fenêtres ouvertes et rafraîchir les catalogues
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    if (stage.getTitle() != null && stage.getTitle().contains("Catalogue")) {
                        // Envoyer un événement de rafraîchissement
                        stage.requestFocus();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
