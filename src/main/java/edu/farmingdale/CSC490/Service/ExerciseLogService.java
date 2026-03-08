package edu.farmingdale.CSC490.Service;

import edu.farmingdale.CSC490.Entity.Exercise_log;
import edu.farmingdale.CSC490.Repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseLogService {

    @Autowired
    private ExerciseRepository repo;

    public void saveLog(String uid, Exercise_log log) throws Exception {
        repo.save(uid, log);
    }

    public Exercise_log getLog(String uid, String date) throws Exception {
        return repo.getByDate(uid, date);
    }

    public List<Exercise_log> getAllLogs(String uid) throws Exception {
        return repo.getAll(uid);
    }

    public List<Exercise_log> getLogsForRange(String uid,
                                              String startDate,
                                              String endDate) throws Exception {
        return repo.getByDateRange(uid, startDate, endDate);
    }

    public void deleteLog(String uid, String date) throws Exception {
        repo.delete(uid, date);
    }
}