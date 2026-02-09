package com.example.pi_dev.user.controllers;

import com.example.pi_dev.user.enums.RoleEnum;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.user.services.UserService;
import com.example.pi_dev.user.utils.UserSession;
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
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label passwordErrorLabel;

    @FXML
    private Label tfaStatusLabel;

    private final UserService userService = new UserService();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            fullNameField.setText(currentUser.getFullName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhoneNumber());
            
            updateTfaStatus();
        }
    }
    
    private void updateTfaStatus() {
        boolean isTfaEnabled = currentUser.getTfaMethod() != null;
        if (isTfaEnabled) {
            tfaStatusLabel.setText("ON (" + currentUser.getTfaMethod() + ")");
            tfaStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            tfaStatusLabel.setText("OFF");
            tfaStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    @FXML
    void handleUpdateProfile(ActionEvent event) {
        if (currentUser != null) {
            currentUser.setFullName(fullNameField.getText());
            currentUser.setPhoneNumber(phoneField.getText());
            
            try {
                userService.updateUser(currentUser);
                // Show success message (Optional: Alert)
                System.out.println("Profile updated!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleChangePassword(ActionEvent event) {
        String currentPass = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();
        
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            passwordErrorLabel.setText("All fields are required.");
            passwordErrorLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        if (!newPass.equals(confirmPass)) {
            passwordErrorLabel.setText("New passwords do not match.");
            passwordErrorLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        if (newPass.length() < 6) {
             passwordErrorLabel.setText("Password must be at least 6 characters.");
             passwordErrorLabel.setStyle("-fx-text-fill: red;");
             return;
        }

        boolean success = userService.changePassword(currentUser.getUserId(), currentPass, newPass);
        
        if (success) {
            passwordErrorLabel.setText("Password updated successfully! 2FA disabled.");
            passwordErrorLabel.setStyle("-fx-text-fill: green;");
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            // Refresh user to get updated fields if any
            currentUser = userService.getUserById(currentUser.getUserId());
            updateTfaStatus(); // Because we disabled 2FA
        } else {
            passwordErrorLabel.setText("Incorrect current password.");
            passwordErrorLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    void handleEnableApp2FA(ActionEvent event) {
        goTo2FA(event, "QR");
    }

    @FXML
    void handleEnableEmail2FA(ActionEvent event) {
        goTo2FA(event, "EMAIL");
    }

    @FXML
    void handleEnableFace2FA(ActionEvent event) {
        goTo2FA(event, "FACE");
    }

    @FXML
    void handleDisable2FA(ActionEvent event) {
        if (currentUser != null) {
            currentUser.setTfaMethod(null);
            userService.updateUser(currentUser);
            updateTfaStatus();
            System.out.println("2FA Disabled");
        }
    }

    private void goTo2FA(ActionEvent event, String method) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pi_dev/user/2fa.fxml"));
            Parent root = loader.load();
            
            TwoFactorController controller = loader.getController();
            controller.initData(true, method); // Setup Mode with Method
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    void handleBack(ActionEvent event) {
        navigateTo("/com/example/pi_dev/user/admin_dashboard.fxml", event);
    }

    @FXML
    void handleLogout(ActionEvent event) {
        UserSession.getInstance().logout();
        navigateTo("/com/example/pi_dev/user/login.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
