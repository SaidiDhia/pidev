package Services;

import Entities.Reservation;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {

    private Connection cnx;

    public ReservationService() {
        cnx = Mydatabase.getInstance().getConnextion();
    }

    //  CREATE
    public void ajouter(Reservation r) throws SQLException {

        String sql = "INSERT INTO reservations (id_event, nom_complet, email, telephone, nombre_personnes) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setInt(1, r.getIdEvent());
        ps.setString(2, r.getNomComplet());
        ps.setString(3, r.getEmail());
        ps.setString(4, r.getTelephone());
        ps.setInt(5, r.getNombrePersonnes());

        ps.executeUpdate();

        // diminuer les places automatiquement
        EventService es = new EventService();
        es.diminuerPlaces(r.getIdEvent(), r.getNombrePersonnes());
    }

    //  READ
    public List<Reservation> afficher() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Reservation r = new Reservation();
            r.setId(rs.getInt("id"));
            r.setIdEvent(rs.getInt("id_event"));
            r.setNomComplet(rs.getString("nom_complet"));
            r.setEmail(rs.getString("email"));
            r.setTelephone(rs.getString("telephone"));
            r.setNombrePersonnes(rs.getInt("nombre_personnes"));
            list.add(r);
        }
        return list;
    }

    //  UPDATE
    public void modifier(Reservation r) throws SQLException {

        // 1️ récupérer ancienne réservation
        String selectSql = "SELECT nombre_personnes, id_event FROM reservations WHERE id = ?";
        PreparedStatement selectPs = cnx.prepareStatement(selectSql);
        selectPs.setInt(1, r.getId());
        ResultSet rs = selectPs.executeQuery();

        int ancienNombre = 0;
        int ancienEvent = 0;

        if (rs.next()) {
            ancienNombre = rs.getInt("nombre_personnes");
            ancienEvent = rs.getInt("id_event");
        }

        //  update réservation
        String updateSql = "UPDATE reservations SET id_event=?, nom_complet=?, email=?, telephone=?, nombre_personnes=? WHERE id=?";

        PreparedStatement ps = cnx.prepareStatement(updateSql);

        ps.setInt(1, r.getIdEvent());
        ps.setString(2, r.getNomComplet());
        ps.setString(3, r.getEmail());
        ps.setString(4, r.getTelephone());
        ps.setInt(5, r.getNombrePersonnes());
        ps.setInt(6, r.getId());

        ps.executeUpdate();

        //  ajuster les places

        EventService es = new EventService();

        // restituer anciennes places
        es.diminuerPlaces(ancienEvent, -ancienNombre);

        // diminuer nouvelles places
        es.diminuerPlaces(r.getIdEvent(), r.getNombrePersonnes());
    }

    //  DELETE
    public void supprimer(int idReservation) throws SQLException {

        //  récupérer infos réservation
        String selectSql = "SELECT id_event, nombre_personnes FROM reservations WHERE id=?";
        PreparedStatement selectPs = cnx.prepareStatement(selectSql);
        selectPs.setInt(1, idReservation);
        ResultSet rs = selectPs.executeQuery();

        int idEvent = 0;
        int nombre = 0;

        if (rs.next()) {
            idEvent = rs.getInt("id_event");
            nombre = rs.getInt("nombre_personnes");
        }

        // 2 supprimer réservation
        String deleteSql = "DELETE FROM reservations WHERE id=?";
        PreparedStatement deletePs = cnx.prepareStatement(deleteSql);
        deletePs.setInt(1, idReservation);
        deletePs.executeUpdate();

        // 3️⃣ restituer places
        EventService es = new EventService();
        es.diminuerPlaces(idEvent, -nombre);
    }
}
