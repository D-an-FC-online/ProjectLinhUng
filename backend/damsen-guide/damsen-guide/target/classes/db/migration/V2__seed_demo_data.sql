INSERT INTO pois (code, internal_name, latitude, longitude, near_radius_m, status, sort_order) VALUES
('P01', N'Hồ Đầm Sen', 10.768000, 106.640000, 35, 'ACTIVE', 1),
('P02', N'Vườn Thượng Uyển', 10.769000, 106.641000, 30, 'ACTIVE', 2),
('P05', N'Điểm đang bảo trì', 10.768100, 106.640100, 30, 'INACTIVE', 3);

INSERT INTO audio_assets (storage_key, mime_type, duration_ms, file_size_bytes, checksum_sha256, status) VALUES
('audio/P01/vi/p01_vi.mp3', 'audio/mpeg', 45000, 1024, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'READY'),
('audio/P01/en/p01_en.mp3', 'audio/mpeg', 42000, 1024, 'a1b2c34298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b111', 'READY'),
('audio/P02/vi/p02_vi.mp3', 'audio/mpeg', 50000, 1024, 'f9e8d74298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b222', 'READY');

INSERT INTO poi_contents (poi_id, language_code, title, body, audio_asset_id) VALUES
(1, 'vi', N'Hồ Đầm Sen', N'Chào mừng bạn đến với Hồ Đầm Sen, trung tâm cảnh quan của công viên.', 1),
(1, 'en', N'Dam Sen Lake', N'Welcome to Dam Sen Lake, the scenic heart of the cultural park.', 2),
(2, 'vi', N'Vườn Thượng Uyển', N'Khu vườn trưng bày nhiều loài hoa quý hiếm theo mùa.', 3);
