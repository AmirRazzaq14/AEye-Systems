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
    // No need to clear avatar here as it's scoped by user id
    localStorage.removeItem('wizcoach-current-uid');
    // localStorage.removeItem('wizcoach-avatar');
    try {
        if (typeof firebase !== "undefined" && firebase.auth) {
            await firebase.auth().signOut();
        }
    } catch(e) { console.error(e); }
    window.location.href = 'login.html';
};

window.loadAvatar = function(user) {
    if (!user) return;
    localStorage.setItem('wizcoach-current-uid', user.uid);
    const saved = localStorage.getItem('wizcoach-avatar-' + user.uid);
    if(saved) {
        const img = document.getElementById('navAvatarImg');
        const txt = document.getElementById('navAvatarText');
        if(img && txt) { img.src = saved; img.style.display = 'block'; txt.style.display = 'none'; }
    }
};

window.addEventListener('DOMContentLoaded', () => {
    if (typeof firebase === 'undefined' || !firebase.auth) {
        const uid = localStorage.getItem('wizcoach-current-uid');
        if (uid) {
            window.loadAvatar({ uid: uid });
        }
    }
});
