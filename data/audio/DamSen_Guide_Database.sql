/* =====================================================================================
   CƠ SỞ DỮ LIỆU : damsen_guide
   HỆ THỐNG      : Thuyết minh đa ngôn ngữ theo vị trí tại Công viên Văn hóa Đầm Sen
   NGUỒN THIẾT KẾ: Báo cáo đồ án "Phân tích và thiết kế hệ thống – Thuyết minh đa ngôn ngữ theo vị trí
                   tại Công viên Văn hóa Đầm Sen" (học phần Công nghệ phần mềm, Trường ĐH Sài Gòn):
                   - Chương 3, mục 3.5  Thiết kế dữ liệu (Bảng 3.39 – 3.52): bảng, cột, kiểu dữ liệu,
                     ràng buộc toàn vẹn, chỉ mục, dữ liệu khởi tạo và dữ liệu mẫu;
                   - Mục 3.1 (trạng thái, quy tắc nghiệp vụ BR01 – BR40) và mục 3.2 (Use Case UC01 – UC14);
                   - Mục 3.8.3 (docker/db-init.sql): tên CSDL, collation, tài khoản ứng dụng.
   HỆ QUẢN TRỊ   : Microsoft SQL Server 2016 SP1 trở lên (báo cáo dùng SQL Server 2022 Developer).

   CẤU TRÚC FILE (đánh số theo thứ tự chạy; có thể tách thành các migration Flyway nếu cần)
     PHẦN 0  Thiết lập phiên làm việc, tạo CSDL damsen_guide (collation Vietnamese_100_CI_AS)
     PHẦN 1  Lược đồ: 12 bảng, khóa, ràng buộc CHECK/UNIQUE, chỉ mục      (tương ứng V1__schema.sql)
     PHẦN 2  Dữ liệu tham chiếu: 3 ngôn ngữ vi, en, ko                    (tương ứng V2__reference_data.sql)
     PHẦN 3  View và hàm tiện ích (tùy chọn): suy ra các trạng thái "tính khi truy vấn" trong báo cáo
     PHẦN 4  Dữ liệu mẫu cho demo và kiểm thử: 2 khu vực, 5 điểm P01–P05 (tùy chọn)
                                                                          (tương ứng db/demo/V100__demo_data.sql)
     PHẦN 5  Truy vấn kiểm tra nhanh sau khi cài
     PHẦN 6  (Tùy chọn, đang tắt) Tạo tài khoản ứng dụng damsen_app
     PHẦN 7  (Tùy chọn, đang tắt) Kịch bản thử các ràng buộc ở tầng dữ liệu
     PHẦN 8  (Tùy chọn, đang tắt) Xóa CSDL để làm lại từ đầu

   CÁCH CHẠY
     * SSMS hoặc Azure Data Studio: mở file này (lưu UTF-8 có BOM) rồi bấm Execute (F5).
     * sqlcmd : sqlcmd -S localhost -U sa -P "<mat_khau_sa>" -C -I -f 65001 -i DamSen_Guide_Database.sql
                (-I bật QUOTED_IDENTIFIER, -f 65001 đọc file UTF-8 để giữ đúng tiếng Việt và tiếng Hàn).
                Nếu sqlcmd báo lỗi cú pháp ngay ở ký tự đầu file do dấu BOM, hãy chạy bằng SSMS hoặc Azure Data
                Studio, hoặc lưu lại file ở dạng UTF-8 không BOM.
     * Có thể chạy lại nhiều lần: script chỉ tạo đối tượng còn thiếu và không nạp chồng dữ liệu.

   LƯU Ý
     1. Quy ước chung của báo cáo: khóa chính BIGINT IDENTITY; thời điểm DATETIME2 lưu theo UTC;
        văn bản hiển thị dùng NVARCHAR; mã trạng thái dùng VARCHAR kèm CHECK; khóa ngoại để mặc định
        NO ACTION (Service xóa bản ghi con theo thứ tự, tránh xóa dây chuyền ngoài ý muốn).
     2. Những mục KHÔNG có trong báo cáo được đánh dấu "[BỔ SUNG]" (2 khóa ngoại ghép bảo vệ con trỏ
        xuất bản, một số chỉ mục hỗ trợ khóa ngoại, các view/hàm ở PHẦN 3). Xóa chúng không ảnh hưởng
        phần còn lại của lược đồ.
     3. Tài khoản quản trị mẫu ở PHẦN 4 (admin / Admin@12345) CHỈ DÙNG CHO DEMO. Hãy đổi mật khẩu hoặc
        xóa tài khoản này khi triển khai thật. Báo cáo (mục 3.4.4) tạo quản trị viên đầu tiên bằng
        ApplicationRunner từ biến môi trường khi bảng admin_users rỗng; PHẦN 4 chỉ tạo tài khoản mẫu
        nếu bảng đang rỗng, và dùng lại tài khoản sẵn có nếu ứng dụng đã tạo trước đó.
     4. CSDL chỉ lưu siêu dữ liệu âm thanh (storage_key, thời lượng, ...). Tệp MP3 nằm trên volume
        media-data của ứng dụng nên các bản ghi âm thanh mẫu chưa có tệp thật kèm theo.
   ===================================================================================== */


/* =====================================================================================
   PHẦN 0. THIẾT LẬP PHIÊN LÀM VIỆC VÀ TẠO CƠ SỞ DỮ LIỆU
   ===================================================================================== */

-- Các tùy chọn SET bắt buộc để tạo và ghi dữ liệu vào bảng có chỉ mục lọc (filtered index).
-- (sqlcmd mặc định tắt QUOTED_IDENTIFIER nên đặt lại tường minh ở đây.)
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET QUOTED_IDENTIFIER ON;
SET NUMERIC_ROUNDABORT OFF;
SET NOCOUNT ON;
SET NOEXEC OFF;
GO

USE master;
GO

-- Giống docker/db-init.sql của báo cáo: chạy lại nhiều lần không gây lỗi.
IF DB_ID(N'damsen_guide') IS NULL
BEGIN
    PRINT N'Đang tạo cơ sở dữ liệu damsen_guide (collation Vietnamese_100_CI_AS)...';
    CREATE DATABASE damsen_guide COLLATE Vietnamese_100_CI_AS;
END
ELSE
    PRINT N'Cơ sở dữ liệu damsen_guide đã tồn tại - giữ nguyên.';
GO

USE damsen_guide;
GO

-- Chốt chặn an toàn: nếu CREATE DATABASE hoặc USE ở trên thất bại (ví dụ lỗi 1807 không khóa được CSDL model),
-- phiên làm việc vẫn đang ở CSDL khác (thường là master). Khi đó dừng toàn bộ phần còn lại để không tạo nhầm
-- bảng vào CSDL hệ thống. SET NOEXEC ON làm các batch sau chỉ biên dịch chứ không thực thi; cuối file có SET NOEXEC OFF.
IF DB_NAME() <> N'damsen_guide'
BEGIN
    PRINT N'DỪNG: không vào được CSDL damsen_guide (xem lỗi đỏ phía trên). Phần còn lại không được chạy để tránh tạo nhầm đối tượng vào CSDL ' + DB_NAME() + N'.';
    SET NOEXEC ON;
END;
GO


/* =====================================================================================
   PHẦN 1. LƯỢC ĐỒ DỮ LIỆU (Bảng 3.39 – 3.51)

   Bảng                       Mục đích                                                   UC chính
   -------------------------  ---------------------------------------------------------  ----------------
   admin_users                Tài khoản quản trị viên, trạng thái khóa                    UC06
   languages                  Danh mục ngôn ngữ, ngôn ngữ nguồn, giọng đọc TTS            UC01, UC09, UC11
   zones, zone_translations   Khu vực công viên và tên khu vực theo ngôn ngữ (nhập tay)   UC03, UC07
   pois                       Điểm thuyết minh: mã, tọa độ, bán kính "đang ở gần"         UC02, UC03, UC07
   narration_versions         Các phiên bản nội dung theo điểm và ngôn ngữ                UC04, UC08–UC10, UC12
   audio_assets               Âm thanh của từng phiên bản                                 UC05, UC11, UC12
   publications               Con trỏ xuất bản: (điểm, ngôn ngữ) -> phiên bản + âm thanh  UC02–UC05, UC12
   processing_jobs            Tác vụ nền: dịch và tạo âm thanh                            UC09, UC11, UC13
   glossary_terms,            Thuật ngữ, cách dịch, cách đọc theo ngôn ngữ                UC09, UC11, UC14
   glossary_translations
   audit_logs                 Nhật ký thao tác quản trị quan trọng                        UC07, UC08, UC10–UC13

   Thứ tự tạo bảng tuân theo phụ thuộc khóa ngoại.
   ===================================================================================== */

-- ------------------------------------------------------------------------------------
-- 1.1  admin_users  (Bảng 3.41; UC06; BR02, BR03)
-- ------------------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.admin_users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.admin_users (
        id                 BIGINT        IDENTITY(1,1) NOT NULL,                                          -- Định danh
        username           NVARCHAR(50)  NOT NULL,                                                        -- Tên đăng nhập
        password_hash      VARCHAR(100)  NOT NULL,                                                        -- Mật khẩu băm BCrypt (BR03)
        display_name       NVARCHAR(100) NOT NULL,                                                        -- Tên hiển thị trong nhật ký
        is_active          BIT           NOT NULL CONSTRAINT DF_admin_users_is_active DEFAULT (1),        -- Tài khoản còn hiệu lực
        failed_login_count INT           NOT NULL CONSTRAINT DF_admin_users_failed_count DEFAULT (0),     -- Số lần sai liên tiếp (5 lần -> khóa 15 phút)
        locked_until       DATETIME2     NULL,                                                            -- Khóa tạm tới thời điểm này (UTC)
        last_login_at      DATETIME2     NULL,                                                            -- Lần đăng nhập gần nhất (UTC)
        created_at         DATETIME2     NOT NULL CONSTRAINT DF_admin_users_created_at DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT PK_admin_users PRIMARY KEY (id),
        CONSTRAINT UQ_admin_users_username UNIQUE (username)
    );
END;
GO

-- ------------------------------------------------------------------------------------
-- 1.2  languages  (Bảng 3.42; UC01, UC09, UC11; BR16)
-- ------------------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.languages', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.languages (
        code       VARCHAR(5)   NOT NULL,                                                       -- Mã ngôn ngữ: vi, en, ko
        name       NVARCHAR(50) NOT NULL,                                                       -- Tên hiển thị bằng chính ngôn ngữ đó
        is_source  BIT          NOT NULL CONSTRAINT DF_languages_is_source DEFAULT (0),         -- Ngôn ngữ nguồn (vi); chỉ một dòng = 1
        is_enabled BIT          NOT NULL CONSTRAINT DF_languages_is_enabled DEFAULT (1),        -- Ngôn ngữ đang bật
        tts_voice  VARCHAR(100) NULL,                                                           -- Giọng đọc TTS; NULL thì không tạo được âm thanh (UC11 E2)
        sort_order INT          NOT NULL CONSTRAINT DF_languages_sort_order DEFAULT (0),        -- Thứ tự hiển thị
        CONSTRAINT PK_languages PRIMARY KEY (code)
    );

    -- "Chỉ một dòng = 1" (Bảng 3.42): tối đa một ngôn ngữ nguồn.
    CREATE UNIQUE NONCLUSTERED INDEX UX_languages_one_source ON dbo.languages (is_source) WHERE is_source = 1;
END;
GO

-- ------------------------------------------------------------------------------------
-- 1.3  zones và zone_translations  (Bảng 3.43; UC03, UC07)
-- ------------------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.zones', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.zones (
        id         BIGINT      IDENTITY(1,1) NOT NULL,                                          -- Định danh khu vực
        code       VARCHAR(30) NOT NULL,                                                        -- Mã khu vực
        sort_order INT         NOT NULL CONSTRAINT DF_zones_sort_order DEFAULT (0),             -- Thứ tự hiển thị
        is_active  BIT         NOT NULL CONSTRAINT DF_zones_is_active DEFAULT (1),              -- Khu vực đang bật
        CONSTRAINT PK_zones PRIMARY KEY (id),
        CONSTRAINT UQ_zones_code UNIQUE (code)
    );
END;
GO

IF OBJECT_ID(N'dbo.zone_translations', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.zone_translations (
        zone_id       BIGINT        NOT NULL,
        language_code VARCHAR(5)    NOT NULL,
        name          NVARCHAR(150) NOT NULL,                                                   -- Tên khu vực theo ngôn ngữ (nhập tay)
        CONSTRAINT PK_zone_translations PRIMARY KEY (zone_id, language_code),
        CONSTRAINT FK_zone_translations_zone     FOREIGN KEY (zone_id)       REFERENCES dbo.zones (id),
        CONSTRAINT FK_zone_translations_language FOREIGN KEY (language_code) REFERENCES dbo.languages (code)
    );
END;
GO

-- ------------------------------------------------------------------------------------
-- 1.4  pois  (Bảng 3.44; UC02, UC03, UC07; BR15, BR27, BR38, BR39)
-- ------------------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.pois', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pois (
        id                BIGINT        IDENTITY(1,1) NOT NULL,                                         -- Định danh điểm
        code              VARCHAR(30)   NOT NULL,                                                       -- Mã điểm (dùng trong khóa lưu tệp và giao diện quản trị)
        zone_id           BIGINT        NULL,                                                           -- Khu vực; NULL = chưa thuộc khu vực nào
        internal_name     NVARCHAR(200) NOT NULL,                                                       -- Tên nội bộ cho quản trị (tên hiển thị lấy từ tiêu đề phiên bản đang xuất bản)
        latitude          DECIMAL(9,6)  NOT NULL,                                                       -- Vĩ độ WGS 84, 6 chữ số thập phân (độ phân giải dưới 0,2 m)
        longitude         DECIMAL(9,6)  NOT NULL,                                                       -- Kinh độ WGS 84
        near_radius_m     INT           NOT NULL CONSTRAINT DF_pois_near_radius DEFAULT (30),           -- Bán kính gắn nhãn "đang ở gần" (BR15)
        status            VARCHAR(20)   NOT NULL CONSTRAINT DF_pois_status DEFAULT ('DRAFT'),           -- DRAFT | ACTIVE | INACTIVE
        sort_order        INT           NOT NULL CONSTRAINT DF_pois_sort_order DEFAULT (0),             -- Thứ tự trong khu vực, dùng khi khoảng cách bằng nhau (BR09)
        image_storage_key VARCHAR(300)  NULL,                                                           -- Khóa tệp ảnh minh họa (tùy chọn)
        ever_published    BIT           NOT NULL CONSTRAINT DF_pois_ever_published DEFAULT (0),         -- Đã từng có con trỏ xuất bản; dùng cho BR39
        row_version       ROWVERSION    NOT NULL,                                                       -- Kiểm soát đồng thời lạc quan
        created_at        DATETIME2     NOT NULL CONSTRAINT DF_pois_created_at DEFAULT (SYSUTCDATETIME()),
        updated_at        DATETIME2     NOT NULL CONSTRAINT DF_pois_updated_at DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT PK_pois PRIMARY KEY (id),
        CONSTRAINT UQ_pois_code UNIQUE (code),
        CONSTRAINT FK_pois_zone FOREIGN KEY (zone_id) REFERENCES dbo.zones (id),
        CONSTRAINT CK_pois_latitude   CHECK (latitude  BETWEEN -90  AND 90),
        CONSTRAINT CK_pois_longitude  CHECK (longitude BETWEEN -180 AND 180),
        CONSTRAINT CK_pois_near_radius CHECK (near_radius_m BETWEEN 5 AND 200),
        CONSTRAINT CK_pois_status     CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'))
    );

    -- Truy vấn khung tọa độ cho UC02 (Bảng 3.51)
    CREATE NONCLUSTERED INDEX IX_pois_lat_lng ON dbo.pois (latitude, longitude) INCLUDE (status);
    -- [BỔ SUNG] Hỗ trợ khóa ngoại zone_id (lọc điểm theo khu vực, UC03)
    CREATE NONCLUSTERED INDEX IX_pois_zone ON dbo.pois (zone_id);
END;
GO

-- ------------------------------------------------------------------------------------
-- 1.5  narration_versions  (Bảng 3.45; UC04, UC08–UC10, UC12; BR17–BR21)
--      Mỗi lần soạn hoặc dịch tạo một phiên bản; phiên bản nào đang phục vụ khách do bảng publications quyết định.
-- ------------------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.narration_versions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.narration_versions (
        id                  BIGINT        IDENTITY(1,1) NOT NULL,                                        -- Định danh phiên bản
        poi_id              BIGINT        NOT NULL,
        language_code       VARCHAR(5)    NOT NULL,
        version_no          INT           NOT NULL,                                                      -- Số thứ tự phiên bản trong cặp (điểm, ngôn ngữ)
        source_version_id   BIGINT        NULL,                                                          -- Bản tiếng Việt nguồn; NULL với bản tiếng Việt; bắt buộc với en, ko (CK_nv_source)
        based_on_version_id BIGINT        NULL,                                                          -- Bản được sao chép để tạo bản nháp này
        title               NVARCHAR(200) NOT NULL,                                                      -- Tiêu đề
        body                NVARCHAR(MAX) NOT NULL,                                                      -- Nội dung (giới hạn độ dài ở Service)
        text_origin         VARCHAR(20)   NOT NULL,                                                      -- SOURCE | MACHINE | HUMAN_EDITED | HUMAN
        machine_title       NVARCHAR(200) NULL,                                                          -- Kết quả dịch máy nguyên gốc, không bao giờ bị sửa
        machine_body        NVARCHAR(MAX) NULL,
        qa_warnings         NVARCHAR(MAX) NULL,                                                          -- Cảnh báo kiểm tra tự động (JSON)
        status              VARCHAR(20)   NOT NULL CONSTRAINT DF_nv_status DEFAULT ('DRAFT'),            -- DRAFT | APPROVED | PUBLISHED | ARCHIVED | DISCARDED
        content_hash        CHAR(64)      NOT NULL,                                                      -- SHA-256 của tiêu đề và nội dung; so với text_hash của âm thanh (BR30)
        approved_by         BIGINT        NULL,                                                          -- Người duyệt
        approved_at         DATETIME2     NULL,                                                          -- Thời điểm duyệt
        created_by          BIGINT        NOT NULL,
        created_at          DATETIME2     NOT NULL CONSTRAINT DF_nv_created_at DEFAULT (SYSUTCDATETIME()),
        updated_at          DATETIME2     NOT NULL CONSTRAINT DF_nv_updated_at DEFAULT (SYSUTCDATETIME()),
        row_version         ROWVERSION    NOT NULL,                                                      -- Kiểm soát đồng thời khi sửa nháp
        CONSTRAINT PK_narration_versions PRIMARY KEY (id),
        CONSTRAINT UQ_nv_version UNIQUE (poi_id, language_code, version_no),
        CONSTRAINT UQ_nv_id_poi_lang UNIQUE (id, poi_id, language_code),                                 -- [BỔ SUNG] làm đích cho khóa ngoại ghép của publications
        CONSTRAINT FK_nv_poi                FOREIGN KEY (poi_id)              REFERENCES dbo.pois (id),
        CONSTRAINT FK_nv_language           FOREIGN KEY (language_code)       REFERENCES dbo.languages (code),
        CONSTRAINT FK_nv_source_version     FOREIGN KEY (source_version_id)   REFERENCES dbo.narration_versions (id),
        CONSTRAINT FK_nv_based_on_version   FOREIGN KEY (based_on_version_id) REFERENCES dbo.narration_versions (id),
        CONSTRAINT FK_nv_approved_by        FOREIGN KEY (approved_by)         REFERENCES dbo.admin_users (id),
        CONSTRAINT FK_nv_created_by         FOREIGN KEY (created_by)          REFERENCES dbo.admin_users (id),
        CONSTRAINT CK_nv_text_origin CHECK (text_origin IN ('SOURCE', 'MACHINE', 'HUMAN_EDITED', 'HUMAN')),
        CONSTRAINT CK_nv_status      CHECK (status IN ('DRAFT', 'APPROVED', 'PUBLISHED', 'ARCHIVED', 'DISCARDED')),
        -- BR17: bản tiếng Việt không có nguồn; bản en, ko bắt buộc có bản tiếng Việt nguồn.
        -- ('vi' khớp cấu hình languages.is_source)
        CONSTRAINT CK_nv_source CHECK (
               (language_code = 'vi' AND source_version_id IS NULL)
            OR (language_code <> 'vi' AND source_version_id IS NOT NULL))
    );

    -- BR18: mỗi cặp (điểm, ngôn ngữ) có tối đa một bản nháp mở.
    CREATE UNIQUE NONCLUSTERED INDEX UX_nv_one_draft ON dbo.narration_versions (poi_id, language_code) WHERE status = 'DRAFT';
    -- BR25: tầng dữ liệu chặn hai bản đang xuất bản cho cùng cặp (điểm, ngôn ngữ).
    CREATE UNIQUE NONCLUSTERED INDEX UX_nv_one_published ON dbo.narration_versions (poi_id, language_code) WHERE status = 'PUBLISHED';
    -- Màn hình quản trị nội dung (Bảng 3.51)
    CREATE NONCLUSTERED INDEX IX_nv_poi_lang ON dbo.narration_versions (poi_id, language_code, status);
    -- [BỔ SUNG] Hỗ trợ khóa ngoại source_version_id (tính trạng thái "cần cập nhật")
    CREATE NONCLUSTERED INDEX IX_nv_source_version ON dbo.narration_versions (source_version_id) WHERE source_version_id IS NOT NULL;
END;
GO

-- ------------------------------------------------------------------------------------
-- 1.6  audio_assets  (Bảng 3.46; UC05, UC11, UC12; BR30–BR32)
--      Tệp MP3 lưu ngoài CSDL (volume media-data); CSDL chỉ lưu storage_key và siêu dữ liệu.
-- ------------------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.audio_assets', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.audio_assets (
        id                   BIGINT         IDENTITY(1,1) NOT NULL,                                       -- Mã âm thanh, xuất hiện trong đường dẫn phát
        narration_version_id BIGINT         NOT NULL,                                                     -- Phiên bản được đọc
        origin               VARCHAR(20)    NOT NULL,                                                     -- TTS | UPLOAD
        status               VARCHAR(20)    NOT NULL CONSTRAINT DF_audio_status DEFAULT ('PENDING'),      -- PENDING | READY | FAILED | DELETED
        voice_name           VARCHAR(100)   NULL,                                                         -- Giọng đọc đã dùng (TTS)
        text_hash            CHAR(64)       NOT NULL,                                                     -- Băm của văn bản được đọc (BR30)
        storage_key          VARCHAR(300)   NULL,                                                         -- Đường dẫn tương đối trong kho tệp; bắt buộc khi READY
        mime_type            VARCHAR(50)    NULL,                                                         -- audio/mpeg
        duration_ms          INT            NULL,                                                         -- Thời lượng
        file_size_bytes      BIGINT         NULL,                                                         -- Kích thước tệp
        checksum_sha256      CHAR(64)       NULL,                                                         -- Phát hiện tệp hỏng
        error_message        NVARCHAR(1000) NULL,                                                         -- Lỗi đã làm sạch
        created_by           BIGINT         NOT NULL,
        created_at           DATETIME2      NOT NULL CONSTRAINT DF_audio_created_at DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT PK_audio_assets PRIMARY KEY (id),
        CONSTRAINT UQ_audio_id_version UNIQUE (id, narration_version_id),                                 -- [BỔ SUNG] làm đích cho khóa ngoại ghép của publications
        CONSTRAINT FK_audio_version    FOREIGN KEY (narration_version_id) REFERENCES dbo.narration_versions (id),
        CONSTRAINT FK_audio_created_by FOREIGN KEY (created_by)           REFERENCES dbo.admin_users (id),
        CONSTRAINT CK_audio_origin CHECK (origin IN ('TTS', 'UPLOAD')),
        CONSTRAINT CK_audio_status CHECK (status IN ('PENDING', 'READY', 'FAILED', 'DELETED')),
        -- Không có âm thanh READY thiếu tệp (Bảng 3.51)
        CONSTRAINT CK_audio_ready  CHECK (status <> 'READY' OR storage_key IS NOT NULL)
    );

    -- [BỔ SUNG] Hỗ trợ khóa ngoại narration_version_id (liệt kê âm thanh của một phiên bản)
    CREATE NONCLUSTERED INDEX IX_audio_version ON dbo.audio_assets (narration_version_id, status);
END;
GO

-- ------------------------------------------------------------------------------------
-- 1.7  publications  (Bảng 3.47; UC02–UC05, UC12; BR23–BR28)
--      Con trỏ xuất bản: nguồn duy nhất cho API công khai.
-- ------------------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.publications', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.publications (
        poi_id               BIGINT    NOT NULL,
        language_code        VARCHAR(5) NOT NULL,
        narration_version_id BIGINT    NOT NULL,                                                         -- Phiên bản đang phục vụ khách
        audio_asset_id       BIGINT    NULL,                                                             -- Âm thanh đang phục vụ; NULL = chỉ văn bản (BR31)
        published_by         BIGINT    NOT NULL,
        published_at         DATETIME2 NOT NULL CONSTRAINT DF_publications_published_at DEFAULT (SYSUTCDATETIME()),
        -- BR25: khóa chính ghép bảo đảm tối đa một con trỏ cho mỗi cặp (điểm, ngôn ngữ).
        CONSTRAINT PK_publications PRIMARY KEY (poi_id, language_code),
        CONSTRAINT UQ_publications_version UNIQUE (narration_version_id),
        CONSTRAINT FK_publications_poi      FOREIGN KEY (poi_id)               REFERENCES dbo.pois (id),
        CONSTRAINT FK_publications_language FOREIGN KEY (language_code)        REFERENCES dbo.languages (code),
        CONSTRAINT FK_publications_version  FOREIGN KEY (narration_version_id) REFERENCES dbo.narration_versions (id),
        CONSTRAINT FK_publications_audio    FOREIGN KEY (audio_asset_id)       REFERENCES dbo.audio_assets (id),
        CONSTRAINT FK_publications_admin    FOREIGN KEY (published_by)         REFERENCES dbo.admin_users (id),
        -- [BỔ SUNG] Báo cáo giao cho Service bảo đảm "phiên bản thuộc đúng điểm và ngôn ngữ" và "âm thanh
        -- gắn với đúng phiên bản"; hai khóa ngoại ghép dưới đây cho CSDL tự kiểm tra thêm một lớp.
        CONSTRAINT FK_publications_version_match FOREIGN KEY (narration_version_id, poi_id, language_code)
            REFERENCES dbo.narration_versions (id, poi_id, language_code),
        CONSTRAINT FK_publications_audio_match   FOREIGN KEY (audio_asset_id, narration_version_id)
            REFERENCES dbo.audio_assets (id, narration_version_id)
    );
END;
GO

-- ------------------------------------------------------------------------------------
-- 1.8  processing_jobs  (Bảng 3.48; UC09, UC11, UC13; BR34, BR35)
--      Tác vụ nền: dịch (TRANSLATE) và tạo âm thanh (TTS). dedup_key = loại:target_version_id[:ngôn ngữ đích],
--      ví dụ 'TRANSLATE:7:ko' hoặc 'TTS:12'.
-- ------------------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.processing_jobs', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.processing_jobs (
        id                 BIGINT         IDENTITY(1,1) NOT NULL,
        job_type           VARCHAR(20)    NOT NULL,                                                      -- TRANSLATE | TTS
        status             VARCHAR(20)    NOT NULL CONSTRAINT DF_jobs_status DEFAULT ('QUEUED'),         -- QUEUED | RUNNING | SUCCEEDED | FAILED | CANCELLED
        target_version_id  BIGINT         NOT NULL,                                                      -- Bản nguồn (TRANSLATE) hoặc bản cần đọc (TTS)
        target_language    VARCHAR(5)     NULL,                                                          -- Ngôn ngữ đích (TRANSLATE)
        audio_asset_id     BIGINT         NULL,                                                          -- Âm thanh được tạo (TTS)
        result_version_id  BIGINT         NULL,                                                          -- Bản nháp kết quả (TRANSLATE)
        dedup_key          VARCHAR(80)    NOT NULL,                                                      -- Dùng cho chỉ mục chống trùng (BR34)
        attempt_count      INT            NOT NULL CONSTRAINT DF_jobs_attempt_count DEFAULT (0),         -- Số lần đã thử
        max_attempts       INT            NOT NULL CONSTRAINT DF_jobs_max_attempts DEFAULT (3),          -- Tối đa 3 lần (BR35)
        next_attempt_at    DATETIME2      NOT NULL CONSTRAINT DF_jobs_next_attempt DEFAULT (SYSUTCDATETIME()), -- Thời điểm được nhận tiếp
        last_error_code    VARCHAR(50)    NULL,                                                          -- Lỗi gần nhất đã làm sạch
        last_error_message NVARCHAR(1000) NULL,
        provider           VARCHAR(50)    NULL,                                                          -- Nhà cung cấp đã dùng (mock, tên nhà cung cấp)
        requested_by       BIGINT         NULL,
        created_at         DATETIME2      NOT NULL CONSTRAINT DF_jobs_created_at DEFAULT (SYSUTCDATETIME()),
        started_at         DATETIME2      NULL,
        finished_at        DATETIME2      NULL,
        CONSTRAINT PK_processing_jobs PRIMARY KEY (id),
        CONSTRAINT FK_jobs_target_version FOREIGN KEY (target_version_id) REFERENCES dbo.narration_versions (id),
        CONSTRAINT FK_jobs_target_language FOREIGN KEY (target_language)  REFERENCES dbo.languages (code),
        CONSTRAINT FK_jobs_audio_asset    FOREIGN KEY (audio_asset_id)    REFERENCES dbo.audio_assets (id),
        CONSTRAINT FK_jobs_result_version FOREIGN KEY (result_version_id) REFERENCES dbo.narration_versions (id),
        CONSTRAINT FK_jobs_requested_by   FOREIGN KEY (requested_by)      REFERENCES dbo.admin_users (id),
        CONSTRAINT CK_job_type   CHECK (job_type IN ('TRANSLATE', 'TTS')),
        CONSTRAINT CK_job_status CHECK (status IN ('QUEUED', 'RUNNING', 'SUCCEEDED', 'FAILED', 'CANCELLED'))
    );

    -- BR34: chống tạo tác vụ trùng khi hai yêu cầu đến cùng lúc.
    CREATE UNIQUE NONCLUSTERED INDEX UX_job_pending ON dbo.processing_jobs (dedup_key) WHERE status IN ('QUEUED', 'RUNNING');
    -- Bộ xử lý nền tìm tác vụ đến hạn (Bảng 3.51)
    CREATE NONCLUSTERED INDEX IX_job_due ON dbo.processing_jobs (status, next_attempt_at);
    -- [BỔ SUNG] Hỗ trợ khóa ngoại target_version_id (trang "Tác vụ xử lý" theo điểm/phiên bản)
    CREATE NONCLUSTERED INDEX IX_job_target ON dbo.processing_jobs (target_version_id);
END;
GO

-- ------------------------------------------------------------------------------------
-- 1.9  glossary_terms và glossary_translations  (Bảng 3.49; UC14; BR22)
-- ------------------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.glossary_terms', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.glossary_terms (
        id            BIGINT        IDENTITY(1,1) NOT NULL,
        term_vi       NVARCHAR(200) NOT NULL,                                                           -- Thuật ngữ tiếng Việt (không phân biệt hoa thường nhờ collation CI)
        category      VARCHAR(20)   NOT NULL CONSTRAINT DF_glossary_category DEFAULT ('TERM'),          -- PROPER_NAME | PLACE | TERM
        keep_original BIT           NOT NULL CONSTRAINT DF_glossary_keep_original DEFAULT (0),          -- Giữ nguyên khi dịch
        note          NVARCHAR(500) NULL,                                                               -- Ghi chú
        is_active     BIT           NOT NULL CONSTRAINT DF_glossary_is_active DEFAULT (1),              -- Đang dùng
        CONSTRAINT PK_glossary_terms PRIMARY KEY (id),
        CONSTRAINT UQ_glossary_terms_term_vi UNIQUE (term_vi),
        CONSTRAINT CK_glossary_category CHECK (category IN ('PROPER_NAME', 'PLACE', 'TERM'))
    );
END;
GO

IF OBJECT_ID(N'dbo.glossary_translations', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.glossary_translations (
        term_id         BIGINT        NOT NULL,
        language_code   VARCHAR(5)    NOT NULL,
        translated_term NVARCHAR(200) NULL,                                                             -- Cách dịch quy ước
        tts_alias       NVARCHAR(200) NULL,                                                             -- Cách đọc cho giọng đọc ngôn ngữ đó (cũng dùng cho vi)
        CONSTRAINT PK_glossary_translations PRIMARY KEY (term_id, language_code),
        CONSTRAINT FK_glossary_translations_term     FOREIGN KEY (term_id)       REFERENCES dbo.glossary_terms (id),
        CONSTRAINT FK_glossary_translations_language FOREIGN KEY (language_code) REFERENCES dbo.languages (code)
    );
END;
GO

-- ------------------------------------------------------------------------------------
-- 1.10 audit_logs  (Bảng 3.50; UC07, UC08, UC10–UC13; BR40)
--      action: APPROVE, PUBLISH, UNPUBLISH, POI_STATUS, JOB_RETRY, JOB_CANCEL, AUDIO_DELETE
-- ------------------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.audit_logs', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.audit_logs (
        id          BIGINT         IDENTITY(1,1) NOT NULL,
        admin_id    BIGINT         NOT NULL,                                                            -- Người thực hiện
        action      VARCHAR(30)    NOT NULL,                                                            -- APPROVE | PUBLISH | UNPUBLISH | POI_STATUS | JOB_RETRY | JOB_CANCEL | AUDIO_DELETE
        entity_type VARCHAR(30)    NOT NULL,                                                            -- Loại đối tượng bị tác động (ví dụ NARRATION_VERSION, POI, PROCESSING_JOB, AUDIO_ASSET)
        entity_id   BIGINT         NOT NULL,                                                            -- Định danh đối tượng bị tác động
        detail      NVARCHAR(1000) NULL,                                                                -- Thông tin bổ sung (ví dụ phiên bản cũ, mới)
        created_at  DATETIME2      NOT NULL CONSTRAINT DF_audit_created_at DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT PK_audit_logs PRIMARY KEY (id),
        CONSTRAINT FK_audit_admin FOREIGN KEY (admin_id) REFERENCES dbo.admin_users (id)
    );

    -- [BỔ SUNG] Tra cứu lịch sử theo đối tượng và theo thời gian
    CREATE NONCLUSTERED INDEX IX_audit_entity ON dbo.audit_logs (entity_type, entity_id);
    CREATE NONCLUSTERED INDEX IX_audit_created_at ON dbo.audit_logs (created_at);
END;
GO

PRINT N'PHẦN 1: lược đồ 12 bảng đã sẵn sàng.';
GO


/* =====================================================================================
   PHẦN 2. DỮ LIỆU THAM CHIẾU (Bảng 3.52) - cần cho mọi môi trường
   Ba ngôn ngữ vi, en, ko (BR16); giọng đọc mặc định theo nhà cung cấp Mock.
   Khi chốt nhà cung cấp TTS thật, cập nhật cột tts_voice (NULL = không tạo được âm thanh).
   ===================================================================================== */

IF NOT EXISTS (SELECT 1 FROM dbo.languages WHERE code = 'vi')
    INSERT INTO dbo.languages (code, name, is_source, is_enabled, tts_voice, sort_order)
    VALUES ('vi', N'Tiếng Việt', 1, 1, 'mock-vi', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.languages WHERE code = 'en')
    INSERT INTO dbo.languages (code, name, is_source, is_enabled, tts_voice, sort_order)
    VALUES ('en', N'English', 0, 1, 'mock-en', 2);

IF NOT EXISTS (SELECT 1 FROM dbo.languages WHERE code = 'ko')
    INSERT INTO dbo.languages (code, name, is_source, is_enabled, tts_voice, sort_order)
    VALUES ('ko', N'한국어', 0, 1, 'mock-ko', 3);
GO

PRINT N'PHẦN 2: dữ liệu tham chiếu (vi, en, ko) đã sẵn sàng.';
GO


/* =====================================================================================
   PHẦN 3. VIEW VÀ HÀM TIỆN ÍCH  [BỔ SUNG - tùy chọn]
   Hiện thực ở mức CSDL các khái niệm "suy ra khi truy vấn" mà báo cáo mô tả (không lưu thành cột):
   nội dung khách được xem (BR23, BR27), trạng thái "cần cập nhật" (mục 3.1.3), âm thanh đủ điều kiện
   xuất bản (BR30), ma trận nội dung cho trang quản trị, tổng quan tác vụ nền (UC13).
   ===================================================================================== */

-- 3.1  Khoảng cách Haversine (mét), R = 6 371 008,8 m như mục 3.9.3 của báo cáo.
--      Chỉ để kiểm tra/báo cáo; theo thiết kế, việc tính khoảng cách khi chạy do backend thực hiện (BR07).
CREATE OR ALTER FUNCTION dbo.fn_haversine_m
(
    @lat1 FLOAT,
    @lon1 FLOAT,
    @lat2 FLOAT,
    @lon2 FLOAT
)
RETURNS FLOAT
WITH SCHEMABINDING
AS
BEGIN
    DECLARE @R    FLOAT = 6371008.8;
    DECLARE @dLat FLOAT = RADIANS(@lat2 - @lat1);
    DECLARE @dLon FLOAT = RADIANS(@lon2 - @lon1);
    DECLARE @a    FLOAT = SIN(@dLat / 2.0) * SIN(@dLat / 2.0)
                        + COS(RADIANS(@lat1)) * COS(RADIANS(@lat2)) * SIN(@dLon / 2.0) * SIN(@dLon / 2.0);

    IF @a > 1.0 SET @a = 1.0;

    RETURN 2.0 * @R * ATN2(SQRT(@a), SQRT(1.0 - @a));
END;
GO

-- 3.2  Điểm khách nhìn thấy (BR27): trạng thái ACTIVE và có bản tiếng Việt (ngôn ngữ nguồn) đang xuất bản.
CREATE OR ALTER VIEW dbo.vw_public_visible_pois
AS
SELECT  p.id                AS poi_id,
        p.code              AS poi_code,
        p.zone_id           AS zone_id,
        p.latitude          AS latitude,
        p.longitude         AS longitude,
        p.near_radius_m     AS near_radius_m,
        p.sort_order        AS sort_order,
        p.image_storage_key AS image_storage_key
FROM    dbo.pois AS p
        INNER JOIN dbo.languages AS src
                ON src.is_source = 1
        INNER JOIN dbo.publications AS pub
                ON pub.poi_id = p.id
               AND pub.language_code = src.code
        INNER JOIN dbo.narration_versions AS nv
                ON nv.id = pub.narration_version_id
               AND nv.status = 'PUBLISHED'
WHERE   p.status = 'ACTIVE';
GO

-- 3.3  Nội dung và âm thanh khách được phép đọc (BR23, BR32): chỉ qua con trỏ xuất bản của điểm ACTIVE.
--      Không có cột trạng thái, nguồn gốc hay cảnh báo nội bộ (DTO công khai, mục 3.3.2 c).
CREATE OR ALTER VIEW dbo.vw_public_narrations
AS
SELECT  pub.poi_id          AS poi_id,
        p.code              AS poi_code,
        pub.language_code   AS language_code,
        nv.id               AS narration_version_id,
        nv.version_no       AS version_no,
        nv.title            AS title,
        nv.body             AS body,
        pub.published_at    AS published_at,
        aud.id              AS audio_asset_id,
        aud.mime_type       AS audio_mime_type,
        aud.duration_ms     AS audio_duration_ms
FROM    dbo.publications AS pub
        INNER JOIN dbo.pois AS p
                ON p.id = pub.poi_id
               AND p.status = 'ACTIVE'
        INNER JOIN dbo.narration_versions AS nv
                ON nv.id = pub.narration_version_id
               AND nv.status = 'PUBLISHED'
        LEFT  JOIN dbo.audio_assets AS aud
                ON aud.id = pub.audio_asset_id
               AND aud.status = 'READY';
GO

-- 3.4  Trạng thái suy ra "cần cập nhật" (mục 3.1.3, Bảng 3.30, BR26): bản en, ko đang xuất bản được dịch từ
--      một bản tiếng Việt khác bản tiếng Việt đang xuất bản.
CREATE OR ALTER VIEW dbo.vw_translation_needs_update
AS
SELECT  pub.poi_id                       AS poi_id,
        p.code                           AS poi_code,
        pub.language_code                AS language_code,
        pub.narration_version_id         AS published_version_id,
        nv.source_version_id             AS translated_from_version_id,
        src_pub.narration_version_id     AS current_source_version_id
FROM    dbo.publications AS pub
        INNER JOIN dbo.pois AS p
                ON p.id = pub.poi_id
        INNER JOIN dbo.narration_versions AS nv
                ON nv.id = pub.narration_version_id
        INNER JOIN dbo.languages AS src
                ON src.is_source = 1
        INNER JOIN dbo.publications AS src_pub
                ON src_pub.poi_id = pub.poi_id
               AND src_pub.language_code = src.code
WHERE   pub.language_code <> src.code
  AND   nv.source_version_id <> src_pub.narration_version_id;
GO

-- 3.5  Âm thanh đủ điều kiện gắn khi xuất bản (BR30, UC12 bước 2): READY, text_hash khớp content_hash,
--      phiên bản đang APPROVED hoặc PUBLISHED.
CREATE OR ALTER VIEW dbo.vw_audio_publishable
AS
SELECT  aud.id                    AS audio_asset_id,
        aud.narration_version_id  AS narration_version_id,
        nv.poi_id                 AS poi_id,
        nv.language_code          AS language_code,
        aud.origin                AS origin,
        aud.voice_name            AS voice_name,
        aud.duration_ms           AS duration_ms,
        aud.created_at            AS created_at
FROM    dbo.audio_assets AS aud
        INNER JOIN dbo.narration_versions AS nv
                ON nv.id = aud.narration_version_id
WHERE   aud.status = 'READY'
  AND   aud.text_hash = nv.content_hash
  AND   nv.status IN ('APPROVED', 'PUBLISHED');
GO

-- 3.6  Ma trận nội dung (điểm x ngôn ngữ) cho trang quản trị (UC07, UC08, UC10, mục 3.6.2).
--      publish_state: NOT_PUBLISHED | TEXT_ONLY | TEXT_AND_AUDIO.
CREATE OR ALTER VIEW dbo.vw_admin_content_matrix
AS
SELECT  p.id                     AS poi_id,
        p.code                   AS poi_code,
        p.status                 AS poi_status,
        l.code                   AS language_code,
        pv.id                    AS published_version_id,
        pv.version_no            AS published_version_no,
        pub.published_at         AS published_at,
        CASE WHEN pub.poi_id IS NULL         THEN 'NOT_PUBLISHED'
             WHEN pub.audio_asset_id IS NULL THEN 'TEXT_ONLY'
             ELSE 'TEXT_AND_AUDIO'
        END                      AS publish_state,
        dr.id                    AS open_draft_version_id,
        CAST(CASE WHEN nu.poi_id IS NULL THEN 0 ELSE 1 END AS BIT) AS needs_update
FROM    dbo.pois AS p
        CROSS JOIN dbo.languages AS l
        LEFT JOIN dbo.publications AS pub
               ON pub.poi_id = p.id
              AND pub.language_code = l.code
        LEFT JOIN dbo.narration_versions AS pv
               ON pv.id = pub.narration_version_id
        LEFT JOIN dbo.narration_versions AS dr
               ON dr.poi_id = p.id
              AND dr.language_code = l.code
              AND dr.status = 'DRAFT'
        LEFT JOIN dbo.vw_translation_needs_update AS nu
               ON nu.poi_id = p.id
              AND nu.language_code = l.code
WHERE   l.is_enabled = 1;
GO

-- 3.7  Tổng quan tác vụ nền cho trang "Tác vụ xử lý" (UC13): lọc theo trạng thái, loại, điểm, ngôn ngữ.
CREATE OR ALTER VIEW dbo.vw_processing_jobs_overview
AS
SELECT  j.id                                AS job_id,
        j.job_type                          AS job_type,
        j.status                            AS status,
        p.code                              AS poi_code,
        COALESCE(j.target_language, nv.language_code) AS language_code,
        j.target_version_id                 AS target_version_id,
        j.result_version_id                 AS result_version_id,
        j.audio_asset_id                    AS audio_asset_id,
        j.attempt_count                     AS attempt_count,
        j.max_attempts                      AS max_attempts,
        j.next_attempt_at                   AS next_attempt_at,
        j.last_error_code                   AS last_error_code,
        j.last_error_message                AS last_error_message,
        j.provider                          AS provider,
        j.requested_by                      AS requested_by,
        j.created_at                        AS created_at,
        j.started_at                        AS started_at,
        j.finished_at                       AS finished_at
FROM    dbo.processing_jobs AS j
        INNER JOIN dbo.narration_versions AS nv
                ON nv.id = j.target_version_id
        INNER JOIN dbo.pois AS p
                ON p.id = nv.poi_id;
GO

PRINT N'PHẦN 3: view và hàm tiện ích đã sẵn sàng.';
GO


/* =====================================================================================
   PHẦN 4. DỮ LIỆU MẪU CHO DEMO VÀ KIỂM THỬ  (tùy chọn; tương ứng db/demo/V100__demo_data.sql)

   Theo Bảng 3.52 và Bảng 3.68 của báo cáo: hai khu vực, năm điểm mẫu P01–P05 quanh tâm công viên
   giả định G = (10,767600; 106,640600), nội dung tiếng Việt mẫu đã xuất bản, một số bản en, ko mẫu
   (ghi rõ "dữ liệu mẫu") và vài thuật ngữ. Khoảng cách tới G tính bằng Haversine (R = 6 371 008,8 m).

       Điểm  Khu   Vĩ độ      Kinh độ     Trạng thái  Nội dung đã xuất bản    Cách G
       P01   Z01   10.768000  106.640000  ACTIVE      vi, en, ko + âm thanh   79,2 m
       P02   Z01   10.769000  106.641000  ACTIVE      vi + âm thanh           161,7 m
       P03   Z02   10.766500  106.642500  ACTIVE      vi, en (chỉ văn bản)    240,9 m
       P04   Z02   10.770000  106.638000  ACTIVE      vi                      389,7 m
       P05   Z01   10.768100  106.640100  INACTIVE    vi (điểm INACTIVE)      77,9 m

   Hai cờ điều khiển ngay bên dưới:
     @LoadDemoData  = 0 : không nạp dữ liệu mẫu (chỉ giữ lược đồ và dữ liệu tham chiếu).
     @IncludeExtras = 0 : chỉ nạp đúng dữ liệu của báo cáo; bỏ phần minh họa [BỔ SUNG] gồm 5 tác vụ nền
                          đã hoàn tất, nhật ký thao tác và 3 phiên bản ở trạng thái APPROVED, DISCARDED,
                          DRAFT của điểm P04 (không phần nào làm thay đổi các ca kiểm thử của báo cáo).
   Chỉ nạp khi bảng pois đang rỗng nên chạy lại nhiều lần không bị nạp chồng.

   Lưu ý khi dùng dữ liệu mẫu:
     * Mọi nội dung đều là dữ liệu mẫu, không phải thông tin thật về Công viên Văn hóa Đầm Sen.
     * Tài khoản quản trị mẫu: admin / Admin@12345 (BCrypt) - CHỈ DÙNG ĐỂ DEMO, đổi hoặc xóa khi triển khai.
     * content_hash của dữ liệu mẫu = SHA-256 (chữ thường) của chuỗi UTF-8 "tiêu đề + LF + nội dung".
       Ứng dụng có thể dùng quy ước khác khi tự tính lại; các bản ghi âm thanh mẫu có text_hash trùng
       content_hash của phiên bản tương ứng (BR30).
     * Âm thanh mẫu chỉ là bản ghi siêu dữ liệu READY, chưa có tệp MP3 thật trên volume media-data
       (checksum_sha256 để NULL). Khi phát sẽ lỗi nhưng văn bản vẫn hiển thị (BR33) cho tới khi có tệp thật
       hoặc quản trị viên tạo lại âm thanh bằng nhà cung cấp Mock.
     * Bản en, ko mẫu có text_origin = HUMAN (nhập tay), không phải kết quả dịch máy.
   ===================================================================================== */

DECLARE @LoadDemoData  BIT = 1;   -- 1 = nạp dữ liệu mẫu | 0 = bỏ qua toàn bộ PHẦN 4
DECLARE @IncludeExtras BIT = 1;   -- 1 = thêm phần minh họa [BỔ SUNG] | 0 = chỉ dữ liệu của báo cáo

IF @LoadDemoData = 0
BEGIN
    PRINT N'PHẦN 4: bỏ qua dữ liệu mẫu (@LoadDemoData = 0).';
END
ELSE IF EXISTS (SELECT 1 FROM dbo.pois)
BEGIN
    PRINT N'PHẦN 4: bảng pois đã có dữ liệu nên không nạp dữ liệu mẫu (tránh nạp chồng).';
END
ELSE
BEGIN
    SET XACT_ABORT ON;
    DECLARE @Now     DATETIME2 = SYSUTCDATETIME();
    DECLARE @AdminId BIGINT;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 4.1  Quản trị viên: dùng tài khoản sẵn có (ví dụ do ApplicationRunner tạo); nếu bảng rỗng thì tạo tài khoản demo.
        SELECT TOP (1) @AdminId = id FROM dbo.admin_users ORDER BY id;
        IF @AdminId IS NULL
        BEGIN
            INSERT INTO dbo.admin_users (username, password_hash, display_name, created_at)
            VALUES (N'admin', '$2a$10$5cvG5iVZ68AzPqVJ4Z2zu.pBLO2qBFmj7Y/3uLCWyFYqn6m788U5m', N'Quản trị viên demo', DATEADD(MINUTE, -1000, @Now));
            SET @AdminId = SCOPE_IDENTITY();
            PRINT N'PHẦN 4: đã tạo tài khoản quản trị demo admin / Admin@12345 (CHỈ DÙNG ĐỂ DEMO).';
        END;

        -- 4.2  Khu vực và tên khu vực theo ngôn ngữ (Bảng 3.43)
        SET IDENTITY_INSERT dbo.zones ON;
        INSERT INTO dbo.zones (id, code, sort_order, is_active)
        VALUES
            (1, 'Z01', 1, 1),
            (2, 'Z02', 2, 1);
        SET IDENTITY_INSERT dbo.zones OFF;
        INSERT INTO dbo.zone_translations (zone_id, language_code, name)
        VALUES
            (1, 'vi', N'Khu vực 1'),
            (1, 'en', N'Zone 1'),
            (1, 'ko', N'구역 1'),
            (2, 'vi', N'Khu vực 2'),
            (2, 'en', N'Zone 2'),
            (2, 'ko', N'구역 2');

        -- 4.3  Năm điểm mẫu P01–P05 (Bảng 3.68). near_radius_m dùng giá trị mặc định 30 m (BR15).
        SET IDENTITY_INSERT dbo.pois ON;
        INSERT INTO dbo.pois (id, code, zone_id, internal_name, latitude, longitude, near_radius_m, status, sort_order, ever_published, created_at, updated_at)
        VALUES
            -- P01: ACTIVE, cách G 79,2 m
            (1, 'P01', 1, N'Điểm mẫu A', 10.768000, 106.640000, 30, 'ACTIVE', 1, 1, DATEADD(MINUTE, -990, @Now), DATEADD(MINUTE, -990, @Now)),
            -- P02: ACTIVE, cách G 161,7 m
            (2, 'P02', 1, N'Điểm mẫu B', 10.769000, 106.641000, 30, 'ACTIVE', 2, 1, DATEADD(MINUTE, -990, @Now), DATEADD(MINUTE, -990, @Now)),
            -- P03: ACTIVE, cách G 240,9 m
            (3, 'P03', 2, N'Điểm mẫu C', 10.766500, 106.642500, 30, 'ACTIVE', 1, 1, DATEADD(MINUTE, -990, @Now), DATEADD(MINUTE, -990, @Now)),
            -- P04: ACTIVE, cách G 389,7 m
            (4, 'P04', 2, N'Điểm mẫu D', 10.770000, 106.638000, 30, 'ACTIVE', 2, 1, DATEADD(MINUTE, -990, @Now), DATEADD(MINUTE, -990, @Now)),
            -- P05: INACTIVE, cách G 77,9 m
            (5, 'P05', 1, N'Điểm mẫu E', 10.768100, 106.640100, 30, 'INACTIVE', 3, 1, DATEADD(MINUTE, -990, @Now), DATEADD(MINUTE, -300, @Now));
        SET IDENTITY_INSERT dbo.pois OFF;

        -- 4.4  Phiên bản nội dung đã xuất bản (Bảng 3.45, 3.68). Bản en, ko tham chiếu bản tiếng Việt nguồn (BR17).
        SET IDENTITY_INSERT dbo.narration_versions ON;
        INSERT INTO dbo.narration_versions (id, poi_id, language_code, version_no, source_version_id, based_on_version_id, title, body, text_origin, machine_title, machine_body, status, content_hash, approved_by, approved_at, created_by, created_at, updated_at)
        VALUES
            -- P01 / vi / v1 / PUBLISHED / SOURCE
            (1, 1, 'vi', 1, NULL, NULL,
                N'Điểm mẫu A',
                N'Đây là nội dung thuyết minh mẫu cho điểm A (dữ liệu mẫu). Nội dung này chỉ dùng để kiểm thử và trình diễn hệ thống; nội dung thật sẽ do quản trị viên soạn, duyệt và xuất bản.',
                'SOURCE', NULL, NULL, 'PUBLISHED', '4463c6a53d0eadc8bfc42a589eea1f1102930da4e4a1090988f0bb6191525f71',
                @AdminId, DATEADD(MINUTE, -975, @Now), @AdminId, DATEADD(MINUTE, -980, @Now), DATEADD(MINUTE, -975, @Now)),
            -- P01 / en / v1 / PUBLISHED / HUMAN
            (2, 1, 'en', 1, 1, NULL,
                N'Sample point A',
                N'This is sample narration text for point A (sample data). It is used only to test and demonstrate the system; the real content will be written, approved and published by an administrator.',
                'HUMAN', NULL, NULL, 'PUBLISHED', 'dc070001eb2df1533e23cb9b0c445c1c39f6a8a5b33f968a56e3188deac5b295',
                @AdminId, DATEADD(MINUTE, -955, @Now), @AdminId, DATEADD(MINUTE, -960, @Now), DATEADD(MINUTE, -955, @Now)),
            -- P01 / ko / v1 / PUBLISHED / HUMAN
            (3, 1, 'ko', 1, 1, NULL,
                N'샘플 지점 A',
                N'이 내용은 A 지점의 샘플 해설 텍스트입니다(샘플 데이터). 시스템 테스트와 시연에만 사용되며, 실제 내용은 관리자가 작성하고 승인한 후 게시합니다.',
                'HUMAN', NULL, NULL, 'PUBLISHED', 'fe8ef5fdb39a8d359741f20f36796f41fae8ebf98c687ecaf51954c4be93b501',
                @AdminId, DATEADD(MINUTE, -955, @Now), @AdminId, DATEADD(MINUTE, -960, @Now), DATEADD(MINUTE, -955, @Now)),
            -- P02 / vi / v1 / PUBLISHED / SOURCE
            (4, 2, 'vi', 1, NULL, NULL,
                N'Điểm mẫu B',
                N'Đây là nội dung thuyết minh mẫu cho điểm B (dữ liệu mẫu). Điểm này chỉ có bản tiếng Việt nên khi khách chọn ngôn ngữ khác, hệ thống hiển thị bản tiếng Việt kèm thông báo.',
                'SOURCE', NULL, NULL, 'PUBLISHED', '35ff41e9cc01542c8afd38affb2767c59a897ba205a00d223fc7952fba47f4a8',
                @AdminId, DATEADD(MINUTE, -975, @Now), @AdminId, DATEADD(MINUTE, -980, @Now), DATEADD(MINUTE, -975, @Now)),
            -- P03 / vi / v1 / PUBLISHED / SOURCE
            (5, 3, 'vi', 1, NULL, NULL,
                N'Điểm mẫu C',
                N'Đây là nội dung thuyết minh mẫu cho điểm C (dữ liệu mẫu). Điểm này có bản tiếng Việt và tiếng Anh, chưa có âm thanh nên chỉ hiển thị văn bản.',
                'SOURCE', NULL, NULL, 'PUBLISHED', '9ad9251e26ef5b790cb7d5d554448242b94851b2160a5f127bb62ba10d2fd029',
                @AdminId, DATEADD(MINUTE, -975, @Now), @AdminId, DATEADD(MINUTE, -980, @Now), DATEADD(MINUTE, -975, @Now)),
            -- P03 / en / v1 / PUBLISHED / HUMAN
            (6, 3, 'en', 1, 5, NULL,
                N'Sample point C',
                N'This is sample narration text for point C (sample data). This point has Vietnamese and English versions and no audio, so only the text is shown.',
                'HUMAN', NULL, NULL, 'PUBLISHED', '950e72d8249ed01fb31de5c14a11f68c04d1fc67449748442eec6ea85e80f1c8',
                @AdminId, DATEADD(MINUTE, -955, @Now), @AdminId, DATEADD(MINUTE, -960, @Now), DATEADD(MINUTE, -955, @Now)),
            -- P04 / vi / v1 / PUBLISHED / SOURCE
            (7, 4, 'vi', 1, NULL, NULL,
                N'Điểm mẫu D',
                N'Đây là nội dung thuyết minh mẫu cho điểm D (dữ liệu mẫu). Điểm này cách tâm công viên giả định khoảng 390 m, ngoài bán kính tìm kiếm mặc định 300 m.',
                'SOURCE', NULL, NULL, 'PUBLISHED', 'b0971e5afad88ce675231c407907b9ba32372676d0d374ee731dbff0f2566023',
                @AdminId, DATEADD(MINUTE, -975, @Now), @AdminId, DATEADD(MINUTE, -980, @Now), DATEADD(MINUTE, -975, @Now)),
            -- P05 / vi / v1 / PUBLISHED / SOURCE
            (8, 5, 'vi', 1, NULL, NULL,
                N'Điểm mẫu E',
                N'Đây là nội dung thuyết minh mẫu cho điểm E (dữ liệu mẫu). Điểm này đang ở trạng thái INACTIVE nên không hiển thị cho khách.',
                'SOURCE', NULL, NULL, 'PUBLISHED', 'c3ff570533d61516a2c78915244f04325e4206205fd7da3b06f1ad0904e8651f',
                @AdminId, DATEADD(MINUTE, -975, @Now), @AdminId, DATEADD(MINUTE, -980, @Now), DATEADD(MINUTE, -975, @Now));
        SET IDENTITY_INSERT dbo.narration_versions OFF;

        -- 4.5  Âm thanh READY của P01 (vi, en, ko) và P02 (vi): chỉ có siêu dữ liệu, chưa có tệp MP3 (xem lưu ý đầu PHẦN 4).
        SET IDENTITY_INSERT dbo.audio_assets ON;
        INSERT INTO dbo.audio_assets (id, narration_version_id, origin, status, voice_name, text_hash, storage_key, mime_type, duration_ms, file_size_bytes, created_by, created_at)
        VALUES
            -- P01 / vi
            (1, 1, 'TTS', 'READY', 'mock-vi', '4463c6a53d0eadc8bfc42a589eea1f1102930da4e4a1090988f0bb6191525f71', 'audio/P01/vi/1.mp3', 'audio/mpeg', 12429, 198864, @AdminId, DATEADD(MINUTE, -950, @Now)),
            -- P01 / en
            (2, 2, 'TTS', 'READY', 'mock-en', 'dc070001eb2df1533e23cb9b0c445c1c39f6a8a5b33f968a56e3188deac5b295', 'audio/P01/en/2.mp3', 'audio/mpeg', 13286, 212576, @AdminId, DATEADD(MINUTE, -950, @Now)),
            -- P01 / ko
            (3, 3, 'TTS', 'READY', 'mock-ko', 'fe8ef5fdb39a8d359741f20f36796f41fae8ebf98c687ecaf51954c4be93b501', 'audio/P01/ko/3.mp3', 'audio/mpeg', 5929, 94864, @AdminId, DATEADD(MINUTE, -950, @Now)),
            -- P02 / vi
            (4, 4, 'TTS', 'READY', 'mock-vi', '35ff41e9cc01542c8afd38affb2767c59a897ba205a00d223fc7952fba47f4a8', 'audio/P02/vi/4.mp3', 'audio/mpeg', 12143, 194288, @AdminId, DATEADD(MINUTE, -950, @Now));
        SET IDENTITY_INSERT dbo.audio_assets OFF;

        -- 4.6  Con trỏ xuất bản (Bảng 3.47): nguồn duy nhất cho API công khai (BR23, BR25)
        INSERT INTO dbo.publications (poi_id, language_code, narration_version_id, audio_asset_id, published_by, published_at)
        VALUES
            -- P01 / vi: văn bản + âm thanh
            (1, 'vi', 1, 1, @AdminId, DATEADD(MINUTE, -940, @Now)),
            -- P01 / en: văn bản + âm thanh
            (1, 'en', 2, 2, @AdminId, DATEADD(MINUTE, -930, @Now)),
            -- P01 / ko: văn bản + âm thanh
            (1, 'ko', 3, 3, @AdminId, DATEADD(MINUTE, -930, @Now)),
            -- P02 / vi: văn bản + âm thanh
            (2, 'vi', 4, 4, @AdminId, DATEADD(MINUTE, -940, @Now)),
            -- P03 / vi: chỉ văn bản
            (3, 'vi', 5, NULL, @AdminId, DATEADD(MINUTE, -940, @Now)),
            -- P03 / en: chỉ văn bản
            (3, 'en', 6, NULL, @AdminId, DATEADD(MINUTE, -930, @Now)),
            -- P04 / vi: chỉ văn bản
            (4, 'vi', 7, NULL, @AdminId, DATEADD(MINUTE, -940, @Now)),
            -- P05 / vi: chỉ văn bản
            (5, 'vi', 8, NULL, @AdminId, DATEADD(MINUTE, -940, @Now));

        -- 4.7  Thuật ngữ mẫu (Bảng 3.33). Bản dịch ko của "Điểm mẫu A" để trống chờ người biết tiếng Hàn xác nhận.
        SET IDENTITY_INSERT dbo.glossary_terms ON;
        INSERT INTO dbo.glossary_terms (id, term_vi, category, keep_original, note, is_active)
        VALUES
            (1, N'Đầm Sen', 'PROPER_NAME', 1, N'Dữ liệu minh họa theo Bảng 3.33, cần người biết ngôn ngữ xác nhận.', 1),
            (2, N'Công viên Văn hóa Đầm Sen', 'PLACE', 0, N'Dữ liệu minh họa theo Bảng 3.33, cần người biết ngôn ngữ xác nhận.', 1),
            (3, N'Điểm mẫu A', 'PROPER_NAME', 1, N'Dữ liệu giả định cho kiểm thử; phiên âm tiếng Hàn chờ người biết tiếng Hàn xác nhận.', 1);
        SET IDENTITY_INSERT dbo.glossary_terms OFF;
        INSERT INTO dbo.glossary_translations (term_id, language_code, translated_term, tts_alias)
        VALUES
            (1, 'en', N'Dam Sen', N'Dam Sen'),
            (1, 'ko', N'담센', NULL),
            (2, 'en', N'Dam Sen Cultural Park', NULL),
            (2, 'ko', N'담센 문화공원', NULL),
            (3, 'en', N'Diem mau A', NULL);

        -- 4.8  [BỔ SUNG] Minh họa thêm (bỏ qua nếu @IncludeExtras = 0)
        IF @IncludeExtras = 1
        BEGIN
            -- Điểm P04: bản en dịch Mock đã duyệt chờ xuất bản (APPROVED), một nháp đã loại bỏ (DISCARDED) và một nháp đang mở (DRAFT)
            SET IDENTITY_INSERT dbo.narration_versions ON;
            INSERT INTO dbo.narration_versions (id, poi_id, language_code, version_no, source_version_id, based_on_version_id, title, body, text_origin, machine_title, machine_body, status, content_hash, approved_by, approved_at, created_by, created_at, updated_at)
            VALUES
                -- P04 / en / v1 / APPROVED / MACHINE
                (9, 4, 'en', 1, 7, NULL,
                    N'Sample point D',
                    N'This is sample narration text for point D (sample data). This point is about 390 m from the assumed park center, outside the default 300 m search radius.',
                    'MACHINE', N'Sample point D', N'This is sample narration text for point D (sample data). This point is about 390 m from the assumed park center, outside the default 300 m search radius.', 'APPROVED', 'ca28221ff0a401bce9d5df9a33f0b71e76bddf52e174885060663fe2f1af9ec7',
                    @AdminId, DATEADD(MINUTE, -395, @Now), @AdminId, DATEADD(MINUTE, -400, @Now), DATEADD(MINUTE, -395, @Now)),
                -- P04 / vi / v2 / DISCARDED / SOURCE
                (10, 4, 'vi', 2, NULL, 7,
                    N'Điểm mẫu D',
                    N'Bản nháp đã bị loại bỏ (dữ liệu mẫu).',
                    'SOURCE', NULL, NULL, 'DISCARDED', '00a7d465a0158bb1e23dd9dc5c9a4f89ecb1aef5a9cfa2e9a83e6a638890fa86',
                    NULL, NULL, @AdminId, DATEADD(MINUTE, -200, @Now), DATEADD(MINUTE, -190, @Now)),
                -- P04 / vi / v3 / DRAFT / SOURCE
                (11, 4, 'vi', 3, NULL, 7,
                    N'Điểm mẫu D',
                    N'Đây là bản nháp chỉnh sửa nội dung thuyết minh mẫu cho điểm D (dữ liệu mẫu). Bản nháp chưa được duyệt nên khách chưa nhìn thấy.',
                    'SOURCE', NULL, NULL, 'DRAFT', 'fed3e6434292ab2f5153535b16358e7abaee5e7029241b3bc0f55abdeb0ed650',
                    NULL, NULL, @AdminId, DATEADD(MINUTE, -100, @Now), DATEADD(MINUTE, -100, @Now));
            SET IDENTITY_INSERT dbo.narration_versions OFF;

            -- Tác vụ nền đã hoàn tất (SUCCEEDED); không thêm tác vụ QUEUED/RUNNING để bộ xử lý nền không có việc phát sinh
            SET IDENTITY_INSERT dbo.processing_jobs ON;
            INSERT INTO dbo.processing_jobs (id, job_type, status, target_version_id, target_language, audio_asset_id, result_version_id, dedup_key, attempt_count, max_attempts, next_attempt_at, provider, requested_by, created_at, started_at, finished_at)
            VALUES
                -- TTS cho phiên bản 1
                (1, 'TTS', 'SUCCEEDED', 1, NULL, 1, NULL, 'TTS:1', 1, 3, DATEADD(MINUTE, -952, @Now), 'mock', @AdminId, DATEADD(MINUTE, -952, @Now), DATEADD(MINUTE, -951, @Now), DATEADD(MINUTE, -950, @Now)),
                -- TTS cho phiên bản 2
                (2, 'TTS', 'SUCCEEDED', 2, NULL, 2, NULL, 'TTS:2', 1, 3, DATEADD(MINUTE, -952, @Now), 'mock', @AdminId, DATEADD(MINUTE, -952, @Now), DATEADD(MINUTE, -951, @Now), DATEADD(MINUTE, -950, @Now)),
                -- TTS cho phiên bản 3
                (3, 'TTS', 'SUCCEEDED', 3, NULL, 3, NULL, 'TTS:3', 1, 3, DATEADD(MINUTE, -952, @Now), 'mock', @AdminId, DATEADD(MINUTE, -952, @Now), DATEADD(MINUTE, -951, @Now), DATEADD(MINUTE, -950, @Now)),
                -- TTS cho phiên bản 4
                (4, 'TTS', 'SUCCEEDED', 4, NULL, 4, NULL, 'TTS:4', 1, 3, DATEADD(MINUTE, -952, @Now), 'mock', @AdminId, DATEADD(MINUTE, -952, @Now), DATEADD(MINUTE, -951, @Now), DATEADD(MINUTE, -950, @Now)),
                -- TRANSLATE cho phiên bản 7 -> en
                (5, 'TRANSLATE', 'SUCCEEDED', 7, 'en', NULL, 9, 'TRANSLATE:7:en', 1, 3, DATEADD(MINUTE, -402, @Now), 'mock', @AdminId, DATEADD(MINUTE, -402, @Now), DATEADD(MINUTE, -401, @Now), DATEADD(MINUTE, -400, @Now));
            SET IDENTITY_INSERT dbo.processing_jobs OFF;

            -- Nhật ký thao tác (BR40)
            SET IDENTITY_INSERT dbo.audit_logs ON;
            INSERT INTO dbo.audit_logs (id, admin_id, action, entity_type, entity_id, detail, created_at)
            VALUES
                (1, @AdminId, 'APPROVE', 'NARRATION_VERSION', 1, N'Dữ liệu mẫu: duyệt phiên bản 1 (vi) của điểm P01', DATEADD(MINUTE, -975, @Now)),
                (2, @AdminId, 'APPROVE', 'NARRATION_VERSION', 4, N'Dữ liệu mẫu: duyệt phiên bản 1 (vi) của điểm P02', DATEADD(MINUTE, -975, @Now)),
                (3, @AdminId, 'APPROVE', 'NARRATION_VERSION', 5, N'Dữ liệu mẫu: duyệt phiên bản 1 (vi) của điểm P03', DATEADD(MINUTE, -975, @Now)),
                (4, @AdminId, 'APPROVE', 'NARRATION_VERSION', 7, N'Dữ liệu mẫu: duyệt phiên bản 1 (vi) của điểm P04', DATEADD(MINUTE, -975, @Now)),
                (5, @AdminId, 'APPROVE', 'NARRATION_VERSION', 8, N'Dữ liệu mẫu: duyệt phiên bản 1 (vi) của điểm P05', DATEADD(MINUTE, -975, @Now)),
                (6, @AdminId, 'APPROVE', 'NARRATION_VERSION', 2, N'Dữ liệu mẫu: duyệt phiên bản 1 (en) của điểm P01', DATEADD(MINUTE, -955, @Now)),
                (7, @AdminId, 'APPROVE', 'NARRATION_VERSION', 3, N'Dữ liệu mẫu: duyệt phiên bản 1 (ko) của điểm P01', DATEADD(MINUTE, -955, @Now)),
                (8, @AdminId, 'APPROVE', 'NARRATION_VERSION', 6, N'Dữ liệu mẫu: duyệt phiên bản 1 (en) của điểm P03', DATEADD(MINUTE, -955, @Now)),
                (9, @AdminId, 'PUBLISH', 'NARRATION_VERSION', 1, N'Dữ liệu mẫu: xuất bản phiên bản 1 (vi) của điểm P01, kèm âm thanh', DATEADD(MINUTE, -940, @Now)),
                (10, @AdminId, 'PUBLISH', 'NARRATION_VERSION', 4, N'Dữ liệu mẫu: xuất bản phiên bản 1 (vi) của điểm P02, kèm âm thanh', DATEADD(MINUTE, -940, @Now)),
                (11, @AdminId, 'PUBLISH', 'NARRATION_VERSION', 5, N'Dữ liệu mẫu: xuất bản phiên bản 1 (vi) của điểm P03, chỉ văn bản', DATEADD(MINUTE, -940, @Now)),
                (12, @AdminId, 'PUBLISH', 'NARRATION_VERSION', 7, N'Dữ liệu mẫu: xuất bản phiên bản 1 (vi) của điểm P04, chỉ văn bản', DATEADD(MINUTE, -940, @Now)),
                (13, @AdminId, 'PUBLISH', 'NARRATION_VERSION', 8, N'Dữ liệu mẫu: xuất bản phiên bản 1 (vi) của điểm P05, chỉ văn bản', DATEADD(MINUTE, -940, @Now)),
                (14, @AdminId, 'PUBLISH', 'NARRATION_VERSION', 2, N'Dữ liệu mẫu: xuất bản phiên bản 1 (en) của điểm P01, kèm âm thanh', DATEADD(MINUTE, -930, @Now)),
                (15, @AdminId, 'PUBLISH', 'NARRATION_VERSION', 3, N'Dữ liệu mẫu: xuất bản phiên bản 1 (ko) của điểm P01, kèm âm thanh', DATEADD(MINUTE, -930, @Now)),
                (16, @AdminId, 'PUBLISH', 'NARRATION_VERSION', 6, N'Dữ liệu mẫu: xuất bản phiên bản 1 (en) của điểm P03, chỉ văn bản', DATEADD(MINUTE, -930, @Now)),
                (17, @AdminId, 'APPROVE', 'NARRATION_VERSION', 9, N'Dữ liệu mẫu: duyệt phiên bản 1 (en) của điểm P04', DATEADD(MINUTE, -395, @Now)),
                (18, @AdminId, 'POI_STATUS', 'POI', 5, N'Dữ liệu mẫu: ACTIVE -> INACTIVE (điểm P05 không còn hiển thị cho khách)', DATEADD(MINUTE, -300, @Now));
            SET IDENTITY_INSERT dbo.audit_logs OFF;
        END;

        COMMIT TRANSACTION;
        PRINT N'PHẦN 4: đã nạp dữ liệu mẫu theo báo cáo (2 khu vực, 5 điểm, 8 phiên bản, 4 âm thanh, 8 con trỏ xuất bản, 3 thuật ngữ).';
        IF @IncludeExtras = 1
            PRINT N'PHẦN 4: đã nạp thêm phần minh họa bổ sung (3 phiên bản của P04, 5 tác vụ nền, 18 dòng nhật ký).';
        SET XACT_ABORT OFF;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        -- SET IDENTITY_INSERT không nằm trong giao dịch nên không tự hoàn tác: tắt lại thủ công
        SET IDENTITY_INSERT dbo.zones OFF;
        SET IDENTITY_INSERT dbo.pois OFF;
        SET IDENTITY_INSERT dbo.narration_versions OFF;
        SET IDENTITY_INSERT dbo.audio_assets OFF;
        SET IDENTITY_INSERT dbo.processing_jobs OFF;
        SET IDENTITY_INSERT dbo.glossary_terms OFF;
        SET IDENTITY_INSERT dbo.audit_logs OFF;
        SET XACT_ABORT OFF;
        PRINT N'PHẦN 4: LỖI khi nạp dữ liệu mẫu - toàn bộ đã được hoàn tác.';
        THROW;
    END CATCH;
END;
GO


/* =====================================================================================
   PHẦN 5. TRUY VẤN KIỂM TRA NHANH
   ===================================================================================== */

-- 5.1  Số dòng của từng bảng (12 bảng của báo cáo)
SELECT  t.name        AS table_name,
        SUM(p.rows)   AS row_count
FROM    sys.tables AS t
        INNER JOIN sys.partitions AS p
                ON p.object_id = t.object_id
               AND p.index_id IN (0, 1)
WHERE   t.name IN (N'admin_users', N'languages', N'zones', N'zone_translations', N'pois',
                   N'narration_versions', N'audio_assets', N'publications', N'processing_jobs',
                   N'glossary_terms', N'glossary_translations', N'audit_logs')
GROUP BY t.name
ORDER BY t.name;

-- 5.2  Khoảng cách Haversine từ tâm công viên giả định G = (10,767600; 106,640600) tới P01–P05,
--      đối chiếu với Bảng 3.68 của báo cáo (sai lệch cho phép ± 0,5 m). Rỗng nếu chưa nạp dữ liệu mẫu.
SELECT  p.code,
        p.status,
        ROUND(dbo.fn_haversine_m(10.767600, 106.640600, p.latitude, p.longitude), 1) AS distance_m,
        e.expected_m                                                                 AS report_table_3_68_m
FROM    dbo.pois AS p
        INNER JOIN (VALUES ('P01', 79.2), ('P02', 161.7), ('P03', 240.9),
                           ('P04', 389.7), ('P05', 77.9)) AS e (code, expected_m)
                ON e.code = p.code
ORDER BY p.code;

-- 5.3  Điểm khách nhìn thấy (BR27) và nội dung đã xuất bản theo ngôn ngữ (P05 INACTIVE phải vắng mặt)
SELECT  v.poi_code, v.zone_id, v.latitude, v.longitude, v.near_radius_m
FROM    dbo.vw_public_visible_pois AS v
ORDER BY v.zone_id, v.sort_order, v.poi_code;

SELECT  n.poi_code, n.language_code, n.version_no, n.title,
        CASE WHEN n.audio_asset_id IS NULL THEN N'Chỉ văn bản' ELSE N'Có âm thanh' END AS audio
FROM    dbo.vw_public_narrations AS n
ORDER BY n.poi_code, n.language_code;

-- 5.4  Bản dịch đang xuất bản nhưng "cần cập nhật" vì bản tiếng Việt đã được xuất bản bản mới (BR26)
SELECT  u.poi_code, u.language_code, u.published_version_id,
        u.translated_from_version_id, u.current_source_version_id
FROM    dbo.vw_translation_needs_update AS u
ORDER BY u.poi_code, u.language_code;
GO


/* =====================================================================================
   PHẦN 6. [TÙY CHỌN - ĐANG TẮT] TẠO TÀI KHOẢN ỨNG DỤNG damsen_app (theo docker/db-init.sql, mục 3.8.3)
   Bỏ dấu "--" ở đầu dòng và thay <mat_khau_manh> bằng mật khẩu thật (đáp ứng chính sách độ phức tạp
   của SQL Server). Báo cáo cấp db_owner để Flyway tạo bảng trong demo; môi trường thật nên giảm quyền
   xuống db_datareader + db_datawriter (hoặc quyền riêng cho từng bảng) sau khi lược đồ đã được tạo.
   ===================================================================================== */

-- USE master;
-- GO
-- IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = N'damsen_app')
--     CREATE LOGIN damsen_app WITH PASSWORD = N'<mat_khau_manh>';
-- GO
-- USE damsen_guide;
-- GO
-- IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'damsen_app')
-- BEGIN
--     CREATE USER damsen_app FOR LOGIN damsen_app;
--     ALTER ROLE db_owner ADD MEMBER damsen_app;
-- END;
-- GO


/* =====================================================================================
   PHẦN 7. [TÙY CHỌN - ĐANG TẮT] KỊCH BẢN THỬ CÁC RÀNG BUỘC Ở TẦNG DỮ LIỆU
   Cần đã nạp dữ liệu mẫu (PHẦN 4 với @LoadDemoData = 1). Để chạy: xóa hai dòng chứa chữ "BẮT ĐẦU KHỐI THỬ" và
   "KẾT THÚC KHỐI THỬ" bên dưới (hai dòng này mở và đóng chú thích bao quanh khối mã), rồi chạy phần còn lại.
   Mỗi phép thử tự chuẩn bị dữ liệu trong một giao dịch riêng rồi ROLLBACK nên không làm thay đổi dữ liệu.
   Kết quả mong đợi: chín dòng bắt đầu bằng "OK".
   ===================================================================================== */

/* BẮT ĐẦU KHỐI THỬ
DECLARE @adm      BIGINT = (SELECT TOP (1) id FROM dbo.admin_users ORDER BY id);
DECLARE @p01      BIGINT = (SELECT id FROM dbo.pois WHERE code = 'P01');
DECLARE @p02      BIGINT = (SELECT id FROM dbo.pois WHERE code = 'P02');
DECLARE @p03      BIGINT = (SELECT id FROM dbo.pois WHERE code = 'P03');
DECLARE @p04      BIGINT = (SELECT id FROM dbo.pois WHERE code = 'P04');
DECLARE @v_p02_vi BIGINT = (SELECT narration_version_id FROM dbo.publications WHERE poi_id = @p02 AND language_code = 'vi');
DECLARE @a_p02_vi BIGINT = (SELECT audio_asset_id       FROM dbo.publications WHERE poi_id = @p02 AND language_code = 'vi');
DECLARE @any_ver  BIGINT = (SELECT TOP (1) id FROM dbo.narration_versions ORDER BY id);
DECLARE @v        BIGINT;

-- T1  BR18: hai bản nháp mở cho cùng (điểm, ngôn ngữ)
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT INTO dbo.narration_versions (poi_id, language_code, version_no, source_version_id, title, body, text_origin, status, content_hash, created_by)
    VALUES (@p02, 'en', 901, @v_p02_vi, N'Test', N'Test', 'HUMAN', 'DRAFT', REPLICATE('0', 64), @adm);
    INSERT INTO dbo.narration_versions (poi_id, language_code, version_no, source_version_id, title, body, text_origin, status, content_hash, created_by)
    VALUES (@p02, 'en', 902, @v_p02_vi, N'Test', N'Test', 'HUMAN', 'DRAFT', REPLICATE('0', 64), @adm);
    ROLLBACK TRANSACTION;
    PRINT N'LỖI T1: thao tác lẽ ra phải bị chặn bởi UX_nv_one_draft nhưng lại thành công.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    IF ERROR_MESSAGE() LIKE N'%UX_nv_one_draft%'
    BEGIN
        PRINT N'OK   T1 - bị chặn bởi UX_nv_one_draft';
    END
    ELSE
    BEGIN
        PRINT N'LỖI T1: bị chặn nhưng bởi lỗi khác - ' + ERROR_MESSAGE();
    END;
END CATCH;

-- T2  BR25: hai bản đang xuất bản cho cùng (điểm, ngôn ngữ)
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT INTO dbo.narration_versions (poi_id, language_code, version_no, title, body, text_origin, status, content_hash, created_by)
    VALUES (@p01, 'vi', 903, N'Test', N'Test', 'SOURCE', 'PUBLISHED', REPLICATE('0', 64), @adm);
    ROLLBACK TRANSACTION;
    PRINT N'LỖI T2: thao tác lẽ ra phải bị chặn bởi UX_nv_one_published nhưng lại thành công.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    IF ERROR_MESSAGE() LIKE N'%UX_nv_one_published%'
    BEGIN
        PRINT N'OK   T2 - bị chặn bởi UX_nv_one_published';
    END
    ELSE
    BEGIN
        PRINT N'LỖI T2: bị chặn nhưng bởi lỗi khác - ' + ERROR_MESSAGE();
    END;
END CATCH;

-- T3  BR17: bản en, ko không có bản tiếng Việt nguồn
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT INTO dbo.narration_versions (poi_id, language_code, version_no, title, body, text_origin, status, content_hash, created_by)
    VALUES (@p03, 'ko', 904, N'Test', N'Test', 'HUMAN', 'DRAFT', REPLICATE('0', 64), @adm);
    ROLLBACK TRANSACTION;
    PRINT N'LỖI T3: thao tác lẽ ra phải bị chặn bởi CK_nv_source nhưng lại thành công.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    IF ERROR_MESSAGE() LIKE N'%CK_nv_source%'
    BEGIN
        PRINT N'OK   T3 - bị chặn bởi CK_nv_source';
    END
    ELSE
    BEGIN
        PRINT N'LỖI T3: bị chặn nhưng bởi lỗi khác - ' + ERROR_MESSAGE();
    END;
END CATCH;

-- T4  Âm thanh READY nhưng thiếu storage_key
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT INTO dbo.audio_assets (narration_version_id, origin, status, text_hash, created_by)
    VALUES (@any_ver, 'TTS', 'READY', REPLICATE('0', 64), @adm);
    ROLLBACK TRANSACTION;
    PRINT N'LỖI T4: thao tác lẽ ra phải bị chặn bởi CK_audio_ready nhưng lại thành công.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    IF ERROR_MESSAGE() LIKE N'%CK_audio_ready%'
    BEGIN
        PRINT N'OK   T4 - bị chặn bởi CK_audio_ready';
    END
    ELSE
    BEGIN
        PRINT N'LỖI T4: bị chặn nhưng bởi lỗi khác - ' + ERROR_MESSAGE();
    END;
END CATCH;

-- T5  BR34: hai tác vụ cùng dedup_key đang chờ
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT INTO dbo.processing_jobs (job_type, target_version_id, target_language, dedup_key, requested_by)
    VALUES ('TRANSLATE', @any_ver, 'ko', 'TEST:ko', @adm);
    INSERT INTO dbo.processing_jobs (job_type, target_version_id, target_language, dedup_key, requested_by)
    VALUES ('TRANSLATE', @any_ver, 'ko', 'TEST:ko', @adm);
    ROLLBACK TRANSACTION;
    PRINT N'LỖI T5: thao tác lẽ ra phải bị chặn bởi UX_job_pending nhưng lại thành công.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    IF ERROR_MESSAGE() LIKE N'%UX_job_pending%'
    BEGIN
        PRINT N'OK   T5 - bị chặn bởi UX_job_pending';
    END
    ELSE
    BEGIN
        PRINT N'LỖI T5: bị chặn nhưng bởi lỗi khác - ' + ERROR_MESSAGE();
    END;
END CATCH;

-- T6  Vĩ độ ngoài khoảng -90..90
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT INTO dbo.pois (code, internal_name, latitude, longitude)
    VALUES ('PX', N'Test', 91, 106.64);
    ROLLBACK TRANSACTION;
    PRINT N'LỖI T6: thao tác lẽ ra phải bị chặn bởi CK_pois_latitude nhưng lại thành công.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    IF ERROR_MESSAGE() LIKE N'%CK_pois_latitude%'
    BEGIN
        PRINT N'OK   T6 - bị chặn bởi CK_pois_latitude';
    END
    ELSE
    BEGIN
        PRINT N'LỖI T6: bị chặn nhưng bởi lỗi khác - ' + ERROR_MESSAGE();
    END;
END CATCH;

-- T7  Hai ngôn ngữ nguồn
BEGIN TRY
    BEGIN TRANSACTION;
    UPDATE dbo.languages SET is_source = 1 WHERE code = 'en';
    ROLLBACK TRANSACTION;
    PRINT N'LỖI T7: thao tác lẽ ra phải bị chặn bởi UX_languages_one_source nhưng lại thành công.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    IF ERROR_MESSAGE() LIKE N'%UX_languages_one_source%'
    BEGIN
        PRINT N'OK   T7 - bị chặn bởi UX_languages_one_source';
    END
    ELSE
    BEGIN
        PRINT N'LỖI T7: bị chặn nhưng bởi lỗi khác - ' + ERROR_MESSAGE();
    END;
END CATCH;

-- T8  [BỔ SUNG] Con trỏ xuất bản (P03, vi) trỏ tới phiên bản của điểm khác (P04)
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT INTO dbo.narration_versions (poi_id, language_code, version_no, title, body, text_origin, status, content_hash, created_by)
    VALUES (@p04, 'vi', 905, N'Test', N'Test', 'SOURCE', 'APPROVED', REPLICATE('0', 64), @adm);
    SET @v = SCOPE_IDENTITY();
    UPDATE dbo.publications SET narration_version_id = @v WHERE poi_id = @p03 AND language_code = 'vi';
    ROLLBACK TRANSACTION;
    PRINT N'LỖI T8: thao tác lẽ ra phải bị chặn bởi FK_publications_version_match nhưng lại thành công.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    IF ERROR_MESSAGE() LIKE N'%FK_publications_version_match%'
    BEGIN
        PRINT N'OK   T8 - bị chặn bởi FK_publications_version_match';
    END
    ELSE
    BEGIN
        PRINT N'LỖI T8: bị chặn nhưng bởi lỗi khác - ' + ERROR_MESSAGE();
    END;
END CATCH;

-- T9  [BỔ SUNG] Con trỏ xuất bản gắn âm thanh của phiên bản khác
BEGIN TRY
    BEGIN TRANSACTION;
    UPDATE dbo.publications SET audio_asset_id = @a_p02_vi WHERE poi_id = @p01 AND language_code = 'vi';
    ROLLBACK TRANSACTION;
    PRINT N'LỖI T9: thao tác lẽ ra phải bị chặn bởi FK_publications_audio_match nhưng lại thành công.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    IF ERROR_MESSAGE() LIKE N'%FK_publications_audio_match%'
    BEGIN
        PRINT N'OK   T9 - bị chặn bởi FK_publications_audio_match';
    END
    ELSE
    BEGIN
        PRINT N'LỖI T9: bị chặn nhưng bởi lỗi khác - ' + ERROR_MESSAGE();
    END;
END CATCH;

PRINT N'Hoàn tất kịch bản thử PHẦN 7.';
KẾT THÚC KHỐI THỬ */

/* =====================================================================================
   PHẦN 8. [TÙY CHỌN - ĐANG TẮT] XÓA CSDL ĐỂ LÀM LẠI TỪ ĐẦU  (CẨN THẬN: MẤT TOÀN BỘ DỮ LIỆU)
   -- USE master;
   -- ALTER DATABASE damsen_guide SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
   -- DROP DATABASE damsen_guide;
   ===================================================================================== */

-- Luôn bật lại thực thi (phòng khi chốt chặn ở đầu file đã bật NOEXEC).
SET NOEXEC OFF;
GO
