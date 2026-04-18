package edu.farmingdale.CSC490.Service;

import edu.farmingdale.CSC490.Entity.Motion_log;
import edu.farmingdale.CSC490.Repository.MotionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MotionLogService {

    @Autowired
    private MotionLogRepository repo;

    public void saveLog(String uid, Motion_log log) throws Exception {
        repo.save(uid, log);
    }

    public Motion_log getLog(String uid, String date) throws Exception {
        return repo.getByDate(uid, date);
    }

    public List<Motion_log> getAllLogs(String uid) throws Exception {
        return repo.getAll(uid);
    }
}

