package edu.farmingdale.CSC490.Repository;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Entity.User;
import edu.farmingdale.CSC490.Food.NutritionCalculationService;
import edu.farmingdale.CSC490.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class NutritionRepository {
    
    @Autowired
    private UserService userService;

    @Autowired
    private NutritionCalculationService nutritionCalculationService;

    private Firestore getDb() {
        return FirestoreClient.getFirestore();
    }

    private DocumentReference docRef(String uid, String dateKey) {
        return getDb().collection("users")
                .document(uid)
                .collection("nutritionLogs")
                .document(dateKey);
    }

    private CollectionReference colRef(String uid) {
        return getDb().collection("users")
                .document(uid)
                .collection("nutritionLogs");
    }

    public void save(String uid, Nutrition_log nutrition_log) throws Exception {
        nutrition_log.setUpdatedAt(Instant.now().toString()); // ← ISO-8601 format
        docRef(uid, nutrition_log.getDate()).set(nutrition_log).get();
        log.info("Nutrition log saved successfully for date {}", nutrition_log.getDate());
    }

    public Nutrition_log getByDate(String uid, String dateKey) throws Exception {
        DocumentSnapshot snap = docRef(uid, dateKey).get().get();
        if (snap.exists()) {
            Nutrition_log nutrition_log = snap.toObject(Nutrition_log.class);
            assert nutrition_log != null;
            log.info("Nutrition log exist on :{}", nutrition_log.getDate());
            return nutrition_log;
        }else {
            log.warn("Nutrition log not exist, return new log");
            Nutrition_log nutrition_log = new Nutrition_log();
            applyDefaultValues(uid,nutrition_log);
            return nutrition_log;

        }
    }


    public List<Nutrition_log> getAll(String uid) throws Exception {
        List<Nutrition_log> list = new ArrayList<>();
        for (DocumentSnapshot d : colRef(uid).get().get().getDocuments()) {
            Nutrition_log log = d.toObject(Nutrition_log.class);
            if (log != null) {
                list.add(log);
            }
        }
        log.info("Nutrition log list size: {}", list.size());
        return list;
    }

    public void delete(String uid, String dateKey) throws Exception {
        docRef(uid, dateKey).delete().get();
        log.info("Nutrition log deleted successfully for date {}", dateKey);
    }

    public void saveMeal(String uid,  String dateKey, Nutrition_log.Meal meal) throws Exception {
        Nutrition_log existingLog = getByDate(uid, dateKey);
        if (existingLog != null) {
            existingLog.getMeals().add(meal);
            nutritionCalculationService.calculateTotalNutrition(existingLog);
            existingLog.setUpdatedAt(Instant.now().toString());
            docRef(uid, existingLog.getId()).set(existingLog).get();
            log.info("Meal saved successfully for log {}", dateKey);
        }else {
            log.error("Nutrition log not exist");
        }
    }

    public void deleteMeal(String uid, String date, String mealId) throws Exception {
        Nutrition_log existingLog = getByDate(uid, date);
        
        if (existingLog != null && existingLog.getMeals() != null) {
            log.debug("Checking nutrition log: {}", existingLog);
            boolean mealRemoved = existingLog.getMeals().removeIf(m -> 
                m.getMealId() != null && m.getMealId().equals(mealId)
            );
            
            if (mealRemoved) {
                nutritionCalculationService.calculateTotalNutrition(existingLog);
                existingLog.setUpdatedAt(Instant.now().toString());
                docRef(uid, existingLog.getId()).set(existingLog).get();
                log.info("Meal {} deleted successfully from log {}", mealId, date);
            } else {
                log.warn("Meal {} not found in nutrition log {}", mealId, date);
            }
        } else {
            log.error("Nutrition log for date {} does not exist or has no meals", date);
        }
    }

    public void saveNotes(String uid, String dateKey, String note) throws Exception {
        Nutrition_log existingLog = getByDate(uid, dateKey);
        if (existingLog != null) {
            existingLog.setNotes(note);
            nutritionCalculationService.calculateTotalNutrition(existingLog);
            existingLog.setUpdatedAt(Instant.now().toString());
            docRef(uid, existingLog.getId()).set(existingLog).get();
            log.info("Notes saved successfully for log {}", dateKey);
        }else {
            log.error("Nutrition log not exist, can't save notes");
        }
    }

    public List<Nutrition_log> getWeekLog(String uid, String Date) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<LocalDate> datesInWeekUpToDate = getLocalDates(Date, formatter);

        List<Nutrition_log> list = new ArrayList<>();
        for (DocumentSnapshot d : colRef(uid).get().get().getDocuments()) {
            Nutrition_log nutrition_log = d.toObject(Nutrition_log.class);
            if (nutrition_log != null) {
                try {
                    LocalDate logDate = LocalDate.parse(nutrition_log.getDate(), formatter);
                    if (datesInWeekUpToDate.contains(logDate)) {
                        list.add(nutrition_log);
                    }
                } catch (Exception e) {
                    log.warn("Invalid date format in log: {}", nutrition_log.getDate());
                }
            }
        }

        return list;
    }

    public void updateCalorieGoal(String uid, String dateKey, String targetCalorie) throws Exception {
        Nutrition_log existingLog = getByDate(uid, dateKey);
        if (existingLog != null) {
            nutritionCalculationService.calculateTargetNutritionByGoal(existingLog, Double.parseDouble(targetCalorie));
            existingLog.setUpdatedAt(Instant.now().toString());
            docRef(uid, existingLog.getId()).set(existingLog).get();
            log.info("Target Calorie updated successfully for log {}", dateKey);
        }else {
            log.error("Nutrition log not exist, can't update target Calorie");
        }
    }


    // which is used in getWeekLog for the weekday of the specified date
    @NotNull
    private static List<LocalDate> getLocalDates(String Date, DateTimeFormatter formatter) {
        LocalDate inputDate = LocalDate.parse(Date, formatter);

        // Get the Monday of the week of the specified date
        LocalDate mondayOfWeek = inputDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Build all dates of the week (from Monday to the specified date)
        List<LocalDate> datesInWeekUpToDate = new ArrayList<>();
        for (LocalDate date = mondayOfWeek; !date.isAfter(inputDate); date = date.plusDays(1)) {
            datesInWeekUpToDate.add(date);
        }
        return datesInWeekUpToDate;
    }


    //  default log setting for not null value in log
    private void applyDefaultValues(String uid, Nutrition_log log) throws Exception {

            log.setUserId(uid);
            log.setId(LocalDate.now().toString());
            log.setDate(LocalDate.now().toString());
            log.setMeals(new ArrayList<>());
            log.setTotalNutrition(new Nutrition_log.Nutrition("0","0","0","0"));
            User user = userService.getUserById(uid);
            nutritionCalculationService.calculateTargetNutritionByGoal(log, user.getCalorieGoal());
            log.setNotes("");
            save(uid, log);

    }



}