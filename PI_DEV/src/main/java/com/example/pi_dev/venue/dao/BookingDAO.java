package com.example.pi_dev.venue.dao;

import com.example.pi_dev.venue.entities.Booking;
import com.example.pi_dev.user.database.UserDatabaseConnection; // Updated import
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    private Connection conn;

    public BookingDAO() {
        try {
            this.conn = UserDatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    public boolean isAvailable(int placeId, Date start, Date end) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE place_id = ? AND status != 'CANCELLED' AND ((start_date BETWEEN ? AND ?) OR (end_date BETWEEN ? AND ?))";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, placeId);
            stmt.setDate(2, start);
            stmt.setDate(3, end);
            stmt.setDate(4, start);
            stmt.setDate(5, end);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return false;
    }

    public void create(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (place_id, renter_id, start_date, end_date, total_price, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, booking.getPlaceId());
            stmt.setString(2, booking.getRenterId());
            stmt.setDate(3, java.sql.Date.valueOf(booking.getStartDate()));
            stmt.setDate(4, java.sql.Date.valueOf(booking.getEndDate()));
            stmt.setDouble(5, booking.getTotalPrice());
            stmt.setString(6, booking.getStatus().name());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                booking.setId(rs.getInt(1));
            }
        }
    }

    public List<Booking> findByRenter(String renterId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE renter_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, renterId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(extractBooking(rs));
            }
        }
        return bookings;
    }

    public List<java.time.LocalDate> getBookedDates(int placeId) throws SQLException {
        List<java.time.LocalDate> bookedDates = new ArrayList<>();
        String sql = "SELECT start_date, end_date FROM bookings WHERE place_id = ? AND status != 'CANCELLED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, placeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                java.time.LocalDate start = rs.getDate("start_date").toLocalDate();
                java.time.LocalDate end = rs.getDate("end_date").toLocalDate();

                // Add all dates in range
                java.time.LocalDate current = start;
                while (!current.isAfter(end)) {
                    bookedDates.add(current);
                    current = current.plusDays(1);
                }
            }
        }
        return bookedDates;
    }

    private Booking extractBooking(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getInt("id"),
                rs.getInt("place_id"),
                rs.getString("renter_id"),
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date").toLocalDate(),
                rs.getDouble("total_price"),
                Booking.Status.valueOf(rs.getString("status")));
    }
}
