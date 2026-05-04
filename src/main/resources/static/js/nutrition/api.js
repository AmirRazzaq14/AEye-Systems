/**
 * Nutrition API Module
 * Handles all API requests for nutrition logs
 */

function normalizeBurnedCalories(raw) {
    if (raw == null) return 0;
    if (typeof raw === 'number' && !Number.isNaN(raw)) return raw;
    if (typeof raw === 'string') {
        const n = parseFloat(raw);
        return Number.isFinite(n) ? n : 0;
    }
    if (typeof raw === 'object') {
        const n = Number(raw.burnedCalories ?? raw.caloriesTotal ?? 0);
        return Number.isFinite(n) ? n : 0;
    }
    return 0;
}

const NutritionAPI = {
    async fetch(path, options = {}) {
        const user = firebase.auth().currentUser;
        if (!user) throw new Error('Not logged in');
        const token = await user.getIdToken();
        const res = await fetch(`/api/nutrition-logs${path}`, {
            ...options,
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                ...options.headers
            }
        });
        if (!res.ok) throw new Error(`API Error: ${res.status}`);
        return res.json();
    },

    getToday() {
        const now = new Date();
        return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
    },

    getTodayLog() {
        return this.fetch(`/${this.getToday()}`);
    },

    getWeekLog() {
        return this.fetch(`/week/${this.getToday()}`);
    },

    addMeal(meal) {
        return this.fetch(`/meals/${this.getToday()}`, {
            method: 'POST',
            body: JSON.stringify(meal)
        });
    },

    deleteMeal(mealId) {
        return this.fetch(`/meals/${this.getToday()}/${mealId}`, {
            method: 'DELETE'
        });
    },

    updateMeal(mealId, meal) {
        return this.fetch(`/meals/${this.getToday()}/${mealId}`, {
            method: 'PUT',
            body: JSON.stringify(meal)
        });
    },

    saveNotes(notes) {
        return this.fetch(`/notes/${this.getToday()}`, {
            method: 'POST',
            body: JSON.stringify({ notes })
        });
    },

    getBurnedCalories() {
        return this.fetch(`/burnedCalories/${this.getToday()}`);
    },

    getSuggestions() {
        return this.fetch(`/suggestions/${this.getToday()}`);
    },

    async analyzeImage(file) {
        const formData = new FormData();
        formData.append('image', file);
        const user = firebase.auth().currentUser;
        const token = await user.getIdToken();
        const res = await fetch('/api/nutrition-logs/analyze-image', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });
        if (!res.ok) {
            let msg = `Server returned ${res.status}`;
            try {
                const ct = res.headers.get('content-type') || '';
                if (ct.includes('application/json')) {
                    const j = await res.json();
                    const parts = [j.message, j.detail].filter(Boolean);
                    msg = parts.length ? parts.join(' — ') : JSON.stringify(j);
                } else {
                    const t = await res.text();
                    if (t) msg = t;
                }
            } catch (e) {
                console.warn('Could not read error body', e);
            }
            throw new Error(msg);
        }
        return res.json();
    }
};
