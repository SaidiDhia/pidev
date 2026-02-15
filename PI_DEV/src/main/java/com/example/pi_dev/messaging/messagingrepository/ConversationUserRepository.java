package com.example.pi_dev.messaging.messagingrepository;


import com.example.pi_dev.messaging.messagingdatabase.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConversationUserRepository {

    public void addUserToConversation(long conversationId, String userId) {

        String sql = "INSERT INTO conversation_user (conversation_id, user_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, conversationId);
            stmt.setString(2, userId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}