package com.example.pi_dev.user.controllers;

import com.example.pi_dev.user.enums.RoleEnum;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.common.services.ActivityLogService;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

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
    private ProgressBar strengthBar;

    @FXML
    private Label strengthLabel;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();
    private final ActivityLogService activityLogService = new ActivityLogService();
    private File selectedImageFile;

    @FXML
    public void initialize() {
        // Filter out ADMIN from roles
        java.util.List<RoleEnum> filteredRoles = new java.util.ArrayList<>();
        for (RoleEnum role : RoleEnum.values()) {
            if (role != RoleEnum.ADMIN) {
                filteredRoles.add(role);
            }
        }
        roleComboBox.setItems(FXCollections.observableArrayList(filteredRoles));
        roleComboBox.getSelectionModel().select(RoleEnum.PARTICIPANT); // Default

        // Password strength listener
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal);
        });
    }

    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            animateStrengthBar(0);
            strengthBar.setStyle("-fx-accent: #E5E7EB;");
            strengthLabel.setText("Strength: None");
            strengthLabel.setStyle("-fx-text-fill: #6B7280;");
            return;
        }

        double strength = 0;
        int criteriaMet = 0;
        
        if (password.length() >= 8) criteriaMet++;
        if (password.matches(".*[a-z].*")) criteriaMet++;
        if (password.matches(".*[A-Z].*")) criteriaMet++;
        if (password.matches(".*[0-9].*")) criteriaMet++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) criteriaMet++;

        strength = criteriaMet / 5.0;
        animateStrengthBar(strength);

        // Clear existing style classes
        strengthBar.getStyleClass().removeAll("very-weak", "weak", "medium", "good", "strong");

        // Update colors and labels based on progress
        if (strength <= 0.2) {
            strengthBar.getStyleClass().add("very-weak");
            strengthBar.setStyle("-fx-accent: #EF4444;"); 
            strengthLabel.setText("Strength: Very Weak");
            strengthLabel.setStyle("-fx-text-fill: #EF4444;");
        } else if (strength <= 0.4) {
            strengthBar.getStyleClass().add("weak");
            strengthBar.setStyle("-fx-accent: #F97316;");
            strengthLabel.setText("Strength: Weak");
            strengthLabel.setStyle("-fx-text-fill: #F97316;");
        } else if (strength <= 0.6) {
            strengthBar.getStyleClass().add("medium");
            strengthBar.setStyle("-fx-accent: #F59E0B;");
            strengthLabel.setText("Strength: Medium");
            strengthLabel.setStyle("-fx-text-fill: #F59E0B;");
        } else if (strength <= 0.8) {
            strengthBar.getStyleClass().add("good");
            strengthBar.setStyle("-fx-accent: #84CC16;");
            strengthLabel.setText("Strength: Good");
            strengthLabel.setStyle("-fx-text-fill: #84CC16;");
        } else {
            strengthBar.getStyleClass().add("strong");
            strengthBar.setStyle("-fx-accent: #10B981;");
            strengthLabel.setText("Strength: Strong");
            strengthLabel.setStyle("-fx-text-fill: #10B981;");
        }
    }

    private void animateStrengthBar(double targetValue) {
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(strengthBar.progressProperty(), targetValue);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(300), keyValue);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
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
            activityLogService.log(newUser.getEmail(), "SIGNUP", "New user registered: " + newUser.getFullName());
            // Navigate to Login
            if (isInsideHomeView(event)) {
                Parent root = FXMLLoader.load(getClass().getResource("/com/example/pi_dev/user/login.fxml"));
                replaceInHomeView(root, event);
            } else {
                goToLogin(event);
            }
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

        if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*[0-9].*")) {
            errorLabel.setText("Password must contain at least one letter and one number");
            return false;
        }

        if (strengthLabel.getText().contains("Weak")) {
            errorLabel.setText("Password is too weak. Please use a stronger password.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            return false;
        }

        return true;
    }

    private boolean isInsideHomeView(ActionEvent event) {
        Node source = (Node) event.getSource();
        Scene scene = source.getScene();
        if (scene != null && scene.getRoot() instanceof javafx.scene.layout.BorderPane) {
            javafx.scene.layout.BorderPane root = (javafx.scene.layout.BorderPane) scene.getRoot();
            return root.getCenter() instanceof javafx.scene.layout.StackPane && 
                   root.getCenter().getId() != null && 
                   root.getCenter().getId().equals("mainContentArea");
        }
        return false;
    }

    private void replaceInHomeView(Parent root, ActionEvent event) {
        Node source = (Node) event.getSource();
        javafx.scene.layout.StackPane mainContentArea = (javafx.scene.layout.StackPane) source.getScene().lookup("#mainContentArea");
        if (mainContentArea != null) {
            root.setStyle("-fx-background-color: rgba(249, 250, 251, 0.98);");
            mainContentArea.getChildren().remove(mainContentArea.getChildren().size() - 1);
            mainContentArea.getChildren().add(root);
        }
    }

    @FXML
    void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pi_dev/user/login.fxml"));
            if (isInsideHomeView(event)) {
                replaceInHomeView(root, event);
            } else {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
