package edu.farmingdale.CSC490.Service;

import edu.farmingdale.CSC490.Entity.Motion_log;
import edu.farmingdale.CSC490.Repository.MotionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MotionLogService {

    @Autowired
    private MotionLogRepository repo;

    /**
     * Upserts the motion log for a calendar day. Merges with any existing document so a
     * client that resets in-memory totals to zero cannot wipe calories already saved for today.
     */
    public void saveLog(String uid, Motion_log incoming) throws Exception {
        Motion_log existing = repo.getByDate(uid, incoming.getDate());
        Motion_log merged = mergeMotionLog(existing, incoming);
        merged.setUpdatedAt(Instant.now().toString());
        repo.save(uid, merged);
    }

    public Motion_log getLog(String uid, String date) throws Exception {
        return repo.getByDate(uid, date);
    }

    public List<Motion_log> getAllLogs(String uid) throws Exception {
        return repo.getAll(uid);
    }

    private static Motion_log mergeMotionLog(Motion_log existing, Motion_log incoming) {
        if (incoming == null) {
            throw new IllegalArgumentException("incoming log required");
        }
        if (existing == null) {
            return incoming;
        }

        double mergedCals = Math.max(safeDouble(existing.getCaloriesTotal()), safeDouble(incoming.getCaloriesTotal()));
        double mergedWeight =
                incoming.getWeightKg() > 0 ? incoming.getWeightKg() : safeDouble(existing.getWeightKg());

        Map<String, Map<String, Object>> mergedPer =
                mergePerExerciseMaps(existing.getPerExercise(), incoming.getPerExercise());

        return Motion_log.builder()
                .id(incoming.getDate())
                .date(incoming.getDate())
                .weightKg(mergedWeight)
                .caloriesTotal(mergedCals)
                .perExercise(mergedPer)
                .build();
    }

    private static Map<String, Map<String, Object>> mergePerExerciseMaps(
            Map<String, Map<String, Object>> a,
            Map<String, Map<String, Object>> b) {
        if (a == null || a.isEmpty()) {
            return b != null ? deepCopyPerExercise(b) : new HashMap<>();
        }
        if (b == null || b.isEmpty()) {
            return deepCopyPerExercise(a);
        }
        Map<String, Map<String, Object>> out = deepCopyPerExercise(a);
        Set<String> keys = new HashSet<>();
        keys.addAll(out.keySet());
        keys.addAll(b.keySet());
        for (String k : keys) {
            Map<String, Object> av = out.get(k);
            Map<String, Object> bv = b.get(k);
            if (bv == null) {
                continue;
            }
            if (av == null) {
                out.put(k, new HashMap<>(bv));
                continue;
            }
            int rA = toInt(av.get("reps"));
            int rB = toInt(bv.get("reps"));
            double kA = toDouble(av.get("kcal"));
            double kB = toDouble(bv.get("kcal"));
            Map<String, Object> merged = new HashMap<>();
            merged.put("reps", Math.max(rA, rB));
            merged.put("kcal", Math.max(kA, kB));
            out.put(k, merged);
        }
        return out;
    }

    private static Map<String, Map<String, Object>> deepCopyPerExercise(Map<String, Map<String, Object>> src) {
        Map<String, Map<String, Object>> copy = new HashMap<>();
        if (src == null) {
            return copy;
        }
        for (Map.Entry<String, Map<String, Object>> e : src.entrySet()) {
            copy.put(e.getKey(), e.getValue() != null ? new HashMap<>(e.getValue()) : new HashMap<>());
        }
        return copy;
    }

    private static int toInt(Object v) {
        if (v == null) {
            return 0;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double toDouble(Object v) {
        if (v == null) {
            return 0;
        }
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double safeDouble(double v) {
        return Double.isFinite(v) ? v : 0;
    }
}
