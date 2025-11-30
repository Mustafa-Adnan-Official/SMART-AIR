package com.example.smartair.services;

import androidx.annotation.NonNull;

import com.example.smartair.models.childcollections.AchievementSummary;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**i
 * R3: Streaks and badges system.
 * Path: children/{childUid}/achievements/summary
 */
public class AchievementService {

    private static final String COLLECTION_CHILDREN = "children";
    private static final String SUBCOLLECTION_ACHIEVEMENTS = "achievements";
    private static final String DOCUMENT_SUMMARY = "summary";

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

    // --- Helpers ---

    private void loadSummary(String childUid, @NonNull AchievementCallback callback,
                             @NonNull java.util.function.Consumer<AchievementSummary> mutateFn) {

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

    // --- Public API ---

    /**
     * Called when a controller dose is logged.
     * Very simple example streak logic (you can adjust thresholds later).
     */
    public void updateAfterControllerDose(
            String childUid,
            Timestamp doseTime,
            AchievementCallback callback
    ) {
        loadSummary(childUid, callback, summary -> {
            // Example logic: just increment HQ controller streak field if you add one,
            // or use the existing streakMasterBadge / other flags.
            // For now, we only flip badges based on your own criteria later.
            // TODO: implement real controller streak logic if desired.
        });
    }

    /**
     * Called when a high-quality technique session is completed.
     */
    public void updateAfterTechniqueSession(
            String childUid,
            Timestamp sessionTime,
            AchievementCallback callback
    ) {
        loadSummary(childUid, callback, summary -> {
            int current = summary.getCurrentHQTechniqueSessionsStreak();
            summary.setCurrentHQTechniqueSessionsStreak(current + 1);

            // Example badge unlocking:
            if (current + 1 >= 10) {
                summary.setTenHQTechniqueSessionsBadgeEarned(true);
            }
            if (current + 1 >= 30) {
                summary.setThirtyHQTechniqueSessionsBadgeEarned(true);
            }
            if (current + 1 >= 100) {
                summary.setHundredHQTechniqueSessionsBadgeEarned(true);
            }

            // TODO: add yearHundredHQTechniqueSessionsBadgeEarned logic based on dates if needed.
        });
    }

    /**
     * Called after a rescue dose to maintain “low rescue month” badge progress.
     * Right now this just exists as a hook; you can implement monthly counting logic later.
     */
    public void updateAfterRescueDose(
            String childUid,
            Timestamp rescueTime,
            AchievementCallback callback
    ) {
        loadSummary(childUid, callback, summary -> {
            // TODO: track rescues per month and update lowRescueMonthBadgeCount appropriately.
            // For now, we leave it unchanged so the app runs without crashing.
        });
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
