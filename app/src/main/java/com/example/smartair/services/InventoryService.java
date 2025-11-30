package com.example.smartair.services;

import androidx.annotation.Nullable;

import com.example.smartair.models.childcollections.Alert;
import com.example.smartair.models.childcollections.InventoryItem;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

/**
 * R3: Inventory tracking, low canister alerts, expired alerts.
 * Paths:
 *   children/{childUid}/inventory/{inventoryId}
 *   children/{childUid}/alerts/{alertId}
 */
public class InventoryService {

    private static final String COLLECTION_CHILDREN = "children";
    private static final String SUBCOLLECTION_INVENTORY = "inventory";
    private static final String SUBCOLLECTION_ALERTS = "alerts";

    private final FirebaseFirestore db;

    public InventoryService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface InventoryCallback {
        void onSuccess(@Nullable InventoryItem updatedItem, @Nullable Alert newAlert);
        void onError(Exception e);
    }

    private CollectionReference inventoryRef(String childUid) {
        return db.collection(COLLECTION_CHILDREN)
                .document(childUid)
                .collection(SUBCOLLECTION_INVENTORY);
    }

    private CollectionReference alertsRef(String childUid) {
        return db.collection(COLLECTION_CHILDREN)
                .document(childUid)
                .collection(SUBCOLLECTION_ALERTS);
    }

    /**
     * Called after ANY dose is logged.
     * Decrements dosesRemaining, updates totalActuations,
     * and creates an alert if inventory is low (<=20%) or expired.
     *
     * @param medicineType "rescue" or "controller"
     */
    public void updateInventoryAfterDose(
            String childUid,
            String medicineType,
            long doseCount,
            InventoryCallback callback
    ) {
        // Find the first inventory document for this medicine type
        inventoryRef(childUid)
                .whereEqualTo("type", medicineType)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        if (callback != null) {
                            callback.onError(new IllegalStateException(
                                    "No inventory item found for type: " + medicineType));
                        }
                        return;
                    }

                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    InventoryItem item = doc.toObject(InventoryItem.class);
                    if (item == null) {
                        if (callback != null) {
                            callback.onError(new IllegalStateException(
                                    "Failed to deserialize InventoryItem"));
                        }
                        return;
                    }

                    // --- Update dosesRemaining & totalActuations ---
                    long remaining = item.getDosesRemaining() == null
                            ? 0L
                            : item.getDosesRemaining();

                    long capacity = item.getTotalActuations() == null
                            ? 0L
                            : item.getTotalActuations();

                    // Don’t go below 0
                    long newRemaining = Math.max(0L, remaining - doseCount);
                    item.setDosesRemaining(newRemaining);

                    long newTotalActuations = capacity + doseCount;
                    item.setTotalActuations(newTotalActuations);

                    // --- Low canister check (<=20% of capacity) ---
                    boolean isLow = false;
                    if (capacity > 0) {
                        long lowThreshold = (long) Math.ceil(capacity * 0.2);
                        isLow = newRemaining <= lowThreshold;
                    }

                    // --- Expired check ---
                    boolean isExpired = false;
                    if (item.getExpirationDate() != null) {
                        Date expiry = item.getExpirationDate().toDate();
                        isExpired = expiry.before(new Date());
                    }

                    boolean needsInventoryAlert = isLow || isExpired;

                    Alert alert = null;
                    if (needsInventoryAlert) {
                        alert = new Alert(
                                Timestamp.now(),
                                false, // rapidRescueRepeats
                                false, // redZoneDay
                                false, // triageEscalation
                                false, // worseAfterDose
                                true   // inventoryLowOrExpired
                        );
                    }

                    Alert finalAlert = alert;
                    InventoryItem finalItem = item;
                    DocumentReference docRef = doc.getReference();

                    // Save updated inventory item
                    docRef.set(item)
                            .addOnSuccessListener(unused -> {
                                if (finalAlert != null) {
                                    // Also write an alert document
                                    alertsRef(childUid)
                                            .add(finalAlert)
                                            .addOnSuccessListener(alertRef -> {
                                                if (callback != null) {
                                                    callback.onSuccess(finalItem, finalAlert);
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                if (callback != null) callback.onError(e);
                                            });
                                } else {
                                    if (callback != null) {
                                        callback.onSuccess(finalItem, null);
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onError(e);
                            });

                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }
}
