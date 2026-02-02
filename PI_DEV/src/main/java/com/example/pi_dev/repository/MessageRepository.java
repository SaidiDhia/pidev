package com.example.pi_dev.repository;

import com.example.pi_dev.database.DatabaseConnection;
import com.example.pi_dev.model.Message;

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
            ps.setLong(2, m.getSenderId());
            ps.setString(3, m.getContent());
            ps.executeUpdate();
        }
    }

    public List<Message> findByConversation(long conversationId) throws SQLException {
        List<Message> list = new ArrayList<>();

        String sql = """
            SELECT id, conversation_id, sender_id, content, created_at
            FROM message
            WHERE conversation_id = ?
            ORDER BY created_at
        """;

        try (PreparedStatement ps =
                     DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {

            ps.setLong(1, conversationId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Message(
                        rs.getLong("id"),
                        rs.getLong("conversation_id"),
                        rs.getLong("sender_id"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        }
        return list;
    }
}
