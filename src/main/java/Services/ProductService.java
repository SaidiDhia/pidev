package Services;

import Entites.Product;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private Connection con;
    public static int CURRENT_USER_ID = 2; // temporary


    public ProductService() {
        con = Mydatabase.getInstance().getConnection();
    }

    // 🔹 Get product by ID (VERY IMPORTANT)


    // Add a new product
    public void addProduct(Product p) {

        String req = """
        INSERT INTO products 
        (title, description, type, price, quantity, category, image, created_date, userId) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try {
            PreparedStatement ps = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, p.getTitle());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getType());
            ps.setFloat(4, p.getPrice());
            ps.setInt(5, p.getQuantity());
            ps.setString(6, p.getCategory());
            ps.setString(7, p.getImage());
            ps.setTimestamp(8, new Timestamp(p.getCreatedDate().getTime()));
            ps.setInt(9, CURRENT_USER_ID); // 🔥 important

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                p.setId(rs.getInt(1));
            }

            System.out.println("Product added for user ID=" + CURRENT_USER_ID);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 🔹 Update product (THE GOOD WAY)
    public void updateProduct(Product p) {
        String req = """
    UPDATE products 
    SET title=?, description=?, type=?, price=?, quantity=?, category=?, image=?, created_date=?
    WHERE id=? AND userId=?
""";


        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, p.getTitle());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getType());
            ps.setFloat(4, p.getPrice());
            ps.setInt(5, p.getQuantity());
            ps.setString(6, p.getCategory());
            ps.setString(7, p.getImage());
            ps.setTimestamp(8, new Timestamp(p.getCreatedDate().getTime()));
            ps.setInt(9, p.getId());
            ps.setInt(10, CURRENT_USER_ID);


            ps.executeUpdate();
            System.out.println("Product updated successfully: ID=" + p.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 🔹 Delete
    public void deleteProduct(int id) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM products WHERE id=? AND userId=?"
            );

            ps.setInt(1, id);
            ps.setInt(2, CURRENT_USER_ID);

            ps.executeUpdate();
            System.out.println("Product deleted: ID=" + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Optional: delete all products (for testing)
    public void deleteAllProducts() {
        String req = "DELETE FROM products";
        try {
            Statement st = con.createStatement();
            st.executeUpdate(req);
            System.out.println("All products deleted.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




    /**
     * Get product by ID
     */
    public Product getProductById(int id) {
        try {
            String sql = "SELECT * FROM products WHERE id = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Product product = new Product(
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getFloat("price"),
                        rs.getInt("quantity"),
                        rs.getString("category"),
                        rs.getString("image"),
                        rs.getTimestamp("created_date"),
                        rs.getInt("userId")
                );
                product.setId(rs.getInt("id"));
                try { product.setReservedQuantity(rs.getInt("reserved_quantity")); } catch(Exception ex) {}
                return product;
            }

        } catch (SQLException e) {
            System.err.println("Error getting product: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all available products (NOT from current user)
     * Only products with quantity > 0
     */
    public List<Product> getAllAvailableProducts(int currentUserId) {
        List<Product> products = new ArrayList<>();

        try {
            String sql = "SELECT * FROM products WHERE userId != ? AND quantity > 0 ORDER BY created_date DESC";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = new Product(
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getFloat("price"),
                        rs.getInt("quantity"),
                        rs.getString("category"),
                        rs.getString("image"),
                        rs.getTimestamp("created_date"),
                        rs.getInt("userId")
                );
                product.setId(rs.getInt("id"));
                try { product.setReservedQuantity(rs.getInt("reserved_quantity")); } catch(Exception ex) {}
                products.add(product);
            }

        } catch (SQLException e) {
            System.err.println("Error getting available products: " + e.getMessage());
            e.printStackTrace();
        }

        return products;
    }

    /**
     * Get all products (for management page)
     */
    public List<Product> getAllProducts(int currentUserId) {
        List<Product> products = new ArrayList<>();

        try {
            String sql = "SELECT * FROM products WHERE userId = ? ORDER BY created_date DESC";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = new Product(
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getFloat("price"),
                        rs.getInt("quantity"),
                        rs.getString("category"),
                        rs.getString("image"),
                        rs.getTimestamp("created_date"),
                        rs.getInt("userId")
                );
                product.setId(rs.getInt("id"));
                try { product.setReservedQuantity(rs.getInt("reserved_quantity")); } catch(Exception ex) {}
                products.add(product);
            }

        } catch (SQLException e) {
            System.err.println("Error getting all products: " + e.getMessage());
            e.printStackTrace();
        }

        return products;
    }
}
