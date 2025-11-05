CREATE DATABASE library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- ==============================================
-- LIBRARY DATABASE - DATA INITIALIZATION SCRIPT
-- ==============================================

USE library_db;

-- Clean existing data (optional - for fresh start)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE penalties;
TRUNCATE TABLE borrow_tickets;
TRUNCATE TABLE books;
TRUNCATE TABLE readers;
SET FOREIGN_KEY_CHECKS = 1;

-- ==============================================
-- INSERT BOOKS DATA
-- ==============================================
INSERT INTO books (book_code, title, author, publisher, publish_year, category, isbn, total_quantity, available_quantity, description, image_url, status, created_at, updated_at) VALUES
-- Văn học Việt Nam
('VH001', 'Số đỏ', 'Vũ Trọng Phụng', 'NXB Văn học', 1936, 'Văn học Việt Nam', '9786041000001', 15, 15, 'Tiểu thuyết hiện thực phê phán của Vũ Trọng Phụng', 'https://example.com/so-do.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('VH002', 'Chí Phèo', 'Nam Cao', 'NXB Kim Đồng', 1941, 'Văn học Việt Nam', '9786041000002', 20, 18, 'Truyện ngắn nổi tiếng của Nam Cao', 'https://example.com/chi-pheo.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('VH003', 'Lão Hạc', 'Nam Cao', 'NXB Kim Đồng', 1943, 'Văn học Việt Nam', '9786041000003', 18, 16, 'Truyện ngắn về nỗi khổ của người nông dân', 'https://example.com/lao-hac.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('VH004', 'Tắt đèn', 'Ngô Tất Tố', 'NXB Văn học', 1939, 'Văn học Việt Nam', '9786041000004', 12, 12, 'Tiểu thuyết hiện thực về cuộc sống nông thôn', 'https://example.com/tat-den.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('VH005', 'Vợ nhặt', 'Kim Lân', 'NXB Văn học', 1962, 'Văn học Việt Nam', '9786041000005', 15, 13, 'Truyện ngắn hay về tình người trong đói khổ', 'https://example.com/vo-nhat.jpg', 'AVAILABLE', CURDATE(), CURDATE()),

-- Văn học nước ngoài
('NN001', 'Nhà giả kim', 'Paulo Coelho', 'NXB Hội Nhà văn', 1988, 'Văn học nước ngoài', '9786041000101', 25, 22, 'Tiểu thuyết triết lý nổi tiếng thế giới', 'https://example.com/nha-gia-kim.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('NN002', 'Đắc nhân tâm', 'Dale Carnegie', 'NXB Tổng hợp TP.HCM', 1936, 'Văn học nước ngoài', '9786041000102', 30, 25, 'Sách kỹ năng sống kinh điển', 'https://example.com/dac-nhan-tam.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('NN003', 'Tuổi trẻ đáng giá bao nhiêu', 'Rosie Nguyễn', 'NXB Hội Nhà văn', 2018, 'Văn học nước ngoài', '9786041000103', 20, 15, 'Sách về phát triển bản thân cho giới trẻ', 'https://example.com/tuoi-tre.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('NN004', 'Sapiens: Lược sử loài người', 'Yuval Noah Harari', 'NXB Thế giới', 2011, 'Văn học nước ngoài', '9786041000104', 18, 16, 'Cuốn sách về lịch sử loài người', 'https://example.com/sapiens.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('NN005', 'Harry Potter và Hòn đá phù thủy', 'J.K. Rowling', 'NXB Trẻ', 1997, 'Văn học nước ngoài', '9786041000105', 22, 18, 'Phần đầu tiên của series Harry Potter', 'https://example.com/harry-potter-1.jpg', 'AVAILABLE', CURDATE(), CURDATE()),

-- Khoa học công nghệ
('KH001', 'Lập trình Java cơ bản', 'Herbert Schildt', 'NXB Lao động', 2019, 'Công nghệ thông tin', '9786041000201', 15, 12, 'Giáo trình Java cho người mới bắt đầu', 'https://example.com/java-basic.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('KH002', 'Clean Code', 'Robert C. Martin', 'NXB Thế giới', 2008, 'Công nghệ thông tin', '9786041000202', 12, 10, 'Sách về viết code sạch và chuyên nghiệp', 'https://example.com/clean-code.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('KH003', 'Design Patterns', 'Gang of Four', 'NXB Thế giới', 1994, 'Công nghệ thông tin', '9786041000203', 10, 8, 'Các mẫu thiết kế phần mềm cổ điển', 'https://example.com/design-patterns.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('KH004', 'Trí tuệ nhân tạo', 'Stuart Russell', 'NXB Khoa học tự nhiên', 2020, 'Công nghệ thông tin', '9786041000204', 14, 14, 'Giới thiệu về AI và Machine Learning', 'https://example.com/ai.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('KH005', 'Blockchain căn bản', 'Andreas Antonopoulos', 'NXB Thông tin và Truyền thông', 2021, 'Công nghệ thông tin', '9786041000205', 10, 9, 'Tìm hiểu về công nghệ blockchain', 'https://example.com/blockchain.jpg', 'AVAILABLE', CURDATE(), CURDATE()),

-- Kinh tế - Quản trị
('KT001', 'Nghìn lẻ một đêm', 'Nguyễn Ngọc Tú', 'NXB Phụ nữ', 2020, 'Kinh tế', '9786041000301', 12, 12, 'Sách về khởi nghiệp và kinh doanh', 'https://example.com/nghin-le-mot-dem.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('KT002', 'Quản trị học', 'Stephen Robbins', 'NXB Thống kê', 2018, 'Kinh tế', '9786041000302', 16, 14, 'Giáo trình quản trị doanh nghiệp', 'https://example.com/quan-tri-hoc.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('KT003', 'Marketing căn bản', 'Philip Kotler', 'NXB Lao động', 2017, 'Kinh tế', '9786041000303', 14, 12, 'Sách về marketing cho doanh nghiệp', 'https://example.com/marketing.jpg', 'AVAILABLE', CURDATE(), CURDATE()),

-- Lịch sử - Địa lý
('LS001', 'Lịch sử Việt Nam', 'Trần Trọng Kim', 'NXB Văn học', 1971, 'Lịch sử', '9786041000401', 15, 15, 'Tổng quan lịch sử Việt Nam', 'https://example.com/lich-su-vn.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('LS002', 'Địa lý Việt Nam', 'Nguyễn Văn Hưng', 'NXB Giáo dục', 2020, 'Địa lý', '9786041000402', 12, 12, 'Giáo trình địa lý Việt Nam', 'https://example.com/dia-ly-vn.jpg', 'AVAILABLE', CURDATE(), CURDATE()),

-- Sách thiếu nhi
('TN001', 'Dế mèn phiêu lưu ký', 'Tô Hoài', 'NXB Kim Đồng', 1941, 'Thiếu nhi', '9786041000501', 25, 20, 'Truyện thiếu nhi kinh điển Việt Nam', 'https://example.com/de-men.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('TN002', 'Doraemon - Tập 1', 'Fujiko F. Fujio', 'NXB Kim Đồng', 1969, 'Thiếu nhi', '9786041000502', 30, 25, 'Truyện tranh Doraemon phổ biến', 'https://example.com/doraemon-1.jpg', 'AVAILABLE', CURDATE(), CURDATE()),
('TN003', 'Thỏ bảy màu', 'Nguyễn Nhật Ánh', 'NXB Trẻ', 2010, 'Thiếu nhi', '9786041000503', 20, 18, 'Truyện thiếu nhi của Nguyễn Nhật Ánh', 'https://example.com/tho-bay-mau.jpg', 'AVAILABLE', CURDATE(), CURDATE());

-- ==============================================
-- INSERT READERS DATA
-- ==============================================
INSERT INTO readers (reader_code, full_name, date_of_birth, gender, address, email, phone_number, identity_card, issue_date, expiry_date, status, avatar_url, created_at, updated_at) VALUES
-- Sinh viên
('DG001', 'Nguyễn Văn An', '2003-05-15', 'Nam', '123 Đường Láng, Đống Đa, Hà Nội', 'nguyenvanan@gmail.com', '0987654321', '001203012345', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG002', 'Trần Thị Bình', '2002-08-20', 'Nữ', '45 Giải Phóng, Hai Bà Trưng, Hà Nội', 'tranthibinh@gmail.com', '0976543210', '001202054321', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG003', 'Lê Minh Cường', '2004-03-10', 'Nam', '78 Nguyễn Trãi, Thanh Xuân, Hà Nội', 'leminhcuong@gmail.com', '0965432109', '001204023456', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG004', 'Phạm Thu Dung', '2003-11-25', 'Nữ', '56 Tây Sơn, Đống Đa, Hà Nội', 'phamthudung@gmail.com', '0954321098', '001203067890', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG005', 'Hoàng Văn Em', '2002-01-30', 'Nam', '89 Cầu Giấy, Cầu Giấy, Hà Nội', 'hoangvanem@gmail.com', '0943210987', '001202034567', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),

-- Giáo viên
('DG006', 'Vũ Thị Hoa', '1985-07-15', 'Nữ', '12 Hoàng Quốc Việt, Cầu Giấy, Hà Nội', 'vuthihoa@gmail.com', '0932109876', '001185045678', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG007', 'Đặng Minh Giang', '1990-12-05', 'Nam', '34 Nguyễn Chí Thanh, Đống Đa, Hà Nội', 'dangminhgiang@gmail.com', '0921098765', '001190056789', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),

-- Học sinh
('DG008', 'Bùi Thị Hương', '2008-04-20', 'Nữ', '67 Phạm Văn Đồng, Bắc Từ Liêm, Hà Nội', 'buithihuong@gmail.com', '0910987654', '001208067890', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG009', 'Ngô Văn Khải', '2007-09-10', 'Nam', '23 Xuân Thủy, Cầu Giấy, Hà Nội', 'ngovankhai@gmail.com', '0909876543', '001207078901', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG010', 'Đinh Thị Lan', '2006-06-15', 'Nữ', '45 Trần Duy Hưng, Cầu Giấy, Hà Nội', 'dinhthilan@gmail.com', '0898765432', '001206089012', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),

-- Người đi làm
('DG011', 'Trịnh Văn Long', '1995-02-28', 'Nam', '90 Láng Hạ, Ba Đình, Hà Nội', 'trinhvanlong@gmail.com', '0887654321', '001195090123', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),
('DG012', 'Lý Thị Mai', '1998-10-12', 'Nữ', '15 Nguyễn Thái Học, Ba Đình, Hà Nội', 'lythimai@gmail.com', '0876543210', '001198012345', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE()),

-- Độc giả hết hạn
('DG013', 'Phan Văn Nam', '2001-05-05', 'Nam', '78 Hồ Tùng Mậu, Cầu Giấy, Hà Nội', 'phanvannam@gmail.com', '0865432109', '001201023456', DATE_SUB(CURDATE(), INTERVAL 14 MONTH), DATE_SUB(CURDATE(), INTERVAL 2 MONTH), 'EXPIRED', NULL, CURDATE(), CURDATE()),

-- Độc giả bị khóa
('DG014', 'Võ Thị Oanh', '2003-08-18', 'Nữ', '56 Phố Vọng, Hai Bà Trưng, Hà Nội', 'vothioanh@gmail.com', '0854321098', '001203034567', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'BLOCKED', NULL, CURDATE(), CURDATE()),

-- Độc giá bình thường
('DG015', 'Đỗ Văn Phúc', '2000-12-30', 'Nam', '123 Minh Khai, Hai Bà Trưng, Hà Nội', 'dovanphuc@gmail.com', '0843210987', '001200045678', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NULL, CURDATE(), CURDATE());

-- ==============================================
-- INSERT BORROW TICKETS DATA
-- ==============================================
INSERT INTO borrow_tickets (ticket_code, reader_id, book_id, borrow_date, due_date, return_date, quantity, status, returned_to, created_at, updated_at) VALUES
-- Phiếu đã trả
('MT0000000001', 1, 1, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 6 DAY), DATE_SUB(CURDATE(), INTERVAL 5 DAY), 1, 'RETURNED', 'Thủ thư Lan', CURDATE(), CURDATE()),
('MT0000000002', 2, 3, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 11 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY), 1, 'RETURNED', 'Thủ thư Hương', CURDATE(), CURDATE()),
('MT0000000003', 3, 6, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 16 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY), 1, 'RETURNED', 'Thủ thư Lan', CURDATE(), CURDATE()),

-- Phiếu đang mượn (chưa quá hạn)
('MT0000000004', 1, 5, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 9 DAY), NULL, 1, 'BORROWED', NULL, CURDATE(), CURDATE()),
('MT0000000005', 4, 7, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 11 DAY), NULL, 1, 'BORROWED', NULL, CURDATE(), CURDATE()),
('MT0000000006', 5, 10, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_ADD(CURDATE(), INTERVAL 7 DAY), NULL, 1, 'BORROWED', NULL, CURDATE(), CURDATE()),
('MT0000000007', 6, 11, DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 10 DAY), NULL, 1, 'BORROWED', NULL, CURDATE(), CURDATE()),
('MT0000000008', 7, 14, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY), NULL, 1, 'BORROWED', NULL, CURDATE(), CURDATE()),

-- Phiếu quá hạn (OVERDUE)
('MT0000000009', 2, 8, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 6 DAY), NULL, 1, 'BORROWED', NULL, CURDATE(), CURDATE()),
('MT0000000010', 8, 20, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 11 DAY), NULL, 1, 'BORROWED', NULL, CURDATE(), CURDATE()),
('MT0000000011', 9, 21, DATE_SUB(CURDATE(), INTERVAL 18 DAY), DATE_SUB(CURDATE(), INTERVAL 4 DAY), NULL, 1, 'BORROWED', NULL, CURDATE(), CURDATE()),

-- Phiếu trả trễ (đã trả nhưng quá hạn)
('MT0000000012', 10, 15, DATE_SUB(CURDATE(), INTERVAL 35 DAY), DATE_SUB(CURDATE(), INTERVAL 21 DAY), DATE_SUB(CURDATE(), INTERVAL 18 DAY), 1, 'RETURNED', 'Thủ thư Lan', CURDATE(), CURDATE()),
('MT0000000013', 11, 17, DATE_SUB(CURDATE(), INTERVAL 40 DAY), DATE_SUB(CURDATE(), INTERVAL 26 DAY), DATE_SUB(CURDATE(), INTERVAL 23 DAY), 1, 'RETURNED', 'Thủ thư Hương', CURDATE(), CURDATE());

-- ==============================================
-- INSERT PENALTIES DATA
-- ==============================================
INSERT INTO penalties (borrow_ticket_id, penalty_type, amount, reason, payment_status, payment_date, processed_by, created_at, updated_at) VALUES
-- Phạt trả trễ đã thanh toán
(12, 'OVERDUE', 15000.00, 'Trả sách trễ 3 ngày', 'PAID', DATE_SUB(CURDATE(), INTERVAL 18 DAY), 'Thủ thư Lan', CURDATE(), CURDATE()),
(13, 'OVERDUE', 15000.00, 'Trả sách trễ 3 ngày', 'PAID', DATE_SUB(CURDATE(), INTERVAL 23 DAY), 'Thủ thư Hương', CURDATE(), CURDATE()),

-- Phạt quá hạn chưa thanh toán
(9, 'OVERDUE', 30000.00, 'Trả sách trễ 6 ngày', 'UNPAID', NULL, NULL, CURDATE(), CURDATE()),
(10, 'OVERDUE', 55000.00, 'Trả sách trễ 11 ngày', 'UNPAID', NULL, NULL, CURDATE(), CURDATE()),
(11, 'OVERDUE', 20000.00, 'Trả sách trễ 4 ngày', 'UNPAID', NULL, NULL, CURDATE(), CURDATE());

-- ==============================================
-- VERIFICATION QUERIES
-- ==============================================

-- Kiểm tra số lượng dữ liệu đã insert
SELECT 'Books' as TableName, COUNT(*) as RecordCount FROM books
UNION ALL
SELECT 'Readers', COUNT(*) FROM readers
UNION ALL
SELECT 'BorrowTickets', COUNT(*) FROM borrow_tickets
UNION ALL
SELECT 'Penalties', COUNT(*) FROM penalties;

-- Xem thông tin sách theo danh mục
SELECT category, COUNT(*) as total_books, SUM(total_quantity) as total_quantity
FROM books
GROUP BY category
ORDER BY total_books DESC;

-- Xem trạng thái độc giả
SELECT status, COUNT(*) as reader_count
FROM readers
GROUP BY status;

-- Xem trạng thái phiếu mượn
SELECT status, COUNT(*) as ticket_count
FROM borrow_tickets
GROUP BY status;

-- Xem phiếu mượn quá hạn
SELECT bt.ticket_code, r.full_name, b.title, bt.borrow_date, bt.due_date,
       DATEDIFF(CURDATE(), bt.due_date) as overdue_days
FROM borrow_tickets bt
JOIN readers r ON bt.reader_id = r.id
JOIN books b ON bt.book_id = b.id
WHERE bt.status = 'BORROWED' AND bt.due_date < CURDATE()
ORDER BY overdue_days DESC;