package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class modifierActiviteController {

    @FXML
    private Button annuler;

    @FXML
    private Button btnModifier;

    @FXML
    private Button catalog;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField imageField1;

    @FXML
    private TextField titreField;

    @FXML
    private TextField typeactField;

    private Connection connection;
    private int activiteId;

    public void initialize() {
        initializeDatabase();
        
        // Activer le bouton modifier au démarrage
        if (btnModifier != null) {
            btnModifier.setDisable(false);
            System.out.println("Bouton modifier activé");
        } else {
            System.out.println("Bouton modifier est null");
        }
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/wonderlust_db", "root", "");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setActiviteId(int id) {
        this.activiteId = id;
        loadActiviteData();
    }

    private void loadActiviteData() {
        try {
            String sql = "SELECT id, titre, description, image FROM activites WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, activiteId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                titreField.setText(rs.getString("titre"));
                descriptionArea.setText(rs.getString("description"));
                typeactField.setText("Sport"); // Type par défaut
                imageField1.setText(rs.getString("image"));
            } else {
                showAlert("Activité non trouvée");
                fermerFenetre();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement des données");
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        fermerFenetre();
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
    void modifier(ActionEvent event) {
        System.out.println("Méthode modifier appelée");
        System.out.println("ID activité: " + activiteId);
        
        String titre = titreField.getText().trim();
        String description = descriptionArea.getText().trim();
        String type = typeactField.getText().trim();
        String imagePath = imageField1.getText().trim();
        
        System.out.println("Titre: " + titre);
        System.out.println("Description: " + description);
        System.out.println("Type: " + type);
        System.out.println("Image: " + imagePath);

        // Validation
        if (titre.isEmpty()) {
            showAlert("Le titre est obligatoire");
            return;
        }

        if (description.isEmpty()) {
            showAlert("La description est obligatoire");
            return;
        }

        if (type.isEmpty()) {
            showAlert("Le type est obligatoire");
            return;
        }

        try {
            String sql = "UPDATE activites SET titre = ?, description = ?, image = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, titre);
            pstmt.setString(2, description);
            pstmt.setString(3, imagePath.isEmpty() ? "default.jpg" : imagePath);
            pstmt.setInt(4, activiteId);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);

            showAlert("Activité modifiée avec succès");
            fermerFenetre();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur lors de la modification de l'activité");
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
