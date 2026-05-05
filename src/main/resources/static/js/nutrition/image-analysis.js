/**
 * Image Analysis Module
 * Handles food image upload and analysis
 */

const ImageAnalysis = {
    currentAnalysis: null,

    async init() {
        const zone = document.getElementById('uploadZone');
        const input = document.getElementById('fileInput');
        const addResultBtn = document.getElementById('addImageResultBtn');
        const cancelResultBtn = document.getElementById('cancelImageResultBtn');

        zone.addEventListener('click', () => input.click());
        input.addEventListener('change', (e) => {
            if (e.target.files[0]) this.handleImage(e.target.files[0]);
        });

        zone.addEventListener('dragover', (e) => {
            e.preventDefault();
            zone.classList.add('drag-over');
        });
        zone.addEventListener('dragleave', () => zone.classList.remove('drag-over'));
        zone.addEventListener('drop', (e) => {
            e.preventDefault();
            zone.classList.remove('drag-over');
            const file = e.dataTransfer.files[0];
            if (file?.type.startsWith('image/')) this.handleImage(file);
        });

        if (addResultBtn) {
            addResultBtn.addEventListener('click', () => this.addAnalysisToLog());
        }

        if (cancelResultBtn) {
            cancelResultBtn.addEventListener('click', () => this.hideResult());
        }
    },

    async handleImage(file) {
        if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
            NotificationSystem.warning('Please upload JPEG, PNG or WebP images only');
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            NotificationSystem.error('Image too large. Maximum size is 5MB');
            return;
        }

        const zone = document.getElementById('uploadZone');
        const processingMsg = document.getElementById('processingMessage');

        // Preview
        const reader = new FileReader();
        reader.onload = (e) => {
            const previewImg = document.getElementById('previewImg');
            if (previewImg) {
                previewImg.src = e.target.result;
            }
            zone.classList.add('has-image');
        };
        reader.readAsDataURL(file);

        // Show processing message
        if (processingMsg) {
            processingMsg.style.display = 'flex';
        }
        zone.classList.add('processingMessage');

        try {
            const result = await NutritionAPI.analyzeImage(file);
            
            // Store current analysis
            this.currentAnalysis = {
                name: result.name || 'Analyzed Food',
                cals: result.cals || 0,
                protein: result.protein || 0,
                carb: result.carb || 0,
                fat: result.fat || 0
            };

            // Display result
            this.displayResult(this.currentAnalysis);

            // Hide processing message
            if (processingMsg) {
                processingMsg.style.display = 'none';
            }
            zone.classList.remove('processingMessage');
        } catch (err) {
            console.warn('Image analysis failed:', err.message);
            NotificationSystem.error('Image analysis failed,try again later');
            if (processingMsg) {
                processingMsg.style.display = 'none';
            }
            zone.classList.remove('processingMessage');
        }
    },

    displayResult(result) {
        const container = document.getElementById('imageResultContainer');
        const nameEl = document.getElementById('imageResultName');
        const calsEl = document.getElementById('imageResultCals');
        const proteinEl = document.getElementById('imageResultProtein');
        const carbEl = document.getElementById('imageResultCarb');
        const fatEl = document.getElementById('imageResultFat');

        if (!container) return;

        if (nameEl) nameEl.textContent = result.name;
        if (calsEl) calsEl.textContent = `${Math.round(result.cals)} kcal`;
        if (proteinEl) proteinEl.textContent = `${Math.round(result.protein * 10) / 10} g`;
        if (carbEl) carbEl.textContent = `${Math.round(result.carb * 10) / 10} g`;
        if (fatEl) fatEl.textContent = `${Math.round(result.fat * 10) / 10} g`;

        container.classList.add('show');
    },

    hideResult() {
        const container = document.getElementById('imageResultContainer');
        if (container) {
            container.classList.remove('show');
        }
        this.currentAnalysis = null;
        
        // Reset upload zone
        const zone = document.getElementById('uploadZone');
        if (zone) {
            zone.classList.remove('has-image');
        }
        const previewImg = document.getElementById('previewImg');
        if (previewImg) {
            previewImg.src = '';
        }
        const fileInput = document.getElementById('fileInput');
        if (fileInput) {
            fileInput.value = '';
        }
    },

    async addAnalysisToLog() {
        if (!this.currentAnalysis) {
            NotificationSystem.warning('No analysis result to add');
            return;
        }

        try {
            // Validate currentAnalysis has required fields
            if (!this.currentAnalysis.name) {
                NotificationSystem.error('Invalid analysis data');
                this.hideResult();
                return;
            }

            await NutritionAPI.addMeal({
                mealId: 'meal_' + Date.now(),
                name: this.currentAnalysis.name,
                cals: String(Math.round(this.currentAnalysis.cals)),
                protein: String(Math.round(this.currentAnalysis.protein * 10) / 10),
                carb: String(Math.round(this.currentAnalysis.carb * 10) / 10),
                fat: String(Math.round(this.currentAnalysis.fat * 10) / 10)
            });

            window.dispatchEvent(new Event('nutrition-updated'));
            
            // Store food name before hiding result
            const foodName = this.currentAnalysis.name;
            
            // Hide result and reset
            this.hideResult();

            NotificationSystem.success(`${foodName} added to log!`);
        } catch (err) {
            console.error('Error adding food:', err);
            NotificationSystem.error('Failed to add food. Please try again.');
        }
    }
};
