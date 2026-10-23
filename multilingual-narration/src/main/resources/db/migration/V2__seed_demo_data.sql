-- =====================================================================
-- V2: DỮ LIỆU MẪU PHỤC VỤ DEMO
--
-- !!! LƯU Ý: Đây là dữ liệu mẫu do nhóm biên soạn để demo, KHÔNG phải dữ liệu chính thức.
--   - Nội dung thuyết minh là bản tóm tắt ngắn, cần được kiểm chứng/biên tập trước khi dùng thật.
--   - Tọa độ là giá trị gần đúng.
--   - image_url và audio_url để NULL vì chưa có file ảnh/âm thanh thật (không bịa URL).
--   - Bản tiếng Nhật của Văn Miếu cố ý để DRAFT nhằm demo trạng thái PENDING.
--
-- Kịch bản demo được bao phủ:
--   location 1 (Văn Miếu)     : vi PUBLISHED, en PUBLISHED, ja DRAFT, ko/zh chưa có
--   location 2 (Dinh Độc Lập) : vi PUBLISHED, en PUBLISHED
--   location 3 (Phố cổ Hội An): vi PUBLISHED, en PUBLISHED
--   location 4 (Chợ Bến Thành): chưa có bản thuyết minh nào => xóa được (204)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Ngôn ngữ hỗ trợ ban đầu
-- ---------------------------------------------------------------------
INSERT INTO languages (id, code, name, native_name, is_active)
VALUES (1, 'vi', 'Tiếng Việt', 'Tiếng Việt', TRUE),
       (2, 'en', 'Tiếng Anh', 'English', TRUE),
       (3, 'ja', 'Tiếng Nhật', '日本語', TRUE),
       (4, 'ko', 'Tiếng Hàn', '한국어', TRUE),
       (5, 'zh', 'Tiếng Trung', '中文', TRUE);

-- ---------------------------------------------------------------------
-- Địa điểm mẫu (tọa độ gần đúng)
-- ---------------------------------------------------------------------
INSERT INTO locations (id, name, description, address, latitude, longitude, image_url)
VALUES (1, 'Văn Miếu – Quốc Tử Giám',
        'Quần thể di tích thờ Khổng Tử và là nơi đặt Quốc Tử Giám, được xem là trường đại học đầu tiên của Việt Nam.',
        '58 Quốc Tử Giám, Hà Nội', 21.027800, 105.835600, NULL),
       (2, 'Dinh Độc Lập',
        'Công trình kiến trúc tiêu biểu thế kỷ XX, gắn với sự kiện ngày 30/4/1975.',
        '135 Nam Kỳ Khởi Nghĩa, TP. Hồ Chí Minh', 10.777000, 106.695300, NULL),
       (3, 'Phố cổ Hội An',
        'Đô thị thương cảng cổ được UNESCO công nhận là Di sản văn hóa thế giới năm 1999.',
        'Khu phố cổ Hội An', 15.877000, 108.326000, NULL),
       (4, 'Chợ Bến Thành',
        'Khu chợ nổi tiếng ở trung tâm TP. Hồ Chí Minh, hoạt động tại vị trí hiện nay từ năm 1914.',
        'Chợ Bến Thành, TP. Hồ Chí Minh', 10.772500, 106.698000, NULL);

-- ---------------------------------------------------------------------
-- Nội dung thuyết minh mẫu
-- ---------------------------------------------------------------------
INSERT INTO narrations (id, location_id, language_id, title, content, audio_url, status)
VALUES
    -- Văn Miếu – Quốc Tử Giám / Tiếng Việt
    (1, 1, 1, 'Văn Miếu – Quốc Tử Giám: Trường đại học đầu tiên của Việt Nam',
     'Văn Miếu được xây dựng năm 1070 dưới triều vua Lý Thánh Tông để thờ Khổng Tử và các bậc hiền triết của Nho giáo. Năm 1076, vua Lý Nhân Tông cho lập Quốc Tử Giám bên cạnh Văn Miếu, nơi được xem là trường đại học đầu tiên của Việt Nam. Điểm nhấn của di tích là 82 tấm bia Tiến sĩ đặt trên lưng rùa đá, ghi danh những người đỗ đạt trong các khoa thi thời Lê và Mạc; các tấm bia này đã được UNESCO ghi danh là Di sản tư liệu thế giới. Ngày nay, Văn Miếu – Quốc Tử Giám là biểu tượng của truyền thống hiếu học và tôn sư trọng đạo của người Việt.',
     NULL, 'PUBLISHED'),

    -- Văn Miếu – Quốc Tử Giám / English
    (2, 1, 2, 'Temple of Literature – Imperial Academy: The first university of Vietnam',
     'The Temple of Literature was built in 1070 under Emperor Lý Thánh Tông to honour Confucius and Confucian scholars. In 1076, Emperor Lý Nhân Tông established the Imperial Academy (Quốc Tử Giám) next to the temple, and it is regarded as the first university of Vietnam. The highlight of the site is a collection of 82 stone steles mounted on stone turtles, recording the names of doctoral laureates of the royal examinations held under the Lê and Mạc dynasties; these steles are inscribed in the UNESCO Memory of the World Register. Today the site is a symbol of the Vietnamese tradition of learning and respect for teachers.',
     NULL, 'PUBLISHED'),

    -- Văn Miếu – Quốc Tử Giám / 日本語 (BẢN NHÁP - để demo trạng thái PENDING)
    (3, 1, 3, '文廟・国子監：ベトナム最初の大学',
     '文廟は1070年、李朝の李聖宗の治世に孔子を祀るために建てられました。1076年には李仁宗が隣に国子監を設立し、ベトナム最初の大学とされています。境内には黎朝・莫朝の科挙合格者の名を刻んだ82基の進士碑が石の亀の上に並び、ユネスコの「世界の記憶」に登録されています。',
     NULL, 'DRAFT'),

    -- Dinh Độc Lập / Tiếng Việt
    (4, 2, 1, 'Dinh Độc Lập – Chứng nhân lịch sử',
     'Dinh Độc Lập (còn gọi là Hội trường Thống Nhất) được xây dựng từ năm 1962 đến năm 1966 theo thiết kế của kiến trúc sư Ngô Viết Thụ, trên nền Dinh Norodom thời Pháp thuộc. Công trình kết hợp hài hòa kiến trúc hiện đại với các yếu tố truyền thống phương Đông. Trưa ngày 30 tháng 4 năm 1975, xe tăng của Quân Giải phóng húc đổ cổng chính, đánh dấu thời khắc kết thúc chiến tranh và mở ra giai đoạn thống nhất đất nước. Ngày nay, Dinh Độc Lập là Di tích quốc gia đặc biệt, mở cửa đón khách tham quan.',
     NULL, 'PUBLISHED'),

    -- Dinh Độc Lập / English
    (5, 2, 2, 'Independence Palace – A witness to history',
     'Independence Palace, also known as Reunification Hall, was built between 1962 and 1966 to a design by architect Ngô Viết Thụ, on the site of the former Norodom Palace from the French colonial period. The building blends modern architecture with traditional Eastern elements. Around noon on 30 April 1975, a Liberation Army tank crashed through the main gate, marking the end of the war and the beginning of national reunification. Today the palace is a Special National Relic and is open to visitors.',
     NULL, 'PUBLISHED'),

    -- Phố cổ Hội An / Tiếng Việt
    (6, 3, 1, 'Phố cổ Hội An – Thương cảng một thời',
     'Từ khoảng thế kỷ XV đến thế kỷ XIX, Hội An là thương cảng sầm uất, nơi thương nhân Nhật Bản, Trung Hoa và châu Âu đến buôn bán. Khu phố cổ còn lưu giữ hơn một nghìn công trình kiến trúc gỗ truyền thống như nhà cổ, hội quán, đình, chùa, cùng Chùa Cầu – cây cầu có mái do cộng đồng thương nhân Nhật Bản xây dựng vào khoảng cuối thế kỷ XVI, đầu thế kỷ XVII. Năm 1999, Đô thị cổ Hội An được UNESCO công nhận là Di sản văn hóa thế giới.',
     NULL, 'PUBLISHED'),

    -- Phố cổ Hội An / English
    (7, 3, 2, 'Hoi An Ancient Town – A historic trading port',
     'From roughly the 15th to the 19th century, Hội An was a busy trading port visited by Japanese, Chinese and European merchants. The ancient town preserves more than a thousand traditional timber-frame buildings, including old houses, assembly halls, communal houses and pagodas, as well as the Japanese Covered Bridge (Chùa Cầu), built by the Japanese merchant community around the late 16th or early 17th century. In 1999, Hội An Ancient Town was inscribed by UNESCO as a World Heritage Site.',
     NULL, 'PUBLISHED');
