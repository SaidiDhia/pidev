package com.example.pi_dev.blog.Utils;

import java.sql.*;

public class BlogDataBase {

    private static BlogDataBase instance;
    private Connection connection;

    private static final String URL      = "jdbc:mysql://localhost:3306/wonderlust_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private BlogDataBase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Connexion etablie avec succes a 'wonderlust_db'");
            ensureDefaultUser();
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL introuvable !");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion a la base de donnees !");
            e.printStackTrace();
        }
    }

    private void ensureDefaultUser() {
        String sql = "INSERT IGNORE INTO users " +
                "(user_id, email, password_hash, full_name, role, is_active, created_at) " +
                "VALUES ('00000000-0000-0000-0000-000000000001', " +
                "'default@blog.com', 'default_hash', 'Utilisateur', 'USER', TRUE, NOW())";
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(sql);
            System.out.println("✅ Default user ensured in DB.");
        } catch (SQLException e) {
            System.err.println("⚠ Could not ensure default user: " + e.getMessage());
        }
    }

    public static BlogDataBase getInstance() {
        if (instance == null) {
            synchronized (BlogDataBase.class) {
                if (instance == null) {
                    instance = new BlogDataBase();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("🔄 Connexion retablie");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur connexion: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Erreur fermeture: " + e.getMessage());
            }
        }
    }

    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}