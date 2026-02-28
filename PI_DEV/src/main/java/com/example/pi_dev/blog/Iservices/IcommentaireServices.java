package com.example.pi_dev.blog.Iservices;

import com.example.pi_dev.blog.Entities.Commentaire;
import java.util.List;

public interface IcommentaireServices {
    // Create
    void ajouterCommentaire(Commentaire commentaire);

    // Read
    List<Commentaire> afficherCommentaires();
    Commentaire getCommentaireById(int idCommentaire);
    List<Commentaire> getCommentairesByPost(int idPost);

    // Replies (sub-comments)
    List<Commentaire> getRepliesByCommentaire(int idParent);

    // Update
    void modifierCommentaire(Commentaire commentaire);

    // Delete
    void supprimerCommentaire(int idCommentaire);
}