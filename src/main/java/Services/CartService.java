package Services;

import Entites.DeliveryAddress;
import Entites.Facture;
import Entites.FactureProduct;
import Entites.Product;
import Utils.Mydatabase;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class CartService {

    private Connection     con;
    private ProductService productService;

    public CartService() {
        con            = Mydatabase.getInstance().getConnection();
        productService = new ProductService();
    }

    // ─── Get or create cart ───────────────────────────────────────────────────

    public int getOrCreateCart(int userId) {
        try {
            String checkSql = "SELECT id FROM cart WHERE user_id = ?";
            PreparedStatement checkStmt = con.prepareStatement(checkSql);
            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) return rs.getInt("id");

            String insertSql = "INSERT INTO cart (user_id, total_price) VALUES (?, 0.00)";
            PreparedStatement insertStmt = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setInt(1, userId);
            insertStmt.executeUpdate();
            ResultSet keys = insertStmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ─── Get cart items ───────────────────────────────────────────────────────

    public Map<Product, Integer> getCartItems(int userId) {
        Map<Product, Integer> cartItems = new LinkedHashMap<>();
        try {
            int cartId = getOrCreateCart(userId);
            if (cartId == -1) return cartItems;

            String sql = "SELECT ci.quantity, p.* FROM cart_item ci " +
                         "JOIN products p ON ci.product_id = p.id WHERE ci.cart_id = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, cartId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product product = new Product(
                    rs.getString("title"), rs.getString("description"),
                    rs.getString("type"), rs.getFloat("price"),
                    rs.getInt("quantity"), rs.getString("category"),
                    rs.getString("image"), rs.getTimestamp("created_date"),
                    rs.getInt("userId")
                );
                product.setId(rs.getInt("id"));
                cartItems.put(product, rs.getInt("quantity"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartItems;
    }

    // ─── Add to cart ──────────────────────────────────────────────────────────

    public boolean addToCart(int userId, int productId, int quantity) {
        try {
            Product product = productService.getProductById(productId);
            if (product == null || quantity < 1) return false;

            // Available = quantity - reserved
            int available = product.getQuantity() - product.getReservedQuantity();
            if (available < quantity) {
                System.out.println("Not enough stock. Available: " + available);
                return false;
            }

            int cartId = getOrCreateCart(userId);
            if (cartId == -1) return false;

            String checkSql = "SELECT quantity FROM cart_item WHERE cart_id = ? AND product_id = ?";
            PreparedStatement checkStmt = con.prepareStatement(checkSql);
            checkStmt.setInt(1, cartId);
            checkStmt.setInt(2, productId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int newQty = rs.getInt("quantity") + quantity;
                if (newQty > available) return false;
                PreparedStatement upd = con.prepareStatement(
                    "UPDATE cart_item SET quantity = ? WHERE cart_id = ? AND product_id = ?");
                upd.setInt(1, newQty); upd.setInt(2, cartId); upd.setInt(3, productId);
                upd.executeUpdate();
            } else {
                PreparedStatement ins = con.prepareStatement(
                    "INSERT INTO cart_item (cart_id, product_id, quantity) VALUES (?, ?, ?)");
                ins.setInt(1, cartId); ins.setInt(2, productId); ins.setInt(3, quantity);
                ins.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Update quantity ──────────────────────────────────────────────────────

    public boolean updateCartItemQuantity(int userId, int productId, int newQuantity) {
        try {
            if (newQuantity < 1) return removeFromCart(userId, productId);
            Product product = productService.getProductById(productId);
            int available = product.getQuantity() - product.getReservedQuantity();
            if (product == null || newQuantity > available) return false;
            int cartId = getOrCreateCart(userId);
            PreparedStatement stmt = con.prepareStatement(
                "UPDATE cart_item SET quantity = ? WHERE cart_id = ? AND product_id = ?");
            stmt.setInt(1, newQuantity); stmt.setInt(2, cartId); stmt.setInt(3, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Remove from cart ─────────────────────────────────────────────────────

    public boolean removeFromCart(int userId, int productId) {
        try {
            int cartId = getOrCreateCart(userId);
            PreparedStatement stmt = con.prepareStatement(
                "DELETE FROM cart_item WHERE cart_id = ? AND product_id = ?");
            stmt.setInt(1, cartId); stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Calculate total ──────────────────────────────────────────────────────

    public double calculateTotal(int userId) {
        double total = 0.0;
        for (Map.Entry<Product, Integer> e : getCartItems(userId).entrySet())
            total += e.getKey().getPrice() * e.getValue();
        return total;
    }

    // ─── BUY ONLINE (Stripe confirmed) → deduct stock immediately ────────────

    public boolean buyCartOnline(int userId, DeliveryAddress deliveryAddress) {
        return processPurchase(userId, "online", "confirmed", deliveryAddress);
    }

    // ─── BUY CASH → reserve stock, wait for delivery confirmation ────────────

    public boolean buyCartCash(int userId, DeliveryAddress deliveryAddress) {
        return processPurchase(userId, "cash", "pending", deliveryAddress);
    }

    // ─── Core purchase logic ──────────────────────────────────────────────────

    private boolean processPurchase(int userId, String paymentMethod,
                                    String deliveryStatus, DeliveryAddress deliveryAddress) {
        FactureService          factureService  = new FactureService();
        FactureProductService   fpService       = new FactureProductService();
        DeliveryAddressService  daService       = new DeliveryAddressService();

        try {
            con.setAutoCommit(false);

            Map<Product, Integer> items = getCartItems(userId);
            if (items.isEmpty()) { con.rollback(); return false; }

            // Re-check available stock
            for (Map.Entry<Product, Integer> entry : items.entrySet()) {
                Product product  = productService.getProductById(entry.getKey().getId());
                int     reqQty   = entry.getValue();
                int     available = product.getQuantity() - product.getReservedQuantity();
                if (product == null || available < reqQty) {
                    System.out.println("Stock issue: " + entry.getKey().getTitle());
                    con.rollback();
                    return false;
                }
            }

            // Calculate total
            double total = 0.0;
            for (Map.Entry<Product, Integer> e : items.entrySet())
                total += e.getKey().getPrice() * e.getValue();

            // Create facture
            Facture facture = new Facture();
            facture.setUserId(userId);
            facture.setDate(new java.util.Date());
            facture.setTotal((float) total);
            facture.setPaymentMethod(paymentMethod);
            facture.setDeliveryStatus(deliveryStatus);

            int factureId = factureService.addFacture(facture);
            if (factureId == -1) { con.rollback(); return false; }

            // Save delivery address
            deliveryAddress.setFactureId(factureId);
            daService.save(deliveryAddress);

            // Update stock + save products
            for (Map.Entry<Product, Integer> entry : items.entrySet()) {
                Product product  = entry.getKey();
                int     quantity = entry.getValue();

                if ("online".equals(paymentMethod)) {
                    // Online: deduct stock now
                    PreparedStatement ps = con.prepareStatement(
                        "UPDATE products SET quantity = quantity - ? WHERE id = ?");
                    ps.setInt(1, quantity); ps.setInt(2, product.getId());
                    ps.executeUpdate();
                } else {
                    // Cash: reserve stock (don't deduct yet)
                    PreparedStatement ps = con.prepareStatement(
                        "UPDATE products SET reserved_quantity = reserved_quantity + ? WHERE id = ?");
                    ps.setInt(1, quantity); ps.setInt(2, product.getId());
                    ps.executeUpdate();
                }

                // Save facture_product
                FactureProduct fp = new FactureProduct();
                fp.setFactureId(factureId);
                fp.setProductId(product.getId());
                fp.setProductTitle(product.getTitle());
                fp.setQuantity(quantity);
                fp.setPrice(product.getPrice());
                fp.setProductImage(product.getImage());
                fpService.addFactureProduct(fp);
            }

            clearCart(userId);
            con.commit();
            con.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            try { con.rollback(); con.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }

    public void clearCart(int userId) {
        try {
            int cartId = getOrCreateCart(userId);
            PreparedStatement stmt = con.prepareStatement("DELETE FROM cart_item WHERE cart_id = ?");
            stmt.setInt(1, cartId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
