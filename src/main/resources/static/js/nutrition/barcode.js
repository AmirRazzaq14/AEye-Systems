/**
 * Barcode Scanner Module
 * Handles barcode scanning and product lookup
 */

const BarcodeScanner = {
    html5QrCode: null,
    isScanning: false,
    currentProduct: null,

    init() {
        this.bindEvents();
    },

    bindEvents() {
        const scanBtn = document.getElementById('scanBarcodeBtn');
        const closeBtn = document.getElementById('closeScannerBtn');
        const barcodeInput = document.getElementById('barcodeInput');
        const enterBtn = document.getElementById('enterBarcodeBtn');
        const addResultBtn = document.getElementById('addBarcodeResultBtn');
        const cancelResultBtn = document.getElementById('cancelBarcodeResultBtn');

        if (scanBtn) {
            scanBtn.addEventListener('click', () => this.toggleScanner());
        }


        if (closeBtn) {
            closeBtn.addEventListener('click', () => this.stopScanner());
        }

        if (enterBtn) {
            enterBtn.addEventListener('click', () => {
                const barcode = barcodeInput ? barcodeInput.value.trim() : '';
                this.lookupBarcode(barcode);
            });
        }


        if (barcodeInput) {
            barcodeInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this.lookupBarcode(barcodeInput.value.trim());
                }
            });
        }


        if (addResultBtn) {
            addResultBtn.addEventListener('click', () => this.addProductToLog());
        }

        if (cancelResultBtn) {
            cancelResultBtn.addEventListener('click', () => this.hideResult());
        }
    },

    async toggleScanner() {
        if (this.isScanning) {
            this.stopScanner();
        } else {
            await this.startScanner();
        }
    },

    async startScanner() {
        const scannerContainer = document.getElementById('scanner-container');
        const reader = document.getElementById('reader');
        const statusEl = document.getElementById('scannerStatus');

        if (!scannerContainer || !reader) {
            console.error('Scanner elements not found');
            return;
        }

        try {
            scannerContainer.classList.remove('hidden');
            
            this.html5QrCode = new Html5Qrcode("reader");
            
            const config = {
                fps: 10,
                qrbox: { width: 250, height: 250 },
                aspectRatio: 1.0
            };

            await this.html5QrCode.start(
                { facingMode: "environment" },
                config,
                (decodedText) => this.onScanSuccess(decodedText),
                (errorMessage) => this.onScanError(errorMessage)
            );

            this.isScanning = true;
            
            // Update status to scanning
            if (statusEl) {
                statusEl.textContent = 'Scanning... Align barcode within the frame';
                statusEl.className = 'scanner-status scanning';
            }
            
            console.log('Scanner started');
        } catch (err) {
            console.error('Failed to start scanner:', err);
            alert('Camera access failed. Please check permissions or enter barcode manually.');
            scannerContainer.classList.add('hidden');
            
            // Update status to error
            if (statusEl) {
                statusEl.textContent = 'Camera access failed';
                statusEl.className = 'scanner-status error';
            }
        }
    },

    async stopScanner() {
        const scannerContainer = document.getElementById('scanner-container');
        const statusEl = document.getElementById('scannerStatus');

        if (this.html5QrCode && this.isScanning) {
            try {
                await this.html5QrCode.stop();
                await this.html5QrCode.clear();
                this.html5QrCode = null;
            } catch (err) {
                console.error('Error stopping scanner:', err);
            }
        }

        this.isScanning = false;
        
        if (scannerContainer) {
            scannerContainer.classList.add('hidden');
        }

        // Reset status
        if (statusEl) {
            statusEl.textContent = 'Ready to scan';
            statusEl.className = 'scanner-status';
        }

        console.log('Scanner stopped');
    },

    onScanSuccess(decodedText) {
        console.log('Barcode scanned:', decodedText);
        
        const statusEl = document.getElementById('scannerStatus');
        
        // Update status to success
        if (statusEl) {
            statusEl.textContent = 'Barcode detected! Looking up product...';
            statusEl.className = 'scanner-status success';
        }
        
        // Stop scanner after successful scan
        this.stopScanner();

        // Fill the barcode input
        const barcodeInput = document.getElementById('barcodeInput');
        if (barcodeInput) {
            barcodeInput.value = decodedText;
        }

        // Lookup the barcode
        this.lookupBarcode(decodedText);
    },

    onScanError(errorMessage) {
        // Ignore common scanning errors (no barcode detected)
        if (errorMessage !== 'QR Code parse error') {
            console.debug('Scan error:', errorMessage);
        }
    },

    async lookupBarcode(barcode) {
        if (!barcode) {
            NotificationSystem.warning('Please enter or scan a barcode');
            
            const statusEl = document.getElementById('scannerStatus');
            if (statusEl) {
                statusEl.textContent = 'Please enter or scan a barcode';
                statusEl.className = 'scanner-status error';
            }
            return;
        }

        console.log('Looking up barcode:', barcode);
        
        const statusEl = document.getElementById('scannerStatus');
        if (statusEl) {
            statusEl.textContent = 'Looking up product information...';
            statusEl.className = 'scanner-status scanning';
        }

        try {
            // Show loading state
            const resultContainer = document.getElementById('barcodeResultContainer');
            if (resultContainer) {
                resultContainer.classList.remove('show');
            }

            // Try to get product info from Open Food Facts API
            const response = await fetch(`https://world.openfoodfacts.org/api/v0/product/${barcode}.json`);
            const data = await response.json();

            if (data.status === 1 && data.product) {
                const product = data.product;
                
                // Extract nutrition info
                const nutritionData = this.extractNutrition(product);
                
                // Store current product
                this.currentProduct = {
                    name: product.product_name || 'Unknown Product',
                    calories: nutritionData.calories || 0,
                    protein: nutritionData.protein || 0,
                    carbs: nutritionData.carbs || 0,
                    fat: nutritionData.fat || 0
                };

                // Display result
                this.displayResult(this.currentProduct);

                console.log('Product found:', product.product_name);
                NotificationSystem.success('Product found! Review and add to log.');
                
                // Update status to success
                if (statusEl) {
                    statusEl.textContent = 'Product found! Review below';
                    statusEl.className = 'scanner-status success';
                }
            } else {
                // Product not found in database
                NotificationSystem.error('Product not found. Please try another barcode.');
                
                // Update status to error
                if (statusEl) {
                    statusEl.textContent = 'Product not found. Try another barcode.';
                    statusEl.className = 'scanner-status error';
                }
            }
        } catch (err) {
            console.error('Error looking up barcode:', err);
            NotificationSystem.error('Failed to lookup barcode. Please try again.');
            
            // Update status to error
            if (statusEl) {
                statusEl.textContent = 'Lookup failed. Please try again.';
                statusEl.className = 'scanner-status error';
            }
        }
    },

    displayResult(product) {
        const container = document.getElementById('barcodeResultContainer');
        const nameEl = document.getElementById('barcodeResultName');
        const calsEl = document.getElementById('barcodeResultCals');
        const proteinEl = document.getElementById('barcodeResultProtein');
        const carbEl = document.getElementById('barcodeResultCarb');
        const fatEl = document.getElementById('barcodeResultFat');

        if (!container) return;

        if (nameEl) nameEl.textContent = product.name;
        if (calsEl) calsEl.textContent = `${Math.round(product.calories)} kcal`;
        if (proteinEl) proteinEl.textContent = `${Math.round(product.protein * 10) / 10} g`;
        if (carbEl) carbEl.textContent = `${Math.round(product.carbs * 10) / 10} g`;
        if (fatEl) fatEl.textContent = `${Math.round(product.fat * 10) / 10} g`;

        container.classList.add('show');
    },

    hideResult() {
        const container = document.getElementById('barcodeResultContainer');
        if (container) {
            container.classList.remove('show');
        }
        this.currentProduct = null;
    },

    async addProductToLog() {
        if (!this.currentProduct) {
            NotificationSystem.warning('No product to add');
            return;
        }

        try {
            // Validate currentProduct has required fields
            if (!this.currentProduct.name) {
                NotificationSystem.error('Invalid product data');
                this.hideResult();
                return;
            }

            await NutritionAPI.addMeal({
                mealId: 'meal_' + Date.now(),
                name: this.currentProduct.name,
                cals: String(Math.round(this.currentProduct.calories)),
                protein: String(Math.round(this.currentProduct.protein * 10) / 10),
                carb: String(Math.round(this.currentProduct.carbs * 10) / 10),
                fat: String(Math.round(this.currentProduct.fat * 10) / 10)
            });

            window.dispatchEvent(new Event('nutrition-updated'));
            
            // Store product name before hiding result
            const productName = this.currentProduct.name;
            
            // Hide result after adding
            this.hideResult();
            
            // Clear barcode input
            const barcodeInput = document.getElementById('barcodeInput');
            if (barcodeInput) barcodeInput.value = '';

            NotificationSystem.success(`${productName} added to food log!`);
        } catch (err) {
            console.error('Error adding product:', err);
            NotificationSystem.error('Failed to add product. Please try again.');
        }
    },

    extractNutrition(product) {
        const nutrition = product.nutriments || {};
        
        // Get serving size info
        const servingSize = product.serving_size_g || 100; // Default to 100g
        
        return {
            calories: nutrition['energy-kcal_100g'] || nutrition['energy-kcal'] || null,
            protein: nutrition.protein_100g || nutrition.protein || null,
            carbs: nutrition.carbohydrates_100g || nutrition.carbohydrates || null,
            fat: nutrition.fat_100g || nutrition.fat || null,
            servingSize: servingSize
        };
    },

    fillNutritionFields(nutrition) {
        const fields = {
            foodCals: nutrition.calories,
            foodProtein: nutrition.protein,
            foodCarb: nutrition.carbs,
            foodFat: nutrition.fat
        };

        Object.entries(fields).forEach(([fieldId, value]) => {
            const element = document.getElementById(fieldId);
            if (element && value !== null) {
                element.value = Math.round(value * 10) / 10; // Round to 1 decimal
            }
        });
    }
};
