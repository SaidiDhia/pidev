package Services;
import Utils.Mydatabase;



import Entites.Personne;
import Iservices.IpersonneServices;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonneService implements IpersonneServices {

    Connection con;

    public PersonneService() {
        con = Mydatabase.getInstance().getConnection();
    }

    @Override
    public void ajouterPersonne(Personne p) {
        String req = "INSERT INTO personne (nom, prenom,age) VALUES (?, ?, ?)";
        Statement ste= null;
        try {
           // ste = con.createStatement();
          //  ste.executeUpdate(req);

            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, p.getNom());
            ps.setString(2, p.getPrenom());
            ps.setInt(3, p.getAge());
            ps.executeUpdate();
            System.out.println("Personne ajoutée avec succès.");




        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void modifierPersonne(Personne p) {
        String req = "UPDATE personne SET nom = ?, prenom = ?, age = ? WHERE id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, p.getNom());
            ps.setString(2, p.getPrenom());
            ps.setInt(3, p.getAge());
            // Assuming Personne class has a method getId() to get the ID
            ps.setInt(4, p.getId());
            ps.executeUpdate();
            System.out.println("Personne modifiée avec succès.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void supprimerPersonne(int id) {
        String req = "DELETE FROM personne WHERE id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Personne supprimée avec succès.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Personne> afficherPersonnes() {
        List<Personne> personnes = new ArrayList<>();
        String req = "SELECT * FROM personne";
        try {
            Statement ste = con.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                Personne p = new Personne(rs.getString("nom"), rs.getString("prenom"), rs.getInt("age"));
                p.setId(rs.getInt("id")); // <-- set the id from DB
                personnes.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return personnes;
    }

}
