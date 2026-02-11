package com.example.pi_dev.venue.dao;

import com.example.pi_dev.venue.entities.Booking;
import com.example.pi_dev.user.database.UserDatabaseConnection;
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

    public void create(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (place_id, renter_id, start_date, end_date, total_price, guests_count, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, booking.getPlaceId());
            stmt.setString(2, booking.getRenterId());
            stmt.setDate(3, Date.valueOf(booking.getStartDate()));
            stmt.setDate(4, Date.valueOf(booking.getEndDate()));
            stmt.setDouble(5, booking.getTotalPrice());
            stmt.setInt(6, booking.getGuestsCount());
            stmt.setString(7, booking.getStatus().name());

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                booking.setId(rs.getInt(1));
            }
        }
    }

    public List<Booking> findByRenter(String renterId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE renter_id = ? ORDER BY created_at DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, renterId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(extractBooking(rs));
            }
        }
        return bookings;
    }

    public List<Booking> findByOwner(String ownerId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.* FROM bookings b JOIN places p ON b.place_id = p.id WHERE p.host_id = ? ORDER BY b.created_at DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ownerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(extractBooking(rs));
            }
        }
        return bookings;
    }

    public void updateStatus(int bookingId, Booking.Status status) throws SQLException {
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, bookingId);
            stmt.executeUpdate();
        }
    }

    public void update(Booking booking) throws SQLException {
        String sql = "UPDATE bookings SET start_date = ?, end_date = ?, total_price = ?, guests_count = ?, status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(booking.getStartDate()));
            stmt.setDate(2, Date.valueOf(booking.getEndDate()));
            stmt.setDouble(3, booking.getTotalPrice());
            stmt.setInt(4, booking.getGuestsCount());
            stmt.setString(5, booking.getStatus().name());
            stmt.setInt(6, booking.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int bookingId) throws SQLException {
        String sql = "DELETE FROM bookings WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            stmt.executeUpdate();
        }
    }

    public boolean isAvailable(int placeId, Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE place_id = ? AND status = 'CONFIRMED' AND " +
                     "((start_date <= ? AND end_date >= ?) OR (start_date <= ? AND end_date >= ?) OR (start_date >= ? AND end_date <= ?))";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, placeId);
            stmt.setDate(2, startDate);
            stmt.setDate(3, startDate);
            stmt.setDate(4, endDate);
            stmt.setDate(5, endDate);
            stmt.setDate(6, startDate);
            stmt.setDate(7, endDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return true;
    }

    public List<java.time.LocalDate> getBookedDates(int placeId) throws SQLException {
        List<java.time.LocalDate> dates = new ArrayList<>();
        String sql = "SELECT start_date, end_date FROM bookings WHERE place_id = ? AND status = 'CONFIRMED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, placeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                java.time.LocalDate start = rs.getDate("start_date").toLocalDate();
                java.time.LocalDate end = rs.getDate("end_date").toLocalDate();
                while (!start.isAfter(end)) {
                    dates.add(start);
                    start = start.plusDays(1);
                }
            }
        }
        return dates;
    }

    private Booking extractBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking(
            rs.getInt("id"),
            rs.getInt("place_id"),
            rs.getString("renter_id"),
            rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date").toLocalDate(),
            rs.getDouble("total_price"),
            Booking.Status.valueOf(rs.getString("status"))
        );
        booking.setGuestsCount(rs.getInt("guests_count"));
        return booking;
    }
}
