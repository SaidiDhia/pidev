package test; // doit correspondre au dossier /package

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Reservation.fxml"));
            Parent root = loader.load();

            // Créer la scène
            Scene scene = new Scene(root);

            // Configurer le stage
            primaryStage.setTitle("Wanderlust - Réservation");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace(); // affiche la vraie erreur
        }
    }

    public static void main(String[] args) {
        launch(args); // lance JavaFX
    }
}
