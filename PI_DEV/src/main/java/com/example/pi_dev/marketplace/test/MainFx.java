package com.example.pi_dev.marketplace.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainFx extends Application {

    private static Stage mainStage;
    private static Scene mainScene;
    private static BorderPane rootPane;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent firstPage = FXMLLoader.load(getClass().getResource("/com/example/pi_dev/marketplace/fxml/RoleSelection.fxml"));

        rootPane = new BorderPane();
        rootPane.setCenter(firstPage);

        mainScene = new Scene(rootPane, 900, 650);

        mainStage = primaryStage;
        mainStage.setTitle("Marketplace App");
        mainStage.setScene(mainScene);
        mainStage.setMinWidth(900);
        mainStage.setMinHeight(650);
        mainStage.show();
    }

    /**
     * Set center directly with an already-loaded Parent node.
     * Use this when you need to pass data to the controller before displaying.
     */
    public static void setCenter(Parent node) {
        rootPane.setCenter(node);
    }

    /**
     * Set center by FXML path — always reloads fresh to avoid
     * scene graph reuse issues and stale controller state.
     */
    public static void setCenter(String fxmlPath) {
        try {
            Parent page = FXMLLoader.load(MainFx.class.getResource(fxmlPath));
            rootPane.setCenter(page);
        } catch (Exception e) {
            System.err.println("Failed to load FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}