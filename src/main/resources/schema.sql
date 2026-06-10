-- Badminton Court Booking & Management System
-- MySQL 8 Schema

CREATE DATABASE IF NOT EXISTS badminton_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE badminton_db;

CREATE TABLE IF NOT EXISTS roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    phone       VARCHAR(20),
    enabled     TINYINT(1) NOT NULL DEFAULT 1,
    created_at  DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE IF NOT EXISTS courts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    price_per_hour  DECIMAL(10, 2) NOT NULL,
    active          TINYINT(1) NOT NULL DEFAULT 1,
    created_at      DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS court_images (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    court_id    BIGINT NOT NULL,
    image_url   VARCHAR(500) NOT NULL,
    public_id   VARCHAR(200),
    created_at  DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_court_images_court FOREIGN KEY (court_id) REFERENCES courts (id)
);

CREATE TABLE IF NOT EXISTS time_slots (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_time  TIME NOT NULL,
    end_time    TIME NOT NULL,
    active      TINYINT(1) NOT NULL DEFAULT 1,
    created_at  DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS bookings (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    court_id      BIGINT NOT NULL,
    time_slot_id  BIGINT NOT NULL,
    booking_date  DATE NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at    DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_booking_court_date_slot UNIQUE (court_id, booking_date, time_slot_id),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_bookings_court FOREIGN KEY (court_id) REFERENCES courts (id),
    CONSTRAINT fk_bookings_time_slot FOREIGN KEY (time_slot_id) REFERENCES time_slots (id)
);

CREATE TABLE IF NOT EXISTS payments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id      BIGINT NOT NULL UNIQUE,
    amount          DECIMAL(10, 2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_id  VARCHAR(100),
    created_at      DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings (id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    token       VARCHAR(500) NOT NULL UNIQUE,
    expiry_date DATETIME(6) NOT NULL,
    revoked     TINYINT(1) NOT NULL DEFAULT 0,
    created_at  DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS token_blacklist (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    token           VARCHAR(500) NOT NULL UNIQUE,
    expiry_date     DATETIME(6) NOT NULL,
    blacklisted_at  DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    action      VARCHAR(100) NOT NULL,
    status      VARCHAR(20) NOT NULL,
    username    VARCHAR(100),
    message     TEXT NOT NULL,
    ip_address  VARCHAR(45),
    created_at  DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)
);

-- Seed roles
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_MANAGER'), ('ROLE_CUSTOMER');
