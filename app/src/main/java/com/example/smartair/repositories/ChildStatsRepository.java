package com.example.smartair.repositories;

import com.example.smartair.callbacks.SimpleResultCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Purpose: Updates child-level streak and badge fields (R3 motivation).
 * Firestore:
 *   children/{childUid}
 *   (fields: currentControllerStreak, highestControllerStreak,
 *    currentHQTechniqueSessionsStreak, ... badge booleans, lowRescueMonthBadgeCount)
 */
public class ChildStatsRepository {

    private final FirebaseFirestore db;

    public ChildStatsRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public ChildStatsRepository(FirebaseFirestore db) {
        this.db = db;
    }

    private DocumentReference childDoc(String childUid) {
        return db.collection("children").document(childUid);
    }

    public void updateControllerStreak(
            final String childUid,
            final int currentControllerStreak,
            final int highestControllerStreak,
            final SimpleResultCallback callback) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("currentControllerStreak", currentControllerStreak);
        updates.put("highestControllerStreak", highestControllerStreak);

        childDoc(childUid)
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to update controller streak: " + e.getMessage()));
    }

    public void updateHQTechniqueStreak(
            final String childUid,
            final int currentHQTechniqueSessionsStreak,
            final SimpleResultCallback callback) {

        childDoc(childUid)
                .update("currentHQTechniqueSessionsStreak", currentHQTechniqueSessionsStreak)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to update HQ technique streak: " + e.getMessage()));
    }

    public void setControllerBadges(
            final String childUid,
            final boolean weekEarned,
            final boolean monthEarned,
            final boolean yearEarned,
            final SimpleResultCallback callback) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("perfectControllerWeekBadgeEarned", weekEarned);
        updates.put("perfectControllerMonthBadgeEarned", monthEarned);
        updates.put("perfectControllerYearBadgeEarned", yearEarned);

        childDoc(childUid)
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to update controller badges: " + e.getMessage()));
    }

    public void setTechniqueBadges(
            final String childUid,
            final boolean tenEarned,
            final boolean thirtyEarned,
            final boolean hundredEarned,
            final boolean yearHundredEarned,
            final SimpleResultCallback callback) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("tenHQTechniqueSessionsBadgeEarned", tenEarned);
        updates.put("thirtyHQTechniqueSessionsBadgeEarned", thirtyEarned);
        updates.put("hundredHQTechniqueSessionsBadgeEarned", hundredEarned);
        updates.put("yearHundredHQTechniqueSessionsBadgeEarned", yearHundredEarned);

        childDoc(childUid)
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to update technique badges: " + e.getMessage()));
    }

    /**
     * Increments lowRescueMonthBadgeCount by 1.
     */
    public void incrementLowRescueMonthBadgeCount(
            final String childUid,
            final SimpleResultCallback callback) {

        childDoc(childUid)
                .update("lowRescueMonthBadgeCount",
                        com.google.firebase.firestore.FieldValue.increment(1))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to increment lowRescueMonthBadgeCount: " + e.getMessage()));
    }
}
