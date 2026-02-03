package com.example.pi_dev.messaging.messagingrepository;

import com.example.pi_dev.messaging.messagingdatabase.DatabaseConnection;
import com.example.pi_dev.messaging.messagingmodel.Conversation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ConversationRepository {

    public void create(Conversation c) throws SQLException {
        String sql = "INSERT INTO conversation (type, context_type, context_id, created_at) VALUES (?, ?, ?, NOW())";

        //zedt lget instance bch yaarf li na7kiw 3leha
        PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql);
        ps.setString(1, c.getType());
        ps.setString(2, c.getContextType());
        ps.setLong(3, c.getContextId());

        ps.executeUpdate();
    }


        public List<Conversation> findAll() throws SQLException {
            List<Conversation> list = new ArrayList<>();
            String sql = "SELECT * FROM conversation";

            try (
                    Statement st = DatabaseConnection.getInstance().getConnection().createStatement();
                    ResultSet rs = st.executeQuery(sql)
            ) {
                while (rs.next()) {
                    list.add(new Conversation(
                            rs.getLong("id"),
                            rs.getString("type"),
                            rs.getString("context_type"),
                            rs.getLong("context_id"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
            return list;
        }
}
