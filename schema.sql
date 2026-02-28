-- ============================================================
-- HomeStay Rentals - Database Schema
-- Database: 3a19 (as configured in Mydatabase.java)
-- ============================================================

-- Create place table
CREATE TABLE IF NOT EXISTS place (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    host_id       INT NOT NULL,
    title         VARCHAR(150) NOT NULL,
    description   TEXT,
    price_per_day DOUBLE NOT NULL DEFAULT 0,
    capacity      INT NOT NULL DEFAULT 1,
    max_guests    INT NOT NULL DEFAULT 1,
    address       VARCHAR(255),
    city          VARCHAR(100),
    category      VARCHAR(100),
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    image_url     VARCHAR(500)
);

-- Create booking table
CREATE TABLE IF NOT EXISTS booking (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    place_id     INT NOT NULL,
    user_id      INT NOT NULL,
    start_date   DATE NOT NULL,
    end_date     DATE NOT NULL,
    total_price  DOUBLE NOT NULL DEFAULT 0,
    guests_count INT NOT NULL DEFAULT 1,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_booking_place FOREIGN KEY (place_id) REFERENCES place(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_booking_place_dates ON booking(place_id, start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_booking_user       ON booking(user_id);
CREATE INDEX IF NOT EXISTS idx_place_host         ON place(host_id);
CREATE INDEX IF NOT EXISTS idx_place_status       ON place(status);

-- ============================================================
-- Sample data (optional - for testing)
-- ============================================================

-- Insert sample approved places
INSERT IGNORE INTO place (id, host_id, title, description, price_per_day, capacity, max_guests, address, city, category, status, image_url) VALUES
(1, 1, 'Cozy Mountain Cabin', 'A beautiful cabin nestled in the mountains with stunning views.', 120.00, 3, 6, '123 Mountain Rd', 'Tunis', 'Cabin', 'APPROVED', 'https://images.unsplash.com/photo-1542718610-a1d656d1884c?w=600'),
(2, 1, 'Modern City Apartment', 'Stylish apartment in the heart of the city, close to everything.', 85.00, 2, 4, '45 Downtown Ave', 'Sfax', 'Apartment', 'APPROVED', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=600'),
(3, 2, 'Beachfront Villa', 'Luxury villa with private pool and direct beach access.', 350.00, 5, 10, '7 Ocean Drive', 'Sousse', 'Villa', 'APPROVED', 'https://images.unsplash.com/photo-1613490493576-7fde63acd811?w=600'),
(4, 2, 'Countryside Farmhouse', 'Peaceful farmhouse surrounded by olive trees and nature.', 75.00, 4, 8, 'Route de la Forêt', 'Nabeul', 'House', 'PENDING', 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=600');
