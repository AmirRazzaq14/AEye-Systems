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
        
        const targetCals = targetNutrition?.cals || 2000;
        const netCals = intake - burned;

        // Update calorie ring
        const circumference = 2 * Math.PI * 62;
        const current = parseFloat(netCals);
        const target = parseFloat(targetNutrition?.cals);
        const percent = Math.min(current / target, 1);
        const offset = circumference - (percent * circumference);
        document.getElementById('calorieRing').style.strokeDashoffset = offset;
        document.getElementById('calorieCurrent').textContent = Math.round(current);
        document.getElementById('calorieTotal').textContent = `/ ${Math.round(target)} kcal`;

        // Color change for calorie ring based on progress
        let ringColor = getComputedStyle(document.documentElement).getPropertyValue('--calorie-ring').trim();
        document.getElementById('calorieRing').style.stroke = ringColor;

        // Update macros
        const macros = [{
                key: 'protein',
                label: 'Protein',
                target: targetNutrition?.protein
            },
            {
                key: 'carb',
                label: 'Carbs',
                target: targetNutrition?.carb
            },
            {
                key: 'fat',
                label: 'Fat',
                target: targetNutrition?.fat
            }
        ];

        macros.forEach(({
            key,
            target
        }) => {
            const current = parseFloat(totalNutrition?.[key] || 0);
            const percent = Math.min((current / target) * 100, 100);
            document.getElementById(`${key}Text`).textContent = `${Math.round(current)} / ${target} g`;
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
            list.innerHTML = meals.map(m => `
                <div class="food-item" data-id="${m.mealId}">
                    <div class="food-info">
                        <div class="food-emoji">🍽️</div>
                        <div class="food-details">
                            <h4>${escape(m.name)}</h4>
                            <div class="food-macros">
                                <span>🔥 ${m.cals}kcal</span>
                                <span>💪 ${m.protein}g</span>
                                <span>🍞 ${m.carb}g</span>
                                <span>🥑 ${m.fat}g</span>
                            </div>
                        </div>
                    </div>
                    <div class="food-actions">
                        <button class="icon-btn delete" data-action="delete" title="Delete">🗑️</button>
                    </div>
                </div>
            `).join('');
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

    getMockData() {
        return {
            totalNutrition: {
                cals: 2500,
                protein: 85,
                carb: 180,
                fat: 45
            },
            targetNutrition: {
                cals: 3000,
                protein: 150,
                carb: 250,
                fat: 70
            },
            meals: [
                {
                    mealId: 'meal1',
                    name: 'Grilled Chicken Salad',
                    cals: '450',
                    protein: '35',
                    carb: '20',
                    fat: '25'
                },
                {
                    mealId: 'meal2',
                    name: 'Oatmeal with Berries',
                    cals: '300',
                    protein: '10',
                    carb: '50',
                    fat: '8'
                },
                {
                    mealId: 'meal3',
                    name: 'Greek Yogurt',
                    cals: '150',
                    protein: '15',
                    carb: '10',
                    fat: '5'
                },
                {
                    mealId: 'meal4',
                    name: 'Quinoa Bowl',
                    cals: '550',
                    protein: '25',
                    carb: '100',
                    fat: '12'
                },
                {
                    mealId: 'meal5',
                    name: 'toast with avocado',
                    cals: '300',
                    protein: '125',
                    carb: '40',
                    fat: '52'
                }
            ],
            notes: 'Feeling good today! Had a productive workout and ate healthy meals.'
        };
    },

    getMockWeekData() {
        const today = new Date();
        const weekData = [];
        for (let i = 6; i >= 0; i--) {
            const date = new Date(today);
            date.setDate(today.getDate() - i);
            const dateStr = date.toISOString().split('T')[0];
            const calories = Math.floor(Math.random() * 500) + 1500;
            const protein = Math.floor(calories * 0.3 / 4);
            const carb = Math.floor(calories * 0.5 / 4);
            const fat = Math.floor(calories * 0.2 / 9);
            weekData.push({
                date: dateStr,
                totalNutrition: {
                    cals: calories,
                    protein: protein,
                    carb: carb,
                    fat: fat
                }
            });
        }
        return weekData;
    },

    getMockBurned() {
        return 300; 
    }
};
