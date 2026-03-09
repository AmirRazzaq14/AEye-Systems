package edu.farmingdale.CSC490.Service;

import edu.farmingdale.CSC490.Entity.Workout_log;
import edu.farmingdale.CSC490.Repository.WorkoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkoutService {

    @Autowired
    private WorkoutRepository workoutRepository;

    public List<Workout_log> getAllWorkouts() throws Exception {
        return workoutRepository.findAll();
    }

    public List<Workout_log> getWorkoutsByUser(String userId) throws Exception {
        return workoutRepository.findByUserId(userId);
    }

    public void createWorkout(Workout_log workout) throws Exception {
        workoutRepository.save(workout);
    }
}