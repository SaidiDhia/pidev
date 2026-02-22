package Controllers;

import Entities.Activite;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class modifierActiviteController {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField typeactField;
    @FXML private TextField imageField1;
    @FXML private Button modifierButton;
    @FXML private Button supprimerButton;
    @FXML private Button annulerButton;
    @FXML private Label titleLabel;

    private Activite currentActivite;
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

    public void setActiviteData(Activite activite) {
        this.currentActivite = activite;
        
        System.out.println("DEBUG Modification - ID: " + activite.getId());
        System.out.println("DEBUG Modification - Titre: " + activite.getTitre());
        System.out.println("DEBUG Modification - Type: " + activite.getTypeActivite());
        System.out.println("DEBUG Modification - Description: " + activite.getDescription());
        System.out.println("DEBUG Modification - Image: " + activite.getImage());
        
        // Pré-remplir les champs avec les données existantes
        titreField.setText(activite.getTitre());
        descriptionArea.setText(activite.getDescription());
        typeactField.setText(activite.getTypeActivite() != null ? activite.getTypeActivite() : "");
        imageField1.setText(activite.getImage() != null ? activite.getImage() : "");
        
        // Mettre à jour le titre de la fenêtre
        if (titleLabel != null) {
            titleLabel.setText("Modifier l'activité: " + activite.getTitre());
        }
    }

    @FXML
    void modifier(ActionEvent event) {
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
        
        if (description.isEmpty()) {
            showAlert("La description est obligatoire");
            descriptionArea.requestFocus();
            return;
        }
        
        if (type.isEmpty()) {
            showAlert("Le type d'activité est obligatoire");
            typeactField.requestFocus();
            return;
        }

        try {
            String sql = "UPDATE activites SET titre = ?, description = ?, type_activite = ?, image = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, titre);
            pstmt.setString(2, description);
            pstmt.setString(3, type);
            pstmt.setString(4, imagePath);
            pstmt.setInt(5, currentActivite.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showAlert("Activité modifiée avec succès");
                fermerFenetre(); // Ferme et retourne au catalogue
            } else {
                showAlert("Aucune modification effectuée");
            }
            
        } catch (SQLException e) {
            showAlert("Erreur lors de la modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void supprimer(ActionEvent event) {
        if (currentActivite == null) return;
        
        try {
            String sql = "DELETE FROM activites WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, currentActivite.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showAlert("Activité supprimée avec succès");
                fermerFenetre();
            } else {
                showAlert("Aucune activité supprimée");
            }
            
        } catch (SQLException e) {
            showAlert("Erreur lors de la suppression: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        fermerFenetre();
    }

    @FXML
    void goToCatalogue(ActionEvent event) {
        fermerFenetre();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }
}
