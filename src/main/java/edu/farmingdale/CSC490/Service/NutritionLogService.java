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

    public List<Nutrition_log> getAllLogs(String uid) throws Exception {
        return repo.getAll(uid);
    }

    public void deleteLog(String uid, String date) throws Exception {
        repo.delete(uid, date);
    }
}












