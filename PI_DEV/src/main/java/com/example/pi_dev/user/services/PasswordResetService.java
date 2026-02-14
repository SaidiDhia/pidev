package com.example.pi_dev.user.services;

import com.example.pi_dev.user.database.UserDatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public class PasswordResetService {

    private final EmailService emailService;

    public PasswordResetService() {
        this.emailService = new EmailService();
    }

    public void initiatePasswordReset(String email) {
        // 1. Verify user exists
        // In a real app, you should inject UserRepository or UserService
        // For now, I'll do a quick DB check or assume valid if calling from UserService
        // But better to do it here.
        
        Long userId = getUserIdByEmail(email);
        if (userId == null) {
            System.out.println("User not found for password reset: " + email);
            return;
        }

        // 2. Generate Token
        String token = String.format("%06d", new java.util.Random().nextInt(999999));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15); // 15 mins expiry

        // 3. Save to DB
        saveResetToken(userId, token, expiresAt);

        // 4. Send Email
        String body = "Your password reset code is: " + token + "\n\nThis code expires in 15 minutes.";
        emailService.sendEmail(email, "Password Reset Code", body);
    }

    // Returns the userId if successful, null otherwise
    public Long resetPassword(String token, String newPasswordHash) {
        // 1. Validate Token
        Long userId = getUserIdByToken(token);
        if (userId == null) {
            System.out.println("Invalid or expired token.");
            return null;
        }

        // 2. Update Password
        updateUserPassword(userId, newPasswordHash);

        // 3. Delete Token
        deleteToken(token);
        return userId;
    }

    private Long getUserIdByEmail(String email) {
        String sql = "SELECT user_id FROM users WHERE email = ?";
        try (PreparedStatement ps = UserDatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("user_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveResetToken(Long userId, String token, LocalDateTime expiresAt) {
        String sql = "INSERT INTO password_reset_tokens (token_id, user_id, token, expires_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = UserDatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setLong(2, userId);
            ps.setString(3, token);
            ps.setTimestamp(4, Timestamp.valueOf(expiresAt));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Long getUserIdByToken(String token) {
        String sql = "SELECT user_id, expires_at FROM password_reset_tokens WHERE token = ?";
        try (PreparedStatement ps = UserDatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp expiresAt = rs.getTimestamp("expires_at");
                if (expiresAt != null && expiresAt.toLocalDateTime().isAfter(LocalDateTime.now())) {
                    return rs.getLong("user_id");
                } else {
                    System.out.println("Token expired: " + token);
                }
            } else {
                System.out.println("Token not found: " + token);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateUserPassword(Long userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (PreparedStatement ps = UserDatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteToken(String token) {
        String sql = "DELETE FROM password_reset_tokens WHERE token = ?";
        try (PreparedStatement ps = UserDatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
