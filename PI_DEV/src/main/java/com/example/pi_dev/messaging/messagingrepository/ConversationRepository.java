package com.example.pi_dev.messaging.messagingrepository;

import com.example.pi_dev.messaging.messagingdatabase.DatabaseConnection;
import com.example.pi_dev.messaging.messagingmodel.Conversation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ConversationRepository {

    public long create(Conversation c) throws SQLException {
        String sql = "INSERT INTO conversation (type, created_at) VALUES (?, NOW())";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, c.getType());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                rs.close();
                stmt.close();
                return id;
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }


        /*public List<Conversation> findAll() throws SQLException {
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
        } we removed this for now cause it brings up all the conversations*/


        //this one brings only the conversation per users involved
        public List<Conversation> findByUser(String userId) throws SQLException {
            String sql = """
                SELECT c.*
                FROM conversation c
                JOIN conversation_user cu
                    ON c.id = cu.conversation_id
                WHERE cu.user_id = ?
                ORDER BY c.id DESC
            """;

            List<Conversation> list = new ArrayList<>();

            try (PreparedStatement ps =
                         DatabaseConnection.getInstance()
                                 .getConnection()
                                 .prepareStatement(sql)) {

                ps.setString(1, userId);

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Conversation c = new Conversation();
                    c.setId(rs.getLong("id"));
                    c.setType(rs.getString("type"));
                    c.setContextType(rs.getString("context_type"));
                    c.setContextId(rs.getLong("context_id"));
                    list.add(c);
                }
            }

            return list;
        }

        //new method hethia bch nzidha bch najjem nda5el users lel conversation
        public void addUserToConversation(long conversationId, long userId)
                    throws SQLException {

                String sql = """
            INSERT INTO conversation_user (conversation_id, user_id)
            VALUES (?, ?)
        """;

                try (PreparedStatement ps =
                             DatabaseConnection.getInstance()
                                     .getConnection()
                                     .prepareStatement(sql)) {

                    ps.setLong(1, conversationId);
                    ps.setLong(2, userId);
                    ps.executeUpdate();
                }
            }
}
