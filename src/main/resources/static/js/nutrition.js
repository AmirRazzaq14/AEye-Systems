/**
 * Nutrition Page Main Entry Point
 * Coordinates all modules and initializes the page
 */

async function handleSignOut() {
    // localStorage.removeItem('wizcoach-avatar'); // No longer needed as handled by theme.js
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
            if (typeof window.loadAvatar === 'function') window.loadAvatar(user);
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
    NutritionFavorites.init();

    // Load initial data
    NutritionEvents.loadData();
}

document.addEventListener('DOMContentLoaded', initNutrition);

// Avatar is handled in onAuthStateChanged
