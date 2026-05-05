/**
 * Nutrition UI Module
 * Handles rendering of UI components
 */

const NutritionUI = {
    data: null,
    burned: null,
    suggestions: null,
    weekData: null,

    getNutritionColor(key, percent) {
        const root = getComputedStyle(document.documentElement);
        if (key === 'protein') {
            return percent > 80 ? root.getPropertyValue('--protein-high').trim() :
                   percent > 50 ? root.getPropertyValue('--protein-mid').trim() :
                   root.getPropertyValue('--protein-low').trim();
        } else if (key === 'carb') {
            return percent > 80 ? root.getPropertyValue('--carb-high').trim() :
                   percent > 50 ? root.getPropertyValue('--carb-mid').trim() :
                   root.getPropertyValue('--carb-low').trim();
        } else if (key === 'fat') {
            return percent > 80 ? root.getPropertyValue('--fat-high').trim() :
                   percent > 50 ? root.getPropertyValue('--fat-mid').trim() :
                   root.getPropertyValue('--fat-low').trim();
        }
        return root.getPropertyValue('--calorie-ring').trim();
    },

    renderAll() {
        this.renderNutrition();
        this.renderFoodList();
        this.renderNotes();
        this.renderAISuggestions();
        NutritionChart.render(this.weekData);
    },

    renderNutrition() {
        console.log('Rendering with data:', this.data);
        const {
            totalNutrition,
            targetNutrition,
            meals,
            notes
        } = this.data;

        const intake = totalNutrition?.cals || 0;
        const burned = this.burned || 0;
        document.getElementById('intakeValue').textContent = `${Math.round(intake)}`;
        document.getElementById('burnedValue').textContent = `${Math.round(burned)}`;
        
        // Ensure targetNutrition has valid values with defaults
        const targetCals = parseFloat(targetNutrition?.cals) || 2000;
        const targetProtein = parseFloat(targetNutrition?.protein) || 150;
        const targetCarb = parseFloat(targetNutrition?.carb) || 250;
        const targetFat = parseFloat(targetNutrition?.fat) || 67;

        const netCals = intake - burned;

        // Update calorie ring
        const circumference = 2 * Math.PI * 62;
        const current = parseFloat(netCals);
        const target = targetCals;
        const percent = target > 0 ? Math.min(current / target, 1) : 0;
        const offset = circumference - (percent * circumference);
        document.getElementById('calorieRing').style.strokeDashoffset = offset;
        document.getElementById('calorieCurrent').textContent = Math.round(current);
        document.getElementById('calorieTotal').textContent = `/ ${Math.round(target)} kcal`;

        // Color change for calorie ring based on progress
        let ringColor = getComputedStyle(document.documentElement).getPropertyValue('--calorie-ring').trim();
        document.getElementById('calorieRing').style.stroke = ringColor;

        // Update macros with safe target values
        const macros = [{
                key: 'protein',
                label: 'Protein',
                target: targetProtein
            },
            {
                key: 'carb',
                label: 'Carbs',
                target: targetCarb
            },
            {
                key: 'fat',
                label: 'Fat',
                target: targetFat
            }
        ];

        macros.forEach(({
            key,
            target
        }) => {
            const current = parseFloat(totalNutrition?.[key] || 0);
            const percent = target > 0 ? Math.min((current / target) * 100, 100) : 0;
            document.getElementById(`${key}Text`).textContent = `${Math.round(current)} / ${Math.round(target)} g`;
            document.getElementById(`${key}Bar`).style.width = `${percent}%`;

            // Color change for macro bars based on progress
            let barColor = this.getNutritionColor(key, percent);
            document.getElementById(`${key}Bar`).style.backgroundColor = barColor;
        });
    },

    renderFoodList() {
        const {
            meals,
            totalNutrition
        } = this.data;
        const list = document.getElementById('foodList');
        if (!meals.length) {
            list.innerHTML = '<p class="food-log-empty">No meals logged yet. Add your first meal!</p>';
            document.getElementById('logSummary').style.display = 'none';
        } else {
            function escape(str) {
                const div = document.createElement('div');
                div.textContent = str;
                return div.innerHTML;
            }
            list.innerHTML = meals.map(m => {
                // Ensure all fields have default values to prevent undefined display
                const name = m.name || 'Unknown Meal';
                const cals = m.cals != null ? String(m.cals) : '0';
                const protein = m.protein != null ? String(m.protein) : '0';
                const carb = m.carb != null ? String(m.carb) : '0';
                const fat = m.fat != null ? String(m.fat) : '0';

                return `
                <div class="food-item" data-id="${m.mealId}">
                    <div class="food-info">
                        <div class="food-emoji">🍽️</div>
                        <div class="food-details">
                            <h4>${escape(name)}</h4>
                            <div class="food-macros">
                                <span>🔥 ${cals}kcal</span>
                                <span>💪 ${protein}g</span>
                                <span>🍞 ${carb}g</span>
                                <span>🥑 ${fat}g</span>
                            </div>
                        </div>
                    </div>
                    <div class="food-actions">
                        <button class="icon-btn favorite" data-action="favorite" title="Add to Favorites">⭐</button>
                        <button class="icon-btn edit" data-action="edit" title="Edit">✏️</button>
                        <button class="icon-btn delete" data-action="delete" title="Delete">🗑️</button>
                    </div>
                </div>
            `}).join('');
            document.getElementById('logSummary').style.display = 'flex';
            document.getElementById('logCountSpan').textContent = `Today ${meals.length} item${meals.length > 1 ? 's' : ''} - ${Math.round(totalNutrition?.cals || 0)} kcal`;
        }
    },

    renderNotes() {
        const {
            notes
        } = this.data;
        document.getElementById('journalInput').value = notes || '';
        document.getElementById('journalDate').textContent = new Date().toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });     
    },

    renderAISuggestions() {
        const bubble = document.getElementById('aiSuggestionText');
        const p = bubble.querySelector('p');

        if (this.suggestions) {
            // Adapts to multiple formats: string, array, or object with suggestions property
            let text = null;
            if (typeof this.suggestions === 'string') {
                text = this.suggestions;
            } else if (Array.isArray(this.suggestions) && this.suggestions.length > 0) {
                text = this.suggestions[0].text || this.suggestions[0];
            } else if (this.suggestions && typeof this.suggestions === 'object' && this.suggestions.suggestions) {
                text = this.suggestions.suggestions;
            }

            console.log('AI suggestions loaded:', text);

            if (text) {
                p.textContent = text.trim();
            } else {
                p.textContent = 'Conversation format not recognized, showing default message';
            }
        } else {
            console.log('No suggestions available, showing default message');
            p.textContent = 'Ask me for personalized nutrition suggestions based on your today\'s dietary habits.';
        }
    },

   isMealFavorite(meal) {
       const favorites = NutritionFavorites.getFavorites();
       return favorites.some(f =>
           f.name === meal.name &&
           f.cals === meal.cals &&
           f.protein === meal.protein &&
           f.carb === meal.carb &&
           f.fat === meal.fat
       );
   }
};
