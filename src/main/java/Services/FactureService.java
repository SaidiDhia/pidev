package Services;

import Entites.Facture;
import Entites.FactureProduct;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FactureService {

    private Connection con;

    public FactureService() {
        con = Mydatabase.getInstance().getConnection();
    }

    // ─── Add facture ──────────────────────────────────────────────────────────

    public int addFacture(Facture f) {
        String sql = "INSERT INTO facture (user_id, date_facture, total_price, payment_method, delivery_status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, f.getUserId());
            ps.setTimestamp(2, new java.sql.Timestamp(f.getDate().getTime()));
            ps.setFloat(3, f.getTotal());
            ps.setString(4, f.getPaymentMethod());
            ps.setString(5, f.getDeliveryStatus());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ─── Get factures by user ─────────────────────────────────────────────────

    public List<Facture> getFacturesByUser(int userId) {
        List<Facture> list = new ArrayList<>();
        String sql = "SELECT * FROM facture WHERE user_id = ? ORDER BY date_facture DESC";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapFacture(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── Get ALL factures for products owned by a seller ─────────────────────
    // Used in the Orders Management page (seller sees orders for their products)

    public List<Facture> getFacturesByProductOwner(int ownerUserId) {
        List<Facture> list = new ArrayList<>();
        String sql = "SELECT DISTINCT f.* FROM facture f " +
                     "JOIN facture_product fp ON f.id_facture = fp.facture_id " +
                     "JOIN products p ON fp.product_id = p.id " +
                     "WHERE p.userId = ? " +
                     "ORDER BY f.date_facture DESC";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, ownerUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapFacture(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── Get facture by ID ────────────────────────────────────────────────────

    public Facture getFactureById(int factureId) {
        String sql = "SELECT * FROM facture WHERE id_facture = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, factureId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapFacture(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─── Update delivery status ───────────────────────────────────────────────

    public boolean updateDeliveryStatus(int factureId, String status) {
        String sql = "UPDATE facture SET delivery_status = ? WHERE id_facture = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, factureId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Confirm cash delivery → deduct reserved stock ────────────────────────

    public boolean confirmCashDelivery(int factureId) {
        try {
            con.setAutoCommit(false);

            // Move reserved → actually deduct stock
            List<FactureProduct> products = getFactureProducts(factureId);
            for (FactureProduct fp : products) {
                String sql = "UPDATE products SET reserved_quantity = reserved_quantity - ? WHERE id = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, fp.getQuantity());
                ps.setInt(2, fp.getProductId());
                ps.executeUpdate();
            }

            // Update delivery status
            updateDeliveryStatus(factureId, "confirmed");

            con.commit();
            con.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try { con.rollback(); con.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }

    // ─── Cancel order → restore stock ────────────────────────────────────────

    public boolean cancelOrder(int factureId) {
        try {
            con.setAutoCommit(false);

            Facture f = getFactureById(factureId);
            List<FactureProduct> products = getFactureProducts(factureId);

            for (FactureProduct fp : products) {
                if ("cash".equals(f.getPaymentMethod())) {
                    // Cash: restore reserved stock → real quantity
                    String sql = "UPDATE products SET quantity = quantity + ?, reserved_quantity = reserved_quantity - ? WHERE id = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, fp.getQuantity());
                    ps.setInt(2, fp.getQuantity());
                    ps.setInt(3, fp.getProductId());
                    ps.executeUpdate();
                } else {
                    // Online: restore actual quantity (stock was already deducted)
                    String sql = "UPDATE products SET quantity = quantity + ? WHERE id = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, fp.getQuantity());
                    ps.setInt(2, fp.getProductId());
                    ps.executeUpdate();
                }
            }

            updateDeliveryStatus(factureId, "cancelled");
            con.commit();
            con.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try { con.rollback(); con.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }

    // ─── Get facture products ─────────────────────────────────────────────────

    public List<FactureProduct> getFactureProducts(int factureId) {
        List<FactureProduct> list = new ArrayList<>();
        String sql = "SELECT * FROM facture_product WHERE facture_id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, factureId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FactureProduct fp = new FactureProduct();
                fp.setFactureId(rs.getInt("facture_id"));
                fp.setProductId(rs.getInt("product_id"));
                fp.setProductTitle(rs.getString("product_title"));
                fp.setQuantity(rs.getInt("quantity"));
                fp.setPrice(rs.getFloat("price"));
                fp.setProductImage(rs.getString("product_image"));
                list.add(fp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Facture mapFacture(ResultSet rs) throws SQLException {
        Facture f = new Facture();
        f.setId(rs.getInt("id_facture"));
        f.setUserId(rs.getInt("user_id"));
        f.setDate(rs.getTimestamp("date_facture"));
        f.setTotal(rs.getFloat("total_price"));
        f.setPaymentMethod(rs.getString("payment_method"));
        f.setDeliveryStatus(rs.getString("delivery_status"));
        return f;
    }
}
