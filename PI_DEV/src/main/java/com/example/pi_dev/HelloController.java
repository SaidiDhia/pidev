package com.example.pi_dev;

import com.example.pi_dev.user.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {
    @FXML
    protected void onCreateEvent(ActionEvent event) {
        try {
            Parent root;
            if (UserSession.getInstance().isLoggedIn()) {
                root = FXMLLoader.load(getClass().getResource("/views/Catalogue.fxml"));
            } else {
                root = FXMLLoader.load(getClass().getResource("/com/example/pi_dev/user/login.fxml"));
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onRentPlace(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pi_dev/user/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
