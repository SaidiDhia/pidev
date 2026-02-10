package com.example.pi_dev.venue.utils;

import com.example.pi_dev.user.database.UserDatabaseConnection; // Updated import
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class SchemaInitializer {

        public static void initialize() {
                String[] sqlStatements = {
                                // Note: users table is created by User module (UserDatabaseConnection)

                                "CREATE TABLE IF NOT EXISTS places (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "host_id VARCHAR(36) NOT NULL, " +
                                                "title VARCHAR(255) NOT NULL, " +
                                                "description TEXT, " +
                                                "price_per_day DECIMAL(10, 2) NOT NULL, " +
                                                "capacity INT NOT NULL, " +
                                                "address VARCHAR(255) NOT NULL, " +
                                                "city VARCHAR(100) NOT NULL, " +
                                                "latitude DECIMAL(10, 8), " +
                                                "longitude DECIMAL(11, 8), " +
                                                "category VARCHAR(50), " +
                                                "status ENUM('PENDING', 'APPROVED', 'DENIED') DEFAULT 'PENDING', " +
                                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                                "FOREIGN KEY (host_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                                                ");",
                                "CREATE TABLE IF NOT EXISTS place_images (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "place_id INT NOT NULL, " +
                                                "image_url VARCHAR(500) NOT NULL, " +
                                                "sort_order INT DEFAULT 0, " +
                                                "FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE" +
                                                ");",
                                "CREATE TABLE IF NOT EXISTS amenities (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "name VARCHAR(50) UNIQUE NOT NULL, " +
                                                "icon_class VARCHAR(50)" +
                                                ");",
                                "CREATE TABLE IF NOT EXISTS place_amenities (" +
                                                "place_id INT NOT NULL, " +
                                                "amenity_id INT NOT NULL, " +
                                                "PRIMARY KEY (place_id, amenity_id), " +
                                                "FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE, " +
                                                "FOREIGN KEY (amenity_id) REFERENCES amenities(id) ON DELETE CASCADE" +
                                                ");",
                                "CREATE TABLE IF NOT EXISTS bookings (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "place_id INT NOT NULL, " +
                                                "renter_id VARCHAR(36) NOT NULL, " +
                                                "start_date DATE NOT NULL, " +
                                                "end_date DATE NOT NULL, " +
                                                "total_price DECIMAL(10, 2) NOT NULL, " +
                                                "status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING', "
                                                +
                                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                                "FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE, " +
                                                "FOREIGN KEY (renter_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                                                ");",
                                "CREATE TABLE IF NOT EXISTS blocked_dates (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "place_id INT NOT NULL, " +
                                                "start_date DATE NOT NULL, " +
                                                "end_date DATE NOT NULL, " +
                                                "reason VARCHAR(255), " +
                                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                                "FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE" +
                                                ");",
                                "CREATE TABLE IF NOT EXISTS admin_actions (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "place_id INT NOT NULL, " +
                                                "admin_id VARCHAR(36) NOT NULL, " +
                                                "action ENUM('APPROVE', 'DENY', 'REVOKE') NOT NULL, " +
                                                "reason TEXT, " +
                                                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                                "FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE, " +
                                                "FOREIGN KEY (admin_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                                                ");",
                                "INSERT IGNORE INTO amenities (name, icon_class) VALUES " +
                                                "('WiFi', 'wifi-icon'), " +
                                                "('Parking', 'parking-icon'), " +
                                                "('Air Conditioning', 'ac-icon'), " +
                                                "('Kitchen', 'kitchen-icon'), " +
                                                "('Pool', 'pool-icon'), " +
                                                "('Projector', 'projector-icon');"
                };

                try (Connection conn = UserDatabaseConnection.getInstance().getConnection();
                                Statement stmt = conn.createStatement()) {

                        for (String sql : sqlStatements) {
                                try {
                                        stmt.execute(sql);
                                } catch (SQLException e) {
                                        System.err.println("Error creating table: " + e.getMessage());
                                        // Continue to next statement
                                }
                        }
                        System.out.println("Venue schema initialization completed.");

                } catch (SQLException e) {
                        System.err.println("Error initializing schema: " + e.getMessage());
                        e.printStackTrace();
                }

                seedData();
        }

        private static void seedData() {
                try (Connection conn = UserDatabaseConnection.getInstance().getConnection();
                                Statement stmt = conn.createStatement()) {

                        // Check if users exist, if not create a dummy host
                        // We need a host ID for the places
                        String checkUsers = "SELECT count(*) FROM users";
                        java.sql.ResultSet rs = stmt.executeQuery(checkUsers);
                        if (rs.next() && rs.getInt(1) == 0) {
                                // Insert a dummy host
                                // UUID: 123e4567-e89b-12d3-a456-426614174000
                                String verifyHost = "INSERT INTO users (user_id, email, password_hash, first_name, last_name, role, is_verified) "
                                                +
                                                "VALUES ('123e4567-e89b-12d3-a456-426614174000', 'host@wonderlust.com', 'hashedpassword', 'Host', 'User', 'HOST', true)";
                                stmt.executeUpdate(verifyHost);
                                System.out.println("Seeded dummy host user.");
                        }

                        // Check if places exist
                        String checkPlaces = "SELECT count(*) FROM places";
                        rs = stmt.executeQuery(checkPlaces);
                        if (rs.next() && rs.getInt(1) == 0) {
                                // Insert dummy places
                                // Ensure we use a valid host_id. If we just created one, use that.
                                // If users existed but we didn't create one, we need a valid ID.
                                // For simplicity in this seed script, we'll try to use the one we might have
                                // just created,
                                // or fetch the first one available.

                                String getHostId = "SELECT user_id FROM users LIMIT 1";
                                rs = stmt.executeQuery(getHostId);
                                String hostId = "";
                                if (rs.next()) {
                                        hostId = rs.getString(1);
                                }

                                if (!hostId.isEmpty()) {
                                        String[] placesSql = {
                                                        "INSERT INTO places (host_id, title, description, price_per_day, capacity, address, city, latitude, longitude, category, status) VALUES "
                                                                        +
                                                                        "('" + hostId
                                                                        + "', 'Cozy Mountain Cabin', 'A beautiful cabin in the woods with a fireplace and stunning views.', 120.00, 4, '123 Pine Rd', 'Aspen', 39.1911, -106.8175, 'Cabins', 'APPROVED')",

                                                        "INSERT INTO places (host_id, title, description, price_per_day, capacity, address, city, latitude, longitude, category, status) VALUES "
                                                                        +
                                                                        "('" + hostId
                                                                        + "', 'Modern Beachfront Villa', 'Luxury villa right on the sand. Private pool and direct beach access.', 450.00, 8, '456 Ocean Dr', 'Miami', 25.7617, -80.1918, 'Beachfront', 'APPROVED')",

                                                        "INSERT INTO places (host_id, title, description, price_per_day, capacity, address, city, latitude, longitude, category, status) VALUES "
                                                                        +
                                                                        "('" + hostId
                                                                        + "', 'Downtown Loft', 'Trendy loft in the heart of the city. Walking distance to everything.', 85.00, 2, '789 Main St', 'New York', 40.7128, -74.0060, 'Apartment', 'APPROVED')"
                                        };

                                        for (String sql : placesSql) {
                                                stmt.executeUpdate(sql);
                                        }

                                        // Seed Images
                                        String[] imagesSql = {
                                                        "INSERT INTO place_images (place_id, image_url, sort_order) SELECT id, 'https://images.unsplash.com/photo-1449156493391-d2cfa28e468b?auto=format&fit=crop&w=800&q=80', 1 FROM places WHERE title = 'Cozy Mountain Cabin'",
                                                        "INSERT INTO place_images (place_id, image_url, sort_order) SELECT id, 'https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?auto=format&fit=crop&w=800&q=80', 1 FROM places WHERE title = 'Modern Beachfront Villa'",
                                                        "INSERT INTO place_images (place_id, image_url, sort_order) SELECT id, 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=80', 1 FROM places WHERE title = 'Downtown Loft'"
                                        };

                                        for (String sql : imagesSql) {
                                                stmt.executeUpdate(sql);
                                        }

                                        System.out.println("Seeded dummy places and images.");
                                }
                        }

                } catch (SQLException e) {
                        System.err.println("Error seeding data: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        public static void main(String[] args) {
                initialize();
        }
}
