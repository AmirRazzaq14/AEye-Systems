/**
 * Nutrition Page Main Entry Point
 * Coordinates all modules and initializes the page
 */

async function handleSignOut() {
    localStorage.removeItem('wizcoach-avatar');
    try {
        if (firebase.apps.length) {
            await firebase.auth().signOut();
        }
    } catch (err) {
        console.warn('Sign out error:', err);
    }
    window.location.href = 'login.html';
}

async function initNutrition() {
    try {
        const res = await fetch('/api/config');
        const config = await res.json();
        firebase.initializeApp(config);
        firebase.auth().onAuthStateChanged(user => {
            if (!user) return window.location.href = 'login.html';
            startApp();
        });
    } catch (err) {
        console.log('Dev mode: no firebase');
        startApp(); // Dev mode, no auth
    }
}

function startApp() {
    // Initialize all modules
    ImageAnalysis.init();
    BarcodeScanner.init();
    NutritionEvents.init();
    
    // Load initial data
    NutritionEvents.loadData();
}

document.addEventListener('DOMContentLoaded', initNutrition);

// Load avatar from localStorage
(function loadLocalAvatar() {
    const saved = localStorage.getItem('wizcoach-avatar');
    if(saved) {
        const img = document.getElementById('navAvatarImg');
        const txt = document.getElementById('navAvatarText');
        if(img && txt) { img.src = saved; img.style.display = 'block'; txt.style.display = 'none'; }
    }
})();
