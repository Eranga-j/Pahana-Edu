-- Pahana Edu schema + seed
DROP DATABASE IF EXISTS pahanaedu;
CREATE DATABASE pahanaedu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pahanaedu;

CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(128) NOT NULL,
  salt VARCHAR(64) NOT NULL,
  role ENUM('ADMIN','CASHIER') NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  account_number VARCHAR(20) UNIQUE NOT NULL,
  name VARCHAR(120) NOT NULL,
  address VARCHAR(255),
  phone VARCHAR(30),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  sku VARCHAR(30) UNIQUE NOT NULL,
  name VARCHAR(200) NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bills (
  id INT AUTO_INCREMENT PRIMARY KEY,
  bill_no VARCHAR(30) UNIQUE NOT NULL,
  customer_id INT NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by INT NOT NULL,
  FOREIGN KEY (customer_id) REFERENCES customers(id),
  FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE bill_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  bill_id INT NOT NULL,
  item_id INT NOT NULL,
  qty INT NOT NULL CHECK (qty > 0),
  unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
  line_total DECIMAL(12,2) NOT NULL,
  FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
  FOREIGN KEY (item_id) REFERENCES items(id)
);

INSERT INTO users (username, password_hash, salt, role) VALUES
('admin','4b841f136399726146ab80aa6aa3fa64cca22191bc30f8e055ac7f978899372d','a1b2c3d4e5f6a7b8','ADMIN'),
('cashier','5baada35f90257efba30d0c75c8ef773045c68bf70ed84890bc95a3c76a37cad','deadbeefcafebabe','CASHIER');

INSERT INTO customers (account_number, name, address, phone) VALUES
('CUST-1001','Kasun Perera','Colombo 05','077-1234567'),
('CUST-1002','Nimali Silva','Kandy','071-4567890');

INSERT INTO items (sku, name, unit_price) VALUES
('BK-001','Grade 10 Maths Textbook', 1450.00),
('BK-002','A/L Physics Past Papers', 1200.00),
('ST-001','A4 Exercise Book', 180.00);
