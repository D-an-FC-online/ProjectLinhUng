/* ==========================================================================
   js/admin.js - Trang quản trị, gọi API thật của backend (/api/v1/admin/...)
   Thay cho bản cũ lưu dữ liệu giả trong localStorage.
   Đăng nhập theo phiên (cookie), mọi yêu cầu thay đổi dữ liệu gửi kèm header X-XSRF-TOKEN.
   ========================================================================== */
(function () {
  "use strict";

  var LANG_NAMES = { vi: "Tiếng Việt", en: "English", ko: "한국어" };
  var POLL_MS = 3000; // Story 2.6: hỏi trạng thái tác vụ mỗi 3 giây

  var data = { zones: [], pois: [], currentPoiId: null, content: null, trackedJobs: {}, uploadVersionId: null };

  function $(id) { return document.getElementById(id); }

  function esc(s) {
    return String(s == null ? "" : s).replace(/[&<>"']/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
    });
  }

  function fmtTime(s) { return s ? String(s).replace("T", " ").slice(0, 16) : ""; }

  function badge(status) { return '<span class="status-badge st-' + esc(status) + '">' + esc(status) + "</span>"; }

  /* ----------------------------- Gọi API ----------------------------- */

  function csrfToken() {
    var m = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
    return m ? decodeURIComponent(m[1]) : "";
  }

  function api(method, url, body, isForm) {
    var opts = { method: method, credentials: "same-origin", headers: { Accept: "application/json" } };
    if (method !== "GET") opts.headers["X-XSRF-TOKEN"] = csrfToken();
    if (body !== undefined && body !== null) {
      if (isForm) { opts.body = body; } else { opts.headers["Content-Type"] = "application/json"; opts.body = JSON.stringify(body); }
    }
    return fetch(url, opts).then(function (res) {
      if (res.status === 401) { showLogin(); throw new Error("Phiên đăng nhập đã hết, vui lòng đăng nhập lại"); }
      if (res.status === 204) return null;
      return res.text().then(function (txt) {
        var json = null;
        try { json = txt ? JSON.parse(txt) : null; } catch (e) { /* không phải JSON */ }
        if (!res.ok) {
          var msg = (json && json.message) || (res.status === 403 ? "Không có quyền hoặc thiếu mã CSRF (hãy tải lại trang)" : "Lỗi " + res.status);
          throw new Error(msg);
        }
        return json;
      });
    });
  }

  function toast(msg, type) {
    var el = document.createElement("div");
    el.className = "alert alert-" + (type || "success") + " shadow-sm small";
    el.textContent = msg;
    $("toast-area").appendChild(el);
    setTimeout(function () { el.remove(); }, type === "danger" ? 8000 : 4000);
  }

  function fail(err) { toast(err.message || String(err), "danger"); }

  /* ----------------------------- Đăng nhập ----------------------------- */

  function showLogin() {
    $("admin-dashboard").classList.add("d-none");
    $("login-screen").classList.remove("d-none");
  }

  function showDashboard(me) {
    $("current-admin-name").textContent = me.username;
    $("login-screen").classList.add("d-none");
    $("admin-dashboard").classList.remove("d-none");
    loadAll();
  }

  $("login-form").addEventListener("submit", function (e) {
    e.preventDefault();
    $("login-alert").classList.add("d-none");
    api("GET", "/api/v1/auth/csrf")
      .then(function () {
        return api("POST", "/api/v1/auth/login", { username: $("login-username").value, password: $("login-password").value });
      })
      .then(function (me) { $("login-password").value = ""; showDashboard(me); })
      .catch(function (err) { $("login-alert").textContent = err.message; $("login-alert").classList.remove("d-none"); });
  });

  $("btn-logout").addEventListener("click", function () {
    api("POST", "/api/v1/auth/logout").catch(function () {}).then(function () { location.reload(); });
  });

  $("password-form").addEventListener("submit", function (e) {
    e.preventDefault();
    api("POST", "/api/v1/auth/change-password", { oldPassword: $("pw-old").value, newPassword: $("pw-new").value })
      .then(function () { toast("Đã đổi mật khẩu"); e.target.reset(); }).catch(fail);
  });

  /* ----------------------------- Tổng quan ----------------------------- */

  function loadOverview() {
    return api("GET", "/api/v1/admin/overview").then(function (o) {
      function stat(label, value, icon) {
        return '<div class="col-6 col-md-3 mb-3"><div class="stat-card"><div class="stat-label"><i class="fa ' + icon + ' mr-1"></i>' +
          esc(label) + "</div><h3>" + esc(value) + "</h3></div></div>";
      }
      var pub = {};
      o.publishedByLanguage.forEach(function (x) { pub[x.key] = x.count; });
      var jobsFailed = 0, jobsActive = 0;
      o.jobsByStatus.forEach(function (x) { if (x.key === "FAILED") jobsFailed = x.count; if (x.key === "QUEUED" || x.key === "RUNNING") jobsActive += x.count; });
      $("overview-stats").innerHTML =
        stat("Điểm tham quan", o.activePoiCount + " / " + o.poiCount, "fa-map-marker") +
        stat("Khu vực", o.zoneCount, "fa-th-large") +
        stat("Đã xuất bản vi / en / ko", (pub.vi || 0) + " / " + (pub.en || 0) + " / " + (pub.ko || 0), "fa-globe") +
        stat("Tác vụ đang chạy / lỗi", jobsActive + " / " + jobsFailed, "fa-tasks");
      $("overview-needs").innerHTML = o.needsUpdate.length
        ? '<ul class="mb-0">' + o.needsUpdate.map(function (n) {
            return '<li><a href="#" data-open-poi="' + n.poiId + '">' + esc(n.poiCode) + "</a> – " + esc(LANG_NAMES[n.languageCode] || n.languageCode) + "</li>";
          }).join("") + "</ul>"
        : '<p class="text-muted small mb-0">Không có bản dịch nào cần cập nhật.</p>';
      $("overview-mt-off").classList.toggle("d-none", o.machineTranslationEnabled);
    }).catch(fail);
  }

  document.addEventListener("click", function (e) {
    var a = e.target.closest("[data-open-poi]");
    if (!a) return;
    e.preventDefault();
    window.jQuery('a[href="#tab-pois"]').tab("show");
    openContent(Number(a.getAttribute("data-open-poi")));
  });

  /* ----------------------------- Khu vực ----------------------------- */

  function loadZones() {
    return api("GET", "/api/v1/admin/zones").then(function (zones) {
      data.zones = zones;
      $("zone-tbody").innerHTML = zones.map(function (z) {
        return "<tr><td><code>" + esc(z.code) + "</code></td><td>" + esc(z.nameVi) + '<br><small class="text-muted">' +
          esc(z.nameEn || "—") + " / " + esc(z.nameKo || "—") + "</small></td><td>" + z.poiCount + "</td><td>" +
          (z.active ? '<span class="status-badge st-APPROVED">Đang bật</span>' : '<span class="status-badge st-ARCHIVED">Đã tắt</span>') +
          '</td><td class="text-right"><button class="btn btn-xs btn-outline-primary" data-edit-zone="' + z.id + '">Sửa</button></td></tr>';
      }).join("");
      $("poi-zone").innerHTML = zones.map(function (z) { return '<option value="' + z.id + '">' + esc(z.code + " – " + z.nameVi) + "</option>"; }).join("");
    }).catch(fail);
  }

  function openZoneModal(z) {
    $("zone-id").value = z ? z.id : "";
    $("zone-code").value = z ? z.code : "";
    $("zone-name-vi").value = z ? z.nameVi : "";
    $("zone-name-en").value = z ? (z.nameEn || "") : "";
    $("zone-name-ko").value = z ? (z.nameKo || "") : "";
    $("zone-active").checked = z ? z.active : true;
    window.jQuery("#zone-modal").modal("show");
  }

  $("btn-new-zone").addEventListener("click", function () { openZoneModal(null); });
  $("zone-tbody").addEventListener("click", function (e) {
    var b = e.target.closest("[data-edit-zone]");
    if (b) openZoneModal(data.zones.filter(function (z) { return z.id === Number(b.getAttribute("data-edit-zone")); })[0]);
  });
  $("zone-form").addEventListener("submit", function (e) {
    e.preventDefault();
    var id = $("zone-id").value;
    var body = { code: $("zone-code").value.trim(), nameVi: $("zone-name-vi").value, nameEn: $("zone-name-en").value,
      nameKo: $("zone-name-ko").value, active: $("zone-active").checked };
    api(id ? "PUT" : "POST", "/api/v1/admin/zones" + (id ? "/" + id : ""), body)
      .then(function () { window.jQuery("#zone-modal").modal("hide"); toast("Đã lưu khu vực"); loadZones(); loadPois(); })
      .catch(fail);
  });

  /* ----------------------------- Điểm tham quan ----------------------------- */

  function loadPois() {
    return api("GET", "/api/v1/admin/pois").then(function (pois) {
      data.pois = pois;
      $("poi-tbody").innerHTML = pois.map(function (p) {
        var pubs = p.publishedLanguages.map(function (l) {
          var needs = p.needsUpdate.indexOf(l) >= 0;
          return '<span class="status-badge ' + (needs ? "st-FAILED" : "st-APPROVED") + '" title="' + (needs ? "Cần cập nhật" : "Đang phục vụ") + '">' +
            l.toUpperCase() + (needs ? " !" : "") + "</span>";
        }).join(" ");
        return "<tr><td><code>" + esc(p.code) + "</code></td><td>" + esc(p.titleVi || "(chưa xuất bản)") + "</td><td>" + esc(p.zoneCode) +
          '</td><td class="small">' + esc(p.latitude) + ", " + esc(p.longitude) + "<br>r = " + p.radiusM + " m</td><td>" +
          (p.status === "ACTIVE" ? '<span class="status-badge st-APPROVED">ACTIVE</span>' : '<span class="status-badge st-ARCHIVED">INACTIVE</span>') +
          "</td><td>" + (pubs || '<span class="text-muted small">—</span>') + '</td><td class="text-right text-nowrap">' +
          '<button class="btn btn-xs btn-outline-secondary mr-1" data-edit-poi="' + p.id + '">Sửa</button>' +
          '<button class="btn btn-xs btn-primary" data-content-poi="' + p.id + '">Nội dung</button></td></tr>';
      }).join("");
      $("job-filter-poi").innerHTML = '<option value="">Mọi điểm</option>' +
        pois.map(function (p) { return '<option value="' + p.id + '">' + esc(p.code) + "</option>"; }).join("");
    }).catch(fail);
  }

  function openPoiModal(p) {
    $("poi-modal-title").textContent = p ? "Sửa điểm " + p.code : "Thêm điểm tham quan";
    $("poi-id").value = p ? p.id : "";
    $("poi-zone").value = p ? p.zoneId : (data.zones[0] ? data.zones[0].id : "");
    $("poi-code").value = p ? p.code : "";
    $("poi-lat").value = p ? p.latitude : "";
    $("poi-lng").value = p ? p.longitude : "";
    $("poi-radius").value = p ? p.radiusM : 30;
    $("poi-status").value = p ? p.status : "ACTIVE";
    window.jQuery("#poi-modal").modal("show");
  }

  $("btn-new-poi").addEventListener("click", function () { openPoiModal(null); });
  $("poi-tbody").addEventListener("click", function (e) {
    var ed = e.target.closest("[data-edit-poi]"), ct = e.target.closest("[data-content-poi]");
    if (ed) openPoiModal(data.pois.filter(function (p) { return p.id === Number(ed.getAttribute("data-edit-poi")); })[0]);
    if (ct) openContent(Number(ct.getAttribute("data-content-poi")));
  });
  $("poi-form").addEventListener("submit", function (e) {
    e.preventDefault();
    var id = $("poi-id").value;
    var body = { zoneId: Number($("poi-zone").value), code: $("poi-code").value.trim(), latitude: $("poi-lat").value,
      longitude: $("poi-lng").value, radiusM: Number($("poi-radius").value), status: $("poi-status").value };
    api(id ? "PUT" : "POST", "/api/v1/admin/pois" + (id ? "/" + id : ""), body)
      .then(function (r) {
        window.jQuery("#poi-modal").modal("hide");
        toast("Đã lưu điểm tham quan");
        loadPois();
        if (!id) openContent(r.id);
      }).catch(fail);
  });

  /* ----------------------- Nội dung của một điểm ----------------------- */

  function openContent(poiId) {
    data.currentPoiId = poiId;
    $("content-panel").classList.remove("d-none");
    return reloadContent().then(function () { $("content-panel").scrollIntoView({ behavior: "smooth" }); });
  }

  function reloadContent() {
    if (!data.currentPoiId) return Promise.resolve();
    return api("GET", "/api/v1/admin/pois/" + data.currentPoiId + "/content").then(function (c) {
      data.content = c;
      $("content-poi-code").textContent = c.poi.code;
      var viApproved = (c.languages.filter(function (l) { return l.languageCode === "vi"; })[0] || { versions: [] })
        .versions.some(function (v) { return v.status === "APPROVED"; });
      $("content-langs").innerHTML = c.languages.map(function (l) { return renderLang(l, viApproved); }).join("");
    }).catch(fail);
  }

  function renderLang(l, viApproved) {
    var pub = l.publication;
    var head = '<div class="d-flex flex-wrap justify-content-between align-items-center mb-2" style="gap:6px"><h6 class="mb-0">' +
      esc(l.languageName) + " (" + l.languageCode + ")</h6><div>";
    if (pub) {
      head += '<span class="small mr-2">Đang phục vụ: phiên bản #' + pub.versionId + (pub.audioAssetId ? " + audio" : " (không audio)") + "</span>";
      if (pub.needsUpdate) head += '<span class="status-badge st-FAILED mr-2">Cần cập nhật</span>';
      head += '<button class="btn btn-xs btn-outline-danger mr-1" data-unpublish="' + l.languageCode + '">Gỡ xuất bản</button>';
    } else {
      head += '<span class="small text-muted mr-2">Chưa xuất bản' + (l.languageCode !== "vi" ? " (khách nhận bản tiếng Việt)" : "") + "</span>";
    }
    if (l.languageCode === "vi" || viApproved) {
      head += '<button class="btn btn-xs btn-outline-primary" data-new-draft="' + l.languageCode + '"><i class="fa fa-pencil mr-1"></i>Tạo bản nháp</button>';
    }
    head += "</div></div>";

    if (!l.versions.length) return '<div class="lang-card">' + head + '<p class="small text-muted mb-0">Chưa có phiên bản nào.</p></div>';

    var rows = l.versions.map(function (v) {
      var btns = [];
      if (v.status === "DRAFT") {
        btns.push('<button class="btn btn-xs btn-outline-primary" data-edit-version="' + v.id + '">Sửa / Duyệt</button>');
        btns.push('<button class="btn btn-xs btn-outline-secondary" data-discard="' + v.id + '">Bỏ nháp</button>');
      } else {
        btns.push('<button class="btn btn-xs btn-outline-secondary" data-view-version="' + v.id + '">Xem</button>');
        btns.push('<button class="btn btn-xs btn-outline-secondary" data-copy="' + v.id + '">Tạo bản nháp mới</button>');
      }
      if (v.status === "APPROVED") {
        if (!v.published) btns.push('<button class="btn btn-xs btn-success" data-publish="' + v.id + '" data-lang="' + v.languageCode + '">Xuất bản</button>');
        if (v.languageCode === "vi") {
          btns.push('<button class="btn btn-xs btn-outline-info" data-translate="' + v.id + '" data-target="en">Dịch → EN</button>');
          btns.push('<button class="btn btn-xs btn-outline-info" data-translate="' + v.id + '" data-target="ko">Dịch → KO</button>');
        }
        btns.push('<button class="btn btn-xs btn-outline-dark" data-tts="' + v.id + '">Tạo giọng đọc</button>');
        btns.push('<button class="btn btn-xs btn-outline-dark" data-upload="' + v.id + '">Tải audio</button>');
      }
      var audios = v.audios.map(function (a) {
        return '<div class="d-flex align-items-center small mt-1" style="gap:6px"><audio controls preload="none" src="' + esc(a.previewUrl) +
          '" style="height:28px;max-width:220px"></audio><span>' + esc(a.source) + " #" + a.id + "</span>" +
          (a.matchesText ? "" : '<span class="text-danger" title="Văn bản đã đổi sau khi tạo audio">không khớp văn bản</span>') +
          (v.status === "APPROVED" && pubAudioMismatch(v, a) ? '<button class="btn btn-xs btn-link p-0" data-publish="' + v.id + '" data-lang="' + v.languageCode + '" data-audio="' + a.id + '">dùng audio này</button>' : "") +
          "</div>";
      }).join("");
      return '<tr class="version-row' + (v.published ? " published" : "") + '"><td>#' + v.id + "<br><small>v" + v.versionNo + "</small></td><td>" +
        badge(v.status) + (v.published ? '<br><small class="text-success font-weight-bold">ĐANG XUẤT BẢN</small>' : "") +
        "</td><td><strong>" + esc(v.title) + '</strong><br><small class="text-muted">' + esc(v.body).slice(0, 140) + (v.body.length > 140 ? "…" : "") +
        (v.sourceVersionId ? "<br>Dịch từ bản Việt #" + v.sourceVersionId : "") + "</small>" + audios + '</td><td class="text-right" style="min-width:210px">' +
        btns.join(" ") + "</td></tr>";
    }).join("");
    return '<div class="lang-card">' + head + '<table class="table table-sm admin-table mb-0"><tbody>' + rows + "</tbody></table></div>";
  }

  function pubAudioMismatch(v, a) {
    if (!v.published || !a.matchesText) return false;
    var lang = data.content.languages.filter(function (l) { return l.languageCode === v.languageCode; })[0];
    return lang && lang.publication && lang.publication.audioAssetId !== a.id;
  }

  $("btn-content-refresh").addEventListener("click", reloadContent);

  $("content-langs").addEventListener("click", function (e) {
    var b = e.target.closest("button");
    if (!b) return;
    var poiId = data.currentPoiId;
    if (b.hasAttribute("data-new-draft")) return openVersionModal(null, b.getAttribute("data-new-draft"));
    if (b.hasAttribute("data-edit-version")) return openVersionModal(Number(b.getAttribute("data-edit-version")));
    if (b.hasAttribute("data-view-version")) return openVersionModal(Number(b.getAttribute("data-view-version")));
    if (b.hasAttribute("data-discard")) {
      if (!confirm("Bỏ bản nháp này?")) return;
      return api("POST", "/api/v1/admin/versions/" + b.getAttribute("data-discard") + "/discard").then(reloadContent).catch(fail);
    }
    if (b.hasAttribute("data-copy")) {
      return api("POST", "/api/v1/admin/versions/" + b.getAttribute("data-copy") + "/copy")
        .then(function (r) { toast("Đã tạo bản nháp #" + r.id); return reloadContent().then(function () { openVersionModal(r.id); }); }).catch(fail);
    }
    if (b.hasAttribute("data-publish")) {
      var body = { languageCode: b.getAttribute("data-lang"), versionId: Number(b.getAttribute("data-publish")) };
      if (b.hasAttribute("data-audio")) body.audioAssetId = Number(b.getAttribute("data-audio"));
      if (!confirm("Xuất bản phiên bản #" + body.versionId + " (" + body.languageCode + ") cho khách? Phiên bản đang phục vụ (nếu có) sẽ chuyển ARCHIVED.")) return;
      return api("POST", "/api/v1/admin/pois/" + poiId + "/publish", body)
        .then(function () { toast("Đã xuất bản"); reloadContent(); loadPois(); loadOverview(); }).catch(fail);
    }
    if (b.hasAttribute("data-unpublish")) {
      var lang = b.getAttribute("data-unpublish");
      if (!confirm("Gỡ xuất bản ngôn ngữ " + lang + "? " + (lang === "vi" ? "Khách sẽ không thấy điểm này nữa." : "Khách chọn ngôn ngữ này sẽ nhận bản tiếng Việt."))) return;
      return api("DELETE", "/api/v1/admin/pois/" + poiId + "/publications/" + lang)
        .then(function () { toast("Đã gỡ xuất bản"); reloadContent(); loadPois(); loadOverview(); }).catch(fail);
    }
    if (b.hasAttribute("data-translate")) {
      return api("POST", "/api/v1/admin/versions/" + b.getAttribute("data-translate") + "/translate", { targetLanguage: b.getAttribute("data-target") })
        .then(function (job) { toast("Đã tạo tác vụ dịch #" + job.id + ", kết quả sẽ là một bản nháp mới", "info"); trackJob(job); }).catch(fail);
    }
    if (b.hasAttribute("data-tts")) {
      return api("POST", "/api/v1/admin/versions/" + b.getAttribute("data-tts") + "/tts")
        .then(function (job) { toast("Đã tạo tác vụ giọng đọc #" + job.id, "info"); trackJob(job); }).catch(fail);
    }
    if (b.hasAttribute("data-upload")) {
      data.uploadVersionId = Number(b.getAttribute("data-upload"));
      $("audio-file-input").value = "";
      $("audio-file-input").click();
    }
  });

  $("audio-file-input").addEventListener("change", function () {
    var file = this.files[0];
    if (!file || !data.uploadVersionId) return;
    var fd = new FormData();
    fd.append("file", file);
    api("POST", "/api/v1/admin/versions/" + data.uploadVersionId + "/audio", fd, true)
      .then(function (r) { toast("Đã tải lên audio #" + r.id + ". Bấm \"Xuất bản\" (hoặc \"dùng audio này\") để khách nghe được."); reloadContent(); })
      .catch(fail);
  });

  /* ----------------------- Soạn / duyệt phiên bản ----------------------- */

  function sourceVi() {
    var vi = data.content.languages.filter(function (l) { return l.languageCode === "vi"; })[0];
    if (!vi) return null;
    var pubId = vi.publication && vi.publication.versionId;
    return vi.versions.filter(function (v) { return v.id === pubId; })[0] ||
      vi.versions.filter(function (v) { return v.status === "APPROVED"; })[0] || null;
  }

  function openVersionModal(versionId, newLang) {
    $("version-qa").innerHTML = "";
    var p = versionId ? api("GET", "/api/v1/admin/versions/" + versionId) : Promise.resolve(null);
    p.then(function (v) {
      var lang = v ? v.languageCode : newLang;
      var editable = !v || v.status === "DRAFT";
      $("version-id").value = v ? v.id : "";
      $("version-lang").value = lang;
      $("version-modal-title").textContent = (v ? "Phiên bản #" + v.id + " (v" + v.versionNo + ")" : "Bản nháp mới") + " – " + (LANG_NAMES[lang] || lang);
      $("version-status").innerHTML = v ? badge(v.status) + (editable ? "" : " Phiên bản đã duyệt bị khóa; muốn sửa hãy tạo bản nháp mới.") : "";
      $("version-title").value = v ? v.title : "";
      $("version-body").value = v ? v.body : "";
      $("version-title").readOnly = !editable;
      $("version-body").readOnly = !editable;
      $("btn-version-save").classList.toggle("d-none", !editable);
      $("btn-version-approve").classList.toggle("d-none", !editable);

      // So sánh song song bản gốc và bản dịch (Story 2.2)
      var src = null;
      if (lang !== "vi") {
        if (v && v.sourceVersionId) {
          var vi = data.content.languages.filter(function (l) { return l.languageCode === "vi"; })[0];
          src = vi.versions.filter(function (x) { return x.id === v.sourceVersionId; })[0];
        } else {
          src = sourceVi();
        }
      }
      $("version-source-col").classList.toggle("d-none", !src);
      if (src) { $("version-source-title").textContent = src.title + "  (#" + src.id + ")"; $("version-source-body").textContent = src.body; }
      window.jQuery("#version-modal").modal("show");
      if (v && editable) runQa(v.id);
    }).catch(fail);
  }

  function saveVersion() {
    var id = $("version-id").value;
    var body = { title: $("version-title").value, body: $("version-body").value };
    if (id) return api("PUT", "/api/v1/admin/versions/" + id, body).then(function () { return Number(id); });
    body.languageCode = $("version-lang").value;
    return api("POST", "/api/v1/admin/pois/" + data.currentPoiId + "/versions", body).then(function (r) {
      $("version-id").value = r.id;
      return r.id;
    });
  }

  function runQa(id) {
    return api("GET", "/api/v1/admin/versions/" + id + "/qa").then(function (qa) {
      $("version-qa").innerHTML = qa.issues.length
        ? '<div class="small font-weight-bold mb-1">Kiểm tra tự động (QA):</div>' + qa.issues.map(function (i) {
            return '<div class="alert alert-' + (i.level === "ERROR" ? "danger" : "warning") + ' py-1 px-2 mb-1 small">' +
              (i.level === "ERROR" ? "Lỗi nghiêm trọng: " : "Cảnh báo: ") + esc(i.message) + "</div>";
          }).join("")
        : '<div class="alert alert-success py-1 px-2 small mb-0">QA: không phát hiện vấn đề.</div>';
      return qa;
    });
  }

  $("btn-version-save").addEventListener("click", function () {
    saveVersion().then(function (id) { toast("Đã lưu bản nháp #" + id); reloadContent(); return runQa(id); }).catch(fail);
  });

  $("btn-version-approve").addEventListener("click", function () {
    saveVersion().then(function (id) {
      return runQa(id).then(function (qa) {
        if (!qa.canApprove) { toast("Chưa duyệt được: còn lỗi nghiêm trọng", "danger"); reloadContent(); return; }
        if (qa.issues.length && !confirm("Còn cảnh báo QA. Vẫn duyệt?")) { reloadContent(); return; }
        return api("POST", "/api/v1/admin/versions/" + id + "/approve").then(function () {
          toast("Đã duyệt phiên bản #" + id + ". Bản này giờ đã bị khóa.");
          window.jQuery("#version-modal").modal("hide");
          reloadContent();
        });
      });
    }).catch(fail);
  });

  /* ----------------------------- Thuật ngữ ----------------------------- */

  var terms = [];
  function loadTerms() {
    return api("GET", "/api/v1/admin/glossary").then(function (list) {
      terms = list;
      $("term-tbody").innerHTML = list.map(function (g) {
        return "<tr><td><strong>" + esc(g.term) + "</strong></td><td>" + esc(g.termType) + "</td><td>" + (g.keepOriginal ? "✔" : "") +
          "</td><td>" + esc(g.translationEn || "") + "</td><td>" + esc(g.translationKo || "") + '</td><td class="small">' +
          esc([g.ttsAliasVi || "—", g.ttsAliasEn || "—", g.ttsAliasKo || "—"].join(" / ")) + '</td><td class="small text-muted">' + esc(g.note || "") +
          '</td><td class="text-right text-nowrap"><button class="btn btn-xs btn-outline-primary mr-1" data-edit-term="' + g.id +
          '">Sửa</button><button class="btn btn-xs btn-outline-danger" data-del-term="' + g.id + '">Xóa</button></td></tr>';
      }).join("");
    }).catch(fail);
  }

  function openTermModal(g) {
    $("term-id").value = g ? g.id : "";
    $("term-term").value = g ? g.term : "";
    $("term-type").value = g ? g.termType : "PLACE";
    $("term-keep").checked = g ? g.keepOriginal : true;
    $("term-en").value = g ? (g.translationEn || "") : "";
    $("term-ko").value = g ? (g.translationKo || "") : "";
    $("term-alias-vi").value = g ? (g.ttsAliasVi || "") : "";
    $("term-alias-en").value = g ? (g.ttsAliasEn || "") : "";
    $("term-alias-ko").value = g ? (g.ttsAliasKo || "") : "";
    $("term-note").value = g ? (g.note || "") : "";
    window.jQuery("#term-modal").modal("show");
  }

  $("btn-new-term").addEventListener("click", function () { openTermModal(null); });
  $("term-tbody").addEventListener("click", function (e) {
    var ed = e.target.closest("[data-edit-term]"), del = e.target.closest("[data-del-term]");
    if (ed) openTermModal(terms.filter(function (g) { return g.id === Number(ed.getAttribute("data-edit-term")); })[0]);
    if (del && confirm("Xóa thuật ngữ này?")) {
      api("DELETE", "/api/v1/admin/glossary/" + del.getAttribute("data-del-term")).then(function () { toast("Đã xóa"); loadTerms(); }).catch(fail);
    }
  });
  $("term-form").addEventListener("submit", function (e) {
    e.preventDefault();
    var id = $("term-id").value;
    var body = { term: $("term-term").value, termType: $("term-type").value, keepOriginal: $("term-keep").checked,
      translationEn: $("term-en").value, translationKo: $("term-ko").value, ttsAliasVi: $("term-alias-vi").value,
      ttsAliasEn: $("term-alias-en").value, ttsAliasKo: $("term-alias-ko").value, note: $("term-note").value };
    api(id ? "PUT" : "POST", "/api/v1/admin/glossary" + (id ? "/" + id : ""), body)
      .then(function () { window.jQuery("#term-modal").modal("hide"); toast("Đã lưu thuật ngữ"); loadTerms(); }).catch(fail);
  });

  /* ----------------------------- Tác vụ (UC13) ----------------------------- */

  function loadJobs() {
    var q = "?status=" + encodeURIComponent($("job-filter-status").value) + "&type=" + encodeURIComponent($("job-filter-type").value) +
      ($("job-filter-poi").value ? "&poiId=" + $("job-filter-poi").value : "");
    return api("GET", "/api/v1/admin/jobs" + q).then(function (jobs) {
      $("job-tbody").innerHTML = jobs.length ? jobs.map(function (j) {
        var actions = "";
        if (j.status === "FAILED" || j.status === "CANCELLED") actions += '<button class="btn btn-xs btn-outline-primary" data-retry="' + j.id + '">Thử lại</button>';
        if (j.status === "QUEUED" || j.status === "RUNNING") actions += '<button class="btn btn-xs btn-outline-danger" data-cancel="' + j.id + '">Hủy</button>';
        return "<tr><td>" + j.id + "</td><td>" + esc(j.jobType) + "</td><td>" + esc(j.poiCode || "") + "</td><td>" +
          esc(j.jobType === "TRANSLATE" ? (j.versionLanguage + " → " + j.targetLanguage) : j.targetLanguage) + "</td><td>" + badge(j.status) +
          (j.resultVersionId ? '<br><small>→ bản nháp #' + j.resultVersionId + "</small>" : "") + '</td><td class="small">' + fmtTime(j.createdAt) +
          '</td><td class="small text-danger" style="max-width:320px">' + esc(j.errorMessage || "") + "</td><td>" + j.retryCount +
          '</td><td class="text-right">' + actions + "</td></tr>";
      }).join("") : '<tr><td colspan="9" class="text-muted small">Chưa có tác vụ nào.</td></tr>';
    }).catch(fail);
  }

  $("job-tbody").addEventListener("click", function (e) {
    var r = e.target.closest("[data-retry]"), c = e.target.closest("[data-cancel]");
    if (r) api("POST", "/api/v1/admin/jobs/" + r.getAttribute("data-retry") + "/retry").then(function (j) { trackJob(j); loadJobs(); }).catch(fail);
    if (c) api("POST", "/api/v1/admin/jobs/" + c.getAttribute("data-cancel") + "/cancel").then(loadJobs).catch(fail);
  });
  ["job-filter-status", "job-filter-type", "job-filter-poi"].forEach(function (id) { $(id).addEventListener("change", loadJobs); });
  $("btn-jobs-refresh").addEventListener("click", loadJobs);

  /** Hỏi GET /jobs/{id} mỗi 3 giây cho tới khi tác vụ kết thúc (SUCCEEDED, FAILED, CANCELLED). */
  function trackJob(job) {
    if (data.trackedJobs[job.id]) return;
    data.trackedJobs[job.id] = setInterval(function () {
      api("GET", "/api/v1/admin/jobs/" + job.id).then(function (j) {
        if (j.status === "QUEUED" || j.status === "RUNNING") return;
        clearInterval(data.trackedJobs[job.id]);
        delete data.trackedJobs[job.id];
        if (j.status === "SUCCEEDED") toast("Tác vụ #" + j.id + " (" + j.jobType + ") hoàn tất" + (j.resultVersionId ? ": đã tạo bản nháp #" + j.resultVersionId + ", hãy kiểm tra và duyệt" : ""));
        else toast("Tác vụ #" + j.id + " " + j.status + ": " + (j.errorMessage || ""), "danger");
        reloadContent();
        loadJobs();
        loadOverview();
      }).catch(function () { clearInterval(data.trackedJobs[job.id]); delete data.trackedJobs[job.id]; });
    }, POLL_MS);
  }

  /* ----------------------------- Khởi động ----------------------------- */

  function loadAll() {
    loadOverview();
    loadZones();
    loadPois();
    loadTerms();
    loadJobs();
  }

  window.jQuery('a[data-toggle="pill"]').on("shown.bs.tab", function (e) {
    var target = e.target.getAttribute("href");
    if (target === "#tab-overview") loadOverview();
    if (target === "#tab-jobs") loadJobs();
  });

  api("GET", "/api/v1/auth/csrf").catch(function () {}).then(function () {
    return fetch("/api/v1/auth/me", { credentials: "same-origin" });
  }).then(function (res) {
    if (res && res.ok) return res.json().then(showDashboard);
    showLogin();
  }).catch(showLogin);
})();
