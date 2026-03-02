package com.example.pi_dev.messaging.messagingrepository;

import com.example.pi_dev.messaging.messagingdatabase.DatabaseConnection;
import com.example.pi_dev.messaging.messagingmodel.Reaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReactionRepository {

    public void addReaction(long messageId, String userId, String reaction) throws SQLException {
        String sql = "INSERT INTO message_reactions (message_id, user_id, reaction) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, messageId);
            stmt.setString(2, userId);
            stmt.setString(3, reaction);
            stmt.executeUpdate();
        }
    }

    public void removeReaction(long messageId, String userId) throws SQLException {
        String sql = "DELETE FROM message_reactions WHERE message_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, messageId);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        }
    }

    public List<Reaction> getReactionsForMessage(long messageId) throws SQLException {
        String sql = """
            SELECT r.*, u.full_name 
            FROM message_reactions r
            LEFT JOIN users u ON r.user_id = u.user_id
            WHERE r.message_id = ?
            ORDER BY r.created_at
        """;

        List<Reaction> reactions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, messageId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reaction r = new Reaction();
                r.setId(rs.getLong("id"));
                r.setMessageId(rs.getLong("message_id"));
                r.setUserId(rs.getString("user_id"));
                r.setReaction(rs.getString("reaction"));
                r.setCreatedAt(rs.getTimestamp("created_at") != null ?
                        rs.getTimestamp("created_at").toLocalDateTime() : null);
                r.setUserFullName(rs.getString("full_name"));
                reactions.add(r);
            }
        }
        return reactions;
    }

    public String getUserReaction(long messageId, String userId) throws SQLException {
        String sql = "SELECT reaction FROM message_reactions WHERE message_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, messageId);
            stmt.setString(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("reaction");
            }
        }
        return null;
    }
}