package com.example.pi_dev.user.services;

import com.example.pi_dev.user.models.User;
import com.google.zxing.WriterException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface IUserService {
    User register(User user);
    String login(String email, String password); // Returns JWT
    User getUserById(long userId);
    List<User> getAllUsers();
    void updateUser(User user);
    void deleteUser(long userId);

    // Two-Factor Authentication
    byte[] enableTwoFactor(long userId) throws IOException, WriterException, SQLException;
    boolean verifyTwoFactor(long userId, int code);

    // Password Reset
    void initiatePasswordReset(String email);
    boolean resetPassword(String token, String newPassword);
}
