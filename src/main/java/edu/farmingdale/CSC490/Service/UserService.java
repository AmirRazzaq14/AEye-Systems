package edu.farmingdale.CSC490.Service;

import edu.farmingdale.CSC490.Entity.User;
import edu.farmingdale.CSC490.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Register
    public void register(User user) throws Exception {
        try {
            User existing = userRepository.findByEmail(user.getEmail());
            if (existing != null) {
                throw new Exception("Email already registered");
            }
            userRepository.save(user);
        } catch (ExecutionException | InterruptedException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }

    // Login
    public User login(String email, String password) throws Exception {
        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new Exception("User not found");
            }
            if (!user.getPassword().equals(password)) {
                throw new Exception("Invalid password");
            }
            return user;
        } catch (ExecutionException | InterruptedException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }

    // Get user by ID
    public User getUserById(String userId) throws Exception {
        try {
            return userRepository.findById(userId);
        } catch (ExecutionException | InterruptedException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }

    // Update user profile metrics
    public void updateProfile(String userId, java.util.Map<String, Object> updates) throws Exception {
        try {
            userRepository.updateProfile(userId, updates);
        } catch (ExecutionException | InterruptedException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }
}