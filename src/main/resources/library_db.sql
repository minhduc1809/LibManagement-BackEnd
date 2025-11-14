-- ==============================================
-- LIBRARY DATABASE - UPDATED SCHEMA
-- Chỉ có 2 vai trò: LIBRARIAN và READER
-- ==============================================

DROP DATABASE IF EXISTS library_db;
CREATE DATABASE library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_db;

-- ==============================================
-- CREATE TABLES
-- ==============================================

-- Bảng Roles (Chỉ 2 roles)
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng User Accounts
CREATE TABLE user_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_login_at DATETIME,
    password_changed_at DATETIME,
    refresh_token VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng User-Role
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user_accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Books
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100),
    publish_year INT,
    category VARCHAR(50),
    isbn VARCHAR(20),
    total_quantity INT NOT NULL,
    available_quantity INT NOT NULL,
    description VARCHAR(500),
    image_url VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at DATE NOT NULL,
    updated_at DATE,
    INDEX idx_book_code (book_code),
    INDEX idx_title (title),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Readers (Độc giả)
CREATE TABLE readers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reader_code VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    address VARCHAR(200),
    email VARCHAR(100),
    phone_number VARCHAR(15),
    identity_card VARCHAR(20),
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    avatar_url VARCHAR(255),
    created_at DATE NOT NULL,
    updated_at DATE,
    INDEX idx_reader_code (reader_code),
    INDEX idx_email (email),
    INDEX idx_phone (phone_number),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Borrow Tickets
CREATE TABLE borrow_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_code VARCHAR(50) NOT NULL UNIQUE,
    reader_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    quantity INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'BORROWED',
    returned_to VARCHAR(100),
    created_at DATE NOT NULL,
    updated_at DATE,
    FOREIGN KEY (reader_id) REFERENCES readers(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    INDEX idx_ticket_code (ticket_code),
    INDEX idx_reader_id (reader_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Penalties
CREATE TABLE penalties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    borrow_ticket_id BIGINT NOT NULL,
    penalty_type VARCHAR(20) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    reason VARCHAR(500),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    payment_date DATE,
    processed_by VARCHAR(100),
    created_at DATE NOT NULL,
    updated_at DATE,
    FOREIGN KEY (borrow_ticket_id) REFERENCES borrow_tickets(id) ON DELETE CASCADE,
    INDEX idx_borrow_ticket_id (borrow_ticket_id),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Reservations
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_code VARCHAR(50) NOT NULL UNIQUE,
    reader_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    reservation_date DATETIME NOT NULL,
    expiry_date DATE NOT NULL,
    notified_at DATETIME,
    fulfilled_at DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (reader_id) REFERENCES readers(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    INDEX idx_reservation_code (reservation_code),
    INDEX idx_reader_id (reader_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================
-- INSERT ROLES (Chỉ 2 roles)
-- ==============================================
INSERT INTO roles (name, description, created_at, updated_at) VALUES
('ROLE_LIBRARIAN', 'Thủ thư - Có toàn quyền quản trị', NOW(), NOW()),
('ROLE_READER', 'Độc giả', NOW(), NOW());

-- ==============================================
-- INSERT USER ACCOUNTS
-- Password mặc định: 123456 (BCrypt)
-- ==============================================
INSERT INTO user_accounts (username, password, email, full_name, phone_number, enabled, account_non_locked, failed_login_attempts, created_at, updated_at) VALUES
-- Thủ thư (có quyền admin)
-- BCrypt hash của "123456": $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
('librarian', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'librarian@library.com', 'Thủ Thư Chính', '0900000001', true, true, 0, NOW(), NOW()),
('thulan', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'thulan@library.com', 'Nguyễn Thị Lan', '0900000002', true, true, 0, NOW(), NOW()),
('thuhuong', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'thuhuong@library.com', 'Trần Thị Hương', '0900000003', true, true, 0, NOW(), NOW()),

-- Độc giả
('reader1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'nguyenvanan@gmail.com', 'Nguyễn Văn An', '0987654321', true, true, 0, NOW(), NOW()),
('reader2', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'tranthibinh@gmail.com', 'Trần Thị Bình', '0976543210', true, true, 0, NOW(), NOW()),
('reader3', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'leminhcuong@gmail.com', 'Lê Minh Cường', '0965432109', true, true, 0, NOW(), NOW());

-- ==============================================
-- ASSIGN ROLES
-- ==============================================
INSERT INTO user_roles (user_id, role_id) VALUES
-- Thủ thư
(1, 1), -- librarian -> ROLE_LIBRARIAN
(2, 1), -- thulan -> ROLE_LIBRARIAN
(3, 1), -- thuhuong -> ROLE_LIBRARIAN

-- Độc giả
(4, 2), -- reader1 -> ROLE_READER
(5, 2), -- reader2 -> ROLE_READER
(6, 2); -- reader3 -> ROLE_READER

-- ==============================================
-- INSERT SAMPLE BOOKS
-- ==============================================
INSERT INTO books (book_code, title, author, publisher, publish_year, category, isbn, total_quantity, available_quantity, description, image_url, status, created_at, updated_at) VALUES
('VH001', 'Số đỏ', 'Vũ Trọng Phụng', 'NXB Văn học', 1936, 'Văn học Việt Nam', '9786041000001', 15, 15, 'Tiểu thuyết hiện thực phê phán', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('VH002', 'Chí Phèo', 'Nam Cao', 'NXB Kim Đồng', 1941, 'Văn học Việt Nam', '9786041000002', 20, 18, 'Truyện ngắn nổi tiếng', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('NN001', 'Nhà giả kim', 'Paulo Coelho', 'NXB Hội Nhà văn', 1988, 'Văn học nước ngoài', '9786041000101', 25, 22, 'Tiểu thuyết triết lý', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('KH001', 'Lập trình Java', 'Herbert Schildt', 'NXB Lao động', 2019, 'Công nghệ thông tin', '9786041000201', 15, 12, 'Giáo trình Java', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('TN001', 'Dế mèn phiêu lưu ký', 'Tô Hoài', 'NXB Kim Đồng', 1941, 'Thiếu nhi', '9786041000501', 25, 20, 'Truyện thiếu nhi kinh điển', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE());

-- ==============================================
-- INSERT SAMPLE READERS
-- ==============================================
INSERT INTO readers (reader_code, full_name, date_of_birth, gender, address, email, phone_number, identity_card, issue_date, expiry_date, status, created_at, updated_at) VALUES
('DG001', 'Nguyễn Văn An', '2003-05-15', 'Nam', '123 Đường Láng, Đống Đa, Hà Nội', 'nguyenvanan@gmail.com', '0987654321', '001203012345', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', CURDATE(), CURDATE()),
('DG002', 'Trần Thị Bình', '2002-08-20', 'Nữ', '45 Giải Phóng, Hai Bà Trưng, Hà Nội', 'tranthibinh@gmail.com', '0976543210', '001202054321', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', CURDATE(), CURDATE()),
('DG003', 'Lê Minh Cường', '2004-03-10', 'Nam', '78 Nguyễn Trãi, Thanh Xuân, Hà Nội', 'leminhcuong@gmail.com', '0965432109', '001204023456', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', CURDATE(), CURDATE());

-- ==============================================
-- TEST CREDENTIALS
-- ==============================================
/*
🔐 TÀI KHOẢN TEST (Password: 123456)

1. THỦ THƯ (Có toàn quyền admin):
   Username: librarian / thulan / thuhuong
   Password: 123456
   
2. ĐỘC GIẢ:
   Username: reader1 / reader2 / reader3
   Password: 123456

📝 TEST LOGIN:
POST http://localhost:8080/api/auth/login
{
  "username": "librarian",
  "password": "123456"
}
*/

-- Verification
SELECT 'ROLES' as Section, name, description FROM roles;
SELECT 'USERS' as Section, u.username, u.full_name, GROUP_CONCAT(r.name) as roles 
FROM user_accounts u 
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id;