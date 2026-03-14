// theme.js
(function initTheme() {
    const key = "wizcoach-theme";
    const saved = localStorage.getItem(key) || "light";
    document.documentElement.setAttribute("data-theme", saved);
})();

function toggleTheme() {
    const key = "wizcoach-theme";
    const current = document.documentElement.getAttribute("data-theme");
    const next = current === "dark" ? "light" : "dark";
    document.documentElement.setAttribute("data-theme", next);
    localStorage.setItem(key, next);
}