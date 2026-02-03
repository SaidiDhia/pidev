package com.example.pi_dev.messaging.messagingdatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance; // SINGLE INSTANCE
    private Connection connection;

    private static final String URL =
            "jdbc:mysql://localhost:3306/wonderlust_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "saididhia";

    // private constructor
    private DatabaseConnection() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // access point (Singleton) hethy zedha baad me mr 9al lezm nimplementiwha 7attitha kima houa bedhabt
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
