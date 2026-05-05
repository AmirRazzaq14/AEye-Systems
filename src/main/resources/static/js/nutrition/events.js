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
        const saveBtn = document.getElementById('saveManualBtn');
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
            const editBtn = e.target.closest('button[data-action="edit"]');
            if (editBtn) {
                const foodItem = editBtn.closest('.food-item');
                if (foodItem) {
                    const mealId = foodItem.getAttribute('data-id');
                    if (mealId) {
                        this.editMeal(mealId);
                    }
                }
            }

            const favBtn = e.target.closest('button[data-action="favorite"]');
            if (favBtn) {
                const foodItem = favBtn.closest('.food-item');
                if (foodItem) {
                    const mealId = foodItem.getAttribute('data-id');
                    const meal = NutritionUI.data.meals.find(m => m.mealId === mealId);
                    if (meal) {
                        NutritionFavorites.addFavorite(meal);
                    }
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
        const saveBtn = document.getElementById('saveManualBtn');
        const isEditMode = saveBtn.dataset.editMode === 'true';
        const editMealId = saveBtn.dataset.editMealId;

        const meal = {
            mealId: 'meal_' + Date.now(),
            name: document.getElementById('foodName').value.trim() || 'Manual Entry',
            cals: document.getElementById('foodCals').value || '0',
            protein: document.getElementById('foodProtein').value || '0',
            carb: document.getElementById('foodCarb').value || '0',
            fat: document.getElementById('foodFat').value || '0'
        };
        
        // Validate input
        if (!meal.name || meal.name === 'Manual Entry') {
            NotificationSystem.warning('Please enter a food name');
            return;
        }
        
        try {
            if (isEditMode) {
                await NutritionAPI.updateMeal(editMealId, meal);
                NotificationSystem.success(`${meal.name} updated successfully!`);
            } else {
                await NutritionAPI.addMeal(meal);
                NotificationSystem.success(`${meal.name} added to food log!`);
            }
            await this.loadData();
            ['foodName', 'foodCals', 'foodProtein', 'foodCarb', 'foodFat'].forEach(id => document.getElementById(id).value = '');

            saveBtn.textContent = 'Add to Log';
            delete saveBtn.dataset.editMode;
            delete saveBtn.dataset.editMealId;

            closeWindow();
        } catch (err) {
            console.error('Failed to add meal:', err);
            NotificationSystem.error('Failed to add meal. Please try again.');
        }
    },

    async deleteMeal(mealId) {
        if (!confirm('Delete this meal?')) return;
        try {
            await NutritionAPI.deleteMeal(mealId);
            await this.loadData();
            NotificationSystem.success('Meal deleted successfully');
        } catch (err) {
            console.error('Failed to delete meal:', err);
            NotificationSystem.error('Failed to delete meal. Please try again.');
        }
    },

    async editMeal(mealId) {
        const meal = NutritionUI.data.meals.find(m => m.mealId === mealId);
        if (!meal) {
            NotificationSystem.error('Meal not found');
            return;
        }

        document.getElementById('foodName').value = meal.name || '';
        document.getElementById('foodCals').value = meal.cals || '0';
        document.getElementById('foodProtein').value = meal.protein || '0';
        document.getElementById('foodCarb').value = meal.carb || '0';
        document.getElementById('foodFat').value = meal.fat || '0';

        const saveBtn = document.getElementById('saveManualBtn');
        saveBtn.textContent = 'Update Meal';
        saveBtn.dataset.editMode = 'true';
        saveBtn.dataset.editMealId = mealId;

        openWindow('manual');
        NotificationSystem.info('Editing meal. Make changes and click Update.');
    },

    async saveJournal() {
        const status = document.getElementById('journalStatus');
        const notes = document.getElementById('journalInput').value;
        status.textContent = 'Saving...';
        try {
            await NutritionAPI.saveNotes(notes);
            status.textContent = 'Saved!';
            status.style.color = 'var(--success)';
            NotificationSystem.success('Journal saved successfully');
            setTimeout(() => status.textContent = '', 2000);
        } catch (err) {
            console.error('Failed to save journal:', err);
            status.textContent = 'Failed';
            status.style.color = 'var(--danger)';
            NotificationSystem.error('Failed to save journal. Please try again.');
        }
    },

    async getSuggestions() {
        const btn = document.getElementById('getSuggestionBtn');
        const originalText = btn.textContent;
        
        try {
            btn.textContent = 'Getting suggestions...';
            btn.disabled = true;
            
            const suggestions = await NutritionAPI.getSuggestions();
            NutritionUI.suggestions = suggestions;
            console.log('Received AI suggestions:', NutritionUI.suggestions);
            NutritionUI.renderAISuggestions();
            
            if (suggestions && suggestions.length > 0) {
                NotificationSystem.success('AI suggestions updated!');
            } else {
                NotificationSystem.info('No new suggestions available');
            }
        } catch (err) {
            console.error('Failed to get suggestions:', err);
            NotificationSystem.error('Failed to get suggestions. Please try again.');
        } finally {
            btn.textContent = originalText;
            btn.disabled = false;
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
        }
    }
};
