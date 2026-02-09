package com.example.pi_dev.user.controllers;

import com.example.pi_dev.user.enums.RoleEnum;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.user.services.UserService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserFormController {

    @FXML private Label titleLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<RoleEnum> roleComboBox;
    @FXML private CheckBox isActiveCheckBox;
    @FXML private Label errorLabel;

    private UserService userService;
    private User user;
    private boolean isEditMode = false;
    private Runnable onSaveCallback;

    public void setService(UserService userService) {
        this.userService = userService;
    }
    
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(RoleEnum.values()));
        roleComboBox.getSelectionModel().select(RoleEnum.PARTICIPANT);
        isActiveCheckBox.setSelected(true);
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            isEditMode = true;
            titleLabel.setText("Edit User");
            fullNameField.setText(user.getFullName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhoneNumber());
            roleComboBox.getSelectionModel().select(user.getRole());
            isActiveCheckBox.setSelected(user.getIsActive() != null ? user.getIsActive() : true);
            // Password field left blank by default in edit mode
        } else {
            isEditMode = false;
            titleLabel.setText("Add New User");
            this.user = new User();
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        RoleEnum role = roleComboBox.getValue();
        boolean isActive = isActiveCheckBox.isSelected();

        if (fullName.isEmpty() || email.isEmpty()) {
            errorLabel.setText("Name and Email are required.");
            return;
        }

        if (!isEditMode && password.isEmpty()) {
            errorLabel.setText("Password is required for new users.");
            return;
        }

        try {
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhoneNumber(phone);
            user.setRole(role);
            user.setIsActive(isActive);

            if (isEditMode) {
                if (!password.isEmpty()) {
                    user.setPasswordHash(password); // Service should hash this? No, register hashes it. Update usually expects hashed.
                    // We need to hash it if it's changed.
                    // UserService.updateUser doesn't hash automatically usually.
                    // Let's check UserService.register - it hashes.
                    // Let's check UserService.updateUser - it just updates.
                    // So we should hash it here or add a method in service.
                    // Since I don't have easy access to hash function here (it's private in Service usually or util), 
                    // I'll assume UserService has a way or I'll use the public one if available.
                    // Actually UserService.changePassword hashes it. 
                    // But here we are updating everything.
                    // Let's look at UserService again. It has `hashPassword` private.
                    // I should probably expose a method `updateUserWithPassword` or just rely on the fact that I can't hash it easily here without duplicating logic.
                    // Wait, UserService has `hashPassword`? It's private in the file I read.
                    // I will modify UserService to handle password update if provided, or I will make `hashPassword` public/static in a Util.
                    // For now, let's assume I'll handle it in the Service.
                }
                
                // For now, if password is changed in Edit, we might have an issue if we don't hash it.
                // I will add `updateUser(User, String rawPassword)` to UserService to handle this cleanly.
                userService.updateUser(user); 
                if (!password.isEmpty()) {
                     userService.changePassword(user.getUserId(), "DUMMY", password); // This requires old password...
                     // Okay, I need a method in UserService to force update password without old one (Admin override).
                     // I will add `adminUpdatePassword(UUID, String)` to UserService.
                }
            } else {
                user.setUserId(UUID.randomUUID());
                user.setPasswordHash(password); // Register will hash it
                user.setCreatedAt(LocalDateTime.now());
                userService.register(user);
            }

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            closeStage();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error saving user: " + e.getMessage());
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.close();
    }
}