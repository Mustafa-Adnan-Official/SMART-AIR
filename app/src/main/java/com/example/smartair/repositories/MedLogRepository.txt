package com.example.smartair.repositories;

import androidx.annotation.NonNull;

import com.example.smartair.callbacks.ModelResultCallback;
import com.example.smartair.callbacks.SimpleResultCallback;
import com.example.smartair.models.MedicineLog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Purpose: Firestore access for R3 medicine logs (rescue + controller).
 * Layer: Repository (used by presenters / managers).
 * Firestore:
 *   children/{childUid}/medLogs/{logId}
 */
public class MedLogRepository {

    private final FirebaseFirestore db;

    public MedLogRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public MedLogRepository(FirebaseFirestore db) {
        this.db = db;
    }

    private CollectionReference medLogsCollection(String childUid) {
        return db.collection("children")
                .document(childUid)
                .collection("medLogs");
    }

    /**
     * Creates a new medicine log document for the given child.
     * If log.getLogId() is null, a new document ID is generated.
     * Returns the final logId via callback.
     */
    public void addLog(
            final String childUid,
            final MedicineLog log,
            final ModelResultCallback<String> callback) {

        CollectionReference col = medLogsCollection(childUid);

        String logId = log.getLogId();
        if (logId == null || logId.isEmpty()) {
            DocumentReference docRef = col.document();
            logId = docRef.getId();
            log.setLogId(logId);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("logId", log.getLogId());
        data.put("childUid", log.getChildUid());
        data.put("medicineType", log.getMedicineType()); // "RESCUE"/"CONTROLLER"
        data.put("puffsOrMeasures", log.getDoseCount());

        Timestamp takenAt = log.getTakenAt();
        data.put("createdAt", takenAt != null ? takenAt : Timestamp.now());

        // Pre/Post feeling + breath ratings
        data.put("feelingBefore", log.getPreStatus());
        data.put("feelingAfter", log.getPostStatus());
        data.put("breathRatingBefore", log.getPreBreathRating());
        data.put("breathRatingAfter", log.getPostBreathRating());

        // Optional extras if you added them to MedicineLog
        // data.put("peakFlow", log.getPeakFlow());
        // data.put("symptoms", log.getSymptoms());
        // data.put("techniqueTrainerUsed", log.getTechniqueTrainerUsed());

        col.document(logId)
                .set(data)
                .addOnSuccessListener(unused -> callback.onSuccess(logId))
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to save med log: " + e.getMessage()));
    }

    /**
     * Updates the pre/post check section for an existing medicine log.
     */
    public void updatePrePostCheck(
            final String childUid,
            final String logId,
            final String preStatus,
            final Integer preBreathRating,
            final String postStatus,
            final Integer postBreathRating,
            final SimpleResultCallback callback) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("feelingBefore", preStatus);
        updates.put("breathRatingBefore", preBreathRating);
        updates.put("feelingAfter", postStatus);
        updates.put("breathRatingAfter", postBreathRating);

        medLogsCollection(childUid)
                .document(logId)
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to update pre/post check: " + e.getMessage()));
    }

    /**
     * Marks whether the technique trainer was used for this controller log.
     */
    public void setTechniqueTrainerUsed(
            final String childUid,
            final String logId,
            final boolean used,
            final SimpleResultCallback callback) {

        medLogsCollection(childUid)
                .document(logId)
                .update("techniqueTrainerUsed", used)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to set techniqueTrainerUsed: " + e.getMessage()));
    }

    /**
     * Fetches all medicine logs for a child (optionally, you can later add date filters).
     */
    public void getLogsForChild(
            final String childUid,
            final ModelResultCallback<List<MedicineLog>> callback) {

        medLogsCollection(childUid)
                .get()
                .addOnSuccessListener(query -> {
                    List<MedicineLog> result = new ArrayList<>();
                    for (DocumentSnapshot snap : query.getDocuments()) {
                        MedicineLog log = snap.toObject(MedicineLog.class);
                        if (log != null) {
                            // make sure logId is set from document id if missing
                            if (log.getLogId() == null) {
                                log.setLogId(snap.getId());
                            }
                            result.add(log);
                        }
                    }
                    callback.onSuccess(result);
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to fetch med logs: " + e.getMessage()));
    }
}
