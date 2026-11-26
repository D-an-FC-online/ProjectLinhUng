USE DamSenAudioGuide;
SET NOCOUNT ON;

------------------------------------------------------------
-- 1. Languages (seed vi/en/ko nếu bảng đang trống)
------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM dbo.languages WHERE code = 'vi')
    INSERT INTO dbo.languages (code, name, is_source) VALUES ('vi', N'Tiếng Việt', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.languages WHERE code = 'en')
    INSERT INTO dbo.languages (code, name, is_source) VALUES ('en', N'English', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.languages WHERE code = 'ko')
    INSERT INTO dbo.languages (code, name, is_source) VALUES ('ko', N'한국어', 0);
GO

------------------------------------------------------------
-- 2. Zones
------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM dbo.zones WHERE code = 'ZONES_GAMES')
    INSERT INTO dbo.zones (code, is_active) VALUES ('ZONES_GAMES', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.zones WHERE code = 'ZONES_ANIMALS')
    INSERT INTO dbo.zones (code, is_active) VALUES ('ZONES_ANIMALS', 1);
GO

------------------------------------------------------------
-- 2b. Ràng buộc: toạ độ phải dương (không âm, khác 0)
------------------------------------------------------------
-- Kiểm tra trước: có dòng nào đang vi phạm không (nếu có, phải sửa trước khi ALTER)
-- SELECT * FROM dbo.pois WHERE latitude <= 0 OR longitude <= 0;

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_pois_latitude_positive')
    ALTER TABLE dbo.pois ADD CONSTRAINT CK_pois_latitude_positive  CHECK (latitude  > 0);

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_pois_longitude_positive')
    ALTER TABLE dbo.pois ADD CONSTRAINT CK_pois_longitude_positive CHECK (longitude > 0);
GO

------------------------------------------------------------
-- 3. POIs (toạ độ đã đổi dấu phẩy -> dấu chấm)
--    Không có cột bán kính riêng — theo đúng ERD mục 4.1.4 của PRD,
--    bán kính nhận diện dùng chung 1 hằng số toàn hệ thống (xử lý ở
--    tầng code), không lưu riêng theo từng POI.
------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM dbo.pois WHERE code = 'POI_ROLLER_COASTER')
    INSERT INTO dbo.pois (zone_id, code, latitude, longitude, status)
    SELECT z.id, 'POI_ROLLER_COASTER', 10.763600, 106.633200, 'ACTIVE'
    FROM dbo.zones z WHERE z.code = 'ZONES_GAMES';

IF NOT EXISTS (SELECT 1 FROM dbo.pois WHERE code = 'POI_LOOPING_CARP')
    INSERT INTO dbo.pois (zone_id, code, latitude, longitude, status)
    SELECT z.id, 'POI_LOOPING_CARP', 10.763600, 106.633800, 'ACTIVE'
    FROM dbo.zones z WHERE z.code = 'ZONES_GAMES';

IF NOT EXISTS (SELECT 1 FROM dbo.pois WHERE code = 'POI_AQUARIUM')
    INSERT INTO dbo.pois (zone_id, code, latitude, longitude, status)
    SELECT z.id, 'POI_AQUARIUM', 10.762600, 106.633600, 'ACTIVE'
    FROM dbo.zones z WHERE z.code = 'ZONES_ANIMALS';

IF NOT EXISTS (SELECT 1 FROM dbo.pois WHERE code = 'POI_BIRD_GARDEN')
    INSERT INTO dbo.pois (zone_id, code, latitude, longitude, status)
    SELECT z.id, 'POI_BIRD_GARDEN', 10.762600, 106.632600, 'ACTIVE'
    FROM dbo.zones z WHERE z.code = 'ZONES_ANIMALS';
GO

------------------------------------------------------------
-- 4. Narration versions — bản tiếng Việt (version_no = 1, status mặc định 'DRAFT')
------------------------------------------------------------

-- 4.1 Roller Coaster
DECLARE @body_rc NVARCHAR(MAX) = N'Là trò chơi cảm giác mạnh tại CVVH Đầm Sen (khu B). Roller Coaster, hay còn gọi là "Tàu lượn siêu tốc", là đoàn tàu 24 chỗ ngồi được kéo lên độ cao 24 mét bằng tời xích, sau đó thả trôi xuống với tốc độ cao bằng quán tính. Đường ray được thiết kế với những cú ngoặt đột ngột, sườn dốc, và cả những đoạn lộn ngược, mang lại cảm giác hồi hộp mạnh mẽ cho người chơi.';

IF NOT EXISTS (
    SELECT 1 FROM dbo.narration_versions nv JOIN dbo.pois p ON p.id = nv.poi_id
    WHERE p.code = 'POI_ROLLER_COASTER' AND nv.language_code = 'vi'
)
INSERT INTO dbo.narration_versions (poi_id, language_code, version_no, source_version_id, title, body, status, content_hash)
SELECT p.id, 'vi', 1, NULL, N'Roller Coaster – Tàu lượn siêu tốc', @body_rc, 'DRAFT',
       CONVERT(CHAR(64), HASHBYTES('SHA2_256', @body_rc), 2)
FROM dbo.pois p WHERE p.code = 'POI_ROLLER_COASTER';

-- 4.2 Looping Carp
DECLARE @body_lc NVARCHAR(MAX) = N'Là trò chơi cảm giác mạnh do Công ty Cổ phần Dịch vụ Du lịch Phú Thọ (Phuthotourist) đầu tư tại CVVH Đầm Sen, theo hình thức con lắc (Pendulum). Trò chơi gồm hai con tàu hình cá chép, đong đưa theo trục đứng và có thể đánh vòng đến 360 độ. Người chơi được đưa lên cao hơn 10 mét rồi rơi tự do xuống, bật lên nhiều vòng; đỉnh điểm là khi cặp cá chép nhào lộn ngược trên không, khiến người chơi phải hét lên vì phấn khích. Trò chơi nằm ở khu cảm giác mạnh, gần cổng số 1 đường Lạc Long Quân.';

IF NOT EXISTS (
    SELECT 1 FROM dbo.narration_versions nv JOIN dbo.pois p ON p.id = nv.poi_id
    WHERE p.code = 'POI_LOOPING_CARP' AND nv.language_code = 'vi'
)
INSERT INTO dbo.narration_versions (poi_id, language_code, version_no, source_version_id, title, body, status, content_hash)
SELECT p.id, 'vi', 1, NULL, N'Looping Carp – Trò chơi con lắc cá chép', @body_lc, 'DRAFT',
       CONVERT(CHAR(64), HASHBYTES('SHA2_256', @body_lc), 2)
FROM dbo.pois p WHERE p.code = 'POI_LOOPING_CARP';

-- 4.3 Aquarium
DECLARE @body_aq NVARCHAR(MAX) = N'Ra mắt năm 2013, Thủy cung Đầm Sen đặt tại vườn Nam Tú Thượng Uyển, có hình dáng một chú cua biển và được xây dựng theo tiêu chuẩn kỹ thuật hàng đầu thế giới, trên diện tích hơn 2.000 mét vuông. Bên trong chứa khoảng 2.500 mét khối nước biển lấy từ Vũng Tàu, được lọc tuần hoàn sinh học để duy trì môi trường sống tự nhiên cho hơn 6.000 cá thể. Du khách sẽ được chiêm ngưỡng nhiều loài cá đẹp, lạ mắt qua các mô hình như Hồ cá mập, Thành phố huyền thoại Atlantic, lưu vực sông Mê Kông và sông Amazon, cùng một số loài nguy cấp như Đào Nhung môi đỏ và Đào Nhung Phú Quốc.';

IF NOT EXISTS (
    SELECT 1 FROM dbo.narration_versions nv JOIN dbo.pois p ON p.id = nv.poi_id
    WHERE p.code = 'POI_AQUARIUM' AND nv.language_code = 'vi'
)
INSERT INTO dbo.narration_versions (poi_id, language_code, version_no, source_version_id, title, body, status, content_hash)
SELECT p.id, 'vi', 1, NULL, N'Thủy cung Đầm Sen (Aquarium)', @body_aq, 'DRAFT',
       CONVERT(CHAR(64), HASHBYTES('SHA2_256', @body_aq), 2)
FROM dbo.pois p WHERE p.code = 'POI_AQUARIUM';

-- 4.4 Bird Garden
DECLARE @body_bg NVARCHAR(MAX) = N'Đây là thế giới hoang dã của các loài chim quý tại Vườn chim Đầm Sen. Khu nuôi chim được thiết kế như một chiếc lồng khổng lồ, nơi các loài chim xải cánh bay, kiếm ăn và sinh sống gần như trong môi trường tự nhiên. Bên cạnh các loài phổ biến như cò, chim trĩ đỏ, đà điểu, nơi đây còn nuôi nhiều loài quý hiếm thuộc nhóm 1B như hơn 40 con Giang Sen, 12 con chim cổ rắn, hơn 19 con già đẫy và 6 con Công Việt Nam. Đặc biệt, vườn chim đang nuôi một cặp thiên nga đen — loài thiên nga quý hiếm nổi tiếng chung thủy, không rời nhau nửa bước, được xem là biểu tượng của tình yêu.';

IF NOT EXISTS (
    SELECT 1 FROM dbo.narration_versions nv JOIN dbo.pois p ON p.id = nv.poi_id
    WHERE p.code = 'POI_BIRD_GARDEN' AND nv.language_code = 'vi'
)
INSERT INTO dbo.narration_versions (poi_id, language_code, version_no, source_version_id, title, body, status, content_hash)
SELECT p.id, 'vi', 1, NULL, N'Vườn chim Đầm Sen (Bird Garden)', @body_bg, 'DRAFT',
       CONVERT(CHAR(64), HASHBYTES('SHA2_256', @body_bg), 2)
FROM dbo.pois p WHERE p.code = 'POI_BIRD_GARDEN';
GO

------------------------------------------------------------
-- 5. Glossary
------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM dbo.glossary WHERE term = N'Đầm Sen')
    INSERT INTO dbo.glossary (term, term_type, keep_original, translation_en, translation_ko, tts_alias_vi, tts_alias_en, tts_alias_ko, note)
    VALUES (N'Đầm Sen', 'PLACE', 1, N'Dam Sen Park', N'담센', NULL, NULL, N'담 센',
            N'Tên công viên, giữ nguyên khi dịch; cần phiên âm tiếng Hàn để giọng đọc không sai.');

IF NOT EXISTS (SELECT 1 FROM dbo.glossary WHERE term = N'Roller Coaster')
    INSERT INTO dbo.glossary (term, term_type, keep_original, translation_en, translation_ko, tts_alias_vi, tts_alias_en, tts_alias_ko, note)
    VALUES (N'Roller Coaster', 'RIDE', 1, N'Roller Coaster', N'롤러코스터', NULL, NULL, NULL,
            N'Tên trò chơi, thường giữ nguyên tiếng Anh.');

IF NOT EXISTS (SELECT 1 FROM dbo.glossary WHERE term = N'Looping Carp')
    INSERT INTO dbo.glossary (term, term_type, keep_original, translation_en, translation_ko, tts_alias_vi, tts_alias_en, tts_alias_ko, note)
    VALUES (N'Looping Carp', 'RIDE', 1, N'Looping Carp', N'루핑 카프', NULL, NULL, NULL,
            N'"Carp" = cá chép nhưng là tên riêng của trò chơi nên không dịch nghĩa.');

IF NOT EXISTS (SELECT 1 FROM dbo.glossary WHERE term = N'Aquarium')
    INSERT INTO dbo.glossary (term, term_type, keep_original, translation_en, translation_ko, tts_alias_vi, tts_alias_en, tts_alias_ko, note)
    VALUES (N'Aquarium', 'ZONE', 1, N'Aquarium', N'수족관', NULL, NULL, NULL,
            N'CHƯA CHỐT: có thể dùng "Thủy cung" trong bài tiếng Việt thay vì "Aquarium" — tạm giữ nguyên "Aquarium", cần bạn xác nhận lại.');

IF NOT EXISTS (SELECT 1 FROM dbo.glossary WHERE term = N'Bird Garden')
    INSERT INTO dbo.glossary (term, term_type, keep_original, translation_en, translation_ko, tts_alias_vi, tts_alias_en, tts_alias_ko, note)
    VALUES (N'Bird Garden', 'ZONE', 1, N'Bird Garden', N'버드 가든', NULL, NULL, NULL,
            N'CHƯA CHỐT: có thể dùng "Vườn chim" trong bài tiếng Việt thay vì "Bird Garden" — tạm giữ nguyên "Bird Garden", cần bạn xác nhận lại.');
GO

------------------------------------------------------------
-- 6. Kiểm tra lại sau khi chạy
------------------------------------------------------------
SELECT * FROM dbo.zones;
SELECT * FROM dbo.pois;
SELECT * FROM dbo.narration_versions;
SELECT * FROM dbo.glossary;

-- Xác nhận các ràng buộc toạ độ đã được tạo
SELECT name AS ten_rang_buoc, definition
FROM sys.check_constraints
WHERE name IN ('CK_pois_latitude_positive', 'CK_pois_longitude_positive');
