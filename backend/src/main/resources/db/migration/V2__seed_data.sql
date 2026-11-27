/* ============================================================================
   V2__seed_data.sql - Dữ liệu mẫu cho demo (thay cho backend/insert_damsen_data (1).sql)
   - 3 ngôn ngữ, 2 khu vực, 4 điểm tham quan, bảng thuật ngữ
   - Bản thuyết minh vi/en/ko đã duyệt + file audio có sẵn trong data/audio
   - Đã xuất bản sẵn để trang khách có nội dung ngay
   - Tài khoản quản trị: admin / Admin@123  (HÃY ĐỔI sau khi đăng nhập lần đầu)
   ============================================================================ */

/* 1. Ngôn ngữ */
INSERT INTO dbo.languages (code, name, is_source) VALUES
 ('vi', N'Tiếng Việt', 1),
 ('en', N'English',    0),
 ('ko', N'한국어',      0);

/* 2. Khu vực (có tên hiển thị 3 ngôn ngữ) */
INSERT INTO dbo.zones (code, name_vi, name_en, name_ko, is_active) VALUES
 ('ZONES_GAMES',   N'Khu trò chơi cảm giác mạnh', N'Thrill Rides Area', N'스릴 놀이기구 구역', 1),
 ('ZONES_ANIMALS', N'Khu động vật',              N'Animal Area',       N'동물 구역',          1);

/* 3. Điểm tham quan (giữ tọa độ của nhóm; HÃY kiểm tra lại trên Google Maps) */
INSERT INTO dbo.pois (zone_id, code, latitude, longitude, radius_m, status)
SELECT z.id, v.code, v.lat, v.lng, v.radius_m, 'ACTIVE'
FROM (VALUES
  ('ZONES_GAMES',   'POI_ROLLER_COASTER', 10.763600, 106.633200, 30),
  ('ZONES_GAMES',   'POI_LOOPING_CARP',   10.763600, 106.633800, 30),
  ('ZONES_ANIMALS', 'POI_AQUARIUM',       10.762600, 106.633600, 40),
  ('ZONES_ANIMALS', 'POI_BIRD_GARDEN',    10.762600, 106.632600, 40)
) AS v(zone_code, code, lat, lng, radius_m)
JOIN dbo.zones z ON z.code = v.zone_code;

/* 4. Thuật ngữ */
INSERT INTO dbo.glossary (term, term_type, keep_original, translation_en, translation_ko, tts_alias_vi, tts_alias_en, tts_alias_ko, note) VALUES
 (N'Đầm Sen',        'PLACE', 1, N'Dam Sen',        N'담센',       NULL, N'Dam Sen', N'담 센', N'Tên công viên, giữ nguyên khi dịch; cần phiên âm tiếng Hàn để giọng đọc không sai.'),
 (N'Roller Coaster', 'RIDE',  1, N'Roller Coaster', N'롤러코스터', N'Rô-lơ Cốt-xtơ', NULL, NULL, N'Tên trò chơi, thường giữ nguyên tiếng Anh.'),
 (N'Looping Carp',   'RIDE',  1, N'Looping Carp',   N'루핑 카프',  N'Lu-pinh Cạp', NULL, NULL, N'"Carp" = cá chép nhưng là tên riêng của trò chơi nên không dịch nghĩa.'),
 (N'Aquarium',       'ZONE',  1, N'Aquarium',       N'수족관',     N'A-qua-ri-um', NULL, NULL, N'CHƯA CHỐT: có thể dùng "Thủy cung" trong bài tiếng Việt.'),
 (N'Bird Garden',    'ZONE',  1, N'Bird Garden',    N'버드 가든',  N'Bớt Gác-đần', NULL, NULL, N'CHƯA CHỐT: có thể dùng "Vườn chim" trong bài tiếng Việt.');

/* 5. Bản thuyết minh. content_hash = SHA-256 của UTF-8(title + xuống dòng + body),
      tính giống hệt cách backend Java tính (HashUtil.contentHash). */
CREATE TABLE #seed_text (
    poi_code VARCHAR(50), lang VARCHAR(5), title NVARCHAR(255), body NVARCHAR(MAX), audio_file VARCHAR(200)
);

INSERT INTO #seed_text VALUES
/* ---- Roller Coaster ---- */
('POI_ROLLER_COASTER', 'vi', N'Roller Coaster – Tàu lượn siêu tốc',
 N'Là trò chơi cảm giác mạnh tại CVVH Đầm Sen (khu B). Roller Coaster, hay còn gọi là "Tàu lượn siêu tốc", là đoàn tàu 24 chỗ ngồi được kéo lên độ cao 24 mét bằng tời xích, sau đó thả trôi xuống với tốc độ cao bằng quán tính. Đường ray được thiết kế với những cú ngoặt đột ngột, sườn dốc, và cả những đoạn lộn ngược, mang lại cảm giác hồi hộp mạnh mẽ cho người chơi.',
 'roller_coaster_vi.mp3'),
('POI_ROLLER_COASTER', 'en', N'Roller Coaster',
 N'A thrill ride at Dam Sen Cultural Park (Zone B). The Roller Coaster is a 24-seat train hauled up to a height of 24 metres by a chain lift, then released to race down at high speed under its own momentum. The track features sudden turns, steep drops and even upside-down sections, giving riders an intense rush of excitement.',
 'roller_coaster_en.mp3'),
('POI_ROLLER_COASTER', 'ko', N'롤러코스터 – 초고속 열차',
 N'담센 문화공원(B구역)에 있는 스릴 놀이기구입니다. “초고속 열차”라고도 불리는 롤러코스터는 24인승 열차가 체인 리프트로 24미터 높이까지 끌어올려진 뒤 관성으로 빠르게 내려가는 놀이기구입니다. 트랙에는 급격한 커브와 가파른 경사, 거꾸로 뒤집히는 구간까지 있어 탑승객에게 강렬한 스릴을 선사합니다.',
 'roller_coaster_ko.mp3'),
/* ---- Looping Carp (file audio tên pendulum_*) ---- */
('POI_LOOPING_CARP', 'vi', N'Looping Carp – Trò chơi con lắc cá chép',
 N'Là trò chơi cảm giác mạnh do Công ty Cổ phần Dịch vụ Du lịch Phú Thọ (Phuthotourist) đầu tư tại CVVH Đầm Sen, theo hình thức con lắc (Pendulum). Trò chơi gồm hai con tàu hình cá chép, đong đưa theo trục đứng và có thể đánh vòng đến 360 độ. Người chơi được đưa lên cao hơn 10 mét rồi rơi tự do xuống, bật lên nhiều vòng; đỉnh điểm là khi cặp cá chép nhào lộn ngược trên không, khiến người chơi phải hét lên vì phấn khích. Trò chơi nằm ở khu cảm giác mạnh, gần cổng số 1 đường Lạc Long Quân.',
 'pendulum_vi.mp3'),
('POI_LOOPING_CARP', 'en', N'Looping Carp – Carp Pendulum Ride',
 N'A thrill ride invested in by Phu Tho Tourist Service Joint Stock Company (Phuthotourist) at Dam Sen Cultural Park, built as a pendulum ride. It features two carp-shaped boats that swing on a vertical axis and can rotate a full 360 degrees. Riders are lifted more than 10 metres high, then drop in free fall and swing back up for several rounds; the climax comes when the pair of carp flip upside down in mid-air, making riders scream with excitement. The ride is located in the thrill-ride area, near Gate 1 on Lac Long Quan Street.',
 'pendulum_en.mp3'),
('POI_LOOPING_CARP', 'ko', N'루핑 카프 – 잉어 진자 놀이기구',
 N'푸토 관광서비스 주식회사(Phuthotourist)가 담센 문화공원에 투자한 진자(펜듈럼) 방식의 스릴 놀이기구입니다. 잉어 모양의 배 두 척이 수직 축을 따라 흔들리며 360도까지 회전할 수 있습니다. 탑승객은 10미터 이상 높이까지 올라갔다가 자유낙하한 뒤 여러 번 다시 솟구치며, 잉어 한 쌍이 공중에서 거꾸로 뒤집히는 순간이 하이라이트로 모두가 환호성을 지르게 됩니다. 이 놀이기구는 락롱꿘 거리 1번 게이트 근처의 스릴 놀이기구 구역에 있습니다.',
 'pendulum_ko.mp3'),
/* ---- Aquarium ---- */
('POI_AQUARIUM', 'vi', N'Thủy cung Đầm Sen (Aquarium)',
 N'Ra mắt năm 2013, Thủy cung Đầm Sen đặt tại vườn Nam Tú Thượng Uyển, có hình dáng một chú cua biển và được xây dựng theo tiêu chuẩn kỹ thuật hàng đầu thế giới, trên diện tích hơn 2.000 mét vuông. Bên trong chứa khoảng 2.500 mét khối nước biển lấy từ Vũng Tàu, được lọc tuần hoàn sinh học để duy trì môi trường sống tự nhiên cho hơn 6.000 cá thể. Du khách sẽ được chiêm ngưỡng nhiều loài cá đẹp, lạ mắt qua các mô hình như Hồ cá mập, Thành phố huyền thoại Atlantic, lưu vực sông Mê Kông và sông Amazon, cùng một số loài nguy cấp như Đào Nhung môi đỏ và Đào Nhung Phú Quốc.',
 'aquarium_vi.mp3'),
('POI_AQUARIUM', 'en', N'Dam Sen Aquarium',
 N'Opened in 2013, the Dam Sen Aquarium is located in the Nam Tu Thuong Uyen garden. Shaped like a sea crab, it was built to world-leading technical standards on an area of more than 2,000 square metres. Inside are about 2,500 cubic metres of seawater brought from Vung Tau and filtered through a biological recirculation system to maintain a natural habitat for more than 6,000 animals. Visitors can admire many beautiful and unusual fish in exhibits such as the Shark Tank, the legendary city of Atlantis, and the Mekong and Amazon river basins, along with endangered species such as the red-lipped Dao Nhung and the Phu Quoc Dao Nhung.',
 'aquarium_en.mp3'),
('POI_AQUARIUM', 'ko', N'담센 수족관 (Aquarium)',
 N'2013년에 문을 연 담센 수족관은 남뚜텅우옌 정원에 자리하고 있으며, 바닷게 모양으로 세계 최고 수준의 기술 기준에 따라 2,000제곱미터가 넘는 면적에 지어졌습니다. 내부에는 붕따우에서 가져온 약 2,500세제곱미터의 바닷물이 생물학적 순환 여과 방식으로 정화되어 6,000마리가 넘는 해양 생물에게 자연에 가까운 서식 환경을 제공합니다. 방문객은 상어 수조, 전설의 도시 아틀란티스, 메콩강과 아마존강 유역 등의 전시에서 아름답고 신기한 다양한 어종을 감상할 수 있으며, 붉은입술 다오늉, 푸꾸옥 다오늉 같은 멸종위기종도 만날 수 있습니다.',
 'aquarium_ko.mp3'),
/* ---- Bird Garden ---- */
('POI_BIRD_GARDEN', 'vi', N'Vườn chim Đầm Sen (Bird Garden)',
 N'Đây là thế giới hoang dã của các loài chim quý tại Vườn chim Đầm Sen. Khu nuôi chim được thiết kế như một chiếc lồng khổng lồ, nơi các loài chim xải cánh bay, kiếm ăn và sinh sống gần như trong môi trường tự nhiên. Bên cạnh các loài phổ biến như cò, chim trĩ đỏ, đà điểu, nơi đây còn nuôi nhiều loài quý hiếm thuộc nhóm 1B như hơn 40 con Giang Sen, 12 con chim cổ rắn, hơn 19 con già đẫy và 6 con Công Việt Nam. Đặc biệt, vườn chim đang nuôi một cặp thiên nga đen — loài thiên nga quý hiếm nổi tiếng chung thủy, không rời nhau nửa bước, được xem là biểu tượng của tình yêu.',
 'birdgarden_vi.mp3'),
('POI_BIRD_GARDEN', 'en', N'Dam Sen Bird Garden',
 N'Welcome to the wild world of rare birds at the Dam Sen Bird Garden. The aviary is designed like a giant cage where birds spread their wings, forage and live in an almost natural environment. Alongside common species such as egrets, red pheasants and ostriches, the garden also keeps many rare species in protected group 1B, including more than 40 painted storks, 12 darters, more than 19 lesser adjutants and 6 Vietnamese green peafowl. In particular, the garden is home to a pair of black swans — a rare swan famous for its lifelong loyalty, never straying from its partner, and seen as a symbol of love.',
 'birdgarden_en.mp3'),
('POI_BIRD_GARDEN', 'ko', N'담센 조류원 (Bird Garden)',
 N'담센 조류원에서 진귀한 새들의 야생 세계를 만나 보세요. 조류 사육장은 거대한 새장처럼 설계되어 새들이 날개를 펴고 먹이를 찾으며 자연에 가까운 환경에서 살아갑니다. 백로, 붉은꿩, 타조 같은 흔한 종 외에도 보호종 1B군에 속하는 희귀종이 많이 사육되고 있는데, 40마리가 넘는 그림황새, 12마리의 뱀목가마우지, 19마리가 넘는 작은무수리황새, 6마리의 베트남 녹색공작이 있습니다. 특히 이곳에는 평생 짝을 떠나지 않는 충성심으로 유명하여 사랑의 상징으로 여겨지는 희귀한 흑고니 한 쌍이 살고 있습니다.',
 'birdgarden_ko.mp3');

/* 5a. Bản tiếng Việt (bản gốc, source_version_id = NULL) */
INSERT INTO dbo.narration_versions (poi_id, language_code, version_no, source_version_id, title, body, status, content_hash)
SELECT p.id, s.lang, 1, NULL, s.title, s.body, 'APPROVED',
       LOWER(CONVERT(CHAR(64), HASHBYTES('SHA2_256',
            CONVERT(VARCHAR(MAX), (s.title + NCHAR(10) + s.body) COLLATE Latin1_General_100_CI_AS_SC_UTF8)), 2))
FROM #seed_text s JOIN dbo.pois p ON p.code = s.poi_code
WHERE s.lang = 'vi';

/* 5b. Bản Anh/Hàn, source_version_id trỏ về bản Việt */
INSERT INTO dbo.narration_versions (poi_id, language_code, version_no, source_version_id, title, body, status, content_hash)
SELECT p.id, s.lang, 1, vi.id, s.title, s.body, 'APPROVED',
       LOWER(CONVERT(CHAR(64), HASHBYTES('SHA2_256',
            CONVERT(VARCHAR(MAX), (s.title + NCHAR(10) + s.body) COLLATE Latin1_General_100_CI_AS_SC_UTF8)), 2))
FROM #seed_text s
JOIN dbo.pois p ON p.code = s.poi_code
JOIN dbo.narration_versions vi ON vi.poi_id = p.id AND vi.language_code = 'vi'
WHERE s.lang <> 'vi';

/* 6. Audio có sẵn (thư mục data/audio). text_hash = content_hash của văn bản tương ứng */
INSERT INTO dbo.audio_assets (narration_version_id, storage_key, status, source, text_hash)
SELECT nv.id, s.audio_file, 'READY', 'UPLOAD', nv.content_hash
FROM #seed_text s
JOIN dbo.pois p ON p.code = s.poi_code
JOIN dbo.narration_versions nv ON nv.poi_id = p.id AND nv.language_code = s.lang;

/* 7. Xuất bản tất cả */
INSERT INTO dbo.publications (language_code, poi_id, narration_version_id, audio_asset_id)
SELECT nv.language_code, nv.poi_id, nv.id, a.id
FROM dbo.narration_versions nv
JOIN dbo.audio_assets a ON a.narration_version_id = nv.id;

DROP TABLE #seed_text;

/* 8. Tài khoản quản trị: admin / Admin@123 (BCrypt) */
INSERT INTO dbo.admin_users (username, password_hash, role, is_active)
VALUES ('admin', '$2a$10$KpcKQW12V3vxg6le4Q1deO6NuiJ/x35rf8Jd2YIjVTIPGlpUvKQYG', 'ADMIN', 1);
