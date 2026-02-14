package com.example.pi_dev.user;

import com.example.pi_dev.user.enums.RoleEnum;
import com.example.pi_dev.user.models.User;
import com.example.pi_dev.user.services.UserService;

import java.time.LocalDateTime;

public class UserMain {
    public static void main(String[] args) {
        UserService userService = new UserService();

        // 1. Register a new user
        User newUser = new User();
        newUser.setEmail("test@example.com");
        newUser.setPasswordHash("password123"); // Will be hashed by service
        newUser.setFullName("Test User");
        newUser.setRole(RoleEnum.PARTICIPANT);
        newUser.setIsActive(true);

        System.out.println("Registering user...");
        try {
            userService.register(newUser);
            System.out.println("User registered successfully: " + newUser.getUserId());
        } catch (Exception e) {
            System.out.println("Registration failed (maybe email exists?): " + e.getMessage());
        }

        // 2. Login
        System.out.println("Logging in...");
        String token = userService.login("test@example.com", "password123");
        if (token != null) {
            System.out.println("Login successful! JWT Token:");
            System.out.println(token);
        } else {
            System.out.println("Login failed.");
        }

        // 3. Get User by ID
        System.out.println("Fetching user by ID...");
        User fetchedUser = userService.getUserById(newUser.getUserId());
        if (fetchedUser != null) {
            System.out.println("User found: " + fetchedUser.getFullName());
        }
    }
}
