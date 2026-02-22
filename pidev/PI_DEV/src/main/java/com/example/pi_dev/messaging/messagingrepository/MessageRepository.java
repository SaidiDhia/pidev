package com.example.pi_dev.messaging.messagingrepository;

import com.example.pi_dev.messaging.messagingdatabase.DatabaseConnection;
import com.example.pi_dev.messaging.messagingmodel.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {

    public void create(Message m) throws SQLException {
        String sql = """
            INSERT INTO message (conversation_id, sender_id, content, created_at)
            VALUES (?, ?, ?, NOW())
        """;

        try (PreparedStatement ps =
                     DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {

            ps.setLong(1, m.getConversationId());
            ps.setString(2, m.getSenderId());
            ps.setString(3, m.getContent());
            ps.executeUpdate();
        }
    }

    public List<Message> findByConversation(long conversationId, String userId) throws SQLException {
        List<Message> list = new ArrayList<>();  // ← ADD THIS LINE

        String sql = """
        SELECT m.*
        FROM message m
        JOIN conversation_user cu ON cu.conversation_id = m.conversation_id
        WHERE m.conversation_id = ? AND cu.user_id = ?
        ORDER BY m.created_at
    """;

        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setLong(1, conversationId);
            ps.setString(2, userId);  // ← Fix: you were using ? but setLong(2) was missing
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Message(
                        rs.getLong("id"),
                        rs.getLong("conversation_id"),
                        rs.getString("sender_id"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        }
        return list;
    }

    public void delete(long messageId, String userId) throws SQLException {
        String sql = """
        DELETE FROM message
        WHERE id = ? AND sender_id = ?
    """;

        try (PreparedStatement ps = DatabaseConnection
                .getInstance()
                .getConnection()
                .prepareStatement(sql)) {

            ps.setLong(1, messageId);
            ps.setString(2, userId);
            ps.executeUpdate();
        }
    }
    public void update(long messageId, String userId, String newContent)
            throws SQLException {

        String sql = """
        UPDATE message
        SET content = ?
        WHERE id = ? AND sender_id = ?
    """;

        try (PreparedStatement ps = DatabaseConnection
                .getInstance()
                .getConnection()
                .prepareStatement(sql)) {

            ps.setString(1, newContent);
            ps.setLong(2, messageId);
            ps.setString(3, userId);
            ps.executeUpdate();
        }
    }


}
