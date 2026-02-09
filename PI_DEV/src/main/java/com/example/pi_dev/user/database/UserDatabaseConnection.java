package com.example.pi_dev.user.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
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
