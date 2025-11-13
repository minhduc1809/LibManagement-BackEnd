-- ==============================================
-- AUTHENTICATION SETUP - FIXED VERSION
-- ==============================================

USE library_db;

-- Xóa dữ liệu cũ
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE user_accounts;
TRUNCATE TABLE roles;
SET FOREIGN_KEY_CHECKS = 1;

-- ==============================================
-- INSERT ROLES
-- ==============================================
INSERT INTO roles (name, description, created_at, updated_at) VALUES
('ROLE_ADMIN', 'Quản trị viên hệ thống', NOW(), NOW()),
('ROLE_LIBRARIAN', 'Thủ thư', NOW(), NOW()),
('ROLE_READER', 'Độc giả', NOW(), NOW());

-- ==============================================
-- INSERT USER ACCOUNTS
-- Password: "123456" cho tất cả tài khoản
-- BCrypt encoded: $2a$10$8.UXoI7jzD8LbNHzrKRn2OZW9HJgYNBFLwPZ6XMZO7hVQnYgRX6jC
-- ==============================================
INSERT INTO user_accounts (username, password, email, full_name, phone_number, enabled, account_non_locked, failed_login_attempts, created_at, updated_at) VALUES
-- Admin Account (username: admin, password: 123456)
('admin', '$2a$10$9ZKqBJT4/AdgUdW1iH5kZeQTWlctgLhFJHQc.dRSoxlZPtFN3RDou', 'admin@library.com', 'Quản Trị Viên', '0900000001', true, true, 0, NOW(), NOW()),

-- Librarian Accounts (password: 123456)
('librarian1', '$2a$10$8.UXoI7jzD8LbNHzrKRn2OZW9HJgYNBFLwPZ6XMZO7hVQnYgRX6jC', 'thulan@library.com', 'Nguyễn Thị Lan', '0900000002', true, true, 0, NOW(), NOW()),
('librarian2', '$2a$10$8.UXoI7jzD8LbNHzrKRn2OZW9HJgYNBFLwPZ6XMZO7hVQnYgRX6jC', 'thuhuong@library.com', 'Trần Thị Hương', '0900000003', true, true, 0, NOW(), NOW()),

-- Reader Accounts (password: 123456)
('reader1', '$2a$10$8.UXoI7jzD8LbNHzrKRn2OZW9HJgYNBFLwPZ6XMZO7hVQnYgRX6jC', 'nguyenvanan@gmail.com', 'Nguyễn Văn An', '0987654321', true, true, 0, NOW(), NOW()),
('reader2', '$2a$10$8.UXoI7jzD8LbNHzrKRn2OZW9HJgYNBFLwPZ6XMZO7hVQnYgRX6jC', 'tranthibinh@gmail.com', 'Trần Thị Bình', '0976543210', true, true, 0, NOW(), NOW()),
('reader3', '$2a$10$8.UXoI7jzD8LbNHzrKRn2OZW9HJgYNBFLwPZ6XMZO7hVQnYgRX6jC', 'leminhcuong@gmail.com', 'Lê Minh Cường', '0965432109', true, true, 0, NOW(), NOW());

-- ==============================================
-- ASSIGN ROLES TO USERS
-- ==============================================
INSERT INTO user_roles (user_id, role_id) VALUES
-- Admin (có tất cả roles)
(1, 1), -- ROLE_ADMIN
(1, 2), -- ROLE_LIBRARIAN
(1, 3), -- ROLE_READER

-- Librarians
(2, 2), -- ROLE_LIBRARIAN
(2, 3), -- ROLE_READER
(3, 2), -- ROLE_LIBRARIAN
(3, 3), -- ROLE_READER

-- Readers
(4, 3), -- ROLE_READER
(5, 3), -- ROLE_READER
(6, 3); -- ROLE_READER

-- ==============================================
-- VERIFICATION
-- ==============================================
SELECT 'Roles created' as Status, COUNT(*) as Count FROM roles;
SELECT 'Users created' as Status, COUNT(*) as Count FROM user_accounts;
SELECT 'Role assignments' as Status, COUNT(*) as Count FROM user_roles;

-- Xem users và roles
SELECT 
    u.username,
    u.email,
    u.full_name,
    GROUP_CONCAT(r.name) as roles,
    u.enabled
FROM user_accounts u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id
ORDER BY u.id;

-- ==============================================
-- TEST CREDENTIALS
-- ==============================================
/*
TẤT CẢ TÀI KHOẢN ĐỀU CÓ PASSWORD: 123456

1. ADMIN:
   Username: admin
   Password: 123456
   
2. LIBRARIAN:
   Username: librarian1
   Password: 123456
   
   Username: librarian2
   Password: 123456
   
3. READER:
   Username: reader1
   Password: 123456
   
   Username: reader2
   Password: 123456
   
   Username: reader3
   Password: 123456
*/