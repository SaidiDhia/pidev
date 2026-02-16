package com.example.pi_dev.venue.services;

import com.example.pi_dev.venue.entities.Amenity;
import com.example.pi_dev.user.database.UserDatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AmenityService {
    private Connection conn;

    public AmenityService() {
        try {
            this.conn = UserDatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    public List<Amenity> findAll() throws SQLException {
        List<Amenity> amenities = new ArrayList<>();
        String sql = "SELECT * FROM amenities";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                amenities.add(new Amenity(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("icon_class")
                ));
            }
        }
        return amenities;
    }

    public List<Amenity> findByPlaceId(int placeId) throws SQLException {
        List<Amenity> amenities = new ArrayList<>();
        String sql = "SELECT a.* FROM amenities a " +
                     "JOIN place_amenities pa ON a.id = pa.amenity_id " +
                     "WHERE pa.place_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, placeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    amenities.add(new Amenity(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("icon_class")
                    ));
                }
            }
        }
        return amenities;
    }

    public void addAmenityToPlace(int placeId, int amenityId) throws SQLException {
        String sql = "INSERT INTO place_amenities (place_id, amenity_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, placeId);
            stmt.setInt(2, amenityId);
            stmt.executeUpdate();
        }
    }

    public void removeAllAmenitiesFromPlace(int placeId) throws SQLException {
        String sql = "DELETE FROM place_amenities WHERE place_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, placeId);
            stmt.executeUpdate();
        }
    }
}
