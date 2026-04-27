/**
 * Nutrition Chart Module
 * Handles weekly nutrition chart rendering
 */

const NutritionChart = {
    weeklyChartInstance: null,
    currentChartView: 'cals',

    getChartColor(view) {
        const root = getComputedStyle(document.documentElement);
        const colors = {
            cals: root.getPropertyValue('--chart-cals').trim(),
            protein: root.getPropertyValue('--chart-protein').trim(),
            carb: root.getPropertyValue('--chart-carb').trim(),
            fat: root.getPropertyValue('--chart-fat').trim()
        };
        return colors[view] || root.getPropertyValue('--chart-cals').trim();
    },

    getChartBackground(view) {
        const root = getComputedStyle(document.documentElement);
        const backgrounds = {
            cals: root.getPropertyValue('--chart-cals-bg').trim(),
            protein: root.getPropertyValue('--chart-protein-bg').trim(),
            carb: root.getPropertyValue('--chart-carb-bg').trim(),
            fat: root.getPropertyValue('--chart-fat-bg').trim()
        };
        return backgrounds[view] || root.getPropertyValue('--chart-cals-bg').trim();
    },

    getChartLabel(view) {
        const labels = {
            cals: 'Calories',
            protein: 'Protein',
            carb: 'Carbs',
            fat: 'Fat'
        };
        return labels[view] || 'Calories';
    },

    getChartUnit(view) {
        return view === 'cals' ? ' kcal' : ' g';
    },

    render(weekData) {
        console.log('Rendering chart with weekData:', weekData, 'view:', this.currentChartView);
        
        if (this.weeklyChartInstance) {
            this.weeklyChartInstance.destroy();
        }

        if (!weekData) {
            // Render default chart with zeros
            const ctx = document.getElementById('weeklyChart').getContext('2d');
            this.weeklyChartInstance = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                    datasets: [{
                        label: this.getChartLabel(this.currentChartView),
                        data: [0, 0, 0, 0, 0, 0, 0],
                        borderColor: this.getChartColor(this.currentChartView),
                        backgroundColor: this.getChartBackground(this.currentChartView),
                        tension: 0.4,
                        fill: true
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                callback: (value) => value + this.getChartUnit(this.currentChartView)
                            }
                        }
                    },
                    plugins: {
                        legend: {
                            display: false
                        }
                    }
                }
            });
            return;
        }

        const ctx = document.getElementById('weeklyChart').getContext('2d');
        const labels = [];
        const data = [];
        const today = new Date();

        for (let i = 6; i >= 0; i--) {
            const date = new Date(today);
            date.setDate(today.getDate() - i);
            const dateStr = date.toISOString().split('T')[0];
            labels.push(date.toLocaleDateString('en-US', {
                weekday: 'short'
            }));
            const dayData = weekData.find(d => d.date === dateStr);
            data.push(dayData ? parseFloat(dayData.totalNutrition?.[this.currentChartView] || 0) : 0);
        }

        this.weeklyChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: this.getChartLabel(this.currentChartView),
                    data,
                    borderColor: this.getChartColor(this.currentChartView),
                    backgroundColor: this.getChartBackground(this.currentChartView),
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: (value) => value + this.getChartUnit(this.currentChartView)
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    }
                }
            }
        });
    },

    switchView(view, weekData) {
        this.currentChartView = view;
        // Update active tab
        document.querySelectorAll('.chart-tab').forEach(tab => {
            tab.classList.toggle('active', tab.getAttribute('data-view') === view);
        });
        // Re-render chart
        this.render(weekData);
    }
};
