-- ===== 1. Tạo database =====
IF DB_ID(N'LinhUngGuide') IS NULL CREATE DATABASE LinhUngGuide;
GO

-- ===== 2. Tạo tài khoản đăng nhập cho ứng dụng Java =====
USE master;
GO
IF SUSER_ID(N'linhung_app') IS NULL
    -- Đổi mật khẩu bên dưới (cần chữ hoa, chữ thường, số, ký tự đặc biệt)
    CREATE LOGIN linhung_app WITH PASSWORD = N'12345678', CHECK_POLICY = OFF;
GO

-- ===== 3. Gán tài khoản vào database và cấp quyền =====
USE LinhUngGuide;
GO
IF USER_ID(N'linhung_app') IS NULL
    CREATE USER linhung_app FOR LOGIN linhung_app;
ALTER ROLE db_owner ADD MEMBER linhung_app;
GO

-- ===== 4. Bảng địa điểm tham quan =====
CREATE TABLE places (
    id          BIGINT        IDENTITY(1,1) NOT NULL,   -- tự tăng
    code        VARCHAR(50)   NOT NULL,                 -- mã định danh, không trùng
    name        NVARCHAR(200) NOT NULL,                 -- NVARCHAR để lưu tiếng Việt có dấu
    description NVARCHAR(MAX) NULL,
    address     NVARCHAR(300) NULL,
    latitude    FLOAT         NOT NULL,                 -- FLOAT trong SQL Server = double của Java
    longitude   FLOAT         NOT NULL,
    image_url   VARCHAR(500)  NULL,
    created_at  DATETIME2(6)  NOT NULL,
    updated_at  DATETIME2(6)  NOT NULL,
    CONSTRAINT pk_places PRIMARY KEY (id),
    CONSTRAINT uk_places_code UNIQUE (code),
    CONSTRAINT ck_places_latitude  CHECK (latitude  BETWEEN -90  AND 90),
    CONSTRAINT ck_places_longitude CHECK (longitude BETWEEN -180 AND 180)
);
CREATE INDEX idx_places_lat_lng ON places (latitude, longitude);
GO

-- ===== 5. Bảng nội dung thuyết minh =====
CREATE TABLE narrations (
    id               BIGINT        IDENTITY(1,1) NOT NULL,
    place_id         BIGINT        NOT NULL,
    language         VARCHAR(10)   NOT NULL,            -- tên enum: VI, EN, KO...
    title            NVARCHAR(200) NOT NULL,
    content          NVARCHAR(MAX) NOT NULL,
    audio_url        VARCHAR(500)  NULL,
    duration_seconds INT           NULL,
    created_at       DATETIME2(6)  NOT NULL,
    updated_at       DATETIME2(6)  NOT NULL,
    CONSTRAINT pk_narrations PRIMARY KEY (id),
    -- Mỗi địa điểm chỉ có 1 bản thuyết minh cho mỗi ngôn ngữ
    CONSTRAINT uk_narration_place_language UNIQUE (place_id, language),
    -- Xóa địa điểm thì xóa luôn thuyết minh
    CONSTRAINT fk_narrations_place FOREIGN KEY (place_id)
        REFERENCES places (id) ON DELETE CASCADE,
    CONSTRAINT ck_narrations_duration CHECK (duration_seconds IS NULL OR duration_seconds >= 0)
);
GO