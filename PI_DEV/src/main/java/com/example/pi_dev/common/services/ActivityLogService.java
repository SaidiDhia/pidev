package com.example.pi_dev.common.services;

import com.example.pi_dev.common.models.ActivityLog;
import com.example.pi_dev.user.database.UserDatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogService {

    public ActivityLogService() {
    }

    private Connection getConnection() throws SQLException {
        return UserDatabaseConnection.getInstance().getConnection();
    }

    public void log(String userEmail, String action, String details) {
        String sql = "INSERT INTO activity_logs (user_email, action, details, created_at) VALUES (?, ?, ?, ?)";
        System.out.println("DEBUG: ActivityLogService.log() attempting - User: " + userEmail + ", Action: " + action);
        
        // Fallback local logging to a file to verify if this method is even called
        try (java.io.FileWriter fw = new java.io.FileWriter("activity_debug.log", true);
             java.io.PrintWriter pw = new java.io.PrintWriter(fw)) {
            pw.println(new java.util.Date() + " - User: " + userEmail + ", Action: " + action + ", Details: " + details);
        } catch (java.io.IOException e) {
            // ignore fallback failure
        }

        try {
            Connection conn = getConnection();
            if (conn == null || conn.isClosed()) {
                System.err.println("ActivityLogService: ERROR - Database connection is null or closed!");
                return;
            }
            // Force auto-commit to be sure
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(true);
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userEmail != null ? userEmail : "System");
                stmt.setString(2, action);
                stmt.setString(3, details != null ? details : "");
                stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                
                int rowsAffected = stmt.executeUpdate();
                System.out.println("ActivityLogService: SUCCESS - Logged action. Rows affected: " + rowsAffected);
            } finally {
                // Restore auto-commit state
                try { conn.setAutoCommit(originalAutoCommit); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            System.err.println("ActivityLogService: FATAL ERROR logging action '" + action + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<ActivityLog> findAll() {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs ORDER BY log_id DESC";
        System.out.println("ActivityLogService: Fetching all logs on thread " + Thread.currentThread().getName());
        try {
            Connection conn = getConnection();
            System.out.println("ActivityLogService: Using connection to " + conn.getMetaData().getURL());
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime timestamp = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();
                    
                    logs.add(new ActivityLog(
                        rs.getInt("log_id"),
                        rs.getString("user_email"),
                        rs.getString("action"),
                        rs.getString("details"),
                        timestamp
                    ));
                }
                System.out.println("ActivityLogService: SUCCESS - Loaded " + logs.size() + " activity logs from database.");
            }
        } catch (SQLException e) {
            System.err.println("ActivityLogService: Error fetching activity logs: " + e.getMessage());
            e.printStackTrace();
        }
        return logs;
    }
}
