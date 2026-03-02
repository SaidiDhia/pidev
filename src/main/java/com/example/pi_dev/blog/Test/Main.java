package com.example.pi_dev.blog.Test;

import com.example.pi_dev.blog.Entities.Post;
import com.example.pi_dev.blog.Entities.Commentaire;
import com.example.pi_dev.blog.Entities.Reaction;
import com.example.pi_dev.blog.Services.Posting_Services;
import com.example.pi_dev.blog.Services.Commentaire_Services;
import com.example.pi_dev.blog.Services.Reaction_Services;
import com.example.pi_dev.blog.Iservices.IpostServices;
import com.example.pi_dev.blog.Iservices.IcommentaireServices;
import com.example.pi_dev.blog.Iservices.IreactionServices;
import com.example.pi_dev.blog.Utils.BlogDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Main {

    private static String createTestUser(Connection conn, String email, String fullName) throws SQLException {
        PreparedStatement check = conn.prepareStatement("SELECT user_id FROM users WHERE email = ?");
        check.setString(1, email);
        ResultSet rs = check.executeQuery();
        if (rs.next()) {
            return rs.getString("user_id");
        }
        String uuid = UUID.randomUUID().toString();
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (user_id, email, password_hash, full_name, role, is_active, created_at) VALUES (?, ?, 'test_hash', ?, 'USER', TRUE, NOW())");
        ps.setString(1, uuid);
        ps.setString(2, email);
        ps.setString(3, fullName);
        ps.executeUpdate();
        return uuid;
    }

    private static void deleteTestUser(Connection conn, String uuid) {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE user_id = ?");
            ps.setString(1, uuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Could not delete test user: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Test CRUD Complet ===\n");

        Connection conn = BlogDataBase.getInstance().getConnection();
        if (conn == null) {
            System.err.println("FATAL: Could not connect to database.");
            return;
        }

        IpostServices postService = new Posting_Services();
        IcommentaireServices commentaireService = new Commentaire_Services();
        IreactionServices reactionService = new Reaction_Services();

        String idUser1 = null;
        String idUser2 = null;

        try {
            // Create real users in DB so foreign key constraints are satisfied
            idUser1 = createTestUser(conn, "testuser1_blog@test.com", "Test User One");
            idUser2 = createTestUser(conn, "testuser2_blog@test.com", "Test User Two");
            System.out.println("Users ready: " + idUser1 + " / " + idUser2);

            // ========== TEST POST ==========
            System.out.println("\n--- Test Post ---");
            Post post1 = new Post("Mon premier post!", "photo.jpg", "publie", idUser1);
            postService.ajouterPost(post1);

            Post post2 = new Post("Deuxieme post de test", "video.mp4", "brouillon", idUser1);
            postService.ajouterPost(post2);

            System.out.println("\nListe des posts:");
            for (Post p : postService.afficherPosts()) {
                System.out.println(p);
            }

            // ========== TEST COMMENTAIRE ==========
            System.out.println("\n--- Test Commentaire ---");
            Commentaire comment1 = new Commentaire("Super post!", post1.getIdPost(), idUser1);
            commentaireService.ajouterCommentaire(comment1);

            Commentaire comment2 = new Commentaire("Merci pour le partage!", post1.getIdPost(), idUser2);
            commentaireService.ajouterCommentaire(comment2);

            System.out.println("\nCommentaires du post " + post1.getIdPost() + ":");
            for (Commentaire c : commentaireService.getCommentairesByPost(post1.getIdPost())) {
                System.out.println(c);
            }

            // ========== TEST REPLY ==========
            System.out.println("\n--- Test Reply sur Commentaire ---");
            Commentaire reply1 = new Commentaire("Merci !", post1.getIdPost(), idUser2, comment1.getIdCommentaire());
            commentaireService.ajouterCommentaire(reply1);

            System.out.println("\nReplies du commentaire " + comment1.getIdCommentaire() + ":");
            for (Commentaire r : commentaireService.getRepliesByCommentaire(comment1.getIdCommentaire())) {
                System.out.println(r);
            }

            // ========== TEST REACTION SUR POST ==========
            System.out.println("\n--- Test Reaction sur Post ---");
            Reaction reactionPost1 = new Reaction("LIKE", post1.getIdPost(), idUser1);
            reactionService.ajouterReaction(reactionPost1);

            Reaction reactionPost2 = new Reaction("LOVE", post1.getIdPost(), idUser2);
            reactionService.ajouterReaction(reactionPost2);

            System.out.println("\nReactions du post " + post1.getIdPost() + ":");
            for (Reaction r : reactionService.getReactionsByPost(post1.getIdPost())) {
                System.out.println(r);
            }

            // ========== TEST REACTION SUR COMMENTAIRE ==========
            System.out.println("\n--- Test Reaction sur Commentaire ---");
            Reaction reactionComment1 = new Reaction("LIKE", null, comment1.getIdCommentaire(), idUser1);
            reactionService.ajouterReaction(reactionComment1);

            Reaction reactionComment2 = new Reaction("HAHA", null, comment1.getIdCommentaire(), idUser2);
            reactionService.ajouterReaction(reactionComment2);

            System.out.println("\nReactions du commentaire " + comment1.getIdCommentaire() + ":");
            for (Reaction r : reactionService.getReactionsByCommentaire(comment1.getIdCommentaire())) {
                System.out.println(r);
            }

            // ========== TEST TOGGLE REACTION ==========
            System.out.println("\n--- Test Toggle Reaction ---");
            Reaction toggleTest = new Reaction("LIKE", post1.getIdPost(), idUser1);
            boolean added = reactionService.toggleReaction(toggleTest);
            System.out.println("Toggle (should unlike, returns false): " + added);
            added = reactionService.toggleReaction(toggleTest);
            System.out.println("Toggle again (should like, returns true): " + added);

            // ========== TEST COMPTAGE REACTIONS ==========
            System.out.println("\n--- Test Comptage Reactions ---");
            int nbLikes = reactionService.compterReactionsByType("LIKE", post1.getIdPost(), null);
            System.out.println("Nombre de LIKE sur le post " + post1.getIdPost() + ": " + nbLikes);

            // ========== TEST MODIFICATION ==========
            System.out.println("\n--- Test Modification ---");
            post1.setContenu("Contenu modifie!");
            postService.modifierPost(post1);
            System.out.println("Post modifie: " + postService.getPostById(post1.getIdPost()));

            // ========== TEST SUPPRESSION ==========
            System.out.println("\n--- Test Suppression ---");
            postService.supprimerPost(post2.getIdPost());

            System.out.println("\nListe finale des posts:");
            for (Post p : postService.afficherPosts()) {
                System.out.println(p);
            }

            System.out.println("\n=== Fin des tests ===");

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup: delete test users, cascade removes all their data too
            if (idUser1 != null) deleteTestUser(conn, idUser1);
            if (idUser2 != null) deleteTestUser(conn, idUser2);
        }
    }
}