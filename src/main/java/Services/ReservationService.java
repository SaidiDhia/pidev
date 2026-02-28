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

        String sql = "INSERT INTO reservations (id_event, nom_complet, email, telephone, nombre_personnes, demandes_speciales, statut) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setInt(1, r.getIdEvent());
        ps.setString(2, r.getNomComplet());
        ps.setString(3, r.getEmail());
        ps.setString(4, r.getTelephone());
        ps.setInt(5, r.getNombrePersonnes());
        ps.setString(6, r.getDemandesSpeciales());
        ps.setString(7, r.getStatut().toString());

        ps.executeUpdate();

        // Récupérer l'ID généré
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            r.setId(generatedKeys.getInt(1));
        }

        // diminuer les places automatiquement
        EventService es = new EventService();
        es.diminuerPlaces(r.getIdEvent(), r.getNombrePersonnes());
    }

    //  READ
    public List<Reservation> afficher() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY id DESC";

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
            // r.setPrixTotal(rs.getDouble("prix_total")); // Supprimé car la colonne n'existe pas
            
            // Conversion de la date - supprimé car la colonne n'existe pas
            // java.sql.Timestamp ts = rs.getTimestamp("date_reservation");
            // if (ts != null) {
            //     r.setDateReservation(ts.toLocalDateTime());
            // }
            
            r.setDemandesSpeciales(rs.getString("demandes_speciales"));
            
            // Conversion du statut
            String statutStr = rs.getString("statut");
            if (statutStr != null) {
                try {
                    r.setStatut(Reservation.StatutReservation.valueOf(statutStr));
                } catch (IllegalArgumentException e) {
                    r.setStatut(Reservation.StatutReservation.EN_ATTENTE);
                }
            }
            
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

        // update réservation
        String updateSql = "UPDATE reservations SET id_event=?, nom_complet=?, email=?, telephone=?, nombre_personnes=?, demandes_speciales=?, statut=? WHERE id=?";

        PreparedStatement ps = cnx.prepareStatement(updateSql);

        ps.setInt(1, r.getIdEvent());
        ps.setString(2, r.getNomComplet());
        ps.setString(3, r.getEmail());
        ps.setString(4, r.getTelephone());
        ps.setInt(5, r.getNombrePersonnes());
        ps.setString(6, r.getDemandesSpeciales());
        ps.setString(7, r.getStatut().toString());
        ps.setInt(8, r.getId());

        ps.executeUpdate();

        // ajuster les places

        EventService es = new EventService();

        // restituer anciennes places
        es.diminuerPlaces(ancienEvent, -ancienNombre);

        // diminuer nouvelles places
        es.diminuerPlaces(r.getIdEvent(), r.getNombrePersonnes());
    }

    // Méthode add pour compatibilité avec le controller
    public void add(Reservation r) throws SQLException {
        ajouter(r);
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
