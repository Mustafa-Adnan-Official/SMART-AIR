package com.example.smartair.services;

import androidx.annotation.Nullable;

import com.example.smartair.models.childcollections.MedLog;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Service: handles Firestore operations for medicine logs.
 * Path: children/{childUid}/medLogs/{logId}
 */
public class MedLogService {

    private static final String COLLECTION_CHILDREN = "children";
    private static final String SUBCOLLECTION_MED_LOGS = "medLogs";

    private final FirebaseFirestore db;

    public MedLogService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface MedLogCallback {
        void onSuccess(MedLog savedLog);
        void onError(Exception e);
    }

    private CollectionReference medLogsRef(String childUid) {
        return db.collection(COLLECTION_CHILDREN)
                .document(childUid)
                .collection(SUBCOLLECTION_MED_LOGS);
    }

    /** R3: Log a rescue dose. */
    public void logRescueDose(
            String childUid,
            long doseCount,
            @Nullable Long peakFlow,
            @Nullable java.util.List<String> symptoms,
            @Nullable String feelingBefore,
            @Nullable String feelingAfter,
            @Nullable String breathRating,  // you can encode this in feelings if you want
            MedLogCallback callback
    ) {
        MedLog log = new MedLog(
                Timestamp.now(),
                peakFlow,
                symptoms,
                feelingBefore,
                feelingAfter,
                doseCount,
                "rescue",
                null   // no technique trainer for rescue
        );

        medLogsRef(childUid)
                .add(log)
                .addOnSuccessListener(docRef -> {
                    if (callback != null) callback.onSuccess(log);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    /** R3: Log a controller dose (with technique flag). */
    public void logControllerDose(
            String childUid,
            long doseCount,
            boolean techniqueTrainerUsed,
            @Nullable Long peakFlow,
            @Nullable java.util.List<String> symptoms,
            @Nullable String feelingBefore,
            @Nullable String feelingAfter,
            @Nullable String breathRating,
            MedLogCallback callback
    ) {
        MedLog log = new MedLog(
                Timestamp.now(),
                peakFlow,
                symptoms,
                feelingBefore,
                feelingAfter,
                doseCount,
                "controller",
                techniqueTrainerUsed
        );

        medLogsRef(childUid)
                .add(log)
                .addOnSuccessListener(docRef -> {
                    if (callback != null) callback.onSuccess(log);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }
}
