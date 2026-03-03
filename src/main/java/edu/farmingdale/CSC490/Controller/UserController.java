package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Entity.User;
import edu.farmingdale.CSC490.Storge.MockDatabase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final MockDatabase mockDatabase;

    public UserController(MockDatabase mockDatabase) {
        this.mockDatabase = mockDatabase;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(mockDatabase.users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        Optional<User> user = mockDatabase.users.stream()
                .filter(u -> u.getUser_id() == id)
                .findFirst();
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // generate a basic auto-incrementing ID based on size
        int newId = mockDatabase.users.size() + 1;
        user.setUser_id(newId);
        mockDatabase.users.add(user);
        return ResponseEntity.status(201).body(user);
    }
}
