package com.example.smartair.repositories;

import com.example.smartair.callbacks.ModelResultCallback;
import com.example.smartair.callbacks.SimpleResultCallback;
import com.example.smartair.models.InventoryItem;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Purpose: Firestore repository for R3 inventory items (inhalers).
 * Firestore:
 *   children/{childUid}/inventory/{itemId}
 */
public class InventoryRepository {

    private final FirebaseFirestore db;

    public InventoryRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public InventoryRepository(FirebaseFirestore db) {
        this.db = db;
    }

    private CollectionReference inventoryCollection(String childUid) {
        return db.collection("children")
                .document(childUid)
                .collection("inventory");
    }

    /**
     * Adds or overwrites an inventory item for the child.
     * If item.getItemId() is null, a new document ID is generated.
     */
    public void saveItem(
            final String childUid,
            final InventoryItem item,
            final ModelResultCallback<String> callback) {

        CollectionReference col = inventoryCollection(childUid);

        String itemId = item.getItemId();
        if (itemId == null || itemId.isEmpty()) {
            DocumentReference docRef = col.document();
            itemId = docRef.getId();
            item.setItemId(itemId);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("itemId", item.getItemId());
        data.put("childUid", item.getChildUid());

        // Schema uses "type" ("rescue"/"controller")
        data.put("type", item.getMedicineType());

        data.put("name", item.getName());
        data.put("purchaseDate", item.getPurchaseDate());
        data.put("expirationDate", item.getExpiryDate());

        // totalActuations & dosesRemaining & expectedDailyUses (schema)
        data.put("totalActuations", item.getTotalDoses());
        data.put("dosesRemaining", item.getRemainingDoses());
        data.put("expectedDailyUses", item.getExpectedDailyUses());

        col.document(itemId)
                .set(data)
                .addOnSuccessListener(unused -> callback.onSuccess(itemId))
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to save inventory item: " + e.getMessage()));
    }

    /**
     * Updates only the dosesRemaining for an item, used after logging doses.
     */
    public void updateDosesRemaining(
            final String childUid,
            final String itemId,
            final int newRemaining,
            final SimpleResultCallback callback) {

        inventoryCollection(childUid)
                .document(itemId)
                .update("dosesRemaining", newRemaining)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to update dosesRemaining: " + e.getMessage()));
    }

    /**
     * Fetch all inventory items for a child.
     */
    public void getInventoryForChild(
            final String childUid,
            final ModelResultCallback<List<InventoryItem>> callback) {

        inventoryCollection(childUid)
                .get()
                .addOnSuccessListener(query -> {
                    List<InventoryItem> result = new ArrayList<>();
                    for (DocumentSnapshot snap : query.getDocuments()) {
                        InventoryItem item = snap.toObject(InventoryItem.class);
                        if (item != null) {
                            if (item.getItemId() == null) {
                                item.setItemId(snap.getId());
                            }
                            result.add(item);
                        }
                    }
                    callback.onSuccess(result);
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to fetch inventory: " + e.getMessage()));
    }
}
