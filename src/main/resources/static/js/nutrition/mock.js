/**
 * Mock Data Provider for Nutrition API
 *
 * This module provides mock data for testing.
 * It should ONLY be called by NutritionAPI when useMock is enabled.
 *
 * DO NOT call these methods directly from UI components.
 */

const NutritionMock = {

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
    },

    getMockMeal(){
     console.warn('Using Mock date');
        // Fallback to mock data
        const mockResults = [
            { name: 'Grilled Chicken Salad', cals: 450, protein: 35, carb: 20, fat: 25 },
            { name: 'Pasta Carbonara', cals: 650, protein: 28, carb: 75, fat: 32 },
            { name: 'Sushi Roll Set', cals: 380, protein: 15, carb: 50, fat: 12 },
            { name: 'Burger with Fries', cals: 850, protein: 35, carb: 90, fat: 40 },
            { name: 'Buddha Bowl', cals: 550, protein: 22, carb: 65, fat: 18 }
        ];
        return mockResults[Math.floor(Math.random() * mockResults.length)];
    }

}