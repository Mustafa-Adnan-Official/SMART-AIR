package com.example.smartair.services;

import androidx.annotation.Nullable;

import com.example.smartair.models.childcollections.Alert;
import com.example.smartair.models.childcollections.InventoryItem;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * InventoryService
 *
 * Handles:
 *  - children/{childUid}/inventory/{medicineType}
 *  - Enforces inventory ONLY if the child is under a parent
 *    (children/{childUid}.hasOwnEmail == false).
 *
 * Callers:
 *  - ChildMedLogPresenter (updateInventoryAfterDose)
 *  - InventoryPresenter (refreshInventory using doseCount = 0)
 */
public class InventoryService {

    private final FirebaseFirestore db;

    public InventoryService() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ---------------------------------------------------------------------
    // Callback interface used by existing code
    // ---------------------------------------------------------------------

    public interface InventoryCallback {
        void onSuccess(@Nullable InventoryItem item, @Nullable Alert alert);
        void onError(Exception e);
    }

    // ---------------------------------------------------------------------
    // Public API used by ChildMedLogPresenter + InventoryPresenter
    // ---------------------------------------------------------------------

    /**
     * Update inventory after a dose.
     *
     * Behaviour:
     *  - If child.hasOwnEmail == true OR field is missing:
     *      → SKIP inventory checks entirely, just callback success.
     *  - If child.hasOwnEmail == false:
     *      → Require an inventory doc children/{childUid}/inventory/{medicineType}
     *        and decrement dosesRemaining by doseCount (if > 0).
     *
     * If doseCount == 0, this acts like a "refresh" read for InventoryPresenter.
     */
    public void updateInventoryAfterDose(
            String childUid,
            String medicineType,   // "rescue" or "controller"
            long doseCount,
            InventoryCallback callback
    ) {
        // 1) Check child.hasOwnEmail
        db.collection("children")
                .document(childUid)
                .get()
                .addOnSuccessListener(childSnap -> handleChildDoc(
                        childUid,
                        medicineType,
                        doseCount,
                        childSnap,
                        callback
                ))
                .addOnFailureListener(callback::onError);
    }

    // ---------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------

    private void handleChildDoc(
            String childUid,
            String medicineType,
            long doseCount,
            DocumentSnapshot childSnap,
            InventoryCallback callback
    ) {
        // Default behaviour if field is missing: treat as standalone child
        Boolean hasOwnEmail = childSnap.getBoolean("hasOwnEmail");

        // Standalone child → NO parent-managed inventory → skip check
        if (hasOwnEmail == null || hasOwnEmail) {
            // We just say "success, no inventory change".
            callback.onSuccess(null, null);
            return;
        }

        // Child under a parent → enforce inventory
        DocumentReference invRef = db.collection("children")
                .document(childUid)
                .collection("inventory")
                .document(medicineType);

        invRef.get()
                .addOnSuccessListener(invSnap -> handleInventoryDoc(
                        invRef,
                        invSnap,
                        doseCount,
                        callback
                ))
                .addOnFailureListener(callback::onError);
    }

    private void handleInventoryDoc(
            DocumentReference invRef,
            DocumentSnapshot invSnap,
            long doseCount,
            InventoryCallback callback
    ) {
        if (!invSnap.exists()) {
            callback.onError(
                    new IllegalStateException("No inventory item found for this medicine type.")
            );
            return;
        }

        // If doseCount == 0, we just return the current item (refresh use case)
        Long currentRemaining = invSnap.getLong("dosesRemaining");
        long remaining = currentRemaining != null ? currentRemaining : 0L;

        if (doseCount <= 0) {
            InventoryItem item = invSnap.toObject(InventoryItem.class);
            callback.onSuccess(item, null);
            return;
        }

        long newRemaining = Math.max(0, remaining - doseCount);

        invRef.update("dosesRemaining", newRemaining)
                .addOnSuccessListener(unused -> {
                    InventoryItem item = invSnap.toObject(InventoryItem.class);
                    // We don’t call any setters on InventoryItem to avoid
                    // depending on its exact fields; UI can re-read if needed.
                    callback.onSuccess(item, null);
                })
                .addOnFailureListener(callback::onError);
    }
}