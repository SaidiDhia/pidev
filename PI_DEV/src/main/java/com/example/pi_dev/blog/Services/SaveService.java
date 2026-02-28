package com.example.pi_dev.blog.Services;

import com.example.pi_dev.blog.Utils.BlogDataBase;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Persists "saved posts" per user in the posts_sauvegardes table.
 * Table is created automatically on first use.
 */
public class SaveService {

    private final Connection connection;
    private final int idUser;

    public SaveService(int idUser) {
        this.connection = BlogDataBase.getInstance().getConnection();
        this.idUser = idUser;
        ensureTableExists();
    }

    // ── Auto-create table if it doesn't exist ────────────────────────────────
    private void ensureTableExists() {
        String sql =
                "CREATE TABLE IF NOT EXISTS posts_sauvegardes (" +
                        "  id_user INT NOT NULL," +
                        "  id_post INT NOT NULL," +
                        "  PRIMARY KEY (id_user, id_post)," +
                        "  FOREIGN KEY (id_post) REFERENCES posts(id_post) ON DELETE CASCADE" +
                        ")";
        try {
            connection.createStatement().executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Load all saved post IDs for current user ──────────────────────────────
    public Set<Integer> loadSavedPostIds() {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT id_post FROM posts_sauvegardes WHERE id_user = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, idUser);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) ids.add(rs.getInt("id_post"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    // ── Save a post ───────────────────────────────────────────────────────────
    public void savePost(int idPost) {
        String sql = "INSERT IGNORE INTO posts_sauvegardes (id_user, id_post) VALUES (?, ?)";
        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, idUser);
            pst.setInt(2, idPost);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Unsave a post ─────────────────────────────────────────────────────────
    public void unsavePost(int idPost) {
        String sql = "DELETE FROM posts_sauvegardes WHERE id_user = ? AND id_post = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, idUser);
            pst.setInt(2, idPost);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Toggle — returns true if now saved, false if now unsaved ─────────────
    public boolean toggle(int idPost) {
        if (loadSavedPostIds().contains(idPost)) {
            unsavePost(idPost);
            return false;
        } else {
            savePost(idPost);
            return true;
        }
    }
}