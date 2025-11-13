-- ==============================================
-- LIBRARY DATABASE - COMPLETE SETUP
-- Password: Plain Text (No BCrypt)
-- ==============================================

-- Tạo database
DROP DATABASE IF EXISTS library_db;
CREATE DATABASE library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_db;

-- ==============================================
-- CREATE TABLES
-- ==============================================

-- Bảng Roles
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

-- Bảng User-Role (Many-to-Many)
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

-- Bảng Readers
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
-- INSERT ROLES
-- ==============================================
INSERT INTO roles (name, description, created_at, updated_at) VALUES
('ROLE_ADMIN', 'Quản trị viên hệ thống', NOW(), NOW()),
('ROLE_LIBRARIAN', 'Thủ thư', NOW(), NOW()),
('ROLE_READER', 'Độc giả', NOW(), NOW());


INSERT INTO user_accounts (username, password, email, full_name, phone_number, enabled, account_non_locked, failed_login_attempts, created_at, updated_at) VALUES

('admin', '$2a$10$9ZKqBJT4/AdgUdW1iH5kZeQTWlctgLhFJHQc.dRSoxlZPtFN3RDou', 'admin@library.com', 'Quản Trị Viên', '0900000001', true, true, 0, NOW(), NOW()),


('librarian1', '$2a$10$9ZKqBJT4/AdgUdW1iH5kZeQTWlctgLhFJHQc.dRSoxlZPtFN3RDou', 'thulan@library.com', 'Nguyễn Thị Lan', '0900000002', true, true, 0, NOW(), NOW()),
('librarian2', '$2a$10$9ZKqBJT4/AdgUdW1iH5kZeQTWlctgLhFJHQc.dRSoxlZPtFN3RDou', 'thuhuong@library.com', 'Trần Thị Hương', '0900000003', true, true, 0, NOW(), NOW()),

('reader1', '$2a$10$9ZKqBJT4/AdgUdW1iH5kZeQTWlctgLhFJHQc.dRSoxlZPtFN3RDou', 'nguyenvanan@gmail.com', 'Nguyễn Văn An', '0987654321', true, true, 0, NOW(), NOW()),
('reader2', '$2a$10$9ZKqBJT4/AdgUdW1iH5kZeQTWlctgLhFJHQc.dRSoxlZPtFN3RDou', 'tranthibinh@gmail.com', 'Trần Thị Bình', '0976543210', true, true, 0, NOW(), NOW()),
('reader3', '$2a$10$9ZKqBJT4/AdgUdW1iH5kZeQTWlctgLhFJHQc.dRSoxlZPtFN3RDou', 'leminhcuong@gmail.com', 'Lê Minh Cường', '0965432109', true, true, 0, NOW(), NOW());

INSERT INTO user_roles (user_id, role_id) VALUES

(1, 1), (1, 2), (1, 3),

(2, 2), (2, 3),
(3, 2), (3, 3),

(4, 3),
(5, 3),
(6, 3);

INSERT INTO books (book_code, title, author, publisher, publish_year, category, isbn, total_quantity, available_quantity, description, image_url, status, created_at, updated_at) VALUES
-- Văn học Việt Nam
('VH001', 'Số đỏ', 'Vũ Trọng Phụng', 'NXB Văn học', 1936, 'Văn học Việt Nam', '9786041000001', 15, 15, 'Tiểu thuyết hiện thực phê phán của Vũ Trọng Phụng', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('VH002', 'Chí Phèo', 'Nam Cao', 'NXB Kim Đồng', 1941, 'Văn học Việt Nam', '9786041000002', 20, 18, 'Truyện ngắn nổi tiếng của Nam Cao', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('VH003', 'Lão Hạc', 'Nam Cao', 'NXB Kim Đồng', 1943, 'Văn học Việt Nam', '9786041000003', 18, 16, 'Truyện ngắn về nỗi khổ của người nông dân', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('VH004', 'Tắt đèn', 'Ngô Tất Tố', 'NXB Văn học', 1939, 'Văn học Việt Nam', '9786041000004', 12, 12, 'Tiểu thuyết hiện thực về cuộc sống nông thôn', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('VH005', 'Vợ nhặt', 'Kim Lân', 'NXB Văn học', 1962, 'Văn học Việt Nam', '9786041000005', 15, 13, 'Truyện ngắn hay về tình người trong đói khổ', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),

-- Văn học nước ngoài
('NN001', 'Nhà giả kim', 'Paulo Coelho', 'NXB Hội Nhà văn', 1988, 'Văn học nước ngoài', '9786041000101', 25, 22, 'Tiểu thuyết triết lý nổi tiếng thế giới', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('NN002', 'Đắc nhân tâm', 'Dale Carnegie', 'NXB Tổng hợp TP.HCM', 1936, 'Văn học nước ngoài', '9786041000102', 30, 25, 'Sách kỹ năng sống kinh điển', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('NN003', 'Tuổi trẻ đáng giá bao nhiêu', 'Rosie Nguyễn', 'NXB Hội Nhà văn', 2018, 'Văn học nước ngoài', '9786041000103', 20, 15, 'Sách về phát triển bản thân cho giới trẻ', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('NN004', 'Sapiens: Lược sử loài người', 'Yuval Noah Harari', 'NXB Thế giới', 2011, 'Văn học nước ngoài', '9786041000104', 18, 16, 'Cuốn sách về lịch sử loài người', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('NN005', 'Harry Potter và Hòn đá phù thủy', 'J.K. Rowling', 'NXB Trẻ', 1997, 'Văn học nước ngoài', '9786041000105', 22, 18, 'Phần đầu tiên của series Harry Potter', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),

-- Công nghệ thông tin
('KH001', 'Lập trình Java cơ bản', 'Herbert Schildt', 'NXB Lao động', 2019, 'Công nghệ thông tin', '9786041000201', 15, 12, 'Giáo trình Java cho người mới bắt đầu', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('KH002', 'Clean Code', 'Robert C. Martin', 'NXB Thế giới', 2008, 'Công nghệ thông tin', '9786041000202', 12, 10, 'Sách về viết code sạch và chuyên nghiệp', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('KH003', 'Design Patterns', 'Gang of Four', 'NXB Thế giới', 1994, 'Công nghệ thông tin', '9786041000203', 10, 8, 'Các mẫu thiết kế phần mềm cổ điển', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('KH004', 'Trí tuệ nhân tạo', 'Stuart Russell', 'NXB Khoa học tự nhiên', 2020, 'Công nghệ thông tin', '9786041000204', 14, 14, 'Giới thiệu về AI và Machine Learning', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('KH005', 'Blockchain căn bản', 'Andreas Antonopoulos', 'NXB Thông tin và Truyền thông', 2021, 'Công nghệ thông tin', '9786041000205', 10, 9, 'Tìm hiểu về công nghệ blockchain', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),

-- Kinh tế
('KT001', 'Nghìn lẻ một đêm', 'Nguyễn Ngọc Tú', 'NXB Phụ nữ', 2020, 'Kinh tế', '9786041000301', 12, 12, 'Sách về khởi nghiệp và kinh doanh', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('KT002', 'Quản trị học', 'Stephen Robbins', 'NXB Thống kê', 2018, 'Kinh tế', '9786041000302', 16, 14, 'Giáo trình quản trị doanh nghiệp', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('KT003', 'Marketing căn bản', 'Philip Kotler', 'NXB Lao động', 2017, 'Kinh tế', '9786041000303', 14, 12, 'Sách về marketing cho doanh nghiệp', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),

-- Lịch sử
('LS001', 'Lịch sử Việt Nam', 'Trần Trọng Kim', 'NXB Văn học', 1971, 'Lịch sử', '9786041000401', 15, 15, 'Tổng quan lịch sử Việt Nam', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('LS002', 'Địa lý Việt Nam', 'Nguyễn Văn Hưng', 'NXB Giáo dục', 2020, 'Địa lý', '9786041000402', 12, 12, 'Giáo trình địa lý Việt Nam', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),

-- Thiếu nhi
('TN001', 'Dế mèn phiêu lưu ký', 'Tô Hoài', 'NXB Kim Đồng', 1941, 'Thiếu nhi', '9786041000501', 25, 20, 'Truyện thiếu nhi kinh điển Việt Nam', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('TN002', 'Doraemon - Tập 1', 'Fujiko F. Fujio', 'NXB Kim Đồng', 1969, 'Thiếu nhi', '9786041000502', 30, 25, 'Truyện tranh Doraemon phổ biến', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE()),
('TN003', 'Thỏ bảy màu', 'Nguyễn Nhật Ánh', 'NXB Trẻ', 2010, 'Thiếu nhi', '9786041000503', 20, 18, 'Truyện thiếu nhi của Nguyễn Nhật Ánh', 'https://via.placeholder.com/150', 'AVAILABLE', CURDATE(), CURDATE());

-- ==============================================
-- INSERT READERS
-- ==============================================
INSERT INTO readers (reader_code, full_name, date_of_birth, gender, address, email, phone_number, identity_card, issue_date, expiry_date, status, avatar_url, created_at, updated_at) VALUES
('DG001', 'Nguyễn Văn An', '2003-05-15', 'Nam', '123 Đường Láng, Đống Đa, Hà Nội', 'nguyenvanan@gmail.com', '0987654321', '001203012345', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG002', 'Trần Thị Bình', '2002-08-20', 'Nữ', '45 Giải Phóng, Hai Bà Trưng, Hà Nội', 'tranthibinh@gmail.com', '0976543210', '001202054321', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG003', 'Lê Minh Cường', '2004-03-10', 'Nam', '78 Nguyễn Trãi, Thanh Xuân, Hà Nội', 'leminhcuong@gmail.com', '0965432109', '001204023456', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG004', 'Phạm Thu Dung', '2003-11-25', 'Nữ', '56 Tây Sơn, Đống Đa, Hà Nội', 'phamthudung@gmail.com', '0954321098', '001203067890', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG005', 'Hoàng Văn Em', '2002-01-30', 'Nam', '89 Cầu Giấy, Cầu Giấy, Hà Nội', 'hoangvanem@gmail.com', '0943210987', '001202034567', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG006', 'Vũ Thị Hoa', '1985-07-15', 'Nữ', '12 Hoàng Quốc Việt, Cầu Giấy, Hà Nội', 'vuthihoa@gmail.com', '0932109876', '001185045678', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG007', 'Đặng Minh Giang', '1990-12-05', 'Nam', '34 Nguyễn Chí Thanh, Đống Đa, Hà Nội', 'dangminhgiang@gmail.com', '0921098765', '001190056789', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG008', 'Bùi Thị Hương', '2008-04-20', 'Nữ', '67 Phạm Văn Đồng, Bắc Từ Liêm, Hà Nội', 'buithihuong@gmail.com', '0910987654', '001208067890', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG009', 'Ngô Văn Khải', '2007-09-10', 'Nam', '23 Xuân Thủy, Cầu Giấy, Hà Nội', 'ngovankhai@gmail.com', '0909876543', '001207078901', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG010', 'Đinh Thị Lan', '2006-06-15', 'Nữ', '45 Trần Duy Hưng, Cầu Giấy, Hà Nội', 'dinhthilan@gmail.com', '0898765432', '001206089012', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE());

-- ==============================================
-- INSERT BORROW TICKETS
-- ==============================================
INSERT INTO borrow_tickets (ticket_code, reader_id, book_id, borrow_date, due_date, return_date, quantity, status, returned_to, created_at, updated_at) VALUES
-- Đã trả
('MT0000000001', 1, 1, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 6 DAY), DATE_SUB(CURDATE(), INTERVAL 5 DAY), 1, 'RETURNED', 'Thủ thư Lan', CURDATE(), CURDATE()),
('MT0000000002', 2, 3, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 11 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY), 1, 'RETURNED', 'Thủ thư Hương', CURDATE(), CURDATE()),

-- Đang mượn
('MT0000000003', 1, 5, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 9 DAY), NULL, 1, 'BORROWED', NULL, CURDATE(), CURDATE()),
('MT0000000004', 3, 7, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 11 DAY), NULL, 1, 'BORROWED', NULL, CURDATE(), CURDATE()),
('MT0000000005', 4, 10, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_ADD(CURDATE(), INTERVAL 7 DAY), NULL, 1, 'BORROWED', NULL, CURDATE(), CURDATE()),

-- Quá hạn
('MT0000000006', 2, 8, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 6 DAY), NULL, 1, 'BORROWED', NULL, CURDATE(), CURDATE()),
('MT0000000007', 5, 11, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 11 DAY), NULL, 1, 'BORROWED', NULL, CURDATE(), CURDATE());

-- ==============================================
-- INSERT PENALTIES
-- ==============================================
INSERT INTO penalties (borrow_ticket_id, penalty_type, amount, reason, payment_status, payment_date, processed_by, created_at, updated_at) VALUES
(6, 'OVERDUE', 30000.00, 'Trả sách trễ 6 ngày', 'UNPAID', NULL, NULL, CURDATE(), CURDATE()),
(7, 'OVERDUE', 55000.00, 'Trả sách trễ 11 ngày', 'UNPAID', NULL, NULL, CURDATE(), CURDATE());

-- ==============================================
-- INSERT RESERVATIONS
-- ==============================================
INSERT INTO reservations (reservation_code, reader_id, book_id, reservation_date, expiry_date, status, notes, created_at, updated_at) VALUES
('RS0000000001', 1, 6, NOW(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'PENDING', 'Cần gấp cho nghiên cứu', NOW(), NOW()),
('RS0000000002', 4, 12, NOW(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'PENDING', NULL, NOW(), NOW());

-- ==============================================
-- VERIFICATION QUERIES
-- ==============================================

SELECT '=== ROLES ===' as Info;
SELECT * FROM roles;

SELECT '=== USERS ===' as Info;
SELECT 
    u.id,
    u.username,
    u.password,
    u.full_name,
    u.email,
    GROUP_CONCAT(r.name) as roles,
    u.enabled,
    u.account_non_locked
FROM user_accounts u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id;

SELECT '=== STATISTICS ===' as Info;
SELECT 'Books' as TableName, COUNT(*) as RecordCount FROM books
UNION ALL SELECT 'Readers', COUNT(*) FROM readers
UNION ALL SELECT 'BorrowTickets', COUNT(*) FROM borrow_tickets
UNION ALL SELECT 'Penalties', COUNT(*) FROM penalties
UNION ALL SELECT 'Reservations', COUNT(*) FROM reservations;

SELECT '=== BOOKS BY CATEGORY ===' as Info;
SELECT category, COUNT(*) as total_books, SUM(total_quantity) as total_quantity
FROM books
GROUP BY category
ORDER BY total_books DESC;

-- ==============================================
-- TEST CREDENTIALS
-- ==============================================
/*
🔐 TÀI KHOẢN TEST (Password: 12345678)

1. ADMIN:
   Username: admin
   Password: 12345678
   
2. LIBRARIAN:
   Username: librarian1 hoặc librarian2
   Password: 12345678
   
3. READER:
   Username: reader1, reader2, reader3
   Password: 12345678

📝 TEST LOGIN:
POST http://localhost:8080/api/auth/login
{
  "username": "admin",
  "password": "12345678"
}
*/