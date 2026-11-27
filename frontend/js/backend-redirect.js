/* ==========================================================================
   js/backend-redirect.js
   Trang admin và trang GPS cần API của backend (/api/v1/...).
   Nếu trang được mở bằng Live Server (cổng 5500/5501) hoặc mở file trực tiếp,
   các lời gọi API sẽ báo 404. Script này tự chuyển sang địa chỉ của backend.
   Đổi BACKEND nếu backend chạy ở cổng khác.
   ========================================================================== */
(function () {
  "use strict";

  var BACKEND = "http://localhost:8080";
  var STATIC_PORTS = ["5500", "5501"];

  var openedWithoutBackend =
    location.protocol === "file:" || STATIC_PORTS.indexOf(location.port) >= 0;
  if (!openedWithoutBackend) return;

  // Giữ nguyên tên trang và tham số, ví dụ .../frontend/admin.html?x=1 -> BACKEND/admin.html?x=1
  var page = location.pathname.split("/").pop() || "index.html";
  var target = BACKEND + "/" + page + location.search + location.hash;

  function showNotRunning() {
    var box = document.createElement("div");
    box.style.cssText =
      "position:fixed;top:0;left:0;right:0;z-index:99999;padding:14px 18px;" +
      "background:#dc3545;color:#fff;font:15px/1.5 sans-serif;text-align:center;";
    box.innerHTML =
      "Trang này cần backend đang chạy ở <b>" + BACKEND + "</b>.<br>" +
      "Mở PowerShell trong thư mục <code>backend</code>, chạy " +
      "<code>java -jar .\\target\\damsen-guide.jar</code>, rồi tải lại trang.";
    document.body.appendChild(box);
  }

  // Kiểm tra backend có đang chạy không rồi mới chuyển trang.
  fetch(BACKEND + "/api/v1/public/languages", { mode: "no-cors", cache: "no-store" })
    .then(function () { location.replace(target); })
    .catch(function () {
      if (document.body) showNotRunning();
      else document.addEventListener("DOMContentLoaded", showNotRunning);
    });
})();
