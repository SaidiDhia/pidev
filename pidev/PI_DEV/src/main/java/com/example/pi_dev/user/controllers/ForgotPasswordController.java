package com.example.pi_dev.user.controllers;

import com.example.pi_dev.user.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ForgotPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField tokenField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private VBox emailContainer;

    @FXML
    private VBox resetContainer;

    @FXML
    private Label statusLabel;

    @FXML
    private Button actionButton;

    private final UserService userService = new UserService();
    private boolean isResetStage = false;

    @FXML
    void handleAction(ActionEvent event) {
        if (!isResetStage) {
            // Stage 1: Send Email
            String email = emailField.getText();
            if (email.isEmpty()) {
                statusLabel.setText("Please enter your email.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            statusLabel.setText("Sending email...");
            statusLabel.setStyle("-fx-text-fill: blue;");
            
            // Run in background thread ideally, but for now:
            try {
                userService.initiatePasswordReset(email);
                
                // Switch to Stage 2
                isResetStage = true;
                emailContainer.setVisible(false);
                emailContainer.setManaged(false);
                resetContainer.setVisible(true);
                resetContainer.setManaged(true);
                actionButton.setText("Reset Password");
                statusLabel.setText("Email sent! Check your inbox for the code.");
                statusLabel.setStyle("-fx-text-fill: green;");
                
            } catch (Exception e) {
                statusLabel.setText("Error sending email: " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }
        } else {
            // Stage 2: Reset Password
            String token = tokenField.getText();
            String newPass = newPasswordField.getText();
            
            if (token.isEmpty() || newPass.isEmpty()) {
                statusLabel.setText("Please fill all fields.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            boolean success = userService.resetPassword(token, newPass);
            if (success) {
                statusLabel.setText("Password reset successfully! Redirecting...");
                statusLabel.setStyle("-fx-text-fill: green;");
                // Navigate back to login after short delay or immediately
                goBack(event);
            } else {
                statusLabel.setText("Invalid code or error resetting password.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    @FXML
    void goBack(ActionEvent event) {
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