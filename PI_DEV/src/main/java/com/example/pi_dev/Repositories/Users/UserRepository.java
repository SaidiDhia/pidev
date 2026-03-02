package com.example.pi_dev.Repositories.Users;

import com.example.pi_dev.Database.Users.UserDatabaseConnection;
import com.example.pi_dev.enums.RoleEnum;
import com.example.pi_dev.enums.TFAMethod;
import com.example.pi_dev.Entities.Users.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepository {

    public void create(User user) throws SQLException {
        String sql = "INSERT INTO users (user_id, email, password_hash, full_name, phone_number, is_active, role, tfa_method, created_at, profile_picture) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = UserDatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getUserId().toString());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getPhoneNumber());
            ps.setBoolean(6, user.getIsActive());
            ps.setString(7, user.getRole().name());
            ps.setString(8, user.getTfaMethod() != null ? user.getTfaMethod().name() : null);
            ps.setTimestamp(9, Timestamp.valueOf(user.getCreatedAt()));
            ps.setString(10, user.getProfilePicture());

            ps.executeUpdate();
        }
    }

    public Optional<User> findById(UUID userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = UserDatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = UserDatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        }
        return Optional.empty();
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement st = UserDatabaseConnection.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET email = ?, password_hash = ?, full_name = ?, phone_number = ?, is_active = ?, role = ?, tfa_method = ?, profile_picture = ? WHERE user_id = ?";
        try (PreparedStatement ps = UserDatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getPhoneNumber());
            ps.setBoolean(5, user.getIsActive());
            ps.setString(6, user.getRole().name());
            ps.setString(7, user.getTfaMethod() != null ? user.getTfaMethod().name() : null);
            ps.setString(8, user.getProfilePicture());
            ps.setString(9, user.getUserId().toString());

            ps.executeUpdate();
        }
    }

    public void delete(UUID userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement ps = UserDatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userId.toString());
            ps.executeUpdate();
        }
    }

    public void updateRole(UUID userId, RoleEnum role) throws SQLException {
        String sql = "UPDATE users SET role=? WHERE user_id = ?";
        try (PreparedStatement ps = UserDatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, role.name());
            ps.setString(2, userId.toString());
            ps.executeUpdate();
        }
    }

    // TFA Methods
    public void saveTfaSecret(UUID userId, String secretKey) throws SQLException {
        String sql = "INSERT INTO tfa_secrets (user_id, secret_key) VALUES (?, ?) ON DUPLICATE KEY UPDATE secret_key = ?";
        try (PreparedStatement ps = UserDatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userId.toString());
            ps.setString(2, secretKey);
            ps.setString(3, secretKey);
            ps.executeUpdate();
        }
    }

    public String getTfaSecret(UUID userId) throws SQLException {
        String sql = "SELECT secret_key FROM tfa_secrets WHERE user_id = ?";
        try (PreparedStatement ps = UserDatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("secret_key");
            }
        }
        return null;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                UUID.fromString(rs.getString("user_id")),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                rs.getString("phone_number"),
                rs.getBoolean("is_active"),
                RoleEnum.valueOf(rs.getString("role")),
                rs.getString("tfa_method") != null ? TFAMethod.valueOf(rs.getString("tfa_method")) : null,
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("profile_picture")
        );
    }
}
