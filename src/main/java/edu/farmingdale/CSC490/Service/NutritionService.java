package edu.farmingdale.CSC490.Service;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Repository.NutritionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NutritionService {

    @Autowired
    private NutritionRepository nutritionRepository;

    public void logMeal(Nutrition_log log) throws Exception {
        // Add any business logic here
        // e.g. calculate total calories
        nutritionRepository.save(log);
    }

    public List<Nutrition_log> getUserLogs(String userId) throws Exception {
        return nutritionRepository.findByUserId(userId);
    }
}