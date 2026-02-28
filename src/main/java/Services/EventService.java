package Services;

import Entities.CategorieActivite;
import Entities.Event;
import Entities.EventPhoto;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService {

    private Connection cnx;
    private EventPhotoService photoService;

    public EventService() {
        cnx = Mydatabase.getInstance().getConnextion();
        photoService = new EventPhotoService();
    }

    // CREATE
    public void ajouter(Event e) throws SQLException {
        String sql = "INSERT INTO events (id_activite, adresse_complete, date_debut, date_fin, prix, capacite_max, places_disponibles, organisateur, materiels_necessaires, image, statut, date_creation, date_modification, video_youtube) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setInt(1, e.getIdActivite());
        ps.setTimestamp(3, e.getDateDebut() != null ? Timestamp.valueOf(e.getDateDebut()) : null);
        ps.setTimestamp(4, e.getDateFin() != null ? Timestamp.valueOf(e.getDateFin()) : null);
        ps.setBigDecimal(5, e.getPrix());
        ps.setInt(6, e.getCapaciteMax());
        ps.setInt(7, e.getPlacesDisponibles());
        ps.setString(8, e.getOrganisateur());
        ps.setString(9, e.getMaterielsNecessaires());
        ps.setString(10, e.getImage());
        ps.setString(11, e.getStatut() != null ? e.getStatut().name() : "A_VENIR");
        ps.setTimestamp(12, e.getDateCreation() != null ? e.getDateCreation() : new Timestamp(System.currentTimeMillis()));
        ps.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
        ps.setString(14, e.getVideoYoutube());

        ps.executeUpdate();

        // Récupérer l'ID généré
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            e.setId(generatedKeys.getInt(1));
            
            // Ajouter les photos supplémentaires
            if (e.getPhotos() != null && !e.getPhotos().isEmpty()) {
                photoService.ajouterPhotos(e.getId(), e.getPhotos());
            }
        }
    }

    // READ
    public List<Event> afficher() throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY date_creation DESC";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Event e = new Event();
            e.setId(rs.getInt("id"));
            e.setIdActivite(rs.getInt("id_activite"));
            e.setPrix(rs.getBigDecimal("prix"));
            e.setCapaciteMax(rs.getInt("capacite_max"));
            e.setPlacesDisponibles(rs.getInt("places_disponibles"));
            e.setOrganisateur(rs.getString("organisateur"));
            e.setMaterielsNecessaires(rs.getString("materiels_necessaires"));
            e.setImage(rs.getString("image"));
            e.setVideoYoutube(rs.getString("video_youtube"));
            
            // Charger les photos supplémentaires
            List<EventPhoto> photos = photoService.getPhotosByEvent(e.getId());
            List<String> cheminsPhotos = new ArrayList<>();
            for (EventPhoto photo : photos) {
                cheminsPhotos.add(photo.getCheminPhoto());
            }
            e.setPhotos(cheminsPhotos);
            
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
    //  DELETE PAR ID
    public void supprimer(int idEvent) throws SQLException {
        // Supprimer d'abord les photos associées
        photoService.supprimerPhotosEvent(idEvent);
        
        String sql = "DELETE FROM events WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idEvent);

        ps.executeUpdate();
    }

    //  DELETE PAR ACTIVITE
    public void supprimerParActivite(int idActivite) throws SQLException {
        // D'abord récupérer tous les événements de cette activité
        List<Event> events = getEventsByActivite(idActivite);
        
        // Supprimer les photos de chaque événement
        for (Event event : events) {
            photoService.supprimerPhotosEvent(event.getId());
        }
        
        String sql = "DELETE FROM events WHERE id_activite = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idActivite);

        ps.executeUpdate();
    }
    
    // Méthode utilitaire pour récupérer les événements par activité
    public List<Event> getEventsByActivite(int idActivite) throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE id_activite = ?";
        
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idActivite);
        
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            Event e = new Event();
            e.setId(rs.getInt("id"));
            e.setIdActivite(rs.getInt("id_activite"));
            e.setPrix(rs.getBigDecimal("prix"));
            e.setCapaciteMax(rs.getInt("capacite_max"));
            e.setPlacesDisponibles(rs.getInt("places_disponibles"));
            e.setOrganisateur(rs.getString("organisateur"));
            e.setMaterielsNecessaires(rs.getString("materiels_necessaires"));
            e.setImage(rs.getString("image"));
            e.setVideoYoutube(rs.getString("video_youtube"));
            
            events.add(e);
        }
        
        return events;
    }
    
    // Méthode pour obtenir un événement par ID avec ses photos
    public Event getEventById(int idEvent) throws SQLException {
        String sql = "SELECT * FROM events WHERE id = ?";
        
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idEvent);
        
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            Event e = new Event();
            e.setId(rs.getInt("id"));
            e.setIdActivite(rs.getInt("id_activite"));
            e.setPrix(rs.getBigDecimal("prix"));
            e.setCapaciteMax(rs.getInt("capacite_max"));
            e.setPlacesDisponibles(rs.getInt("places_disponibles"));
            e.setOrganisateur(rs.getString("organisateur"));
            e.setMaterielsNecessaires(rs.getString("materiels_necessaires"));
            e.setImage(rs.getString("image"));
            e.setVideoYoutube(rs.getString("video_youtube"));
            
            // Charger les photos supplémentaires
            List<EventPhoto> photos = photoService.getPhotosByEvent(e.getId());
            List<String> cheminsPhotos = new ArrayList<>();
            for (EventPhoto photo : photos) {
                cheminsPhotos.add(photo.getCheminPhoto());
            }
            e.setPhotos(cheminsPhotos);
            
            return e;
        }
        
        return null;
    }
    public Event findById(int id) throws SQLException {

        String sql = "SELECT e.*, a.titre, a.description as activite_description, a.type_activite, a.categorie, a.date_creation as activite_date_creation " +
                   "FROM events e " +
                   "LEFT JOIN activites a ON e.id_activite = a.id " +
                   "WHERE e.id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Event e = new Event();
            e.setId(rs.getInt("id"));
            e.setIdActivite(rs.getInt("id_activite"));
            e.setPrix(rs.getBigDecimal("prix"));
            e.setPlacesDisponibles(rs.getInt("places_disponibles"));
            e.setDateDebut(rs.getTimestamp("date_debut") != null ? rs.getTimestamp("date_debut").toLocalDateTime() : null);
            e.setDateFin(rs.getTimestamp("date_fin") != null ? rs.getTimestamp("date_fin").toLocalDateTime() : null);
            e.setCapaciteMax(rs.getInt("capacite_max"));
            e.setOrganisateur(rs.getString("organisateur"));
            e.setLieu(rs.getString("lieu"));
            e.setDescription(rs.getString("description"));
            e.setEmail(rs.getString("email"));
            e.setTelephone(rs.getInt("telephone"));
            e.setMaterielsNecessaires(rs.getString("materiels_necessaires"));
            e.setImage(rs.getString("image"));
            e.setVideoYoutube(rs.getString("video_youtube"));
            e.setDateCreation(rs.getTimestamp("date_creation"));
            e.setDateModification(rs.getTimestamp("date_modification"));
            
            // Charger le statut
            String statutStr = rs.getString("statut");
            if (statutStr != null) {
                try {
                    e.setStatut(Event.StatutEvent.valueOf(statutStr));
                } catch (IllegalArgumentException ex) {
                    e.setStatut(Event.StatutEvent.A_VENIR); // Valeur par défaut
                }
            }
            
            // Charger l'activité associée avec sa description
            if (rs.getString("titre") != null) {
                Entities.Activite activite = new Entities.Activite();
                activite.setId(rs.getInt("id_activite"));
                activite.setTitre(rs.getString("titre"));
                activite.setDescription(rs.getString("activite_description"));
                activite.setTypeActivite(rs.getString("type_activite"));
                
                // Gérer la catégorie avec valeur par défaut si null
                String categorieStr = rs.getString("categorie");
                if (categorieStr != null && !categorieStr.trim().isEmpty()) {
                    try {
                        activite.setCategorie(CategorieActivite.valueOf(categorieStr));
                    } catch (IllegalArgumentException ex) {
                        System.err.println("Catégorie invalide: " + categorieStr + ", utilisation de la valeur par défaut");
                        activite.setCategorie(CategorieActivite.NATURE); // Valeur par défaut
                    }
                } else {
                    activite.setCategorie(CategorieActivite.NATURE); // Valeur par défaut si null
                }
                activite.setDateCreation(rs.getTimestamp("activite_date_creation") != null ? Timestamp.valueOf(rs.getTimestamp("activite_date_creation").toLocalDateTime()) : null);
                e.setActivite(activite);
            }
            
            return e;
        }
        return null;
    }

}
