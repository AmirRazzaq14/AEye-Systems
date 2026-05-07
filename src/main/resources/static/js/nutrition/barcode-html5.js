/**
 * HTML5 QR Code Scanner Module
 * Handles barcode scanning and product lookup using html5-qrcode library
 */

const BarcodeScanner = {
    isScanning: false,
    currentProduct: null,
    html5QrCode: null,

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
        const uploadBarcodeBtn = document.getElementById('uploadBarcodeBtn');
        const barcodeFileInput = document.getElementById('barcodeFileInput');
        const scannerContainer = document.getElementById('scanner-container');

        if (scanBtn) {
            scanBtn.addEventListener('click', () => this.toggleScanner());
        }

        if (closeBtn) {
            closeBtn.addEventListener('click', () => this.stopScanner());
        }

        if (enterBtn) {
            enterBtn.addEventListener('click', () => {
                if (scannerContainer) {
                    scannerContainer.classList.add('hidden');
                }
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

        if (uploadBarcodeBtn) {
            uploadBarcodeBtn.addEventListener('click', () => {
               if (scannerContainer) {
                    scannerContainer.classList.add('hidden');
                }

                if (barcodeFileInput) {
                    barcodeFileInput.click();
                }
            });
        }

        if (barcodeFileInput) {
            barcodeFileInput.addEventListener('change', (e) => {
                this.handleBarcodeImageUpload(e);
            });
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

        if (!reader) {
            console.error('Scanner elements not found');
            return;
        }

        try {
            scannerContainer.classList.remove('hidden');

            // Initialize html5-qrcode
            this.html5QrCode = new Html5Qrcode("reader");
            
            const config = {
                fps: 10,
                aspectRatio: 1.0,
                disableFlip: false,
                videoConstraints: {
                    facingMode: "environment",
                    width: { min: 640, ideal: 1280, max: 1920 },
                    height: { min: 480, ideal: 720, max: 1080 }
                }
            };

            await this.html5QrCode.start(
                { facingMode: "environment" },
                config,
                (decodedText, decodedResult) => {
                    this.onBarcodeDetected(decodedText);
                },
                (errorMessage) => {
                    // Parsing error, ignore
                }
            ).then(() => {
                this.isScanning = true;
                const scanStatus = document.getElementById('scanStatus');
                if (scanStatus) {
                    scanStatus.textContent = 'Looking for barcode...';
                    scanStatus.className = 'scan-status';
                }
            }).catch((err) => {
                console.error('Failed to start scanner:', err);
                NotificationSystem.error('Camera access failed. Please check permissions.');
                scannerContainer.classList.add('hidden');
            });

        } catch (err) {
            console.error('Error starting scanner:', err);
            NotificationSystem.error('Failed to start camera.');
            scannerContainer.classList.add('hidden');
        }
    },

    onBarcodeDetected(code) {
        // Verify barcode format
        if (!this.isValidBarcode(code)) {
            const scanStatus = document.getElementById('scanStatus');
            if (scanStatus) {
                scanStatus.textContent = 'Invalid barcode format';
                scanStatus.className = 'scan-status';
            }
            return;
        }

        console.log('Barcode detected:', code);
        const scanStatus = document.getElementById('scanStatus');
        if (scanStatus) {
            scanStatus.textContent = '✓ Barcode detected!';
            scanStatus.className = 'scan-status detected';
        }

        // Stop scanner after successful detection
        this.stopScanner();

        // Set the barcode in input field
        const barcodeInput = document.getElementById('barcodeInput');
        if (barcodeInput) {
            barcodeInput.value = code;
        }

        // Lookup the barcode
        this.lookupBarcode(code);
    },

    // Verify barcode format
    isValidBarcode(code) {
        // EAN-13
        if (/^\d{13}$/.test(code)) return true;
        // UPC-A
        if (/^\d{12}$/.test(code)) return true;
        // EAN-8
        if (/^\d{8}$/.test(code)) return true;
        // Code 128/39
        if (/^[A-Za-z0-9\-\. \$\/\+\%]+$/.test(code) && code.length >= 3) return true;

        return false;
    },

    async stopScanner() {
        const scannerContainer = document.getElementById('scanner-container');

        if (this.isScanning && this.html5QrCode) {
            try {
                await this.html5QrCode.stop();
                this.html5QrCode.clear();
            } catch (err) {
                console.error('Error stopping scanner:', err);
            }
            this.isScanning = false;
        }

        if (scannerContainer) {
            scannerContainer.classList.add('hidden');
        }

        NotificationSystem.info('Scanner stopped');
    },

    async handleBarcodeImageUpload(event) {
        const file = event.target.files[0];

        if (!file || !file.type.startsWith('image/')) {
            NotificationSystem.warning('Please select an image file');
            return;
        }

        console.log('Processing barcode image:', file.name);
        NotificationSystem.info('Analyzing barcode from image...');

        try {
            const html5QrCode = new Html5Qrcode("reader");
            
            const config = {
                fps: 10,
                qrbox: { width: 250, height: 150 },
                aspectRatio: 1.0,
                disableFlip: false
            };

            const result = await html5QrCode.scanFile(file, true);
            
            if (result) {
                console.log('Barcode detected from image:', result);

                if (this.isValidBarcode(result)) {
                    const barcodeInput = document.getElementById('barcodeInput');
                    if (barcodeInput) {
                        barcodeInput.value = result;
                    }

                    this.lookupBarcode(result);
                } else {
                    NotificationSystem.warning('Invalid barcode format detected.');
                }
            } else {
                NotificationSystem.warning('No barcode detected. Try another image.');
            }

            html5QrCode.clear();
        } catch (err) {
            console.error('Error scanning image:', err);

            if (err.message && err.message.includes('No MultiFormat Readers')) {
                NotificationSystem.warning('Could not detect barcode in this image. Please ensure the barcode is clear and well-lit.');
            } else {
                NotificationSystem.warning('Failed to scan image. Please try a clearer image.');
            }
        }

        event.target.value = '';
    },

    async lookupBarcode(barcode) {
        if (!barcode) {
            NotificationSystem.warning('Please enter or scan a barcode');
            return;
        }

        console.log('Looking up barcode:', barcode);
        NotificationSystem.info('Looking up product...');

        try {
            const resultContainer = document.getElementById('barcodeResultContainer');
            if (resultContainer) {
                resultContainer.classList.remove('show');
            }

            const response = await fetch(`https://world.openfoodfacts.org/api/v0/product/${barcode}.json`);
            const data = await response.json();

            if (data.status === 1 && data.product) {
                const product = data.product;
                const nutrition = product.nutriments || {};

                this.currentProduct = {
                    name: product.product_name || 'Unknown Product',
                    calories: nutrition['energy-kcal_serving'] || nutrition['energy-kcal_100g'] || 0,
                    protein: nutrition['proteins_serving'] || nutrition.proteins_100g || 0,
                    carbs: nutrition['carbohydrates_serving'] || nutrition.carbohydrates_100g || 0,
                    fat: nutrition['fat_serving'] || nutrition.fat_100g || 0
                };

                this.displayResult(this.currentProduct);
            } else {
                NotificationSystem.warning('Product not found. Try another barcode.');
            }
        } catch (err) {
            console.error('Error looking up barcode:', err);
            NotificationSystem.error('Failed to lookup barcode.');
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
        const barcodeResultContainer = document.getElementById('barcodeResultContainer');
        const barcodeInput = document.getElementById('barcodeInput');
        if (barcodeInput) {
            barcodeInput.value = '';
        }
        if (barcodeResultContainer) {
            barcodeResultContainer.classList.remove('show');
        }
        this.currentProduct = null;
    },

    async addProductToLog() {
        if (!this.currentProduct) {
            NotificationSystem.warning('No product to add');
            return;
        }

        try {
            await NutritionAPI.addMeal({
                mealId: 'meal_' + Date.now(),
                name: this.currentProduct.name,
                cals: String(Math.round(this.currentProduct.calories)),
                protein: String(Math.round(this.currentProduct.protein * 10) / 10),
                carb: String(Math.round(this.currentProduct.carbs * 10) / 10),
                fat: String(Math.round(this.currentProduct.fat * 10) / 10)
            });

            window.dispatchEvent(new Event('nutrition-updated'));

            const productName = this.currentProduct.name;
            this.hideResult();

            const barcodeInput = document.getElementById('barcodeInput');
            if (barcodeInput) barcodeInput.value = '';

            NotificationSystem.success(`${productName} added to food log!`);
        } catch (err) {
            console.error('Error adding product:', err);
            NotificationSystem.error('Failed to add product.');
        }
    }
};
