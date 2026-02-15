package com.example.pi_dev.messaging.messagingrepository;

import com.example.pi_dev.messaging.messagingdatabase.DatabaseConnection;
import com.example.pi_dev.messaging.messagingmodel.Conversation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ConversationRepository {

    public long create(Conversation c) throws SQLException {
        // Using text block for better readability (Java 13+)
        String sql = """
        INSERT INTO conversation (name, type, created_at)
        VALUES (?, ?, NOW())
    """;

        Connection conn = DatabaseConnection.getInstance().getConnection();

        // Using try-with-resources to automatically close PreparedStatement
        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            // Set both parameters: name and type
            stmt.setString(1, c.getName());    // First ? = name
            stmt.setString(2, c.getType());    // Second ? = type

            stmt.executeUpdate();

            // Get the generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw to let caller handle it
        }

        return -1; // Return -1 if no ID was generated
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



    public void updateName(long id, String newName) throws SQLException {

        String sql = "UPDATE conversation SET name = ? WHERE id = ?";

        try (PreparedStatement ps =
                     DatabaseConnection.getInstance()
                             .getConnection()
                             .prepareStatement(sql)) {

            ps.setString(1, newName);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }
    public void delete(long id) throws SQLException {

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try {
            conn.setAutoCommit(false);

            // 1️⃣ delete messages
            try (PreparedStatement ps =
                         conn.prepareStatement("DELETE FROM message WHERE conversation_id = ?")) {

                ps.setLong(1, id);
                ps.executeUpdate();
            }

            // 2️⃣ delete participants
            try (PreparedStatement ps =
                         conn.prepareStatement("DELETE FROM conversation_user WHERE conversation_id = ?")) {

                ps.setLong(1, id);
                ps.executeUpdate();
            }

            // 3️⃣ delete conversation
            try (PreparedStatement ps =
                         conn.prepareStatement("DELETE FROM conversation WHERE id = ?")) {

                ps.setLong(1, id);
                ps.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
