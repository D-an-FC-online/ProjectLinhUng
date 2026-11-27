/* ============================================================================
   00_create_database.sql
   Chạy MỘT LẦN trong SSMS (kết nối vào server, database đang chọn: master).

   Script này chỉ tạo database rỗng + tài khoản cho ứng dụng.
   KHÔNG tạo bảng: các bảng và dữ liệu mẫu do ứng dụng Spring Boot tự tạo
   khi khởi động lần đầu (Flyway chạy các file trong
   backend/src/main/resources/db/migration).

   Yêu cầu: SQL Server phải bật "SQL Server and Windows Authentication mode"
   (Server > Properties > Security) và bật TCP/IP cổng 1433
   (SQL Server Configuration Manager). Xem README.md mục "Chạy không dùng Docker".
   ============================================================================ */
USE master;
GO

/* Nếu bạn ĐÃ có database DamSenAudioGuide tạo bằng tay trước đây (có sẵn bảng),
   Flyway sẽ báo lỗi "non-empty schema". Khi đó bỏ dấu comment 3 dòng dưới để xóa
   database cũ rồi chạy lại cả file. CẢNH BÁO: lệnh này xóa toàn bộ dữ liệu cũ. */
-- ALTER DATABASE DamSenAudioGuide SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
-- DROP DATABASE DamSenAudioGuide;
-- GO

IF DB_ID(N'DamSenAudioGuide') IS NULL
    CREATE DATABASE DamSenAudioGuide;
GO

/* Tài khoản đăng nhập dùng cho ứng dụng (trùng với backend/src/main/resources/application.yml).
   Đổi mật khẩu ở cả hai nơi nếu muốn. */
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = N'damsen_app')
    CREATE LOGIN damsen_app WITH PASSWORD = N'DamSen@2026', CHECK_POLICY = OFF, DEFAULT_DATABASE = DamSenAudioGuide;
GO

USE DamSenAudioGuide;
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'damsen_app')
    CREATE USER damsen_app FOR LOGIN damsen_app;
GO

/* db_owner để Flyway được phép tạo bảng. */
ALTER ROLE db_owner ADD MEMBER damsen_app;
GO

PRINT N'Đã tạo database DamSenAudioGuide và login damsen_app. Bây giờ hãy chạy ứng dụng backend.';
GO
