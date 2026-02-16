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
import java.sql.Timestamp;
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
        System.out.println("UserDatabaseConnection: Successfully connected to " + URL);
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (InputStream inputStream = getClass().getResourceAsStream("/sql/user_schema.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Ignore comments and empty lines
                if (line.trim().startsWith("--") || line.trim().isEmpty()) continue;
                sb.append(line);
                if (line.contains(";")) {
                    String sql = sb.toString().trim();
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(sql);
                    } catch (SQLException e) {
                        System.err.println("Error executing SQL: " + sql + " - " + e.getMessage());
                    }
                    sb.setLength(0);
                }
            }
            
            try (Statement statement = connection.createStatement()) {
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
                                System.out.println("Migrating 'activity_logs' table: adding user_email and dropping user_id...");
                                try {
                                    // Try renaming first with INPLACE if supported
                                    statement.execute("ALTER TABLE activity_logs CHANGE COLUMN user_id user_email VARCHAR(255), ALGORITHM=INPLACE, LOCK=NONE");
                                } catch (SQLException e1) {
                                    System.out.println("Standard rename failed, trying add/drop approach...");
                                    try {
                                        statement.execute("ALTER TABLE activity_logs ADD COLUMN user_email VARCHAR(255) AFTER log_id");
                                        statement.execute("UPDATE activity_logs SET user_email = 'Unknown' WHERE user_email IS NULL");
                                        statement.execute("ALTER TABLE activity_logs DROP COLUMN user_id");
                                    } catch (SQLException e2) {
                                        System.err.println("Could not migrate activity_logs: " + e2.getMessage());
                                    }
                                }
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

                // Ensure activity_logs table has action and details columns
                try (ResultSet rs = metaData.getColumns(null, null, "activity_logs", "action")) {
                    if (!rs.next()) {
                        System.out.println("Adding missing 'action' column to 'activity_logs' table...");
                        statement.execute("ALTER TABLE activity_logs ADD COLUMN action VARCHAR(255) AFTER user_email");
                    }
                }
                try (ResultSet rs = metaData.getColumns(null, null, "activity_logs", "details")) {
                    if (!rs.next()) {
                        System.out.println("Adding missing 'details' column to 'activity_logs' table...");
                        statement.execute("ALTER TABLE activity_logs ADD COLUMN details TEXT AFTER action");
                    }
                }

                // Verify activity_logs table has log_id as AUTO_INCREMENT
                try (ResultSet rs = metaData.getColumns(null, null, "activity_logs", "log_id")) {
                    if (rs.next()) {
                        String isAutoInc = rs.getString("IS_AUTOINCREMENT");
                        if (!"YES".equalsIgnoreCase(isAutoInc)) {
                            System.out.println("Fixing activity_logs table: Setting log_id to AUTO_INCREMENT...");
                            try {
                                // First try just setting AUTO_INCREMENT
                                statement.execute("ALTER TABLE activity_logs MODIFY COLUMN log_id INT AUTO_INCREMENT");
                            } catch (SQLException e) {
                                // If that fails (e.g. not a key yet), try setting as PRIMARY KEY too
                                if (e.getMessage().contains("Multiple primary key")) {
                                    // It's already a primary key, just not auto-inc? This shouldn't happen with the first try but let's be safe
                                    statement.execute("ALTER TABLE activity_logs MODIFY COLUMN log_id INT AUTO_INCREMENT");
                                } else {
                                    try {
                                        statement.execute("ALTER TABLE activity_logs MODIFY COLUMN log_id INT AUTO_INCREMENT PRIMARY KEY");
                                    } catch (SQLException e2) {
                                        System.err.println("Failed to fix log_id: " + e2.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }

                // Emergency fix for 'user_id' column preventing inserts (handling foreign key)
                try (ResultSet rs = metaData.getColumns(null, null, "activity_logs", "user_id")) {
                    if (rs.next()) {
                        System.out.println("Emergency fix: 'user_id' column found in 'activity_logs'. Attempting to remove constraints and drop it...");
                        
                        // First, try to drop the foreign key constraint
                        try {
                            statement.execute("ALTER TABLE activity_logs DROP FOREIGN KEY activity_logs_ibfk_1");
                            System.out.println("Successfully dropped foreign key 'activity_logs_ibfk_1'.");
                        } catch (SQLException e) {
                            System.out.println("Could not drop FK 'activity_logs_ibfk_1' (it might have a different name): " + e.getMessage());
                        }

                        // Also try to drop the index if it exists separately
                        try {
                            statement.execute("ALTER TABLE activity_logs DROP INDEX user_id");
                            System.out.println("Successfully dropped index 'user_id'.");
                        } catch (SQLException e) {
                            // Ignore if index doesn't exist or was dropped with FK
                        }

                        // Now try to drop the column
                        try {
                            statement.execute("ALTER TABLE activity_logs DROP COLUMN user_id");
                            System.out.println("Successfully dropped 'user_id' from 'activity_logs'.");
                        } catch (SQLException e) {
                            System.err.println("Failed to drop 'user_id': " + e.getMessage());
                            System.out.println("Attempting to make 'user_id' nullable as last resort...");
                            try {
                                statement.execute("ALTER TABLE activity_logs MODIFY COLUMN user_id VARCHAR(255) NULL");
                                System.out.println("Successfully made 'user_id' nullable.");
                            } catch (SQLException e2) {
                                System.err.println("Failed to make 'user_id' nullable: " + e2.getMessage());
                            }
                        }
                    }
                }

                // Verify activity_logs table has records
                try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM activity_logs")) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Database Check: Current log count in DB is " + count);
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
                    } else {
                        System.out.println("Creating missing 'tfa_secrets' table...");
                        statement.execute("CREATE TABLE tfa_secrets (user_id VARCHAR(36) PRIMARY KEY, secret_key VARCHAR(255) NOT NULL, qr_code TEXT, FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE)");
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
        if (instance == null || instance.connection == null || instance.connection.isClosed()) {
            instance = new UserDatabaseConnection();
        }
        if (instance.connection == null) {
            throw new SQLException("Failed to establish database connection.");
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
