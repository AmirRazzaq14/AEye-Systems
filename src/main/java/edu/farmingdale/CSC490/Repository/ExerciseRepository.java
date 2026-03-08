package edu.farmingdale.CSC490.Repository;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.farmingdale.CSC490.Entity.Exercise_log;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class ExerciseRepository {

    private Firestore getDb() {
        return FirestoreClient.getFirestore();
    }

    private DocumentReference docRef(String uid, String dateKey) {
        return getDb().collection("users")
                .document(uid)
                .collection("exerciseLogs")
                .document(dateKey);
    }

    private CollectionReference colRef(String uid) {
        return getDb().collection("users")
                .document(uid)
                .collection("exerciseLogs");
    }

    public void save(String uid, Exercise_log log) throws Exception {
        log.setUpdatedAt(new Date().toString());
        docRef(uid, log.getDate()).set(log).get();
    }

    public Exercise_log getByDate(String uid, String dateKey) throws Exception {
        DocumentSnapshot snap = docRef(uid, dateKey).get().get();
        if (snap.exists()) {
            Exercise_log log = snap.toObject(Exercise_log.class);
            if (log != null) {
                log.setId(snap.getId());
                log.setDate(snap.getId()); // ← FIX: populate date from document ID
            }
            return log;
        }
        return null;
    }

    public List<Exercise_log> getAll(String uid) throws Exception {
        List<Exercise_log> list = new ArrayList<>();
        for (DocumentSnapshot d : colRef(uid).get().get().getDocuments()) {
            Exercise_log log = d.toObject(Exercise_log.class);
            if (log != null) {
                log.setId(d.getId());
                log.setDate(d.getId()); // ← FIX: populate date from document ID
                list.add(log);
            }
        }
        return list;
    }

    public List<Exercise_log> getByDateRange(String uid,
                                             String startDate,
                                             String endDate) throws Exception {
        List<Exercise_log> list = new ArrayList<>();
        for (DocumentSnapshot d : colRef(uid)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", Query.Direction.ASCENDING)
                .get().get().getDocuments()) {
            Exercise_log log = d.toObject(Exercise_log.class);
            if (log != null) {
                log.setId(d.getId());
                log.setDate(d.getId()); // ← FIX: populate date from document ID
                list.add(log);
            }
        }
        return list;
    }

    public void delete(String uid, String dateKey) throws Exception {
        docRef(uid, dateKey).delete().get();
    }
}