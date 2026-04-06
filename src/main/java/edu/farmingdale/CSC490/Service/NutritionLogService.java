package edu.farmingdale.CSC490.Service;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Repository.NutritionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NutritionLogService {

    @Autowired
    private NutritionRepository repo;

    public void saveLog(String uid, Nutrition_log log) throws Exception {
        repo.save(uid, log);
    }

    public Nutrition_log getLog(String uid, String date) throws Exception {
        return repo.getByDate(uid, date);
    }

    public List<Nutrition_log> getWeekLog(String uid, String date) throws Exception {
        return repo.getWeekLog(uid, date);
    }

    public List<Nutrition_log> getAllLogs(String uid) throws Exception {
        return repo.getAll(uid);
    }

    public void deleteLog(String uid, String date) throws Exception {
        repo.delete(uid, date);
    }

    public void saveMeal(String uid, String dateKey, Nutrition_log.Meal meal) throws Exception {
        repo.saveMeal(uid, dateKey, meal);
    }

    public void deleteMeal(String uid, String date, String id) throws Exception {
        repo.deleteMeal(uid, date, id);
    }

    public void saveNotes(String uid, String dateKey, String notes) throws Exception {
        repo.saveNotes(uid, dateKey,notes);
    }




}












