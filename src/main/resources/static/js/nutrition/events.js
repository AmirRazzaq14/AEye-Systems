/**
 * Nutrition Events Module
 * Handles all event listeners
 */

const NutritionEvents = {
    init() {
        this.bindManualEntryEvents();
        this.bindFoodListEvents();
        this.bindJournalEvents();
        this.bindChartTabEvents();
        this.bindSuggestionsEvents();
        this.bindVisibilityEvents();
        this.bindBarcodeEvents();
    },

    bindManualEntryEvents() {
////        const addBtn = document.getElementById('addFoodBtn');
//        const form = document.getElementById('manualEntryForm');
        const saveBtn = document.getElementById('saveManualBtn');

//        addBtn.addEventListener('click', () => {
//            form.classList.toggle('hidden');
//            addBtn.textContent = form.classList.contains('hidden') ? '+' : '−';
//        });

        saveBtn.addEventListener('click', () => this.addManualMeal());
    },

    bindFoodListEvents() {
        document.getElementById('foodList').addEventListener('click', (e) => {
            const btn = e.target.closest('button[data-action="delete"]');
            if (btn) {
                const foodItem = btn.closest('.food-item');
                if (foodItem) {
                    const mealId = foodItem.getAttribute('data-id');
                    if (mealId) this.deleteMeal(mealId);
                }
            }
        });
    },

    bindJournalEvents() {
        let timeout;
        const journalInput = document.getElementById('journalInput');
        const status = document.getElementById('journalStatus');
        const saveBtn = document.getElementById('saveJournalBtn');
        
        saveBtn.addEventListener('click', () => this.saveJournal());

        journalInput.addEventListener('input', () => {
            status.textContent = 'Unsaved...';
            status.style.color = 'var(--warning)';
            clearTimeout(timeout);
            timeout = setTimeout(() => this.saveJournal(), 1500);
        });
    },

    bindChartTabEvents() {
        document.querySelectorAll('.chart-tab').forEach(tab => {
            tab.addEventListener('click', (e) => {
                const view = e.target.getAttribute('data-view');
                NutritionChart.switchView(view, NutritionUI.weekData);
            });
        });
    },

    bindSuggestionsEvents() {
        document.getElementById('getSuggestionBtn').addEventListener('click', () => {
            this.getSuggestions();
        });
    },

    bindVisibilityEvents() {
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState !== 'visible') return;
            this.refreshBurnedOnly().catch((err) => console.warn('Burned refresh:', err));
        });

        // Listen for nutrition updates from other modules
        window.addEventListener('nutrition-updated', () => {
            this.loadData();
        });
    },

    bindBarcodeEvents() {
        // Barcode events are handled by BarcodeScanner module
        // This is just a placeholder for future extensions
    },

    async addManualMeal() {
        const meal = {
            mealId: 'meal_' + Date.now(),
            name: document.getElementById('foodName').value.trim() || 'Manual Entry',
            cals: document.getElementById('foodCals').value || '0',
            protein: document.getElementById('foodProtein').value || '0',
            carb: document.getElementById('foodCarb').value || '0',
            fat: document.getElementById('foodFat').value || '0'
        };
        try {
            await NutritionAPI.addMeal(meal);
            await this.loadData();
            ['foodName', 'foodCals', 'foodProtein', 'foodCarb', 'foodFat'].forEach(id => document.getElementById(id).value = '');
//            document.getElementById('manualEntryForm').classList.add('hidden');
//            document.getElementById('addFoodBtn').textContent = '+';
        } catch (err) {
            alert('Failed to add: ' + err.message);
        }
    },

    async deleteMeal(mealId) {
        if (!confirm('Delete this meal?')) return;
        try {
            await NutritionAPI.deleteMeal(mealId);
            await this.loadData();
        } catch (err) {
            alert('Failed to delete');
        }
    },

    async saveJournal() {
        const status = document.getElementById('journalStatus');
        const notes = document.getElementById('journalInput').value;
        status.textContent = 'Saving...';
        try {
            await NutritionAPI.saveNotes(notes);
            status.textContent = 'Saved!';
            status.style.color = 'var(--success)';
            setTimeout(() => status.textContent = '', 2000);
        } catch (err) {
            status.textContent = 'Failed';
            status.style.color = 'var(--danger)';
        }
    },

    async getSuggestions() {
        try {
            const suggestions = await NutritionAPI.getSuggestions();
            NutritionUI.suggestions = suggestions;
            console.log('Received AI suggestions:', NutritionUI.suggestions);
            NutritionUI.renderAISuggestions();
        } catch (err) {
            alert('Failed to get suggestions: ' + err.message);
        }
    },

    async refreshBurnedOnly() {
        try {
            if (!firebase.auth().currentUser) return;
            const rawBurned = await NutritionAPI.getBurnedCalories();
            NutritionUI.burned = normalizeBurnedCalories(rawBurned);
            NutritionUI.renderNutrition();
        } catch (e) {
            console.warn('Burned calories refresh unavailable:', e);
        }
    },

    async loadData() {
        try {
            NutritionUI.data = await NutritionAPI.getTodayLog();
            NutritionUI.data.meals = NutritionUI.data.meals || [];
            try {
                const rawBurned = await NutritionAPI.getBurnedCalories();
                NutritionUI.burned = normalizeBurnedCalories(rawBurned);
            } catch (burnErr) {
                console.warn('Burned calories unavailable:', burnErr);
                NutritionUI.burned = 0;
            }

            try {
                NutritionUI.weekData = await NutritionAPI.getWeekLog();
            } catch (weekErr) {
                console.warn('Failed to load week data:', weekErr);
                NutritionUI.weekData = null; 
            }
            NutritionUI.renderAll();
        } catch (err) {
            console.error('Failed to load:', err);
            console.log('Using mock data for demo');
            // Use mock data for demo
            NutritionUI.data = NutritionUI.getMockData();
            NutritionUI.weekData = NutritionUI.getMockWeekData();
            NutritionUI.burned = NutritionUI.getMockBurned();
            NutritionUI.renderAll();
        }
    }
};
