/* ==========================================================================
   Dam Sen Park - i18n (js/i18n.js)
   - Dịch các phần tử có data-i18n / data-i18n-placeholder
   - Nhớ ngôn ngữ đã chọn trong localStorage
   - Phát sự kiện "damsen:langchange" khi đổi ngôn ngữ
   - Thiếu bản dịch: dùng tiếng Anh (văn bản gốc của trang), rồi tới key
   ========================================================================== */
(function () {
  "use strict";

  var STORAGE_KEY = "damsen_lang";
  var DEFAULT_LANG = "en";

  var dict = {
    en: {
      nav_home: "Home",
      nav_gps: "GPS Location",
      nav_contact: "Contact",

      hero_subheading: "Welcome to Dam Sen Park",
      hero_title: "Discover Your Favorite Place with Us",
      attr_subheading: "Welcome to Dam Sen Park",
      attr_p1: "Dam Sen Cultural Park features 13 thrill rides (Roller Coaster, Waterfall Flume, Power Surge, and more), 5 modern virtual interactive attractions, 5 relaxing rides, 12 children's rides, and many other exciting games.",
      attr_p2: "Dam Sen is also known as a zoo capable of breeding rare and endangered species listed in the Red Book, such as Sumatran orangutans (which have successfully bred twice), yellow-cheeked gibbons, adjutant storks, and painted storks. In addition, the park features an Aquarium home to a rich variety of marine life and Amazonian fish, including sharks and 2-meter arapaima.",
      attr_btn: "Search Destination",

      footer_about_title: "About",
      footer_about_text: "A location-based multilingual audio guide that helps visitors find nearby attractions and listen to their stories in Vietnamese, English or Korean.",
      footer_info_title: "Information",
      footer_info_1: "Online Enquiry",
      footer_info_2: "General Enquiries",
      footer_info_3: "Booking Conditions",
      footer_info_4: "Privacy and Policy",
      footer_info_5: "Refund Policy",
      footer_info_6: "Call Us",
      footer_exp_title: "Experience",
      footer_exp_1: "Adventure",
      footer_exp_2: "Hotel and Restaurant",
      footer_exp_3: "Beach",
      footer_exp_4: "Nature",
      footer_exp_5: "Camping",
      footer_exp_6: "Party",
      footer_questions_title: "Contact",

      contact_breadcrumb_home: "Home",
      contact_breadcrumb_current: "Contact us",
      contact_title: "Contact us",
      contact_address_title: "Address",
      contact_phone_title: "Contact Number",
      contact_email_title: "Fanpage",
      contact_website_title: "Website",
      contact_intro_title: "Explore Dam Sen with the audio guide",
      contact_intro_text: "Find nearby attractions and listen to their stories in Vietnamese, English or Korean.",
      form_name_ph: "Your Name",
      form_email_ph: "Your Email",
      form_subject_ph: "Subject",
      form_message_ph: "Message",
      form_submit: "Send Message",

      dest_breadcrumb_home: "Home",
      dest_breadcrumb_current: "GPS Location",
      dest_title: "GPS Location",
      dest_subheading: "Navigation & Coordinates",
      dest_h2: "Find Your Current GPS Location",
      dest_desc: "Enable location access in your browser to view your real-time position on Google Maps and check distances to nearby attractions.",
      dest_status_ready: "Ready for GPS positioning",
      dest_substatus_ready: "Click \"Get Current Location\" or enable live tracking.",
      dest_btn_locate: "Get Current Location",
      dest_btn_watch_off: "Live Tracking: OFF",
      dest_btn_watch_on: "Live Tracking: ON",
      dest_btn_demo: "Sample Coordinates",
      dest_list_title: "Destinations & Distance From You",
      dest_list_hint: "Select a destination to show Google Maps directions from your location",
      dest_btn_clear_route: "Show My Pin Only",
      dest_not_located: "Not located",
      dest_status_acquiring: "Acquiring GPS location...",
      dest_status_acquiring_sub: "Please allow location access if your browser asks.",
      dest_status_acquired: "Location acquired",
      dest_status_acquired_sub: "Your position is shown on the map.",
      dest_err_title: "Unable to get location",
      dest_err_generic: "An unknown error occurred while getting your location.",
      dest_err_denied: "Location permission was denied. You can still browse the destinations below.",
      dest_err_unavailable: "Location information is unavailable.",
      dest_err_timeout: "Getting your location timed out. Please try again.",
      dest_err_no_geo: "Geolocation is not supported",
      dest_err_no_geo_sub: "Your browser does not support the Geolocation API.",
      dest_watch_starting: "Starting live tracking...",
      dest_watch_starting_sub: "Waiting for the first GPS position.",
      dest_watch_active: "Live tracking active",
      dest_watch_active_sub: "Your position updates automatically as you move.",
      dest_watch_stopped: "Live tracking stopped",
      dest_watch_stopped_sub: "Click \"Live Tracking\" to start again.",
      dest_watch_lost: "GPS signal lost",
      dest_demo_active: "Using sample coordinates",
      dest_demo_active_sub: "Sample position at Dam Sen Park (for testing without GPS).",

      // --- Trang GPS noi API (js/destination.js) ---
      card_aquarium: "Aquarium",
      card_birdgarden: "Bird Garden",
      card_looping: "Looping Carp",
      card_roller: "Roller Coaster",
      dest_accuracy: "Accuracy ±{m} m",
      dest_all_zones: "All areas",
      dest_btn_manual: "Browse manually",
      dest_close: "Close",
      dest_empty_list: "No attractions found.",
      dest_empty_nearby: "No attractions within {r} m. Walk a bit further or browse manually.",
      dest_fallback: "No translation in this language yet; showing Vietnamese.",
      dest_group_label: "These attractions are close together – choose the one you want to hear",
      dest_has_audio: "Audio available",
      dest_heard: "Listened",
      dest_https_hint: "Browsers only allow geolocation over HTTPS or on localhost.",
      dest_load_error: "Could not load data. Please check your connection or the server.",
      dest_loading: "Loading...",
      dest_manual_title: "Browse attractions by area",
      dest_next: "Next",
      dest_no_audio: "There is no audio guide for this attraction yet.",
      dest_page: "Page {p}/{n}",
      dest_prev: "Previous",
      dest_search_ph: "Search by name...",
      dest_select_hint: "Choose an attraction from the list to view and listen to its guide.",
      dest_warn_poor: "Low location accuracy (about {m} m) – distances may be off.",
      dest_warn_stale: "Location is out of date (measured {s} seconds ago) – check your GPS.",
      dest_btn_nearby: "Near me",
      dest_offline: "Offline – showing previously loaded content.",
      footer_address: "3 Hoa Binh St, Binh Thoi Ward, Ho Chi Minh City",
      footer_copyright: "Software Engineering course project – Group 09, Saigon University.",
      footer_link_admin: "Admin",
      footer_links_title: "Quick links",
      footer_spots_title: "Guided attractions"
    },

    vi: {
      nav_home: "Trang chủ",
      nav_gps: "Định vị GPS",
      nav_contact: "Liên hệ",

      hero_subheading: "Chào mừng đến Công viên Đầm Sen",
      hero_title: "Khám phá địa điểm yêu thích cùng chúng tôi",
      attr_subheading: "Chào mừng đến Công viên Đầm Sen",
      attr_p1: "Công viên Văn hóa Đầm Sen có 13 trò chơi cảm giác mạnh (Roller Coaster, Thác nước, Power Surge...), 5 trò chơi tương tác ảo hiện đại, 5 trò chơi thư giãn, 12 trò chơi thiếu nhi và nhiều trò chơi hấp dẫn khác.",
      attr_p2: "Đầm Sen còn được biết đến là vườn thú có khả năng nhân giống các loài quý hiếm trong Sách Đỏ như đười ươi Sumatra (đã sinh sản thành công hai lần), vượn má vàng, già đẫy, giang sen. Ngoài ra, công viên còn có Thủy cung với nhiều loài sinh vật biển và cá Amazon, gồm cá mập và cá hải tượng dài 2 mét.",
      attr_btn: "Tìm điểm tham quan",

      footer_about_title: "Giới thiệu",
      footer_about_text: "Hệ thống thuyết minh đa ngôn ngữ theo vị trí tại Công viên Văn hóa Đầm Sen.",
      footer_info_title: "Thông tin",
      footer_info_1: "Hỏi đáp trực tuyến",
      footer_info_2: "Thắc mắc chung",
      footer_info_3: "Điều kiện đặt vé",
      footer_info_4: "Chính sách bảo mật",
      footer_info_5: "Chính sách hoàn tiền",
      footer_info_6: "Gọi cho chúng tôi",
      footer_exp_title: "Trải nghiệm",
      footer_exp_1: "Phiêu lưu",
      footer_exp_2: "Khách sạn & Nhà hàng",
      footer_exp_3: "Bãi biển",
      footer_exp_4: "Thiên nhiên",
      footer_exp_5: "Cắm trại",
      footer_exp_6: "Tiệc",
      footer_questions_title: "Thông tin liên hệ",

      contact_breadcrumb_home: "Trang chủ",
      contact_breadcrumb_current: "Liên hệ",
      contact_title: "Liên hệ",
      contact_address_title: "Địa chỉ",
      contact_phone_title: "Số điện thoại",
      contact_email_title: "Fanpage",
      contact_website_title: "Website",
      contact_intro_title: "Tham quan Đầm Sen cùng thuyết minh âm thanh",
      contact_intro_text: "Tìm điểm tham quan gần bạn và nghe giới thiệu bằng tiếng Việt, tiếng Anh hoặc tiếng Hàn.",
      form_name_ph: "Họ tên",
      form_email_ph: "Email",
      form_subject_ph: "Tiêu đề",
      form_message_ph: "Nội dung",
      form_submit: "Gửi tin nhắn",

      dest_breadcrumb_home: "Trang chủ",
      dest_breadcrumb_current: "Định vị GPS",
      dest_title: "Định vị GPS",
      dest_subheading: "Điều hướng & Tọa độ",
      dest_h2: "Tìm vị trí GPS hiện tại của bạn",
      dest_desc: "Bật quyền truy cập vị trí trên trình duyệt để xem vị trí thời gian thực trên Google Maps và khoảng cách tới các điểm tham quan gần đó.",
      dest_status_ready: "Sẵn sàng định vị GPS",
      dest_substatus_ready: "Bấm \"Lấy vị trí hiện tại\" hoặc bật theo dõi liên tục.",
      dest_btn_locate: "Lấy vị trí hiện tại",
      dest_btn_watch_off: "Theo dõi liên tục: TẮT",
      dest_btn_watch_on: "Theo dõi liên tục: BẬT",
      dest_btn_demo: "Tọa độ mẫu",
      dest_list_title: "Điểm tham quan & khoảng cách",
      dest_list_hint: "Chọn một điểm để xem chỉ đường Google Maps từ vị trí của bạn",
      dest_btn_clear_route: "Chỉ hiện vị trí của tôi",
      dest_not_located: "Chưa định vị",
      dest_status_acquiring: "Đang lấy vị trí GPS...",
      dest_status_acquiring_sub: "Vui lòng cho phép truy cập vị trí nếu trình duyệt hỏi.",
      dest_status_acquired: "Đã lấy được vị trí",
      dest_status_acquired_sub: "Vị trí của bạn đang hiển thị trên bản đồ.",
      dest_err_title: "Không lấy được vị trí",
      dest_err_generic: "Đã xảy ra lỗi không xác định khi lấy vị trí.",
      dest_err_denied: "Bạn đã từ chối quyền vị trí. Bạn vẫn có thể xem danh sách điểm bên dưới.",
      dest_err_unavailable: "Không có thông tin vị trí.",
      dest_err_timeout: "Hết thời gian lấy vị trí. Vui lòng thử lại.",
      dest_err_no_geo: "Không hỗ trợ định vị",
      dest_err_no_geo_sub: "Trình duyệt của bạn không hỗ trợ Geolocation API.",
      dest_watch_starting: "Đang bật theo dõi liên tục...",
      dest_watch_starting_sub: "Đang chờ vị trí GPS đầu tiên.",
      dest_watch_active: "Đang theo dõi liên tục",
      dest_watch_active_sub: "Vị trí tự cập nhật khi bạn di chuyển.",
      dest_watch_stopped: "Đã dừng theo dõi",
      dest_watch_stopped_sub: "Bấm \"Theo dõi liên tục\" để bật lại.",
      dest_watch_lost: "Mất tín hiệu GPS",
      dest_demo_active: "Đang dùng tọa độ mẫu",
      dest_demo_active_sub: "Vị trí mẫu tại Công viên Đầm Sen (để thử khi không có GPS).",

      // --- Trang GPS noi API (js/destination.js) ---
      card_aquarium: "Thủy cung",
      card_birdgarden: "Vườn chim",
      card_looping: "Looping Carp",
      card_roller: "Roller Coaster",
      dest_accuracy: "Sai số ±{m} m",
      dest_all_zones: "Tất cả khu vực",
      dest_btn_manual: "Duyệt thủ công",
      dest_close: "Đóng",
      dest_empty_list: "Không tìm thấy điểm tham quan nào.",
      dest_empty_nearby: "Không có điểm tham quan nào trong bán kính {r} m. Hãy di chuyển thêm hoặc duyệt thủ công.",
      dest_fallback: "Chưa có bản dịch cho ngôn ngữ này, đang hiển thị tiếng Việt.",
      dest_group_label: "Các điểm nằm sát nhau – hãy chọn điểm bạn muốn nghe",
      dest_has_audio: "Có âm thanh",
      dest_heard: "Đã nghe",
      dest_https_hint: "Trình duyệt chỉ cho phép định vị trên HTTPS hoặc localhost.",
      dest_load_error: "Không tải được dữ liệu. Hãy kiểm tra kết nối mạng hoặc máy chủ.",
      dest_loading: "Đang tải...",
      dest_manual_title: "Duyệt điểm tham quan theo khu vực",
      dest_next: "Sau",
      dest_no_audio: "Chưa có âm thanh thuyết minh cho điểm này.",
      dest_page: "Trang {p}/{n}",
      dest_prev: "Trước",
      dest_search_ph: "Tìm theo tên điểm...",
      dest_select_hint: "Chọn một điểm trong danh sách để xem và nghe thuyết minh.",
      dest_warn_poor: "Vị trí kém chính xác (sai số khoảng {m} m) – khoảng cách có thể chưa đúng.",
      dest_warn_stale: "Vị trí đã cũ (đo cách đây {s} giây) – hãy kiểm tra GPS.",
      dest_btn_nearby: "Điểm gần tôi",
      dest_offline: "Mất kết nối – đang hiển thị nội dung đã tải trước đó.",
      footer_address: "Số 3 Hòa Bình, P. Bình Thới, TP. Hồ Chí Minh",
      footer_copyright: "Đồ án môn Công nghệ phần mềm – Nhóm 09, Trường Đại học Sài Gòn.",
      footer_link_admin: "Trang quản trị",
      footer_links_title: "Liên kết nhanh",
      footer_spots_title: "Điểm thuyết minh"
    },

    ko: {
      nav_home: "홈",
      nav_gps: "GPS 위치",
      nav_contact: "연락처",

      hero_subheading: "담센 공원에 오신 것을 환영합니다",
      hero_title: "우리와 함께 좋아하는 장소를 찾아보세요",
      attr_subheading: "담센 공원에 오신 것을 환영합니다",
      attr_p1: "담센 문화공원에는 13개의 스릴 놀이기구(롤러코스터, 워터폴 플룸, 파워 서지 등), 5개의 최신 가상 체험 시설, 5개의 휴식형 놀이기구, 12개의 어린이 놀이기구와 다양한 즐길 거리가 있습니다.",
      attr_p2: "담센은 수마트라 오랑우탄(두 번 번식 성공), 노랑뺨긴팔원숭이, 대머리황새, 채색황새 등 적색목록 희귀·멸종위기종을 번식시키는 동물원으로도 유명합니다. 또한 상어와 2미터 피라루쿠 등 다양한 해양 생물과 아마존 어류가 있는 수족관이 있습니다.",
      attr_btn: "명소 찾기",

      footer_about_title: "소개",
      footer_about_text: "담센 문화공원 위치 기반 다국어 오디오 가이드 시스템입니다.",
      footer_info_title: "정보",
      footer_info_1: "온라인 문의",
      footer_info_2: "일반 문의",
      footer_info_3: "예약 조건",
      footer_info_4: "개인정보 처리방침",
      footer_info_5: "환불 정책",
      footer_info_6: "전화 문의",
      footer_exp_title: "체험",
      footer_exp_1: "모험",
      footer_exp_2: "호텔 및 레스토랑",
      footer_exp_3: "해변",
      footer_exp_4: "자연",
      footer_exp_5: "캠핑",
      footer_exp_6: "파티",
      footer_questions_title: "연락처",

      contact_breadcrumb_home: "홈",
      contact_breadcrumb_current: "연락처",
      contact_title: "연락처",
      contact_address_title: "주소",
      contact_phone_title: "전화번호",
      contact_email_title: "팬페이지",
      contact_website_title: "웹사이트",
      contact_intro_title: "오디오 가이드와 함께 담센을 둘러보세요",
      contact_intro_text: "가까운 명소를 찾고 베트남어·영어·한국어로 해설을 들어 보세요.",
      form_name_ph: "이름",
      form_email_ph: "이메일",
      form_subject_ph: "제목",
      form_message_ph: "메시지",
      form_submit: "메시지 보내기",

      dest_breadcrumb_home: "홈",
      dest_breadcrumb_current: "GPS 위치",
      dest_title: "GPS 위치",
      dest_subheading: "내비게이션 및 좌표",
      dest_h2: "현재 GPS 위치 찾기",
      dest_desc: "브라우저에서 위치 접근을 허용하면 Google 지도에서 실시간 위치와 주변 명소까지의 거리를 확인할 수 있습니다.",
      dest_status_ready: "GPS 위치 확인 준비 완료",
      dest_substatus_ready: "\"현재 위치 가져오기\"를 누르거나 실시간 추적을 켜세요.",
      dest_btn_locate: "현재 위치 가져오기",
      dest_btn_watch_off: "실시간 추적: 끔",
      dest_btn_watch_on: "실시간 추적: 켬",
      dest_btn_demo: "샘플 좌표",
      dest_list_title: "명소 및 거리",
      dest_list_hint: "명소를 선택하면 현재 위치에서 Google 지도 길찾기를 보여줍니다",
      dest_btn_clear_route: "내 위치만 표시",
      dest_not_located: "위치 없음",
      dest_status_acquiring: "GPS 위치를 가져오는 중...",
      dest_status_acquiring_sub: "브라우저가 요청하면 위치 접근을 허용해 주세요.",
      dest_status_acquired: "위치 확인 완료",
      dest_status_acquired_sub: "지도에 현재 위치가 표시됩니다.",
      dest_err_title: "위치를 가져올 수 없음",
      dest_err_generic: "위치를 가져오는 중 알 수 없는 오류가 발생했습니다.",
      dest_err_denied: "위치 권한이 거부되었습니다. 아래 명소 목록은 계속 볼 수 있습니다.",
      dest_err_unavailable: "위치 정보를 사용할 수 없습니다.",
      dest_err_timeout: "위치 확인 시간이 초과되었습니다. 다시 시도해 주세요.",
      dest_err_no_geo: "위치 기능 미지원",
      dest_err_no_geo_sub: "이 브라우저는 Geolocation API를 지원하지 않습니다.",
      dest_watch_starting: "실시간 추적 시작 중...",
      dest_watch_starting_sub: "첫 GPS 위치를 기다리는 중입니다.",
      dest_watch_active: "실시간 추적 중",
      dest_watch_active_sub: "이동하면 위치가 자동으로 업데이트됩니다.",
      dest_watch_stopped: "실시간 추적 중지됨",
      dest_watch_stopped_sub: "\"실시간 추적\"을 눌러 다시 시작하세요.",
      dest_watch_lost: "GPS 신호 끊김",
      dest_demo_active: "샘플 좌표 사용 중",
      dest_demo_active_sub: "담센 공원의 샘플 위치입니다 (GPS 없이 테스트용).",

      // --- Trang GPS noi API (js/destination.js) ---
      card_aquarium: "수족관",
      card_birdgarden: "조류원",
      card_looping: "루핑 카프",
      card_roller: "롤러코스터",
      dest_accuracy: "오차 ±{m}m",
      dest_all_zones: "전체 구역",
      dest_btn_manual: "직접 둘러보기",
      dest_close: "닫기",
      dest_empty_list: "명소를 찾을 수 없습니다.",
      dest_empty_nearby: "반경 {r}m 안에 명소가 없습니다. 조금 더 이동하거나 직접 둘러보세요.",
      dest_fallback: "이 언어의 번역이 아직 없어 베트남어로 표시합니다.",
      dest_group_label: "서로 가까운 명소입니다 – 듣고 싶은 곳을 선택하세요",
      dest_has_audio: "오디오 있음",
      dest_heard: "들음",
      dest_https_hint: "브라우저는 HTTPS 또는 localhost에서만 위치 기능을 허용합니다.",
      dest_load_error: "데이터를 불러올 수 없습니다. 네트워크 또는 서버를 확인하세요.",
      dest_loading: "불러오는 중...",
      dest_manual_title: "구역별 명소 둘러보기",
      dest_next: "다음",
      dest_no_audio: "이 명소에는 아직 오디오 해설이 없습니다.",
      dest_page: "{p}/{n} 페이지",
      dest_prev: "이전",
      dest_search_ph: "이름으로 검색...",
      dest_select_hint: "목록에서 명소를 선택하면 해설을 보고 들을 수 있습니다.",
      dest_warn_poor: "위치 정확도가 낮습니다(오차 약 {m}m). 거리가 정확하지 않을 수 있습니다.",
      dest_warn_stale: "위치 정보가 오래되었습니다({s}초 전 측정). GPS를 확인하세요.",
      dest_btn_nearby: "내 주변",
      dest_offline: "연결이 끊겼습니다 – 이전에 불러온 내용을 표시합니다.",
      footer_address: "호찌민시 빈터이동 호아빈 거리 3번지",
      footer_copyright: "소프트웨어 공학 과목 프로젝트 – 09조, 사이공 대학교.",
      footer_link_admin: "관리자",
      footer_links_title: "바로가기",
      footer_spots_title: "해설 명소"
    }
  };

  function getSavedLang() {
    try {
      var saved = localStorage.getItem(STORAGE_KEY);
      if (saved && dict[saved]) return saved;
    } catch (_) {}
    return DEFAULT_LANG;
  }

  var currentLang = getSavedLang();

  function t(key, params) {
    var table = dict[currentLang] || {};
    var text;
    if (Object.prototype.hasOwnProperty.call(table, key)) text = table[key];
    else if (Object.prototype.hasOwnProperty.call(dict.en, key)) text = dict.en[key];
    else return key;
    // Thay {m}, {r}, {p}... bang gia tri truyen vao (dung cho js/destination.js)
    if (params) {
      Object.keys(params).forEach(function (k) { text = text.split("{" + k + "}").join(params[k]); });
    }
    return text;
  }

  function applyTranslations() {
    document.documentElement.setAttribute("lang", currentLang);

    var nodes = document.querySelectorAll("[data-i18n]");
    for (var i = 0; i < nodes.length; i++) {
      nodes[i].textContent = t(nodes[i].getAttribute("data-i18n"));
    }

    var phNodes = document.querySelectorAll("[data-i18n-placeholder]");
    for (var j = 0; j < phNodes.length; j++) {
      phNodes[j].setAttribute("placeholder", t(phNodes[j].getAttribute("data-i18n-placeholder")));
    }

    var label = document.getElementById("current-lang-label");
    if (label) label.textContent = currentLang.toUpperCase();
  }

  function setLang(lang) {
    if (!dict[lang]) return;
    currentLang = lang;
    try {
      localStorage.setItem(STORAGE_KEY, lang);
    } catch (_) {}
    applyTranslations();
    var evt;
    try {
      evt = new CustomEvent("damsen:langchange", { detail: { lang: lang } });
    } catch (_) {
      evt = document.createEvent("CustomEvent");
      evt.initCustomEvent("damsen:langchange", false, false, { lang: lang });
    }
    document.dispatchEvent(evt);
  }

  window.DamSenI18n = {
    t: t,
    setLang: setLang,
    getLang: function () { return currentLang; },
    lang: function () { return currentLang; },   // js/destination.js goi lang() de gui ?lang= len API
    apply: applyTranslations
  };

  document.addEventListener("click", function (e) {
    var target = e.target;
    while (target && target !== document) {
      if (target.classList && target.classList.contains("lang-option")) {
        e.preventDefault();
        setLang(target.getAttribute("data-lang"));
        return;
      }
      target = target.parentNode;
    }
  });

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", applyTranslations);
  } else {
    applyTranslations();
  }
})();
