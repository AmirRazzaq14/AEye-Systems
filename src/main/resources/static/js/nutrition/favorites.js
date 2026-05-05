
/**
 * Nutrition Favorites Module
 * Handles favorite meals storage and management
 */

const NutritionFavorites = {
    STORAGE_KEY: 'wizcoach_favorite_meals',

    getFavorites() {
        try {
            const stored = localStorage.getItem(this.STORAGE_KEY);
            return stored ? JSON.parse(stored) : [];
        } catch (e) {
            console.error('Failed to load favorites:', e);
            return [];
        }
    },

    saveFavorites(favorites) {
        try {
            localStorage.setItem(this.STORAGE_KEY, JSON.stringify(favorites));
        } catch (e) {
            console.error('Failed to save favorites:', e);
        }
    },

    addFavorite(meal) {
        const favorites = this.getFavorites();

        // Check if already exists
        const exists = favorites.some(f =>
            f.name === meal.name &&
            f.cals === meal.cals &&
            f.protein === meal.protein &&
            f.carb === meal.carb &&
            f.fat === meal.fat
        );

        if (!exists) {
            favorites.push({
                ...meal,
                favoritedAt: Date.now()
            });
            this.saveFavorites(favorites);
            NotificationSystem.success(`${meal.name} added to favorites!`);
            this.renderFavorites();
        } else {
            NotificationSystem.info(`${meal.name} is already in favorites`);
        }
    },

    removeFavorite(mealName) {
        let favorites = this.getFavorites();
        favorites = favorites.filter(f => f.name !== mealName);
        this.saveFavorites(favorites);
        NotificationSystem.success(`${mealName} removed from favorites`);
        this.renderFavorites();
    },

    useFavorite(meal) {
        document.getElementById('foodName').value = meal.name || '';
        document.getElementById('foodCals').value = meal.cals || '0';
        document.getElementById('foodProtein').value = meal.protein || '0';
        document.getElementById('foodCarb').value = meal.carb || '0';
        document.getElementById('foodFat').value = meal.fat || '0';

        closeWindow();
        openWindow('manual');
        NotificationSystem.info(`Loaded ${meal.name}. Click "Add to Log" to save.`);
    },

    renderFavorites() {
        const section = document.getElementById('favoriteMealsSection');
        const list = document.getElementById('favoriteMealsList');

        if (!section || !list) return;

        const favorites = this.getFavorites();

        if (favorites.length === 0) {
            section.style.display = 'none';
            return;
        }

        section.style.display = 'block';
        list.innerHTML = favorites.map(meal => `
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 10px 12px; background: var(--input-bg); border-radius: 8px; border: 1px solid var(--border); cursor: pointer; transition: all 0.2s;"
                 onmouseover="this.style.borderColor='var(--blue)'"
                 onmouseout="this.style.borderColor='var(--border)'"
                 onclick="NutritionFavorites.useFavorite(${JSON.stringify(meal).replace(/"/g, '&quot;')})">
                <div style="flex: 1; min-width: 0;">
                    <div style="font-size: 14px; font-weight: 600; color: var(--text); margin-bottom: 4px;">${this.escapeHtml(meal.name)}</div>
                    <div style="font-size: 12px; color: var(--muted);">
                        🔥 ${meal.cals}kcal | 💪 ${meal.protein}g | 🍞 ${meal.carb}g | 🥑 ${meal.fat}g
                    </div>
                </div>
                <button onclick="event.stopPropagation(); NutritionFavorites.removeFavorite('${this.escapeHtml(meal.name)}')"
                        style="background: none; border: none; color: var(--muted); cursor: pointer; padding: 4px 8px; font-size: 16px; border-radius: 4px;"
                        onmouseover="this.style.background='rgba(239, 68, 68, 0.1)'; this.style.color='var(--danger)'"
                        onmouseout="this.style.background='none'; this.style.color='var(--muted)'">
                    ✕
                </button>
            </div>
        `).join('');
    },

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    },

    init() {
        this.renderFavorites();
    }
};
