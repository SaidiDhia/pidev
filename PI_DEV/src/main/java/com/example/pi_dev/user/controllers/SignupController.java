package com.example.pi_dev.user.controllers;

import com.example.pi_dev.user.enums.RoleEnum;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.common.dao.ActivityLogDAO;
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

import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.util.UUID;

public class SignupController {

    @FXML
    private Circle profileCircle;

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
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private File selectedImageFile;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(RoleEnum.values()));
        roleComboBox.getSelectionModel().select(RoleEnum.PARTICIPANT); // Default
    }

    @FXML
    void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        
        File file = fileChooser.showOpenDialog(profileCircle.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            Image image = new Image(file.toURI().toString());
            profileCircle.setFill(new ImagePattern(image));
        }
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

        // Handle Profile Picture
        if (selectedImageFile != null) {
            try {
                File uploadDir = new File("uploads/profiles");
                if (!uploadDir.exists()) uploadDir.mkdirs();
                
                String fileName = UUID.randomUUID().toString() + "_" + selectedImageFile.getName();
                File destFile = new File(uploadDir, fileName);
                Files.copy(selectedImageFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                newUser.setProfilePicture(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            userService.register(newUser);
            activityLogDAO.log(newUser.getEmail(), "SIGNUP", "New user registered: " + newUser.getFullName());
            // Navigate to Login
            goToLogin(event);
        } catch (Exception e) {
            errorLabel.setText("Registration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInput() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Please fill in all required fields");
            return false;
        }

        if (fullName.length() < 3) {
            errorLabel.setText("Full Name must be at least 3 characters");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorLabel.setText("Please enter a valid email address");
            return false;
        }

        if (!phone.matches("^\\+?[0-9]{8,15}$")) {
            errorLabel.setText("Please enter a valid phone number (8-15 digits)");
            return false;
        }

        if (password.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters long");
            return false;
        }

        if (!password.equals(confirmPassword)) {
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
