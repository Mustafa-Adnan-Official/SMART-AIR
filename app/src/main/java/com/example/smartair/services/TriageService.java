package com.example.smartair.services;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles Firestore writes for Triage:
 * - incidents/{incidentId}
 * - medLogs/{logId} for rescue/controller (if inventory doc exists).
 * Also updates inventory dosesRemaining and totalAcutations.
 */
public class TriageService {

    public interface TriageCallback {
        void onSuccess();
        void onError(Exception e);
    }

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public TriageService() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Record a triage outcome for the current child.
     */
    public void recordTriageResult(
            String feelingBefore,
            String feelingAfter,
            int rescuePuffs,
            int controllerPuffs,
            @Nullable Integer peakFlow,
            boolean escalatedIncident,
            @Nullable List<String> symptoms
    ) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }
        String childUid = user.getUid();

        if (symptoms == null) {
            symptoms = new ArrayList<>();
        }

        // INCIDENT DOC
        Map<String, Object> incident = new HashMap<>();
        incident.put("timeOfIncident", FieldValue.serverTimestamp());
        incident.put("peakFlow", (peakFlow != null && peakFlow > 0) ? peakFlow : null);
        incident.put("symptoms", symptoms);
        incident.put("feelingBefore", feelingBefore);
        incident.put("feelingAfter", feelingAfter);

        String type;
        if (rescuePuffs > 0 && controllerPuffs > 0) {
            type = "rescue & controller";
        } else if (rescuePuffs > 0) {
            type = "rescue";
        } else if (controllerPuffs > 0) {
            type = "controller";
        } else {
            // no meds taken; default to "rescue" so type is never empty
            type = "rescue";
        }
        incident.put("type", type);

        // For combined type, this is the total puffs (rescue + controller)
        Integer puffsOrMeasures = (rescuePuffs + controllerPuffs) > 0
                ? (rescuePuffs + controllerPuffs)
                : null;
        incident.put("puffsOrMeasures", puffsOrMeasures);

        incident.put("rapidRescueRepeatEvent", false);
        incident.put("escalatedIncident", escalatedIncident);

        // Write incident
        db.collection("children")
                .document(childUid)
                .collection("incidents")
                .add(incident);

        // medlogs: only if inventory exists for that type (per requirement #7)
        if (rescuePuffs > 0) {
            maybeCreateMedLog(
                    childUid,
                    "rescue",
                    rescuePuffs,
                    peakFlow,
                    feelingBefore,
                    feelingAfter,
                    symptoms,
                    null
            );
        }

        if (controllerPuffs > 0) {
            // techniqueTrainerUsed is null in triage flow for now
            maybeCreateMedLog(
                    childUid,
                    "controller",
                    controllerPuffs,
                    peakFlow,
                    feelingBefore,
                    feelingAfter,
                    symptoms,
                    null
            );
        }
    }

    /**
     * Creates medLogs doc only if children/{childUid}/inventory/{medicineType} exists.
     * Also updates dosesRemaining and totalAcutations.
     */
    private void maybeCreateMedLog(
            final String childUid,
            final String medicineType,
            final int puffs,
            @Nullable final Integer peakFlow,
            final String feelingBefore,
            final String feelingAfter,
            final List<String> symptoms,
            @Nullable final Boolean techniqueTrainerUsed
    ) {
        db.collection("children")
                .document(childUid)
                .collection("inventory")
                .document(medicineType)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // No inventory doc → do NOT create medLog, but incident is already created.
                            return;
                        }

                        // Update inventory counts (puffs may be 0; this is fine)
                        db.collection("children")
                                .document(childUid)
                                .collection("inventory")
                                .document(medicineType)
                                .update(
                                        "dosesRemaining", FieldValue.increment(-puffs),
                                        "totalAcutations", FieldValue.increment(puffs)
                                );

                        Map<String, Object> medLog = new HashMap<>();
                        medLog.put("createdAt", FieldValue.serverTimestamp());
                        medLog.put("peakFlow", (peakFlow != null && peakFlow > 0) ? peakFlow : null);
                        medLog.put("symptoms", symptoms);
                        medLog.put("feelingBefore", feelingBefore);
                        medLog.put("feelingAfter", feelingAfter);
                        medLog.put("puffsOrMeasures", puffs);
                        medLog.put("medicineType", medicineType);
                        medLog.put("techniqueTrainerUsed", techniqueTrainerUsed);

                        db.collection("children")
                                .document(childUid)
                                .collection("medLogs")
                                .add(medLog);
                    }
                });
    }

    /**
     * Logs an immediate escalated incident when Step 1 finds serious symptoms.
     *
     * - Always writes an incident (escalatedIncident = true, feelingBefore = "Bad", feelingAfter = "Worse").
     * - If peakFlow is provided, also writes a medLog for "rescue" with 0 puffs (so home screen can use PEF).
     * - No inventory is decremented because puffs = 0.
     */
    public void logImmediateEmergencyFromStep1(
            @Nullable Long peakFlow,
            @Nullable List<String> symptoms,
            @Nullable final TriageCallback callback
    ) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (callback != null) {
                callback.onError(new IllegalStateException("No logged-in child user"));
            }
            return;
        }

        final String childUid = user.getUid();

        // Make a final, safe copy for lambdas
        final List<String> finalSymptoms;
        if (symptoms == null) {
            finalSymptoms = new ArrayList<>();
        } else {
            finalSymptoms = new ArrayList<>(symptoms);
        }

        Map<String, Object> incident = new HashMap<>();
        incident.put("timeOfIncident", FieldValue.serverTimestamp());
        incident.put("peakFlow", peakFlow); // can be null
        incident.put("symptoms", finalSymptoms);
        incident.put("feelingBefore", "Bad");
        incident.put("feelingAfter", "Worse");
        incident.put("type", "rescue");
        incident.put("puffsOrMeasures", null);
        incident.put("rapidRescueRepeatEvent", false);
        incident.put("escalatedIncident", true);

        db.collection("children")
                .document(childUid)
                .collection("incidents")
                .add(incident)
                .addOnSuccessListener(docRef -> {
                    // ALSO create a medLog with 0 puffs if we have a PEF value and inventory exists.
                    if (peakFlow != null && peakFlow > 0) {
                        maybeCreateMedLog(
                                childUid,
                                "rescue",
                                0,                             // no dose taken
                                peakFlow.intValue(),          // Long -> Integer
                                "Bad",
                                "Worse",
                                finalSymptoms,
                                null
                        );
                    }

                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
    }
}