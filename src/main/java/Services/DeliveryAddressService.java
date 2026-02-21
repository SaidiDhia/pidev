package Services;

import Entites.DeliveryAddress;
import Utils.Mydatabase;

import java.sql.*;

public class DeliveryAddressService {

    private Connection con;

    public DeliveryAddressService() {
        con = Mydatabase.getInstance().getConnection();
    }

    public boolean save(DeliveryAddress da) {
        String sql = "INSERT INTO delivery_address (facture_id, full_name, phone, address, city, postal_code, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, da.getFactureId());
            ps.setString(2, da.getFullName());
            ps.setString(3, da.getPhone());
            ps.setString(4, da.getAddress());
            ps.setString(5, da.getCity());
            ps.setString(6, da.getPostalCode());
            ps.setString(7, da.getNotes());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public DeliveryAddress getByFactureId(int factureId) {
        String sql = "SELECT * FROM delivery_address WHERE facture_id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, factureId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                DeliveryAddress da = new DeliveryAddress();
                da.setId(rs.getInt("id"));
                da.setFactureId(rs.getInt("facture_id"));
                da.setFullName(rs.getString("full_name"));
                da.setPhone(rs.getString("phone"));
                da.setAddress(rs.getString("address"));
                da.setCity(rs.getString("city"));
                da.setPostalCode(rs.getString("postal_code"));
                da.setNotes(rs.getString("notes"));
                return da;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
