-- ==============================================
-- AUTHENTICATION & AUTHORIZATION SETUP
-- ==============================================

USE library_db;

-- ==============================================
-- INSERT ROLES
-- ==============================================
INSERT INTO roles (name, description, created_at, updated_at) VALUES
('ROLE_ADMIN', 'Quản trị viên hệ thống', NOW(), NOW()),
('ROLE_LIBRARIAN', 'Thủ thư', NOW(), NOW()),
('ROLE_READER', 'Độc giả', NOW(), NOW());

-- ==============================================
-- INSERT USER ACCOUNTS
-- Password mặc định cho tất cả: "password123"
-- Đã được mã hóa bằng BCrypt
-- ==============================================
INSERT INTO user_accounts (username, password, email, full_name, phone_number, enabled, account_non_locked, failed_login_attempts, created_at, updated_at) VALUES
-- Admin Account
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@library.com', 'Quản Trị Viên', '0900000001', true, true, 0, NOW(), NOW()),

-- Librarian Accounts
('librarian1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'thulan@library.com', 'Nguyễn Thị Lan', '0900000002', true, true, 0, NOW(), NOW()),
('librarian2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'thuhuong@library.com', 'Trần Thị Hương', '0900000003', true, true, 0, NOW(), NOW()),

-- Reader Accounts
('reader1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'nguyenvanan@gmail.com', 'Nguyễn Văn An', '0987654321', true, true, 0, NOW(), NOW()),
('reader2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'tranthibinh@gmail.com', 'Trần Thị Bình', '0976543210', true, true, 0, NOW(), NOW()),
('reader3', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'leminhcuong@gmail.com', 'Lê Minh Cường', '0965432109', true, true, 0, NOW(), NOW());

-- ==============================================
-- ASSIGN ROLES TO USERS
-- ==============================================
INSERT INTO user_roles (user_id, role_id) VALUES
-- Admin có tất cả các role
(1, 1), -- admin -> ROLE_ADMIN
(1, 2), -- admin -> ROLE_LIBRARIAN
(1, 3), -- admin -> ROLE_READER

-- Librarians
(2, 2), -- librarian1 -> ROLE_LIBRARIAN
(2, 3), -- librarian1 -> ROLE_READER
(3, 2), -- librarian2 -> ROLE_LIBRARIAN
(3, 3), -- librarian2 -> ROLE_READER

-- Readers
(4, 3), -- reader1 -> ROLE_READER
(5, 3), -- reader2 -> ROLE_READER
(6, 3); -- reader3 -> ROLE_READER

-- ==============================================
-- INSERT RESERVATIONS DATA
-- ==============================================
INSERT INTO reservations (reservation_code, reader_id, book_id, reservation_date, expiry_date, status, notes, created_at, updated_at) VALUES
-- Đặt trước đang chờ
('RS0000000001', 1, 6, NOW(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'PENDING', 'Cần gấp cho nghiên cứu', NOW(), NOW()),
('RS0000000002', 4, 12, NOW(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'PENDING', NULL, NOW(), NOW()),
('RS0000000003', 5, 16, NOW(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'PENDING', 'Dùng cho luận văn', NOW(), NOW()),

-- Đặt trước đã sẵn sàng
('RS0000000004', 2, 9, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'AVAILABLE', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),

-- Đặt trước đã hoàn thành
('RS0000000005', 3, 13, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'FULFILLED', NULL, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- Đặt trước đã hủy
('RS0000000006', 6, 19, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'CANCELLED', 'Không cần nữa', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ==============================================
-- VERIFICATION QUERIES
-- ==============================================

-- Kiểm tra roles
SELECT * FROM roles;

-- Kiểm tra users và roles của họ
SELECT 
    u.id,
    u.username,
    u.full_name,
    u.email,
    GROUP_CONCAT(r.name) as roles,
    u.enabled,
    u.account_non_locked
FROM user_accounts u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id;

-- Kiểm tra reservations
SELECT 
    res.reservation_code,
    r.full_name as reader_name,
    b.title as book_title,
    res.reservation_date,
    res.expiry_date,
    res.status
FROM reservations res
JOIN readers r ON res.reader_id = r.id
JOIN books b ON res.book_id = b.id
ORDER BY res.reservation_date DESC;

-- Thống kê reservations theo trạng thái
SELECT status, COUNT(*) as count
FROM reservations
GROUP BY status;

-- ==============================================
-- TEST CREDENTIALS
-- ==============================================
/*
Tài khoản test - Tất cả đều có password: password123

1. ADMIN:
   Username: admin
   Email: admin@library.com
   
2. LIBRARIAN:
   Username: librarian1 / librarian2
   Email: thulan@library.com / thuhuong@library.com
   
3. READER:
   Username: reader1 / reader2 / reader3
   Email: nguyenvanan@gmail.com / tranthibinh@gmail.com / leminhcuong@gmail.com
*/