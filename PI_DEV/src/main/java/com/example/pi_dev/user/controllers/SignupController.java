package com.example.pi_dev.user.controllers;

import com.example.pi_dev.user.enums.RoleEnum;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.user.services.UserService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.UUID;

public class SignupController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<RoleEnum> roleComboBox;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(RoleEnum.values()));
        roleComboBox.getSelectionModel().select(RoleEnum.PARTICIPANT); // Default
    }

    @FXML
    void handleSignup(ActionEvent event) {
        if (!validateInput()) return;

        User newUser = new User();
        newUser.setUserId(UUID.randomUUID());
        newUser.setFullName(fullNameField.getText());
        newUser.setEmail(emailField.getText());
        newUser.setPhoneNumber(phoneField.getText());
        newUser.setRole(roleComboBox.getValue());
        newUser.setPasswordHash(passwordField.getText());
        newUser.setIsActive(true); // Default active

        try {
            userService.register(newUser);
            // Navigate to Login
            goToLogin(event);
        } catch (Exception e) {
            errorLabel.setText("Registration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInput() {
        if (fullNameField.getText().isEmpty() || emailField.getText().isEmpty() || 
            passwordField.getText().isEmpty() || confirmPasswordField.getText().isEmpty()) {
            errorLabel.setText("Please fill in all required fields");
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errorLabel.setText("Passwords do not match");
            return false;
        }

        return true;
    }

    @FXML
    void goToLogin(ActionEvent event) {
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
