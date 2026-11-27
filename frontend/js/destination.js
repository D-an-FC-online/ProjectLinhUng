/* ==========================================================================
   js/destination.js - Trang khách: định vị, điểm gần, duyệt thủ công, xem & nghe
   Gọi API công khai của backend (/api/v1/public/...). Dữ liệu KHÔNG còn viết cứng trong code.
   UC02 (tìm điểm gần), UC03 (duyệt thủ công), UC04 (xem), UC05 (nghe).
   ========================================================================== */
(function () {
  "use strict";

  var API = "/api/v1/public";
  var RADIUS_M = 300;          // Story 1.2: bán kính mặc định
  var MOVE_REFRESH_M = 20;     // cập nhật danh sách khi di chuyển hơn 20 m
  var POOR_ACCURACY_M = 50;    // cảnh báo POOR khi sai số > 50 m
  var STALE_MS = 30000;        // cảnh báo STALE khi vị trí cũ hơn 30 giây
  var DEMO_POS = { lat: 10.7637, lng: 106.6333, accuracy: 10 };
  var PAGE_SIZE = 10;

  var I18N = window.DamSenI18n;
  function t(k, p) { return I18N ? I18N.t(k, p) : k; }
  function lang() { return I18N ? I18N.lang() : "vi"; }
  function $(id) { return document.getElementById(id); }

  var state = {
    mode: "nearby",          // "nearby" | "manual"
    pos: null,               // {lat, lng, accuracy, time}
    lastFetchPos: null,
    watchId: null,
    nearby: [],
    selectedId: null,
    manual: { zoneId: "", q: "", page: 0, total: 0 },
    status: { type: "", titleKey: "dest_status_ready", subKey: "dest_substatus_ready", raw: null }
  };

  /* ------------------------- Tiện ích ------------------------- */

  function escapeHtml(s) {
    return String(s == null ? "" : s).replace(/[&<>"']/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
    });
  }

  function distanceM(a, b) {
    var R = 6371000, toRad = Math.PI / 180;
    var dLat = (b.lat - a.lat) * toRad, dLng = (b.lng - a.lng) * toRad;
    var h = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(a.lat * toRad) * Math.cos(b.lat * toRad) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
    return R * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
  }

  function formatDistance(m) {
    return m < 1000 ? Math.round(m) + " m" : (m / 1000).toFixed(2) + " km";
  }

  /** Gọi API; khi mất mạng thì dùng bản đã lưu trước đó (Story 1.3: mất mạng vẫn giữ nội dung đã tải). */
  function getJson(path) {
    var cacheKey = "damsen_cache:" + path;
    return fetch(path, { headers: { Accept: "application/json" } })
      .then(function (res) {
        if (!res.ok) {
          return res.json().catch(function () { return {}; }).then(function (b) {
            var err = new Error(b.message || ("HTTP " + res.status));
            err.status = res.status;
            throw err;
          });
        }
        return res.json();
      })
      .then(function (data) {
        try { sessionStorage.setItem(cacheKey, JSON.stringify(data)); } catch (e) { /* bỏ qua */ }
        showNetWarning(null);
        return data;
      })
      .catch(function (err) {
        if (err.status) throw err; // lỗi từ máy chủ (404...), không dùng cache
        var cached = null;
        try { cached = sessionStorage.getItem(cacheKey); } catch (e) { /* bỏ qua */ }
        if (cached) { showNetWarning("dest_offline"); return JSON.parse(cached); }
        showNetWarning("dest_load_error");
        throw err;
      });
  }

  function showNetWarning(key) {
    var el = $("net-warning");
    if (!key) { el.classList.add("d-none"); return; }
    el.textContent = t(key);
    el.classList.remove("d-none");
  }

  /* ----------------- Điểm đã nghe (theo ngày tham quan) ----------------- */

  function heardKey() {
    var d = new Date();
    return "damsen_heard_" + d.getFullYear() + "-" + (d.getMonth() + 1) + "-" + d.getDate();
  }

  function heardSet() {
    try { return JSON.parse(localStorage.getItem(heardKey()) || "[]"); } catch (e) { return []; }
  }

  function markHeard(id) {
    var s = heardSet();
    if (s.indexOf(id) < 0) {
      s.push(id);
      try { localStorage.setItem(heardKey(), JSON.stringify(s)); } catch (e) { /* bỏ qua */ }
    }
  }

  /* ------------------------- Trạng thái GPS ------------------------- */

  function setStatus(type, titleKey, subKey, raw) {
    state.status = { type: type, titleKey: titleKey, subKey: subKey, raw: raw || null };
    renderStatus();
  }

  function renderStatus() {
    var s = state.status;
    $("gps-status-dot").className = "status-dot" + (s.type ? " " + s.type : "");
    $("gps-status-text").textContent = t(s.titleKey);
    $("gps-substatus").textContent = s.raw !== null ? s.raw : (s.subKey ? t(s.subKey) : "");
    $("gps-accuracy").textContent = state.pos && state.pos.accuracy ? t("dest_accuracy", { m: Math.round(state.pos.accuracy) }) : "";
    renderQualityWarning();
  }

  /** Cảnh báo POOR (sai số > 50 m) hoặc STALE (vị trí cũ hơn 30 giây). */
  function renderQualityWarning() {
    var el = $("gps-warning");
    var msgs = [];
    if (state.pos && state.mode === "nearby") {
      if (state.pos.accuracy > POOR_ACCURACY_M) msgs.push(t("dest_warn_poor", { m: Math.round(state.pos.accuracy) }));
      var age = Date.now() - state.pos.time;
      if (age > STALE_MS) msgs.push(t("dest_warn_stale", { s: Math.round(age / 1000) }));
    }
    el.textContent = msgs.join(" ");
    el.classList.toggle("d-none", msgs.length === 0);
  }

  function setWatchButton(on) {
    var btn = $("btn-watch");
    btn.classList.toggle("btn-success", on);
    btn.classList.toggle("btn-outline-success", !on);
    $("btn-watch-label").textContent = t(on ? "dest_btn_watch_on" : "dest_btn_watch_off");
  }

  function onPosition(p, titleKey, subKey) {
    state.pos = { lat: p.coords.latitude, lng: p.coords.longitude, accuracy: p.coords.accuracy, time: p.timestamp || Date.now() };
    setStatus("active", titleKey, subKey);
    updateMap();
    // Chỉ gọi lại API khi di chuyển hơn 20 m (hoặc lần đầu)
    if (!state.lastFetchPos || distanceM(state.lastFetchPos, state.pos) > MOVE_REFRESH_M) {
      loadNearby();
    }
  }

  function onGeoError(err) {
    var key = "dest_err_generic";
    if (err.code === 1) key = "dest_err_denied";
    else if (err.code === 2) key = "dest_err_unavailable";
    else if (err.code === 3) key = "dest_err_timeout";
    if (!window.isSecureContext) {
      setStatus("error", "dest_err_title", null, t(key) + " " + t("dest_https_hint"));
    } else {
      setStatus("error", "dest_err_title", key);
    }
    if (err.code === 1) setMode("manual"); // từ chối định vị -> chuyển sang duyệt thủ công
  }

  function locateOnce() {
    if (!navigator.geolocation) { setStatus("error", "dest_err_no_geo", "dest_err_no_geo_sub"); setMode("manual"); return; }
    setStatus("", "dest_status_acquiring", "dest_status_acquiring_sub");
    navigator.geolocation.getCurrentPosition(function (p) {
      state.lastFetchPos = null; // ép tải lại
      onPosition(p, "dest_status_acquired", "dest_status_acquired_sub");
    }, onGeoError, { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 });
  }

  function startWatch() {
    if (!navigator.geolocation) { setStatus("error", "dest_err_no_geo", "dest_err_no_geo_sub"); setMode("manual"); return; }
    if (state.watchId !== null) return;
    setWatchButton(true);
    setStatus("active", "dest_watch_starting", "dest_watch_starting_sub");
    state.watchId = navigator.geolocation.watchPosition(function (p) {
      onPosition(p, "dest_watch_active", "dest_watch_active_sub");
    }, function (err) {
      if (err.code === 1) { stopWatch(); onGeoError(err); return; }
      setStatus("error", "dest_watch_lost", null, err.message);
    }, { enableHighAccuracy: true, maximumAge: 5000, timeout: 20000 });
  }

  function stopWatch() {
    if (state.watchId !== null) navigator.geolocation.clearWatch(state.watchId);
    state.watchId = null;
    setWatchButton(false);
  }

  /* ------------------------- Bản đồ ------------------------- */

  var lastMapUrl = "";
  function setMap(url) {
    if (url !== lastMapUrl) { lastMapUrl = url; $("google-gps-map").src = url; }
  }

  function findSpot(id) {
    return state.nearby.concat(state.manualItems || []).filter(function (s) { return s.id === id; })[0];
  }

  function updateMap() {
    var hl = lang();
    var spot = state.selectedId ? findSpot(state.selectedId) : null;
    $("btn-clear-route").classList.toggle("d-none", !spot);
    if (spot && state.pos) {
      setMap("https://maps.google.com/maps?saddr=" + state.pos.lat + "," + state.pos.lng +
        "&daddr=" + spot.latitude + "," + spot.longitude + "&dirflg=w&hl=" + hl + "&output=embed");
    } else if (spot) {
      setMap("https://maps.google.com/maps?q=" + spot.latitude + "," + spot.longitude + "&hl=" + hl + "&z=18&output=embed");
    } else if (state.pos) {
      setMap("https://maps.google.com/maps?q=" + state.pos.lat + "," + state.pos.lng + "&hl=" + hl + "&z=17&output=embed");
    }
  }

  /* ---------------------- Danh sách điểm gần (UC02) ---------------------- */

  function loadNearby() {
    if (!state.pos) { renderNearby(); return; }
    state.lastFetchPos = { lat: state.pos.lat, lng: state.pos.lng };
    getJson(API + "/pois/nearby?lat=" + state.pos.lat + "&lng=" + state.pos.lng + "&radius=" + RADIUS_M + "&lang=" + lang())
      .then(function (items) { state.nearby = items; renderNearby(); })
      .catch(function () { renderNearby(); });
  }

  function spotCard(spot, heard) {
    var badges = "";
    if (spot.fallback) badges += ' <span class="badge-lang">VI</span>';
    if (spot.hasAudio) badges += ' <span class="fa fa-volume-up text-muted" title="' + escapeHtml(t("dest_has_audio")) + '"></span>';
    if (heard) badges += ' <span class="badge-heard">' + escapeHtml(t("dest_heard")) + "</span>";
    var dist = spot.distanceM != null ? formatDistance(spot.distanceM) : t("dest_not_located");
    return '<div class="spot-item' + (state.selectedId === spot.id ? " active" : "") + (heard ? " heard" : "") +
      '" data-id="' + spot.id + '" role="button" tabindex="0">' +
      '<div class="d-flex justify-content-between align-items-start"><div class="pr-2">' +
      '<strong class="d-block text-dark">' + escapeHtml(spot.title) + badges + "</strong>" +
      '<small class="text-muted">' + escapeHtml(spot.zoneName) + "</small></div>" +
      (spot.distanceM != null || state.mode === "nearby" ? '<span class="badge-distance">' + escapeHtml(dist) + "</span>" : "") +
      "</div></div>";
  }

  function renderNearby() {
    var box = $("spots-list");
    if (!state.pos) {
      box.innerHTML = '<p class="text-muted mb-0">' + escapeHtml(t("dest_substatus_ready")) + "</p>";
      return;
    }
    if (!state.nearby.length) {
      box.innerHTML = '<p class="text-muted mb-0">' + escapeHtml(t("dest_empty_nearby", { r: RADIUS_M })) + "</p>";
      return;
    }
    var heard = heardSet();
    // Điểm chưa nghe trước, đã nghe sau; trong mỗi phần giữ thứ tự gần -> xa (API đã sắp xếp)
    var sorted = state.nearby.slice().sort(function (a, b) {
      var ha = heard.indexOf(a.id) >= 0 ? 1 : 0, hb = heard.indexOf(b.id) >= 0 ? 1 : 0;
      return ha - hb || a.distanceM - b.distanceM;
    });
    var html = "", rendered = {};
    sorted.forEach(function (spot) {
      if (rendered[spot.id]) return;
      if (spot.groupId == null) {
        html += spotCard(spot, heard.indexOf(spot.id) >= 0);
        rendered[spot.id] = true;
        return;
      }
      // Các điểm cách nhau < 20 m: hiển thị chung một nhóm để khách tự chọn
      var members = sorted.filter(function (s) { return s.groupId === spot.groupId; });
      html += '<div class="spot-group"><div class="spot-group-label"><span class="fa fa-object-group mr-1"></span>' +
        escapeHtml(t("dest_group_label")) + "</div>";
      members.forEach(function (m) { html += spotCard(m, heard.indexOf(m.id) >= 0); rendered[m.id] = true; });
      html += "</div>";
    });
    box.innerHTML = html;
  }

  /* --------------------- Duyệt thủ công (UC03) --------------------- */

  function loadZones() {
    return getJson(API + "/zones?lang=" + lang()).then(function (zones) {
      var sel = $("zone-select");
      var current = state.manual.zoneId;
      sel.innerHTML = '<option value="">' + escapeHtml(t("dest_all_zones")) + "</option>" +
        zones.map(function (z) { return '<option value="' + z.id + '">' + escapeHtml(z.name) + "</option>"; }).join("");
      sel.value = current;
    }).catch(function () { /* đã hiện cảnh báo mạng */ });
  }

  function loadManual() {
    var m = state.manual;
    var url = API + "/pois?lang=" + lang() + "&page=" + m.page + "&size=" + PAGE_SIZE +
      (m.zoneId ? "&zoneId=" + m.zoneId : "") + (m.q ? "&q=" + encodeURIComponent(m.q) : "");
    getJson(url).then(function (page) {
      m.total = page.total;
      state.manualItems = page.items;
      renderManual(page);
    }).catch(function () { /* đã hiện cảnh báo mạng */ });
  }

  function renderManual(page) {
    var heard = heardSet();
    var items = page.items.map(function (s) {
      if (state.pos) s.distanceM = Math.round(distanceM(state.pos, { lat: s.latitude, lng: s.longitude }));
      return s;
    });
    $("manual-list").innerHTML = items.length
      ? items.map(function (s) { return spotCard(s, heard.indexOf(s.id) >= 0); }).join("")
      : '<p class="text-muted mb-0">' + escapeHtml(t("dest_empty_list")) + "</p>";
    var pages = Math.max(1, Math.ceil(page.total / PAGE_SIZE));
    $("page-info").textContent = t("dest_page", { p: page.page + 1, n: pages });
    $("btn-prev").disabled = page.page <= 0;
    $("btn-next").disabled = page.page + 1 >= pages;
  }

  function setMode(mode) {
    state.mode = mode;
    $("nearby-section").classList.toggle("d-none", mode !== "nearby");
    $("manual-section").classList.toggle("d-none", mode !== "manual");
    $("btn-mode-label").textContent = t(mode === "nearby" ? "dest_btn_manual" : "dest_btn_nearby");
    if (mode === "manual") { loadZones(); loadManual(); }
    renderQualityWarning();
  }

  /* --------------------- Xem & nghe (UC04, UC05) --------------------- */

  function openDetail(id) {
    state.selectedId = id;
    renderNearby();
    if (state.mode === "manual" && state.manualItems) renderManual({ items: state.manualItems, page: state.manual.page, total: state.manual.total });
    updateMap();
    $("detail-placeholder").classList.add("d-none");
    $("detail-content").classList.remove("d-none");
    $("detail-title").textContent = t("dest_loading");
    $("detail-body").textContent = "";
    getJson(API + "/pois/" + id + "?lang=" + lang()).then(function (d) {
      if (state.selectedId !== id) return;
      $("detail-zone").textContent = d.zoneName;
      $("detail-title").textContent = d.title;
      $("detail-body").textContent = d.body;
      $("detail-fallback").classList.toggle("d-none", !d.fallback);
      var audio = $("detail-audio");
      if (d.audioUrl) {
        if (audio.getAttribute("src") !== d.audioUrl) { audio.pause(); audio.setAttribute("src", d.audioUrl); }
        audio.dataset.poiId = String(id);
        audio.classList.remove("d-none");
        $("detail-no-audio").classList.add("d-none");
      } else {
        audio.pause();
        audio.removeAttribute("src");
        audio.classList.add("d-none");
        $("detail-no-audio").classList.remove("d-none");
        markHeard(id); // không có audio: đọc xong coi như đã nghe
      }
      if (window.innerWidth < 992) $("detail-panel").scrollIntoView({ behavior: "smooth", block: "start" });
    }).catch(function (err) {
      $("detail-title").textContent = err.message || t("dest_load_error");
    });
  }

  function closeDetail() {
    state.selectedId = null;
    $("detail-audio").pause();
    $("detail-content").classList.add("d-none");
    $("detail-placeholder").classList.remove("d-none");
    renderNearby();
    updateMap();
  }

  /* ------------------------- Sự kiện ------------------------- */

  function onListClick(e) {
    var item = e.target.closest(".spot-item");
    if (item) openDetail(Number(item.getAttribute("data-id")));
  }

  function onListKey(e) {
    if (e.key === "Enter" || e.key === " ") { e.preventDefault(); onListClick(e); }
  }

  ["spots-list", "manual-list"].forEach(function (id) {
    $(id).addEventListener("click", onListClick);
    $(id).addEventListener("keydown", onListKey);
  });

  // Nghe xong thì đánh dấu "Đã nghe" (lưu theo ngày tham quan); không tự phát lại
  $("detail-audio").addEventListener("ended", function () {
    var id = Number(this.dataset.poiId);
    if (id) { markHeard(id); renderNearby(); }
  });

  $("btn-detail-close").addEventListener("click", closeDetail);
  $("btn-clear-route").addEventListener("click", function () { state.selectedId = null; renderNearby(); updateMap(); });
  $("btn-locate").addEventListener("click", locateOnce);
  $("btn-watch").addEventListener("click", function () {
    if (state.watchId !== null) { stopWatch(); setStatus("", "dest_watch_stopped", "dest_watch_stopped_sub"); }
    else startWatch();
  });
  $("btn-demo").addEventListener("click", function () {
    stopWatch();
    setMode("nearby");
    state.lastFetchPos = null;
    onPosition({ coords: { latitude: DEMO_POS.lat, longitude: DEMO_POS.lng, accuracy: DEMO_POS.accuracy }, timestamp: Date.now() },
      "dest_demo_active", "dest_demo_active_sub");
  });
  $("btn-mode").addEventListener("click", function () { setMode(state.mode === "nearby" ? "manual" : "nearby"); });

  $("zone-select").addEventListener("change", function () { state.manual.zoneId = this.value; state.manual.page = 0; loadManual(); });
  var searchTimer = null;
  $("search-input").addEventListener("input", function () {
    var v = this.value;
    clearTimeout(searchTimer);
    searchTimer = setTimeout(function () { state.manual.q = v.trim(); state.manual.page = 0; loadManual(); }, 300);
  });
  $("btn-prev").addEventListener("click", function () { if (state.manual.page > 0) { state.manual.page--; loadManual(); } });
  $("btn-next").addEventListener("click", function () { state.manual.page++; loadManual(); });

  // Đổi ngôn ngữ: lấy lại nội dung đã xuất bản theo ngôn ngữ mới (không gọi dịch vụ dịch)
  document.addEventListener("damsen:langchange", function () {
    renderStatus();
    setWatchButton(state.watchId !== null);
    $("btn-mode-label").textContent = t(state.mode === "nearby" ? "dest_btn_manual" : "dest_btn_nearby");
    state.lastFetchPos = null;
    loadNearby();
    if (state.mode === "manual") { loadZones(); loadManual(); }
    if (state.selectedId) openDetail(state.selectedId);
  });

  // Kiểm tra vị trí cũ (STALE) định kỳ
  setInterval(renderQualityWarning, 5000);

  /* ------------------------- Khởi động ------------------------- */
  setWatchButton(false);
  renderNearby();
  startWatch(); // theo dõi liên tục (watchPosition) - giữ quyền vị trí và màn hình danh sách sau khi nghe
})();
