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

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Test CRUD Complet ===\n");

        // Initialiser les services
        IpostServices postService = new Posting_Services();
        IcommentaireServices commentaireService = new Commentaire_Services();
        IreactionServices reactionService = new Reaction_Services();

        // ⚠ Replace these with real user IDs that exist in your 'users' table
        int idUser1 = 1;
        int idUser2 = 2;

        // ========== TEST POST ==========
        System.out.println("--- Test Post ---");
        // Constructor: Post(String contenu, String media, String statut, int idUser)
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
        // Constructor: Commentaire(String contenu, int idPost, int idUser)
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
        // Constructor: Commentaire(String contenu, int idPost, int idUser, int idParent)
        Commentaire reply1 = new Commentaire("Merci !", post1.getIdPost(), idUser2, comment1.getIdCommentaire());
        commentaireService.ajouterCommentaire(reply1);

        System.out.println("\nReplies du commentaire " + comment1.getIdCommentaire() + ":");
        for (Commentaire r : commentaireService.getRepliesByCommentaire(comment1.getIdCommentaire())) {
            System.out.println(r);
        }

        // ========== TEST REACTION SUR POST ==========
        System.out.println("\n--- Test Reaction sur Post ---");
        // Constructor: Reaction(String type, int idPost, int idUser)
        // Use different users so there's no unique-constraint conflict
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
        // Constructor: Reaction(String type, Integer idPost, Integer idCommentaire, int idUser)
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
    }
}