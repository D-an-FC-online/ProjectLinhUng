/* ============================================================================
   V1__schema.sql - Lược đồ CSDL theo ERD (docs/ERD_DamSenAudioGuide_v2.drawio)
   Flyway tự chạy file này khi ứng dụng khởi động lần đầu.
   KHÔNG sửa file này sau khi đã chạy; muốn đổi lược đồ thì thêm V3__..., V4__...
   ============================================================================ */

/* ---------- languages ---------- */
CREATE TABLE dbo.languages (
    code        VARCHAR(5)    NOT NULL CONSTRAINT PK_languages PRIMARY KEY,
    name        NVARCHAR(50)  NOT NULL,
    is_source   BIT           NOT NULL CONSTRAINT DF_languages_is_source DEFAULT 0
);
-- Chỉ được có đúng một ngôn ngữ nguồn (tiếng Việt)
CREATE UNIQUE INDEX UX_languages_one_source ON dbo.languages(is_source) WHERE is_source = 1;

/* ---------- zones ---------- */
CREATE TABLE dbo.zones (
    id          BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_zones PRIMARY KEY,
    code        VARCHAR(50)   NOT NULL CONSTRAINT UQ_zones_code UNIQUE,
    name_vi     NVARCHAR(100) NOT NULL,
    name_en     NVARCHAR(100) NULL,
    name_ko     NVARCHAR(100) NULL,
    is_active   BIT           NOT NULL CONSTRAINT DF_zones_is_active DEFAULT 1
);

/* ---------- pois ---------- */
CREATE TABLE dbo.pois (
    id          BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_pois PRIMARY KEY,
    zone_id     BIGINT        NOT NULL CONSTRAINT FK_pois_zone REFERENCES dbo.zones(id),
    code        VARCHAR(50)   NOT NULL CONSTRAINT UQ_pois_code UNIQUE,
    latitude    DECIMAL(9,6)  NOT NULL,
    longitude   DECIMAL(9,6)  NOT NULL,
    radius_m    INT           NOT NULL CONSTRAINT DF_pois_radius DEFAULT 30,
    status      VARCHAR(20)   NOT NULL CONSTRAINT DF_pois_status DEFAULT 'ACTIVE',
    CONSTRAINT CK_pois_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT CK_pois_radius CHECK (radius_m BETWEEN 5 AND 300),
    -- Tọa độ phải nằm trong khung bao quanh khu vực Đầm Sen (Q.11, TP.HCM)
    CONSTRAINT CK_pois_in_damsen CHECK (latitude BETWEEN 10.755000 AND 10.780000
                                    AND longitude BETWEEN 106.625000 AND 106.650000)
);

/* ---------- narration_versions ---------- */
CREATE TABLE dbo.narration_versions (
    id                BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_narration_versions PRIMARY KEY,
    poi_id            BIGINT        NOT NULL CONSTRAINT FK_nv_poi REFERENCES dbo.pois(id),
    language_code     VARCHAR(5)    NOT NULL CONSTRAINT FK_nv_language REFERENCES dbo.languages(code),
    version_no        INT           NOT NULL,
    source_version_id BIGINT        NULL CONSTRAINT FK_nv_source REFERENCES dbo.narration_versions(id),
    title             NVARCHAR(255) NOT NULL,
    body              NVARCHAR(MAX) NOT NULL,
    status            VARCHAR(20)   NOT NULL CONSTRAINT DF_nv_status DEFAULT 'DRAFT',
    content_hash      CHAR(64)      NOT NULL,   -- SHA-256 (hex thường) của UTF-8(title + '\n' + body)
    created_at        DATETIME2(0)  NOT NULL CONSTRAINT DF_nv_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_nv_status CHECK (status IN ('DRAFT', 'APPROVED', 'ARCHIVED')),
    CONSTRAINT UQ_nv_poi_lang_version UNIQUE (poi_id, language_code, version_no),
    -- Đích cho khóa ngoại kép từ publications (bảo đảm phiên bản đúng POI + ngôn ngữ)
    CONSTRAINT UQ_nv_id_poi_lang UNIQUE (id, poi_id, language_code)
);

/* ---------- audio_assets ---------- */
CREATE TABLE dbo.audio_assets (
    id                   BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_audio_assets PRIMARY KEY,
    narration_version_id BIGINT        NOT NULL CONSTRAINT FK_audio_nv REFERENCES dbo.narration_versions(id),
    storage_key          VARCHAR(500)  NOT NULL,          -- tên file trong thư mục media
    status               VARCHAR(20)   NOT NULL CONSTRAINT DF_audio_status DEFAULT 'PENDING',
    source               VARCHAR(10)   NOT NULL CONSTRAINT DF_audio_source DEFAULT 'UPLOAD',
    text_hash            CHAR(64)      NULL,              -- content_hash của văn bản lúc tạo audio
    content_hash         CHAR(64)      NULL,              -- SHA-256 của chính file âm thanh
    created_at           DATETIME2(0)  NOT NULL CONSTRAINT DF_audio_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_audio_status CHECK (status IN ('PENDING', 'READY', 'FAILED')),
    CONSTRAINT CK_audio_source CHECK (source IN ('TTS', 'UPLOAD')),
    CONSTRAINT UQ_audio_id_nv UNIQUE (id, narration_version_id)
);

/* ---------- publications (con trỏ xuất bản) ---------- */
CREATE TABLE dbo.publications (
    language_code        VARCHAR(5)   NOT NULL CONSTRAINT FK_pub_language REFERENCES dbo.languages(code),
    poi_id               BIGINT       NOT NULL CONSTRAINT FK_pub_poi REFERENCES dbo.pois(id),
    narration_version_id BIGINT       NOT NULL CONSTRAINT UQ_pub_nv UNIQUE,
    audio_asset_id       BIGINT       NULL,
    published_at         DATETIME2(0) NOT NULL CONSTRAINT DF_pub_published DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_publications PRIMARY KEY (language_code, poi_id),
    -- Phiên bản xuất bản phải đúng POI và đúng ngôn ngữ của dòng publication
    CONSTRAINT FK_pub_nv FOREIGN KEY (narration_version_id, poi_id, language_code)
        REFERENCES dbo.narration_versions(id, poi_id, language_code),
    -- FK kép: audio phải thuộc đúng phiên bản đang xuất bản
    CONSTRAINT FK_pub_audio FOREIGN KEY (audio_asset_id, narration_version_id)
        REFERENCES dbo.audio_assets(id, narration_version_id)
);
-- Mỗi file audio chỉ được một publication dùng (bội số 0..1 - 0..1)
CREATE UNIQUE INDEX UX_pub_audio ON dbo.publications(audio_asset_id) WHERE audio_asset_id IS NOT NULL;

/* ---------- processing_jobs ---------- */
CREATE TABLE dbo.processing_jobs (
    id                BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_processing_jobs PRIMARY KEY,
    job_type          VARCHAR(20)   NOT NULL,
    status            VARCHAR(20)   NOT NULL CONSTRAINT DF_jobs_status DEFAULT 'QUEUED',
    target_version_id BIGINT        NOT NULL CONSTRAINT FK_jobs_target REFERENCES dbo.narration_versions(id),
    target_language   VARCHAR(5)    NULL CONSTRAINT FK_jobs_language REFERENCES dbo.languages(code),
    result_version_id BIGINT        NULL CONSTRAINT FK_jobs_result REFERENCES dbo.narration_versions(id),
    created_at        DATETIME2(0)  NOT NULL CONSTRAINT DF_jobs_created DEFAULT SYSUTCDATETIME(),
    started_at        DATETIME2(0)  NULL,
    finished_at       DATETIME2(0)  NULL,
    error_message     NVARCHAR(MAX) NULL,
    retry_count       INT           NOT NULL CONSTRAINT DF_jobs_retry DEFAULT 0,
    CONSTRAINT CK_jobs_type   CHECK (job_type IN ('TRANSLATE', 'TTS')),
    CONSTRAINT CK_jobs_status CHECK (status IN ('QUEUED', 'RUNNING', 'SUCCEEDED', 'FAILED', 'CANCELLED')),
    -- Job dịch bắt buộc phải có ngôn ngữ đích
    CONSTRAINT CK_jobs_translate_lang CHECK (job_type <> 'TRANSLATE' OR target_language IS NOT NULL)
);
CREATE INDEX IX_jobs_status ON dbo.processing_jobs(status, created_at);

/* ---------- glossary ---------- */
CREATE TABLE dbo.glossary (
    id             BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_glossary PRIMARY KEY,
    term           NVARCHAR(255) NOT NULL CONSTRAINT UQ_glossary_term UNIQUE,
    term_type      VARCHAR(20)   NOT NULL,
    keep_original  BIT           NOT NULL CONSTRAINT DF_glossary_keep DEFAULT 0,
    translation_en NVARCHAR(255) NULL,
    translation_ko NVARCHAR(255) NULL,
    tts_alias_vi   NVARCHAR(255) NULL,
    tts_alias_en   NVARCHAR(255) NULL,
    tts_alias_ko   NVARCHAR(255) NULL,
    note           NVARCHAR(500) NULL,
    CONSTRAINT CK_glossary_type CHECK (term_type IN ('PLACE', 'RIDE', 'ZONE', 'PERSON', 'ANIMAL', 'ORGANIZATION', 'OTHER'))
);

/* ---------- admin_users (Story 2.5 - đăng nhập quản trị) ---------- */
CREATE TABLE dbo.admin_users (
    id            BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_admin_users PRIMARY KEY,
    username      VARCHAR(50)   NOT NULL CONSTRAINT UQ_admin_users_username UNIQUE,
    password_hash VARCHAR(100)  NOT NULL,   -- BCrypt
    role          VARCHAR(20)   NOT NULL CONSTRAINT DF_admin_users_role DEFAULT 'ADMIN',
    is_active     BIT           NOT NULL CONSTRAINT DF_admin_users_active DEFAULT 1,
    created_at    DATETIME2(0)  NOT NULL CONSTRAINT DF_admin_users_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_admin_users_role CHECK (role IN ('ADMIN'))
);
