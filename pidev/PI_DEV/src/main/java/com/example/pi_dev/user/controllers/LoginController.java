package com.example.pi_dev.user.controllers;

import com.example.pi_dev.user.enums.RoleEnum;
import com.example.pi_dev.user.enums.TFAMethod;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.user.services.UserService;
import com.example.pi_dev.user.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }

        String token = userService.login(email, password);

        if (token != null) {
            // Login success
            try {
                // Fetch full user details
                User user = userService.getAllUsers().stream()
                        .filter(u -> u.getEmail().equals(email))
                        .findFirst()
                        .orElseThrow();

                UserSession.getInstance().login(user, token);
                
                System.out.println("Login successful!");
                System.out.println("JWT Token: " + token);
                
                // Check for 2FA
                if (user.getTfaMethod() != null) {
                    // Navigate to 2FA Verify
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pi_dev/user/2fa.fxml"));
                    Parent root = loader.load();
                    
                    TwoFactorController controller = loader.getController();
                    controller.initData(false); // Verify Mode (auto-detects method)
                    
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    // Navigate based on Role
                    if (user.getRole() == RoleEnum.ADMIN) {
                        navigateTo("/com/example/pi_dev/user/admin_dashboard.fxml", event);
                    } else {
                        navigateTo("/com/example/pi_dev/user/settings.fxml", event);
                    }
                }

            } catch (Exception e) {
                errorLabel.setText("Login failed: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Invalid credentials");
        }
    }

    @FXML
    void goToSignup(ActionEvent event) {
        navigateTo("/com/example/pi_dev/user/signup.fxml", event);
    }
    
    @FXML
    void goToForgotPassword(ActionEvent event) {
        navigateTo("/com/example/pi_dev/user/forgot_password.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Navigation error: " + e.getMessage());
        }
    }
}
