package com.example.pi_dev.Services.Booking;

import com.example.pi_dev.Entities.Booking.Booking;
import com.example.pi_dev.Iservices.Booking.IBookingService;
import com.example.pi_dev.Utils.Booking.Mydatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingService implements IBookingService {

    private final Connection con;

    public BookingService() {
        con = Mydatabase.getInstance().getConnextion();
    }

    // DTO for booking lists that need place title
    public static class BookingView {
        public final Booking booking;
        public final String placeTitle;

        public BookingView(Booking booking, String placeTitle) {
            this.booking = booking;
            this.placeTitle = placeTitle;
        }
    }

    @Override
    public boolean isAvailable(int placeId, LocalDate start, LocalDate end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("Dates invalides: start doit être avant end");
        }

        String sql = "SELECT COUNT(*) " +
                "FROM booking " +
                "WHERE place_id = ? " +
                "  AND status IN ('PENDING','CONFIRMED') " +
                "  AND NOT (end_date <= ? OR start_date >= ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, placeId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur check disponibilité", e);
        }
    }

    @Override
    public void ajouterBooking(Booking b) {
        boolean ok = isAvailable(b.getPlaceId(), b.getStartDate(), b.getEndDate());
        if (!ok) {
            throw new RuntimeException("Place déjà réservée pour ces dates (chevauchement).");
        }

        String sql = "INSERT INTO booking (place_id, user_id, start_date, end_date, total_price, guests_count, status) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, b.getPlaceId());
            ps.setString(2, b.getUserId());
            ps.setDate(3, java.sql.Date.valueOf(b.getStartDate()));
            ps.setDate(4, java.sql.Date.valueOf(b.getEndDate()));
            ps.setDouble(5, b.getTotalPrice());
            ps.setInt(6, b.getGuestsCount());
            ps.setString(7, Booking.Status.PENDING.name());

            ps.executeUpdate();
            System.out.println("Booking ajouté");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout booking", e);
        }
    }

    @Override
    public void modifierBooking(Booking b) {
        String checkSql = "SELECT COUNT(*) FROM booking " +
                "WHERE place_id=? AND id != ? " +
                "  AND status IN ('PENDING','CONFIRMED') " +
                "  AND NOT (end_date <= ? OR start_date >= ?)";

        try (PreparedStatement check = con.prepareStatement(checkSql)) {
            check.setInt(1, b.getPlaceId());
            check.setInt(2, b.getId());
            check.setDate(3, Date.valueOf(b.getStartDate()));
            check.setDate(4, Date.valueOf(b.getEndDate()));
            try (ResultSet rs = check.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    throw new RuntimeException("Ces dates ne sont pas disponibles.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur vérification disponibilité", e);
        }

        String sql = "UPDATE booking SET place_id=?, user_id=?, start_date=?, end_date=?, total_price=?, guests_count=?, status=? "
                +
                "WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, b.getPlaceId());
            ps.setString(2, b.getUserId());
            ps.setDate(3, Date.valueOf(b.getStartDate()));
            ps.setDate(4, Date.valueOf(b.getEndDate()));
            ps.setDouble(5, b.getTotalPrice());
            ps.setInt(6, b.getGuestsCount());
            ps.setString(7, b.getStatus().name());
            ps.setInt(8, b.getId());
            ps.executeUpdate();
            System.out.println("Booking modifié id=" + b.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Erreur modification booking", e);
        }
    }

    @Override
    public void supprimerBooking(int id) {
        String sql = "DELETE FROM booking WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Booking supprimé id=" + id);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression booking", e);
        }
    }

    @Override
    public List<Booking> afficherBookings() {
        String sql = "SELECT * FROM booking ORDER BY id DESC";
        List<Booking> list = new ArrayList<>();

        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur affichage bookings", e);
        }
        return list;
    }

    @Override
    public Booking getBookingById(int id) {
        String sql = "SELECT * FROM booking WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapBooking(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur getBookingById", e);
        }
    }

    @Override
    public List<Booking> afficherBookingsParPlace(int placeId) {
        String sql = "SELECT * FROM booking WHERE place_id=? ORDER BY id DESC";
        List<Booking> list = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, placeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur afficherBookingsParPlace", e);
        }
        return list;
    }

    @Override
    public List<Booking> findByUser(String userId) {
        String sql = "SELECT * FROM booking WHERE user_id=? ORDER BY id DESC";
        List<Booking> list = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByUser", e);
        }
        return list;
    }

    @Override
    public List<Booking> findPending() {
        String sql = "SELECT * FROM booking WHERE status='PENDING' ORDER BY id DESC";
        List<Booking> list = new ArrayList<>();

        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findPending bookings", e);
        }
        return list;
    }

    @Override
    public void updateStatus(int bookingId, Booking.Status status) {
        String sql = "UPDATE booking SET status=? WHERE id=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, bookingId);
            ps.executeUpdate();
            System.out.println("Booking status updated: id=" + bookingId + " -> " + status);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur updateStatus booking", e);
        }
    }

    /**
     * Updates the PDF file path for a specific booking.
     */
    public void updatePdfPath(int bookingId, String pdfPath) {
        String sql = "UPDATE booking SET pdf_path=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pdfPath);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
            System.out.println("PDF path updated for bookingId=" + bookingId);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur updatePdfPath", e);
        }
    }

    /**
     * Checks if a user is eligible to review a place.
     * Returns true if there exists a booking for this user+place where:
     * - status = COMPLETED
     * - OR status = CONFIRMED AND end_date < CURDATE()
     */
    public boolean canReview(String userId, int placeId) {
        String sql = "SELECT COUNT(*) FROM booking " +
                "WHERE user_id=? AND place_id=? " +
                "AND (status='COMPLETED' OR (status='CONFIRMED' AND end_date < CURDATE()))";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, placeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur canReview", e);
        }
    }

    // Fetch bookings with place title (JOIN) for a specific user
    public List<BookingView> findByUserWithTitle(String userId) {
        String sql = "SELECT b.*, p.title AS place_title " +
                "FROM booking b " +
                "JOIN place p ON b.place_id = p.id " +
                "WHERE b.user_id=? ORDER BY b.id DESC";
        List<BookingView> list = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BookingView(mapBooking(rs), rs.getString("place_title")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByUserWithTitle", e);
        }
        return list;
    }

    // Fetch pending bookings with place title (JOIN) for admin
    public List<BookingView> findPendingWithTitle() {
        String sql = "SELECT b.*, p.title AS place_title " +
                "FROM booking b " +
                "JOIN place p ON b.place_id = p.id " +
                "WHERE b.status='PENDING' ORDER BY b.id DESC";
        List<BookingView> list = new ArrayList<>();

        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new BookingView(mapBooking(rs), rs.getString("place_title")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findPendingWithTitle", e);
        }
        return list;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setPlaceId(rs.getInt("place_id"));
        b.setUserId(rs.getString("user_id"));
        b.setStartDate(rs.getDate("start_date").toLocalDate());
        b.setEndDate(rs.getDate("end_date").toLocalDate());
        b.setTotalPrice(rs.getDouble("total_price"));
        b.setGuestsCount(rs.getInt("guests_count"));
        b.setStatus(Booking.Status.valueOf(rs.getString("status").toUpperCase()));

        // pdf_path column (may be null or not yet added in DB)
        try {
            b.setPdfPath(rs.getString("pdf_path"));
        } catch (SQLException ignored) {
            /* column not yet in DB */ }

        return b;
    }
}
