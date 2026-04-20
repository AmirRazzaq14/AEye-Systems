package edu.farmingdale.CSC490.Repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import edu.farmingdale.CSC490.Entity.Motion_log;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
@SuppressWarnings("null")
public class MotionLogRepository {

    private com.google.cloud.firestore.Firestore getDb() {
        return FirestoreClient.getFirestore();
    }

    private DocumentReference docRef(String uid, String dateKey) {
        return getDb().collection("users")
                .document(uid)
                .collection("motionLogs")
                .document(dateKey);
    }

    private CollectionReference colRef(String uid) {
        return getDb().collection("users")
                .document(uid)
                .collection("motionLogs");
    }

    public void save(String uid, Motion_log log) throws Exception {
        log.setUpdatedAt(Instant.now().toString());
        docRef(uid, log.getDate()).set(log).get();
    }

    public Motion_log getByDate(String uid, String dateKey) throws Exception {
        DocumentSnapshot snap = docRef(uid, dateKey).get().get();
        if (!snap.exists()) return null;
        Motion_log log = snap.toObject(Motion_log.class);
        if (log != null) {
            log.setId(snap.getId());
            log.setDate(snap.getId());
        }
        return log;
    }

    public List<Motion_log> getAll(String uid) throws Exception {
        List<Motion_log> list = new ArrayList<>();
        for (DocumentSnapshot d : colRef(uid).get().get().getDocuments()) {
            Motion_log log = d.toObject(Motion_log.class);
            if (log != null) {
                log.setId(d.getId());
                log.setDate(d.getId());
                list.add(log);
            }
        }
        return list;
    }
}

