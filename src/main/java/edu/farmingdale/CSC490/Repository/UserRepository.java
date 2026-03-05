package edu.farmingdale.CSC490.Repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import edu.farmingdale.CSC490.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class UserRepository {

    @Autowired
    private Firestore firestore;

    // Save a new user
    public void save(User user) throws ExecutionException, InterruptedException {
        firestore.collection("users")
                .document()
                .set(user)
                .get();
    }

    // Find user by email
    public User findByEmail(String email) throws ExecutionException, InterruptedException {
        System.out.println("Looking for email: " + email);

        List<QueryDocumentSnapshot> docs = firestore.collection("users")
                .whereEqualTo("email", email)
                .get().get()
                .getDocuments();

        System.out.println("Found " + docs.size() + " users");

        if (docs.isEmpty()) return null;

        User user = docs.get(0).toObject(User.class);
        System.out.println("Found user: " + user.getEmail());
        return user;
    }

    // Find user by ID
    public User findById(String userId) throws ExecutionException, InterruptedException {
        return firestore.collection("users")
                .document(userId)
                .get().get()
                .toObject(User.class);
    }

    // Update user
    public void update(String userId, User user) throws ExecutionException, InterruptedException {
        firestore.collection("users")
                .document(userId)
                .set(user)
                .get();
    }

    // Delete user
    public void delete(String userId) throws ExecutionException, InterruptedException {
        firestore.collection("users")
                .document(userId)
                .delete()
                .get();
    }


}