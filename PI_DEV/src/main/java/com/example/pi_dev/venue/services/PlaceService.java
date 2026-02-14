package com.example.pi_dev.venue.services;

import com.example.pi_dev.venue.entities.Place;
import com.example.pi_dev.venue.entities.Amenity;
import com.example.pi_dev.user.database.UserDatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaceService {
    private Connection conn;

    public PlaceService() {
        try {
            this.conn = UserDatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    public List<String> getImages(int placeId) throws SQLException {
        List<String> images = new ArrayList<>();
        String sql = "SELECT image_url FROM place_images WHERE place_id = ? ORDER BY sort_order";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, placeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    images.add(rs.getString("image_url"));
                }
            }
        }
        return images;
    }

    public void clearImages(int placeId) throws SQLException {
        String sql = "DELETE FROM place_images WHERE place_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, placeId);
            stmt.executeUpdate();
        }
    }

    public void create(Place place) throws SQLException {
        String sql = "INSERT INTO places (host_id, title, description, price_per_day, capacity, max_guests, address, city, latitude, longitude, category, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, place.getHostId());
            stmt.setString(2, place.getTitle());
            stmt.setString(3, place.getDescription());
            stmt.setDouble(4, place.getPricePerDay());
            stmt.setInt(5, place.getCapacity());
            stmt.setInt(6, place.getMaxGuests());
            stmt.setString(7, place.getAddress());
            stmt.setString(8, place.getCity());
            stmt.setDouble(9, place.getLatitude());
            stmt.setDouble(10, place.getLongitude());
            stmt.setString(11, place.getCategory());
            stmt.setString(12, place.getStatus().name());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                place.setId(rs.getInt(1));
            }
            
            // If place has an image URL, save it
            if (place.getImageUrl() != null && !place.getImageUrl().isEmpty()) {
                saveImage(place.getId(), place.getImageUrl());
            }

            // Save amenities
            if (place.getAmenities() != null && !place.getAmenities().isEmpty()) {
                saveAmenities(place.getId(), place.getAmenities());
            }
        }
    }

    public void saveAmenities(int placeId, List<Amenity> amenities) throws SQLException {
        String sql = "INSERT INTO place_amenities (place_id, amenity_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Amenity amenity : amenities) {
                stmt.setInt(1, placeId);
                stmt.setInt(2, amenity.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void saveImage(int placeId, String imageUrl) throws SQLException {
        String sql = "INSERT INTO place_images (place_id, image_url, sort_order) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, placeId);
            stmt.setString(2, imageUrl);
            stmt.setInt(3, 0); // Default sort order
            stmt.executeUpdate();
        }
    }

    public List<Place> findAllApproved() throws SQLException {
        List<Place> places = new ArrayList<>();
        String sql = "SELECT * FROM places WHERE status = 'APPROVED'";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                places.add(extractPlace(rs));
            }
        }
        return places;
    }

    public List<Place> findPending() throws SQLException {
        List<Place> places = new ArrayList<>();
        String sql = "SELECT * FROM places WHERE status = 'PENDING'";

        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                places.add(extractPlace(rs));
            }
        }
        return places;
    }

    public void updateStatus(int placeId, Place.Status status) throws SQLException {
        String sql = "UPDATE places SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, placeId);
            stmt.executeUpdate();
        }
    }

    public void update(Place place) throws SQLException {
        String sql = "UPDATE places SET title = ?, description = ?, price_per_day = ?, " +
                "capacity = ?, max_guests = ?, address = ?, city = ?, category = ?, latitude = ?, longitude = ? " +
                "WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, place.getTitle());
            stmt.setString(2, place.getDescription());
            stmt.setDouble(3, place.getPricePerDay());
            stmt.setInt(4, place.getCapacity());
            stmt.setInt(5, place.getMaxGuests());
            stmt.setString(6, place.getAddress());
            stmt.setString(7, place.getCity());
            stmt.setString(8, place.getCategory());
            stmt.setDouble(9, place.getLatitude());
            stmt.setDouble(10, place.getLongitude());
            stmt.setInt(11, place.getId());
            stmt.executeUpdate();

            // If place has an image URL, update it (or add if it doesn't exist)
            if (place.getImageUrl() != null && !place.getImageUrl().isEmpty()) {
                // Delete old images first for simplicity (or we could manage them better)
                String deleteSql = "DELETE FROM place_images WHERE place_id = ?";
                try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                    delStmt.setInt(1, place.getId());
                    delStmt.executeUpdate();
                }
                saveImage(place.getId(), place.getImageUrl());
            }

            // Update amenities
            String deleteAmenitiesSql = "DELETE FROM place_amenities WHERE place_id = ?";
            try (PreparedStatement delStmt = conn.prepareStatement(deleteAmenitiesSql)) {
                delStmt.setInt(1, place.getId());
                delStmt.executeUpdate();
            }
            if (place.getAmenities() != null && !place.getAmenities().isEmpty()) {
                saveAmenities(place.getId(), place.getAmenities());
            }
        }
    }

    public List<Place> findByHost(long hostId) throws SQLException {
        List<Place> places = new ArrayList<>();
        String sql = "SELECT * FROM places WHERE host_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, hostId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                places.add(extractPlace(rs));
            }
        }
        return places;
    }

    public void delete(int placeId) throws SQLException {
        // First delete images
        clearImages(placeId);
        // Then delete place
        String sql = "DELETE FROM places WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, placeId);
            stmt.executeUpdate();
        }
    }

    public List<Place> findAll() throws SQLException {
        List<Place> places = new ArrayList<>();
        String sql = "SELECT * FROM places";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                places.add(extractPlace(rs));
            }
        }
        return places;
    }

    public Place findById(int id) throws SQLException {
        String sql = "SELECT * FROM places WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractPlace(rs);
            }
        }
        return null;
    }

    public void addImage(int placeId, String imageUrl, int sortOrder) throws SQLException {
        String sql = "INSERT INTO place_images (place_id, image_url, sort_order) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, placeId);
            stmt.setString(2, imageUrl);
            stmt.setInt(3, sortOrder);
            stmt.executeUpdate();
        }
    }

    private Place extractPlace(ResultSet rs) throws SQLException {
        Place place = new Place(
                rs.getInt("id"),
                rs.getLong("host_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getDouble("price_per_day"),
                rs.getInt("capacity"),
                rs.getString("address"),
                rs.getString("city"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getString("category"),
                Place.Status.valueOf(rs.getString("status")));
        place.setMaxGuests(rs.getInt("max_guests"));

        // Fetch first image
        String imgSql = "SELECT image_url FROM place_images WHERE place_id = ? ORDER BY sort_order LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(imgSql)) {
            stmt.setInt(1, place.getId());
            ResultSet imgRs = stmt.executeQuery();
            if (imgRs.next()) {
                place.setImageUrl(imgRs.getString("image_url"));
            }
        }

        // Fetch amenities
        String amenitySql = "SELECT a.* FROM amenities a " +
                "JOIN place_amenities pa ON a.id = pa.amenity_id " +
                "WHERE pa.place_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(amenitySql)) {
            stmt.setInt(1, place.getId());
            ResultSet amRs = stmt.executeQuery();
            while (amRs.next()) {
                place.getAmenities().add(new Amenity(
                        amRs.getInt("id"),
                        amRs.getString("name"),
                        amRs.getString("icon_class")));
            }
        }

        return place;
    }
}
