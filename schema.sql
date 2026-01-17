-- Hotel Booking System Database Schema
-- Run this SQL in PostgreSQL database: hotel

-- Drop existing tables if needed (for fresh start)
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS rooms CASCADE;
DROP TABLE IF EXISTS staff CASCADE;

-- Drop existing types
DROP TYPE IF EXISTS room_type CASCADE;
DROP TYPE IF EXISTS booking_status CASCADE;

-- Create ENUM types
CREATE TYPE room_type AS ENUM ('SINGLE', 'DOUBLE', 'FAMILY', 'VIP');
CREATE TYPE booking_status AS ENUM ('CONFIRMED', 'CANCELLED');

-- Staff table
CREATE TABLE staff (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    telegram_id BIGINT UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Rooms table
CREATE TABLE rooms (
    id SERIAL PRIMARY KEY,
    room_number VARCHAR(10) UNIQUE NOT NULL,
    room_type room_type NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE
);

-- Bookings table
CREATE TABLE bookings (
    id SERIAL PRIMARY KEY,
    guest_name VARCHAR(100) NOT NULL,
    guest_email VARCHAR(150),
    guest_phone VARCHAR(20) NOT NULL,
    room_id INTEGER REFERENCES rooms(id) ON DELETE SET NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status booking_status DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    telegram_chat_id BIGINT
);

-- Create indexes for performance
CREATE INDEX idx_bookings_phone ON bookings(guest_phone);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_check_in ON bookings(check_in_date);
CREATE INDEX idx_staff_telegram_id ON staff(telegram_id);
CREATE INDEX idx_staff_email ON staff(email);

-- Insert sample rooms
-- SINGLE rooms: $40/night
INSERT INTO rooms (room_number, room_type, price, is_available) VALUES
('101', 'SINGLE', 40.00, TRUE),
('102', 'SINGLE', 40.00, TRUE),
('103', 'SINGLE', 40.00, TRUE);

-- DOUBLE rooms: $60/night
INSERT INTO rooms (room_number, room_type, price, is_available) VALUES
('201', 'DOUBLE', 60.00, TRUE),
('202', 'DOUBLE', 60.00, TRUE),
('203', 'DOUBLE', 60.00, TRUE);

-- FAMILY rooms: $100/night
INSERT INTO rooms (room_number, room_type, price, is_available) VALUES
('301', 'FAMILY', 100.00, TRUE),
('302', 'FAMILY', 100.00, TRUE);

-- VIP rooms: $200/night
INSERT INTO rooms (room_number, room_type, price, is_available) VALUES
('401', 'VIP', 200.00, TRUE),
('402', 'VIP', 200.00, TRUE);

-- Insert sample staff (plain text password for development)
INSERT INTO staff (name, email, password_hash) VALUES
('Admin', 'admin@hotel.com', 'admin123');

-- Verify data
SELECT 'Rooms created:' AS info, COUNT(*) AS count FROM rooms;
SELECT 'Staff created:' AS info, COUNT(*) AS count FROM staff;
