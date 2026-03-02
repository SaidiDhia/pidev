package com.example.pi_dev.Services.Blog;

import com.example.pi_dev.Entities.Blog.Commentaire;
import com.example.pi_dev.Iservices.Blog.IcommentaireServices;
import com.example.pi_dev.Utils.Blog.BlogDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Commentaire_Services implements IcommentaireServices {

    private Connection connection;

    public Commentaire_Services() {
        connection = BlogDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouterCommentaire(Commentaire commentaire) {
        String requete = "INSERT INTO commentaires (contenu, id_post, id_user, id_parent) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pst = connection.prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, commentaire.getContenu());
            pst.setInt(2, commentaire.getIdPost());
            pst.setInt(3, commentaire.getIdUser());
            if (commentaire.getIdParent() != null) pst.setInt(4, commentaire.getIdParent());
            else pst.setNull(4, Types.INTEGER);
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) commentaire.setIdCommentaire(generatedKeys.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<Commentaire> afficherCommentaires() {
        List<Commentaire> list = new ArrayList<>();
        String requete = "SELECT * FROM commentaires ORDER BY date ASC";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Commentaire getCommentaireById(int idCommentaire) {
        String requete = "SELECT * FROM commentaires WHERE id_commentaire = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, idCommentaire);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Commentaire> getCommentairesByPost(int idPost) {
        List<Commentaire> list = new ArrayList<>();
        // Only top-level comments
        String requete = "SELECT * FROM commentaires WHERE id_post = ? AND id_parent IS NULL ORDER BY date ASC";
        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, idPost);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // NEW — load replies from DB (persistent across sessions)
    public List<Commentaire> getRepliesByCommentaire(int idParent) {
        List<Commentaire> list = new ArrayList<>();
        String requete = "SELECT * FROM commentaires WHERE id_parent = ? ORDER BY date ASC";
        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, idParent);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public void modifierCommentaire(Commentaire commentaire) {
        String requete = "UPDATE commentaires SET contenu = ? WHERE id_commentaire = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setString(1, commentaire.getContenu());
            pst.setInt(2, commentaire.getIdCommentaire());
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void supprimerCommentaire(int idCommentaire) {
        // Delete replies first, then the comment itself
        try {
            PreparedStatement p1 = connection.prepareStatement("DELETE FROM commentaires WHERE id_parent = ?");
            p1.setInt(1, idCommentaire); p1.executeUpdate();
            PreparedStatement p2 = connection.prepareStatement("DELETE FROM commentaires WHERE id_commentaire = ?");
            p2.setInt(1, idCommentaire); p2.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Commentaire mapRow(ResultSet rs) throws SQLException {
        int idUser = 0;
        try { idUser = rs.getInt("id_user"); } catch (Exception ignored) {}
        Integer idParent = null;
        try {
            Object p = rs.getObject("id_parent");
            if (p != null) idParent = rs.getInt("id_parent");
        } catch (Exception ignored) {}
        return new Commentaire(
                rs.getInt("id_commentaire"),
                rs.getString("contenu"),
                rs.getTimestamp("date").toLocalDateTime(),
                rs.getInt("id_post"),
                idUser,
                idParent
        );
    }
}