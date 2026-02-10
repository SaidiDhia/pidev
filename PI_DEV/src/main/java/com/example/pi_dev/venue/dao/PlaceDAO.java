package com.example.pi_dev.venue.dao;

import com.example.pi_dev.venue.entities.Place;
import com.example.pi_dev.user.database.UserDatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaceDAO {
    private Connection conn;

    public PlaceDAO() {
        try {
            this.conn = UserDatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    public void create(Place place) throws SQLException {
        String sql = "INSERT INTO places (host_id, title, description, price_per_day, capacity, address, city, latitude, longitude, category, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, place.getHostId());
            stmt.setString(2, place.getTitle());
            stmt.setString(3, place.getDescription());
            stmt.setDouble(4, place.getPricePerDay());
            stmt.setInt(5, place.getCapacity());
            stmt.setString(6, place.getAddress());
            stmt.setString(7, place.getCity());
            stmt.setDouble(8, place.getLatitude());
            stmt.setDouble(9, place.getLongitude());
            stmt.setString(10, place.getCategory());
            stmt.setString(11, place.getStatus().name());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                place.setId(rs.getInt(1));
            }
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
                "capacity = ?, address = ?, city = ?, category = ?, latitude = ?, longitude = ? " +
                "WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, place.getTitle());
            stmt.setString(2, place.getDescription());
            stmt.setDouble(3, place.getPricePerDay());
            stmt.setInt(4, place.getCapacity());
            stmt.setString(5, place.getAddress());
            stmt.setString(6, place.getCity());
            stmt.setString(7, place.getCategory());
            stmt.setDouble(8, place.getLatitude());
            stmt.setDouble(9, place.getLongitude());
            stmt.setInt(10, place.getId());
            stmt.executeUpdate();
        }
    }

    public List<Place> findByHost(String hostId) throws SQLException {
        List<Place> places = new ArrayList<>();
        String sql = "SELECT * FROM places WHERE host_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hostId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                places.add(extractPlace(rs));
            }
        }
        return places;
    }

    private Place extractPlace(ResultSet rs) throws SQLException {
        Place place = new Place(
                rs.getInt("id"),
                rs.getString("host_id"),
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

        // Fetch first image
        String imgSql = "SELECT image_url FROM place_images WHERE place_id = ? ORDER BY sort_order LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(imgSql)) {
            stmt.setInt(1, place.getId());
            ResultSet imgRs = stmt.executeQuery();
            if (imgRs.next()) {
                place.setImageUrl(imgRs.getString("image_url"));
            }
        }

        return place;
    }
}
