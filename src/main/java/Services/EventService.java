package Services;

import Entities.Event;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService {

    private Connection cnx;

    public EventService() {
        cnx = Mydatabase.getInstance().getConnextion();
    }

    // CREATE
    public void ajouter(Event e) throws SQLException {
        String sql = "INSERT INTO events (id_activite, lieu, date_debut, date_fin, prix, capacite_max, places_disponibles) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setInt(1, e.getIdActivite());
        ps.setString(2, e.getLieu());
        ps.setTimestamp(3, Timestamp.valueOf(e.getDateDebut()));
        ps.setTimestamp(4, Timestamp.valueOf(e.getDateFin()));
        ps.setBigDecimal(5, e.getPrix());
        ps.setInt(6, e.getCapaciteMax());
        ps.setInt(7, e.getPlacesDisponibles());

        ps.executeUpdate();
    }

    // READ
    public List<Event> afficher() throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Event e = new Event();
            e.setId(rs.getInt("id"));
            e.setIdActivite(rs.getInt("id_activite"));
            e.setLieu(rs.getString("lieu"));
            e.setPrix(rs.getBigDecimal("prix"));
            e.setCapaciteMax(rs.getInt("capacite_max"));
            e.setPlacesDisponibles(rs.getInt("places_disponibles"));
            list.add(e);
        }
        return list;
    }

    // UPDATE places
    public void diminuerPlaces(int idEvent, int nombre) throws SQLException {
        String sql = "UPDATE events SET places_disponibles = places_disponibles - ? WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, nombre);
        ps.setInt(2, idEvent);

        ps.executeUpdate();
    }
    // ================= DELETE PAR ID =================
    public void supprimer(int idEvent) throws SQLException {
        String sql = "DELETE FROM events WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idEvent);

        ps.executeUpdate();
    }

    // ================= DELETE PAR ACTIVITE =================
    public void supprimerParActivite(int idActivite) throws SQLException {
        String sql = "DELETE FROM events WHERE id_activite = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idActivite);

        ps.executeUpdate();
    }
    public Event findById(int id) throws SQLException {

        String sql = "SELECT * FROM events WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Event e = new Event();
            e.setId(rs.getInt("id"));
            e.setLieu(rs.getString("lieu"));
            e.setPrix(rs.getBigDecimal("prix"));
            e.setPlacesDisponibles(rs.getInt("places_disponibles"));
            e.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime());
            e.setImage(rs.getString("image"));
            return e;
        }
        return null;
    }

}
