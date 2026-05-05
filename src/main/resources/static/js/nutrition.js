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

    // Mock mode
    toggleMockMode();
}

function toggleMockMode() {
  // Expose dev tools to window for easy access
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
        window.DevTools = {
            enableMock: () => {
                NutritionAPI.toggleMock();
                console.log('Mock mode enabled. Refresh page to see mock data.');
            },
            disableMock: () => {
                NutritionAPI.useMock = false;
                console.log('Mock mode disabled. Refresh page to use real API.');
            },
            status: () => {
                console.log(`Current mode: ${NutritionAPI.useMock ? '🟡 MOCK' : '🔵 REAL API'}`);
            }
        };
        console.log('🛠️ Dev Tools available: window.DevTools.enableMock(), .disableMock(), .status()');
    }

}

document.addEventListener('DOMContentLoaded', initNutrition);

// Avatar is handled in onAuthStateChanged
