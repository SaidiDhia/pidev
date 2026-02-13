package com.example.pi_dev.user.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class UserDatabaseConnection {

    private static UserDatabaseConnection instance;
    private Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/wonderlust_db?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private UserDatabaseConnection() throws SQLException {
        try {
            // Ensure driver is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("MySQL Driver not found", e);
        }
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (InputStream inputStream = getClass().getResourceAsStream("/sql/user_schema.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            String sqlScript = reader.lines().collect(Collectors.joining("\n"));
            String[] statements = sqlScript.split(";");
            
            try (Statement statement = connection.createStatement()) {
                for (String sql : statements) {
                    if (!sql.trim().isEmpty()) {
                        statement.execute(sql.trim());
                    }
                }
                
                // Robustly ensure profile_picture column exists using DatabaseMetaData
                DatabaseMetaData metaData = connection.getMetaData();
                try (ResultSet rs = metaData.getColumns(null, null, "users", "profile_picture")) {
                    if (!rs.next()) {
                        System.out.println("Adding missing 'profile_picture' column to 'users' table...");
                        statement.execute("ALTER TABLE users ADD COLUMN profile_picture VARCHAR(255)");
                    }
                }

                // Ensure activity_logs table has user_email instead of user_id if needed
                try (ResultSet rs = metaData.getColumns(null, null, "activity_logs", "user_email")) {
                    if (!rs.next()) {
                        // Check if it has user_id instead (old schema)
                        try (ResultSet rs2 = metaData.getColumns(null, null, "activity_logs", "user_id")) {
                            if (rs2.next()) {
                                System.out.println("Migrating 'activity_logs' table: user_id -> user_email...");
                                statement.execute("ALTER TABLE activity_logs CHANGE COLUMN user_id user_email VARCHAR(255)");
                            } else {
                                // Table might exist but missing column entirely
                                System.out.println("Adding missing 'user_email' column to 'activity_logs' table...");
                                statement.execute("ALTER TABLE activity_logs ADD COLUMN user_email VARCHAR(255) AFTER log_id");
                            }
                        }
                    }
                }

                // Ensure activity_logs table has created_at column
                 try (ResultSet rs = metaData.getColumns(null, null, "activity_logs", "created_at")) {
                     if (!rs.next()) {
                         System.out.println("Adding missing 'created_at' column to 'activity_logs' table...");
                         statement.execute("ALTER TABLE activity_logs ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                     }
                 }

                 // Ensure tfa_secrets table exists and has correct columns
                 try (ResultSet rs = metaData.getTables(null, null, "tfa_secrets", null)) {
                     if (rs.next()) {
                         try (ResultSet rsCol = metaData.getColumns(null, null, "tfa_secrets", "secret_key")) {
                             if (!rsCol.next()) {
                                 System.out.println("Adding missing 'secret_key' column to 'tfa_secrets' table...");
                                 statement.execute("ALTER TABLE tfa_secrets ADD COLUMN secret_key VARCHAR(255) NOT NULL");
                             }
                         }
                     }
                 }
             }
            System.out.println("Database tables initialized successfully.");
            
        } catch (Exception e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static UserDatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new UserDatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
