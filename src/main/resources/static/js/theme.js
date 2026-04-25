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
window.signOut = async function() {
    const days = ['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'];
    days.forEach(day => {
        localStorage.removeItem("planner_split_" + day);
        localStorage.removeItem("planner_exercises_" + day);
    });
    localStorage.removeItem('wizcoach-avatar');
    try {
        if (typeof firebase !== "undefined" && firebase.auth) {
            await firebase.auth().signOut();
        }
    } catch(e) { console.error(e); }
    window.location.href = 'login.html';
};
