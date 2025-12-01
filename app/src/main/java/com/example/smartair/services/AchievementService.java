package com.example.smartair.services;

import androidx.annotation.NonNull;

import com.example.smartair.models.childcollections.AchievementSummary;
import com.example.smartair.models.childcollections.MedLog;
import com.example.smartair.models.childcollections.StreakMasterBadge;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * R3: Streaks and badges system.
 * Path: children/{childUid}/achievements/summary
 *
 * This version:
 *  - Uses ONLY a range filter on createdAt (no composite index needed).
 *  - Recomputes controller streak + perfect controller week progress.
 *  - Recomputes low-rescue-month status (≤4 rescue days in last 30 days).
 */
public class AchievementService {

    private static final String COLLECTION_CHILDREN = "children";
    private static final String SUBCOLLECTION_ACHIEVEMENTS = "achievements";
    private static final String DOCUMENT_SUMMARY = "summary";

    // We also need medLogs to recompute streaks / rescue usage
    private static final String SUBCOLLECTION_MED_LOGS = "medLogs";

    private final FirebaseFirestore db;

    public AchievementService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface AchievementCallback {
        void onSuccess(AchievementSummary summary);
        void onError(Exception e);
    }

    private DocumentReference summaryRef(String childUid) {
        return db.collection(COLLECTION_CHILDREN)
                .document(childUid)
                .collection(SUBCOLLECTION_ACHIEVEMENTS)
                .document(DOCUMENT_SUMMARY);
    }

    private CollectionReference medLogsRef(String childUid) {
        return db.collection(COLLECTION_CHILDREN)
                .document(childUid)
                .collection(SUBCOLLECTION_MED_LOGS);
    }

    // --- Shared helper: load + mutate + save summary ---

    private void loadSummary(
            String childUid,
            @NonNull AchievementCallback callback,
            @NonNull java.util.function.Consumer<AchievementSummary> mutateFn
    ) {
        summaryRef(childUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    AchievementSummary summary = snapshot.toObject(AchievementSummary.class);
                    if (summary == null) {
                        summary = new AchievementSummary(); // all defaults
                    }

                    mutateFn.accept(summary);

                    final AchievementSummary finalSummary = summary;

                    summaryRef(childUid)
                            .set(finalSummary)
                            .addOnSuccessListener(unused -> callback.onSuccess(finalSummary))
                            .addOnFailureListener(callback::onError);

                })
                .addOnFailureListener(callback::onError);
    }

    // Small helper to convert a Timestamp to "yyyy-MM-dd" day key.
    private String dayKeyFromTimestamp(Timestamp ts) {
        if (ts == null) return null;
        Date d = ts.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(d);
    }

    private Timestamp daysAgo(int days) {
        long nowMs = System.currentTimeMillis();
        long delta = days * 24L * 60L * 60L * 1000L;
        return new Timestamp(new Date(nowMs - delta));
    }

    // ---------------------------------------------------------------------
    //  Public API
    // ---------------------------------------------------------------------

    /**
     * Called when a controller dose is logged.
     *
     * Logic:
     *  - Look at all medLogs with createdAt in the last 7 days.
     *  - Count distinct days with medicineType == "controller".
     *    -> That = current streak days (approximation).
     *  - Update streakMasterBadge.current/highest.
     *  - Perfect Controller Week badge:
     *      if highestStreakDays >= 7 => mark earned.
     */
    public void updateAfterControllerDose(
            String childUid,
            Timestamp doseTime,
            AchievementCallback callback
    ) {
        // We only need last 7 days of logs for controller streak
        Timestamp sevenDaysAgo = daysAgo(7);

        medLogsRef(childUid)
                .whereGreaterThanOrEqualTo("createdAt", sevenDaysAgo)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    // Distinct days with a controller log in last 7 days
                    Set<String> controllerDays = new HashSet<>();

                    for (var doc : querySnapshot.getDocuments()) {
                        MedLog log = doc.toObject(MedLog.class);
                        if (log == null) continue;
                        if (log.getCreatedAt() == null) continue;

                        String dayKey = dayKeyFromTimestamp(log.getCreatedAt());
                        if (dayKey == null) continue;

                        if ("controller".equalsIgnoreCase(log.getMedicineType())) {
                            controllerDays.add(dayKey);
                        }
                    }

                    int controllerStreakDays = controllerDays.size();

                    // Now mutate the summary based on this derived info
                    loadSummary(childUid, callback, summary -> {
                        StreakMasterBadge badge = summary.getStreakMasterBadge();
                        if (badge == null) {
                            badge = new StreakMasterBadge();
                        }

                        // Update current streak (capped at 7 for perfect-week display)
                        badge.setCurrentStreakDays(controllerStreakDays);

                        // Update highest streak if needed
                        if (controllerStreakDays > badge.getHighestStreakDays()) {
                            badge.setHighestStreakDays(controllerStreakDays);
                        }

                        summary.setStreakMasterBadge(badge);

                        // Perfect controller week badge earned if highest streak >= 7
                        if (badge.getHighestStreakDays() >= 7) {
                            summary.setPerfectControllerWeekBadgeEarned(true);
                        }
                    });

                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Called when a high-quality technique session is completed.
     * (You can wire this from TechniqueTrainerActivity later if needed.)
     */
    public void updateAfterTechniqueSession(
            String childUid,
            Timestamp sessionTime,
            AchievementCallback callback
    ) {
        loadSummary(childUid, callback, summary -> {
            int current = summary.getCurrentHQTechniqueSessionsStreak();
            int newValue = current + 1;
            summary.setCurrentHQTechniqueSessionsStreak(newValue);

            // Example badge unlocking thresholds:
            if (newValue >= 10) {
                summary.setTenHQTechniqueSessionsBadgeEarned(true);
            }
            if (newValue >= 30) {
                summary.setThirtyHQTechniqueSessionsBadgeEarned(true);
            }
            if (newValue >= 100) {
                summary.setHundredHQTechniqueSessionsBadgeEarned(true);
            }
            // yearHundredHQTechniqueSessionsBadgeEarned would need date-based logic.
        });
    }

    /**
     * Called after a rescue dose to maintain “low rescue month” badge.
     *
     * Logic:
     *  - Look at medLogs with createdAt in last 30 days.
     *  - Count distinct days where medicineType == "rescue".
     *  - Store that count in lowRescueMonthBadgeCount.
     *    (UI will show blue if ≤4, red if >4.)
     */
    public void updateAfterRescueDose(
            String childUid,
            Timestamp rescueTime,
            AchievementCallback callback
    ) {
        Timestamp thirtyDaysAgo = daysAgo(30);

        medLogsRef(childUid)
                .whereGreaterThanOrEqualTo("createdAt", thirtyDaysAgo)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    Set<String> rescueDays = new HashSet<>();

                    for (var doc : querySnapshot.getDocuments()) {
                        MedLog log = doc.toObject(MedLog.class);
                        if (log == null) continue;
                        if (log.getCreatedAt() == null) continue;

                        String dayKey = dayKeyFromTimestamp(log.getCreatedAt());
                        if (dayKey == null) continue;

                        if ("rescue".equalsIgnoreCase(log.getMedicineType())) {
                            rescueDays.add(dayKey);
                        }
                    }

                    int rescueDaysLast30 = rescueDays.size();

                    loadSummary(childUid, callback, summary -> {
                        // Store the *count* of rescue days in last 30.
                        summary.setLowRescueMonthBadgeCount(rescueDaysLast30);
                    });

                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Simple "getter" if a screen just wants to show current achievements.
     */
    public void getCurrentSummary(String childUid, AchievementCallback callback) {
        summaryRef(childUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    AchievementSummary summary = snapshot.toObject(AchievementSummary.class);
                    if (summary == null) summary = new AchievementSummary();
                    callback.onSuccess(summary);
                })
                .addOnFailureListener(callback::onError);
    }
}