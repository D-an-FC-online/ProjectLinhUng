CREATE TABLE pois (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    internal_name NVARCHAR(200) NOT NULL,
    latitude DECIMAL(9,6) NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude DECIMAL(9,6) NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    near_radius_m INT NOT NULL DEFAULT 30 CHECK (near_radius_m BETWEEN 5 AND 200),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE')),
    sort_order INT NOT NULL DEFAULT 0
);

CREATE INDEX IX_pois_lat_lng ON pois(latitude, longitude) INCLUDE (status);

CREATE TABLE audio_assets (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    storage_key VARCHAR(300) NOT NULL,
    mime_type VARCHAR(50) NOT NULL DEFAULT 'audio/mpeg',
    duration_ms INT NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    checksum_sha256 CHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'READY' CHECK (status IN ('READY', 'DELETED'))
);

CREATE TABLE poi_contents (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    poi_id BIGINT NOT NULL REFERENCES pois(id),
    language_code VARCHAR(5) NOT NULL CHECK (language_code IN ('vi', 'en', 'ko')),
    title NVARCHAR(200) NOT NULL,
    body NVARCHAR(MAX) NOT NULL,
    audio_asset_id BIGINT NULL REFERENCES audio_assets(id),
    CONSTRAINT UQ_poi_lang UNIQUE (poi_id, language_code)
);
