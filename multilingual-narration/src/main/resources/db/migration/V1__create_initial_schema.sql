-- =====================================================================
-- V1: Tạo schema ban đầu cho Multilingual Auto Narration System
--
-- Quy tắc Flyway: file migration đã chạy thì KHÔNG ĐƯỢC SỬA (Flyway lưu checksum trong bảng
-- flyway_schema_history và sẽ báo lỗi nếu file bị thay đổi). Muốn đổi schema => tạo file V3__..., V4__...
--
-- Bảng mã utf8mb4: lưu được đầy đủ tiếng Việt, tiếng Nhật, tiếng Hàn, tiếng Trung và emoji.
-- Collation utf8mb4_unicode_ci: so sánh không phân biệt hoa thường và dấu
-- (tìm "van mieu" khớp "Văn Miếu"); dùng được trên cả MySQL 8 và MariaDB.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Bảng languages: danh mục ngôn ngữ hỗ trợ
-- ---------------------------------------------------------------------
CREATE TABLE languages
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code        VARCHAR(10)  NOT NULL COMMENT 'Mã ISO 639-1 / BCP 47, chữ thường: vi, en, ja, ko, zh',
    name        VARCHAR(100) NOT NULL COMMENT 'Tên ngôn ngữ bằng tiếng Việt',
    native_name VARCHAR(100) NULL COMMENT 'Tên ngôn ngữ viết bằng chính ngôn ngữ đó',
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE COMMENT 'FALSE = tạm ngừng hỗ trợ',
    CONSTRAINT pk_languages PRIMARY KEY (id),
    CONSTRAINT uk_languages_code UNIQUE (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Ngôn ngữ được hệ thống hỗ trợ';

-- ---------------------------------------------------------------------
-- Bảng locations: địa điểm tham quan
-- ---------------------------------------------------------------------
CREATE TABLE locations
(
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255)  NOT NULL,
    description TEXT          NULL,
    address     VARCHAR(500)  NULL,
    latitude    DECIMAL(9, 6) NULL COMMENT 'Vĩ độ [-90, 90]',
    longitude   DECIMAL(9, 6) NULL COMMENT 'Kinh độ [-180, 180]',
    image_url   VARCHAR(1000) NULL,
    created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_locations PRIMARY KEY (id),
    CONSTRAINT ck_locations_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT ck_locations_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Địa điểm tham quan';

-- Index phục vụ sắp xếp theo tên và tìm kiếm theo tiền tố tên.
-- (Tìm kiếm dạng LIKE '%tu_khoa%' không tận dụng được B-Tree index; với quy mô đồ án vẫn đủ nhanh.
--  Khi dữ liệu lớn có thể nâng cấp lên FULLTEXT INDEX ... WITH PARSER ngram.)
CREATE INDEX idx_locations_name ON locations (name);

-- ---------------------------------------------------------------------
-- Bảng narrations: nội dung thuyết minh của 1 địa điểm bằng 1 ngôn ngữ
-- ---------------------------------------------------------------------
CREATE TABLE narrations
(
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    location_id BIGINT        NOT NULL,
    language_id BIGINT        NOT NULL,
    title       VARCHAR(255)  NOT NULL,
    content     MEDIUMTEXT    NOT NULL COMMENT 'Nội dung dài, tối đa ~16MB',
    audio_url   VARCHAR(1000) NULL COMMENT 'NULL = chưa có âm thanh',
    status      VARCHAR(20)   NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT | PUBLISHED',
    created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_narrations PRIMARY KEY (id),
    -- Mỗi địa điểm chỉ có tối đa 1 bản thuyết minh cho mỗi ngôn ngữ.
    -- Index (location_id, language_id) này cũng phục vụ luôn truy vấn "WHERE location_id = ?".
    CONSTRAINT uk_narrations_location_language UNIQUE (location_id, language_id),
    -- ON DELETE RESTRICT: database từ chối xóa địa điểm / ngôn ngữ còn được tham chiếu
    CONSTRAINT fk_narrations_location FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE RESTRICT,
    CONSTRAINT fk_narrations_language FOREIGN KEY (language_id) REFERENCES languages (id) ON DELETE RESTRICT,
    CONSTRAINT ck_narrations_status CHECK (status IN ('DRAFT', 'PUBLISHED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Nội dung thuyết minh đa ngôn ngữ';

CREATE INDEX idx_narrations_language_id ON narrations (language_id);
CREATE INDEX idx_narrations_status ON narrations (status);
