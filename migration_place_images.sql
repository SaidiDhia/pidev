-- ============================================================
-- Migration: Create place_images table for multi-image gallery
-- Run this in MySQL Workbench or CLI before launching the app
-- ============================================================

CREATE TABLE IF NOT EXISTS place_images (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  place_id   INT          NOT NULL,
  url        VARCHAR(500) NOT NULL,
  sort_order INT          NOT NULL DEFAULT 0,
  is_primary TINYINT(1)  NOT NULL DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (place_id) REFERENCES place(id) ON DELETE CASCADE,
  INDEX idx_place_images_place (place_id),
  INDEX idx_place_images_primary (is_primary)
);

-- Verify:
-- DESCRIBE place_images;
-- SELECT * FROM place_images LIMIT 10;
