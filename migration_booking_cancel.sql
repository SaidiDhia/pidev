-- ============================================================
-- Migration: Add cancellation columns to booking table
-- Run this in MySQL Workbench or CLI before launching the app
-- ============================================================

ALTER TABLE booking
  ADD COLUMN cancelled_at  DATETIME     NULL,
  ADD COLUMN refund_amount DOUBLE       NULL DEFAULT 0,
  ADD COLUMN cancelled_by  VARCHAR(10)  NULL,
  ADD COLUMN cancel_reason VARCHAR(255) NULL;

-- Verify the new columns:
-- DESCRIBE booking;
