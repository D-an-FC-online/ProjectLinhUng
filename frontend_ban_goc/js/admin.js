/* ==========================================================================
   Dam Sen Park - Admin Controller Script (js/admin.js)
   ========================================================================== */

(function () {
  "use strict";

  /* ==================== 1. INITIAL DATA ==================== */
  const STORAGE_KEY = "damsen_admin_state_v1";

  const defaultLanguages = [
    { ma_ngon_ngu: "vi", ten_ngon_ngu: "Tiếng Việt", ten_tieng_viet: "Vietnamese", ma_giong_doc: "vi-VN-Standard-A", thu_tu: 1, la_mac_dinh: 1, dang_dung: 1, has_refs: true },
    { ma_ngon_ngu: "en", ten_ngon_ngu: "English", ten_tieng_viet: "English", ma_giong_doc: "en-US-Standard-C", thu_tu: 2, la_mac_dinh: 0, dang_dung: 1, has_refs: true },
    { ma_ngon_ngu: "es", ten_ngon_ngu: "Español", ten_tieng_viet: "Spanish", ma_giong_doc: "es-ES-Standard-A", thu_tu: 3, la_mac_dinh: 0, dang_dung: 1, has_refs: true },
    { ma_ngon_ngu: "ko", ten_ngon_ngu: "한국어", ten_tieng_viet: "Korean", ma_giong_doc: "ko-KR-Standard-A", thu_tu: 4, la_mac_dinh: 0, dang_dung: 1, has_refs: true },
    { ma_ngon_ngu: "zh", ten_ngon_ngu: "中文", ten_tieng_viet: "Chinese", ma_giong_doc: "cmn-CN-Standard-A", thu_tu: 5, la_mac_dinh: 0, dang_dung: 1, has_refs: true },
    { ma_ngon_ngu: "ru", ten_ngon_ngu: "Русский", ten_tieng_viet: "Russian", ma_giong_doc: "ru-RU-Standard-A", thu_tu: 6, la_mac_dinh: 0, dang_dung: 1, has_refs: true }
  ];

  const defaultSpots = [
    { id: 1, slug: "dam-sen-cultural-park", ten_diem: "Dam Sen Cultural Park", vi_do: 10.7685000, kinh_do: 106.6365000, ban_kinh_m: 80, hinh_anh: "images/destination-1.jpg", thu_tu: 1, dang_dung: 1, ngay_cap_nhat: "2026-11-20 09:15:00" },
    { id: 2, slug: "au-lac-square", ten_diem: "Au Lac Square", vi_do: 10.7689000, kinh_do: 106.6371000, ban_kinh_m: 45, hinh_anh: "images/destination-2.jpg", thu_tu: 2, dang_dung: 1, ngay_cap_nhat: "2026-11-20 10:00:00" },
    { id: 3, slug: "nam-tu-hanging-garden", ten_diem: "Nam Tu Hanging Garden", vi_do: 10.7678000, kinh_do: 106.6358000, ban_kinh_m: 50, hinh_anh: "images/destination-3.jpg", thu_tu: 3, dang_dung: 1, ngay_cap_nhat: "2026-11-21 08:30:00" }
  ];

  const defaultContents = {
    "1_vi": {
      tieu_de: "Welcome to Dam Sen Cultural Park",
      mo_ta_ngan: "Green entertainment and cultural complex in District 11, Ho Chi Minh City.",
      noi_dung: "Dam Sen Cultural Park spans over 50 hectares with lush botanical gardens and scenic lakes, offering a vibrant blend of Vietnamese culture and family attractions.",
      duong_dan_am_thanh: "audio/dam_sen_vi.mp3",
      am_thanh_hash: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
      trang_thai: "cong_bo"
    },
    "1_en": {
      tieu_de: "Welcome to Dam Sen Cultural Park",
      mo_ta_ngan: "Green recreational and cultural complex in District 11, HCMC.",
      noi_dung: "Spanning over 50 hectares with lush gardens and scenic lakes, Dam Sen Cultural Park offers a vibrant blend of Vietnamese heritage and modern attractions.",
      duong_dan_am_thanh: "audio/dam_sen_en.mp3",
      am_thanh_hash: "8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4",
      trang_thai: "cong_bo"
    }
  };

  const historyLogs = [
    { ma_su_kien: "EVT-90124", ma_phien: "SES-A81F2", ma_ngon_ngu: "vi", diem_id: 1, loai_thiet_bi: "Mobile", thoi_gian: "2026-11-21 10:14:22" },
    { ma_su_kien: "EVT-90125", ma_phien: "SES-B44C9", ma_ngon_ngu: "en", diem_id: 1, loai_thiet_bi: "Mobile", thoi_gian: "2026-11-21 10:28:05" },
    { ma_su_kien: "EVT-90126", ma_phien: "SES-C19E0", ma_ngon_ngu: "ko", diem_id: 2, loai_thiet_bi: "Mobile", thoi_gian: "2026-11-21 11:02:49" },
    { ma_su_kien: "EVT-90127", ma_phien: "SES-D77A1", ma_ngon_ngu: "zh", diem_id: 3, loai_thiet_bi: "Tablet", thoi_gian: "2026-11-21 11:45:10" },
    { ma_su_kien: "EVT-90128", ma_phien: "SES-E55B3", ma_ngon_ngu: "vi", diem_id: 2, loai_thiet_bi: "Desktop", thoi_gian: "2026-11-21 13:12:00" },
    { ma_su_kien: "EVT-90129", ma_phien: "SES-F02D8", ma_ngon_ngu: "es", diem_id: 1, loai_thiet_bi: "Mobile", thoi_gian: "2026-11-21 13:50:33" }
  ];

  function loadPersistedState() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      return JSON.parse(raw);
    } catch (_) {
      return null;
    }
  }

  function savePersistedState() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ languages, spots, contents }));
    } catch (_) {
      // Ignore storage errors
    }
  }

  const persisted = loadPersistedState();
  let languages = persisted?.languages || defaultLanguages;
  let spots = persisted?.spots || defaultSpots;
  let contents = persisted?.contents || defaultContents;
  let filteredHistory = [...historyLogs];
  let currentPage = 1;
  const pageSize = 5;

  /* ==================== 2. AUTHENTICATION & SESSION TIMEOUT ==================== */
  const MAX_FAILED_ATTEMPTS = 5;
  const LOCKOUT_MS = 15 * 60 * 1000;
  const INACTIVITY_TIMEOUT_SEC = 30 * 60;

  let failedAttempts = 0;
  let lockedUntil = 0;
  let inactivityRemaining = INACTIVITY_TIMEOUT_SEC;
  let sessionInterval = null;

  const loginScreen = document.getElementById("login-screen");
  const adminDashboard = document.getElementById("admin-dashboard");
  const loginAlert = document.getElementById("login-alert");

  function showLoginError(msg) {
    if (!loginAlert) return;
    loginAlert.textContent = msg;
    loginAlert.classList.remove("d-none");
  }

  function startSessionTimer() {
    clearInterval(sessionInterval);
    inactivityRemaining = INACTIVITY_TIMEOUT_SEC;
    sessionInterval = setInterval(() => {
      inactivityRemaining--;
      const mins = String(Math.floor(inactivityRemaining / 60)).padStart(2, "0");
      const secs = String(inactivityRemaining % 60).padStart(2, "0");
      const badge = document.getElementById("session-timer-badge");
      if (badge) {
        badge.innerHTML = `<i class="fa fa-clock-o mr-1"></i>Session: ${mins}:${secs}`;
      }
      if (inactivityRemaining <= 0) {
        logoutAdmin("Session expired after 30 minutes of inactivity. Please sign in again.");
      }
    }, 1000);
  }

  ["click", "keydown", "mousemove"].forEach((evt) => {
    document.addEventListener(evt, () => {
      if (adminDashboard && !adminDashboard.classList.contains("d-none")) {
        inactivityRemaining = INACTIVITY_TIMEOUT_SEC;
      }
    });
  });

  function logoutAdmin(reasonMsg) {
    clearInterval(sessionInterval);
    sessionStorage.removeItem("damsen_admin_session");
    if (adminDashboard) adminDashboard.classList.add("d-none");
    if (loginScreen) loginScreen.classList.remove("d-none");
    const passEl = document.getElementById("login-password");
    if (passEl) passEl.value = "";
    if (reasonMsg) {
      showLoginError(reasonMsg);
    } else if (loginAlert) {
      loginAlert.classList.add("d-none");
    }
  }

  const loginForm = document.getElementById("admin-login-form");
  if (loginForm) {
    loginForm.addEventListener("submit", function (e) {
      e.preventDefault();
      const now = Date.now();
      if (now < lockedUntil) {
        const remainMin = Math.ceil((lockedUntil - now) / 60000);
        showLoginError(`Account temporarily locked after 5 failed attempts. Try again in ${remainMin} minute(s).`);
        return;
      }

      const user = document.getElementById("login-username").value.trim();
      const pass = document.getElementById("login-password").value;

      if (user === "admin" && pass === "admin123") {
        failedAttempts = 0;
        if (loginAlert) loginAlert.classList.add("d-none");
        sessionStorage.setItem("damsen_admin_session", JSON.stringify({ user, token: "SESS-" + Date.now() }));
        const nameEl = document.getElementById("current-admin-name");
        if (nameEl) nameEl.textContent = user;
        loginScreen.classList.add("d-none");
        adminDashboard.classList.remove("d-none");
        startSessionTimer();
        initAllModules();
      } else {
        failedAttempts++;
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
          lockedUntil = Date.now() + LOCKOUT_MS;
          showLoginError("Too many failed attempts (5/5). Account locked for 15 minutes.");
        } else {
          showLoginError(`Invalid credentials (${failedAttempts}/${MAX_FAILED_ATTEMPTS} attempts).`);
        }
      }
    });
  }

  const logoutBtn = document.getElementById("btn-logout");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => logoutAdmin(""));
  }

  /* ==================== 3. ANALYTICS & HEATMAP ==================== */
  function renderStats() {
    const topSpots = [
      { name: "Dam Sen Cultural Park", count: 1120, pct: 52 },
      { name: "Au Lac Square", count: 640, pct: 30 },
      { name: "Nam Tu Hanging Garden", count: 390, pct: 18 }
    ];
    const topBars = document.getElementById("top-spots-bars");
    if (topBars) {
      topBars.innerHTML = topSpots.map((s) => `
        <div class="mb-3">
          <div class="d-flex justify-content-between small font-weight-bold mb-1">
            <span>${s.name}</span>
            <span>${s.count} listens (${s.pct}%)</span>
          </div>
          <div class="progress" style="height: 8px;">
            <div class="progress-bar" style="width: ${s.pct}%; background-color: #f15d30;"></div>
          </div>
        </div>
      `).join("");
    }

    const langStats = [
      { label: "Vietnamese (vi)", pct: 58, color: "#f15d30" },
      { label: "English (en)", pct: 24, color: "#17a2b8" },
      { label: "Korean (ko) / Chinese (zh)", pct: 12, color: "#28a745" },
      { label: "Spanish (es) / Russian (ru)", pct: 6, color: "#6c757d" }
    ];
    const langBars = document.getElementById("lang-ratio-bars");
    if (langBars) {
      langBars.innerHTML = langStats.map((l) => `
        <div class="mb-2">
          <div class="d-flex justify-content-between small mb-1">
            <span>${l.label}</span><span>${l.pct}%</span>
          </div>
          <div class="progress" style="height: 6px;">
            <div class="progress-bar" style="width: ${l.pct}%; background-color: ${l.color};"></div>
          </div>
        </div>
      `).join("");
    }

    const heatmapTable = document.getElementById("heatmap-table");
    if (heatmapTable) {
      const days = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
      const hours = ["08h", "10h", "12h", "14h", "16h", "18h"];
      const matrix = [
        [12, 34, 20, 28, 30, 15],
        [15, 40, 22, 31, 35, 18],
        [14, 38, 25, 33, 36, 20],
        [18, 45, 29, 39, 42, 22],
        [22, 55, 34, 48, 52, 28],
        [45, 95, 68, 88, 92, 54],
        [52, 110, 75, 96, 102, 60]
      ];
      let html = `<thead><tr><th>Day / Hour</th>${hours.map((h) => `<th>${h}</th>`).join("")}</tr></thead><tbody>`;
      days.forEach((d, rIdx) => {
        html += `<tr><td class="font-weight-bold">${d}</td>`;
        matrix[rIdx].forEach((val) => {
          const alpha = Math.min(0.15 + val / 120, 0.95).toFixed(2);
          const textColor = val > 65 ? "#fff" : "#212529";
          html += `<td><div class="heatmap-cell" style="background: rgba(241, 93, 48, ${alpha}); color: ${textColor};">${val}</div></td>`;
        });
        html += `</tr>`;
      });
      html += `</tbody>`;
      heatmapTable.innerHTML = html;
    }
  }

  /* ==================== 4. ACTIVITY LOGS & CSV EXPORT ==================== */
  function renderHistoryTable() {
    const tbody = document.getElementById("history-table-body");
    const infoEl = document.getElementById("history-pagination-info");
    if (!tbody) return;

    if (filteredHistory.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No records match the current filter.</td></tr>`;
      if (infoEl) infoEl.textContent = "Showing 0 results";
      return;
    }

    const totalPages = Math.max(1, Math.ceil(filteredHistory.length / pageSize));
    if (currentPage > totalPages) currentPage = totalPages;
    const startIdx = (currentPage - 1) * pageSize;
    const pageRows = filteredHistory.slice(startIdx, startIdx + pageSize);

    tbody.innerHTML = pageRows.map((row) => {
      const spotObj = spots.find((s) => s.id === row.diem_id);
      return `
        <tr>
          <td><code>${row.ma_su_kien}</code></td>
          <td>${row.ma_phien}</td>
          <td><span class="badge badge-light border">${row.ma_ngon_ngu.toUpperCase()}</span></td>
          <td>${spotObj ? spotObj.ten_diem : "N/A"}</td>
          <td>${row.loai_thiet_bi}</td>
          <td>${row.thoi_gian}</td>
        </tr>
      `;
    }).join("");

    if (infoEl) {
      infoEl.textContent = `Showing ${startIdx + 1}–${Math.min(startIdx + pageSize, filteredHistory.length)} of ${filteredHistory.length} results (Page ${currentPage}/${totalPages})`;
    }
  }

  const prevPageBtn = document.getElementById("btn-prev-page");
  const nextPageBtn = document.getElementById("btn-next-page");
  if (prevPageBtn) {
    prevPageBtn.addEventListener("click", () => {
      if (currentPage > 1) {
        currentPage--;
        renderHistoryTable();
      }
    });
  }
  if (nextPageBtn) {
    nextPageBtn.addEventListener("click", () => {
      const totalPages = Math.max(1, Math.ceil(filteredHistory.length / pageSize));
      if (currentPage < totalPages) {
        currentPage++;
        renderHistoryTable();
      }
    });
  }

  const applyFilterBtn = document.getElementById("btn-apply-history-filter");
  if (applyFilterBtn) {
    applyFilterBtn.addEventListener("click", () => {
      const fromVal = document.getElementById("filter-from").value;
      const toVal = document.getElementById("filter-to").value;
      const errBox = document.getElementById("history-filter-error");

      if (fromVal && toVal && new Date(fromVal) > new Date(toVal)) {
        if (errBox) {
          errBox.textContent = "Filter error: Start date/time cannot be later than end date/time.";
          errBox.classList.remove("d-none");
        }
        return;
      }
      if (errBox) errBox.classList.add("d-none");

      const langVal = document.getElementById("filter-lang").value;
      const spotVal = document.getElementById("filter-spot").value;

      filteredHistory = historyLogs.filter((item) => {
        if (langVal && item.ma_ngon_ngu !== langVal) return false;
        if (spotVal && String(item.diem_id) !== spotVal) return false;
        if (fromVal) {
          const itemTs = new Date(item.thoi_gian.replace(" ", "T")).getTime();
          if (itemTs < new Date(fromVal).getTime()) return false;
        }
        if (toVal) {
          const itemTs = new Date(item.thoi_gian.replace(" ", "T")).getTime();
          if (itemTs > new Date(toVal).getTime()) return false;
        }
        return true;
      });
      currentPage = 1;
      renderHistoryTable();
    });
  }

  const exportCsvBtn = document.getElementById("btn-export-csv");
  if (exportCsvBtn) {
    exportCsvBtn.addEventListener("click", () => {
      const headers = ["Event ID", "Session ID", "Language", "Location", "Device", "Timestamp (UTC+7)"];
      const rows = filteredHistory.map((r) => {
        const spotObj = spots.find((s) => s.id === r.diem_id);
        return [r.ma_su_kien, r.ma_phien, r.ma_ngon_ngu, spotObj ? spotObj.ten_diem : "", r.loai_thiet_bi, r.thoi_gian];
      });
      const csvContent = "\uFEFF" + [headers, ...rows].map((e) => e.map((v) => `"${v}"`).join(",")).join("\n");
      const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = `access_history_${new Date().toISOString().slice(0, 10)}.csv`;
      link.click();
    });
  }

  /* ==================== 5. LOCATIONS & GPS ZONES ==================== */
  function haversineMeters(lat1, lon1, lat2, lon2) {
    const R = 6371000;
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((lon2 - lon1) * Math.PI) / 180;
    const a =
      Math.sin(dLat / 2) ** 2 +
      Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) * Math.sin(dLon / 2) ** 2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  function renderSpotsTable() {
    const tbody = document.getElementById("admin-spots-tbody");
    if (!tbody) return;
    tbody.innerHTML = spots.map((s) => `
      <tr>
        <td>${s.thu_tu}</td>
        <td>
          <strong>${s.ten_diem}</strong><br>
          <small class="text-muted">/${s.slug}</small>
        </td>
        <td>
          <small>${s.vi_do}, ${s.kinh_do}</small><br>
          <span class="badge-brand">R = ${s.ban_kinh_m}m</span>
        </td>
        <td>
          ${s.dang_dung ? '<span class="badge badge-success">Active</span>' : '<span class="badge badge-secondary">Hidden</span>'}
        </td>
        <td>
          <button type="button" class="btn btn-xs btn-outline-primary btn-sm" onclick="window.__editSpot(${s.id})"><i class="fa fa-edit"></i></button>
          <button type="button" class="btn btn-xs btn-outline-danger btn-sm" onclick="window.__toggleSpot(${s.id})" title="Toggle visibility"><i class="fa fa-eye-slash"></i></button>
        </td>
      </tr>
    `).join("");
    syncDropdowns();
  }

  window.__editSpot = function (id) {
    const s = spots.find((x) => x.id === id);
    if (!s) return;
    document.getElementById("spot-form-title").textContent = "Edit Location #" + s.id;
    document.getElementById("spot-id").value = s.id;
    document.getElementById("spot-name").value = s.ten_diem;
    document.getElementById("spot-slug").value = s.slug;
    document.getElementById("spot-order").value = s.thu_tu;
    document.getElementById("spot-lat").value = s.vi_do;
    document.getElementById("spot-lng").value = s.kinh_do;
    document.getElementById("spot-radius").value = s.ban_kinh_m;
    document.getElementById("spot-image").value = s.hinh_anh;
    document.getElementById("spot-active").checked = !!s.dang_dung;
  };

  window.__toggleSpot = function (id) {
    const s = spots.find((x) => x.id === id);
    if (s) {
      s.dang_dung = s.dang_dung ? 0 : 1;
      s.ngay_cap_nhat = new Date().toISOString().replace("T", " ").slice(0, 19);
      savePersistedState();
      renderSpotsTable();
    }
  };

  function resetSpotForm() {
    const form = document.getElementById("spot-form");
    if (form) form.reset();
    document.getElementById("spot-id").value = "";
    document.getElementById("spot-radius").value = document.getElementById("cfg-default-radius")?.value || 50;
    document.getElementById("spot-form-title").textContent = "Add / Update Location";
    const warnBox = document.getElementById("overlap-warning");
    if (warnBox) {
      warnBox.classList.add("d-none");
      warnBox.dataset.confirmed = "false";
    }
  }

  document.getElementById("btn-new-spot")?.addEventListener("click", resetSpotForm);
  document.getElementById("btn-reset-spot")?.addEventListener("click", resetSpotForm);

  document.getElementById("spot-form")?.addEventListener("submit", function (e) {
    e.preventDefault();
    const idVal = document.getElementById("spot-id").value;
    const name = document.getElementById("spot-name").value.trim();
    const slug = document.getElementById("spot-slug").value.trim();
    const order = parseInt(document.getElementById("spot-order").value, 10) || 1;
    const lat = parseFloat(document.getElementById("spot-lat").value);
    const lng = parseFloat(document.getElementById("spot-lng").value);
    const radius = parseInt(document.getElementById("spot-radius").value, 10);
    const img = document.getElementById("spot-image").value.trim();
    const active = document.getElementById("spot-active").checked ? 1 : 0;
    const nowStr = new Date().toISOString().replace("T", " ").slice(0, 19);

    const overlapped = spots
      .filter((s) => String(s.id) !== String(idVal) && s.dang_dung === 1)
      .find((s) => haversineMeters(lat, lng, s.vi_do, s.kinh_do) < radius + s.ban_kinh_m);

    const warnBox = document.getElementById("overlap-warning");
    if (overlapped && warnBox && warnBox.dataset.confirmed !== "true") {
      warnBox.innerHTML = `<i class="fa fa-exclamation-triangle mr-1"></i> <strong>GPS Zone Overlap Warning:</strong> This zone overlaps with <em>${overlapped.ten_diem}</em>. Click "Save Location" again to keep the overlapping zone.`;
      warnBox.classList.remove("d-none");
      warnBox.dataset.confirmed = "true";
      return;
    }

    if (warnBox) {
      warnBox.classList.add("d-none");
      warnBox.dataset.confirmed = "false";
    }

    if (idVal) {
      const idx = spots.findIndex((s) => String(s.id) === String(idVal));
      if (idx > -1) {
        spots[idx] = { ...spots[idx], ten_diem: name, slug, thu_tu: order, vi_do: lat, kinh_do: lng, ban_kinh_m: radius, hinh_anh: img, dang_dung: active, ngay_cap_nhat: nowStr };
      }
    } else {
      const newId = spots.length ? Math.max(...spots.map((s) => s.id)) + 1 : 1;
      spots.push({ id: newId, ten_diem: name, slug, thu_tu: order, vi_do: lat, kinh_do: lng, ban_kinh_m: radius, hinh_anh: img, dang_dung: active, ngay_cap_nhat: nowStr });
    }

    savePersistedState();
    renderSpotsTable();
    resetSpotForm();
  });

  /* ==================== 6. CONTENT & AUDIO ==================== */
  function loadSelectedContent() {
    const spotId = document.getElementById("content-spot-select")?.value;
    const langCode = document.getElementById("content-lang-select")?.value;
    if (!spotId || !langCode) return;

    const item = contents[`${spotId}_${langCode}`] || {
      tieu_de: "",
      mo_ta_ngan: "",
      noi_dung: "",
      duong_dan_am_thanh: "",
      am_thanh_hash: "-",
      trang_thai: "nhap"
    };

    document.getElementById("content-title").value = item.tieu_de;
    document.getElementById("content-short").value = item.mo_ta_ngan;
    document.getElementById("content-body").value = item.noi_dung;
    document.getElementById("content-status").value = item.trang_thai;
    document.getElementById("content-audio-path").textContent = item.duong_dan_am_thanh || "No file attached";
    document.getElementById("content-audio-hash").textContent = item.am_thanh_hash || "-";
    document.getElementById("content-audio-player").src = item.duong_dan_am_thanh || "";
    document.getElementById("content-alert")?.classList.add("d-none");
  }

  document.getElementById("content-spot-select")?.addEventListener("change", loadSelectedContent);
  document.getElementById("content-lang-select")?.addEventListener("change", loadSelectedContent);

  document.getElementById("content-audio-file")?.addEventListener("change", async function (e) {
    const file = e.target.files[0];
    if (!file) return;
    const buf = await file.arrayBuffer();
    const hashBuf = await crypto.subtle.digest("SHA-256", buf);
    const hashHex = Array.from(new Uint8Array(hashBuf)).map((b) => b.toString(16).padStart(2, "0")).join("");

    document.getElementById("content-audio-path").textContent = "audio/" + file.name;
    document.getElementById("content-audio-hash").textContent = hashHex;
    document.getElementById("content-audio-player").src = URL.createObjectURL(file);
  });

  document.getElementById("btn-remove-audio")?.addEventListener("click", () => {
    document.getElementById("content-audio-path").textContent = "No file attached";
    document.getElementById("content-audio-hash").textContent = "-";
    document.getElementById("content-audio-player").src = "";
    document.getElementById("content-status").value = "nhap";
  });

  document.getElementById("content-form")?.addEventListener("submit", function (e) {
    e.preventDefault();
    const spotId = document.getElementById("content-spot-select").value;
    const langCode = document.getElementById("content-lang-select").value;
    const title = document.getElementById("content-title").value.trim();
    const shortDesc = document.getElementById("content-short").value.trim();
    const body = document.getElementById("content-body").value.trim();
    const status = document.getElementById("content-status").value;
    const audioPath = document.getElementById("content-audio-path").textContent;
    const audioHash = document.getElementById("content-audio-hash").textContent;
    const alertBox = document.getElementById("content-alert");

    if (status === "cong_bo" && (!title || !body || audioPath === "No file attached")) {
      if (alertBox) {
        alertBox.className = "alert alert-danger small";
        alertBox.textContent = "Cannot publish: Title, transcript text, and an audio file are all required.";
        alertBox.classList.remove("d-none");
      }
      return;
    }

    contents[`${spotId}_${langCode}`] = {
      tieu_de: title,
      mo_ta_ngan: shortDesc,
      noi_dung: body,
      duong_dan_am_thanh: audioPath === "No file attached" ? "" : audioPath,
      am_thanh_hash: audioHash,
      trang_thai: status
    };

    savePersistedState();
    if (alertBox) {
      alertBox.className = "alert alert-success small";
      alertBox.textContent = "Content and audio metadata saved successfully.";
      alertBox.classList.remove("d-none");
    }
  });

  /* ==================== 7. SETTINGS & QR CODE ==================== */
  document.getElementById("qr-preset-select")?.addEventListener("change", function () {
    document.getElementById("qr-url-input").value = this.value;
  });

  document.getElementById("btn-generate-qr")?.addEventListener("click", () => {
    const url = document.getElementById("qr-url-input").value.trim();
    if (!url) return;
    const encoded = encodeURIComponent(url);
    document.getElementById("qr-image-preview").src = `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encoded}`;
    document.getElementById("btn-download-qr").href = `https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=${encoded}`;
    document.getElementById("qr-url-caption").textContent = url;
  });

  document.getElementById("btn-save-cfg")?.addEventListener("click", () => {
    alert("Default trigger radius updated for new locations.");
  });

  /* ==================== 8. LANGUAGES ==================== */
  function renderLanguagesTable() {
    const tbody = document.getElementById("admin-langs-tbody");
    if (!tbody) return;
    tbody.innerHTML = languages.map((l) => `
      <tr>
        <td><code>${l.ma_ngon_ngu}</code></td>
        <td><strong>${l.ten_ngon_ngu}</strong></td>
        <td>${l.ten_tieng_viet}</td>
        <td><small>${l.ma_giong_doc || "-"}</small></td>
        <td>${l.la_mac_dinh ? '<span class="badge-brand">Default</span>' : '<span class="text-muted small">-</span>'}</td>
        <td>${l.dang_dung ? '<span class="badge badge-success">Enabled</span>' : '<span class="badge badge-secondary">Disabled</span>'}</td>
        <td>
          <button type="button" class="btn btn-xs btn-outline-primary btn-sm" onclick="window.__toggleLang('${l.ma_ngon_ngu}')">
            ${l.dang_dung ? "Disable" : "Enable"}
          </button>
        </td>
      </tr>
    `).join("");
    syncDropdowns();
  }

  window.__toggleLang = function (code) {
    const lang = languages.find((x) => x.ma_ngon_ngu === code);
    if (!lang) return;
    if (lang.la_mac_dinh && lang.dang_dung) {
      alert("Cannot disable the system default language.");
      return;
    }
    lang.dang_dung = lang.dang_dung ? 0 : 1;
    savePersistedState();
    renderLanguagesTable();
  };

  document.getElementById("lang-form")?.addEventListener("submit", function (e) {
    e.preventDefault();
    const code = document.getElementById("lang-code").value.trim().toLowerCase();
    if (languages.some((l) => l.ma_ngon_ngu === code)) {
      alert("Language code already exists.");
      return;
    }
    languages.push({
      ma_ngon_ngu: code,
      ten_ngon_ngu: document.getElementById("lang-native").value.trim(),
      ten_tieng_viet: document.getElementById("lang-vi").value.trim(),
      ma_giong_doc: document.getElementById("lang-voice").value.trim(),
      thu_tu: languages.length + 1,
      la_mac_dinh: 0,
      dang_dung: 0,
      has_refs: false
    });
    savePersistedState();
    this.reset();
    renderLanguagesTable();
  });

  /* ==================== 9. SYNC & INIT ==================== */
  function syncDropdowns() {
    const spotOptions = spots.map((s) => `<option value="${s.id}">${s.ten_diem}</option>`).join("");
    const contentSpotSelect = document.getElementById("content-spot-select");
    const filterSpotSelect = document.getElementById("filter-spot");
    const qrSelect = document.getElementById("qr-preset-select");
    const contentLangSelect = document.getElementById("content-lang-select");

    if (contentSpotSelect) contentSpotSelect.innerHTML = spotOptions;
    if (filterSpotSelect) filterSpotSelect.innerHTML = `<option value="">All locations</option>` + spotOptions;
    if (qrSelect) {
      qrSelect.innerHTML =
        `<option value="https://damsenpark.vn/guide/">Visitor Guide Home (Default)</option>` +
        spots.map((s) => `<option value="https://damsenpark.vn/guide/?spot=${s.slug}">${s.ten_diem} (/${s.slug})</option>`).join("");
    }
    if (contentLangSelect) {
      contentLangSelect.innerHTML = languages
        .map((l) => `<option value="${l.ma_ngon_ngu}">${l.ten_tieng_viet} (${l.ma_ngon_ngu}) ${l.dang_dung ? "" : "[Disabled]"}</option>`)
        .join("");
    }
  }

  function initAllModules() {
    renderStats();
    renderHistoryTable();
    renderSpotsTable();
    renderLanguagesTable();
    loadSelectedContent();
  }
})();