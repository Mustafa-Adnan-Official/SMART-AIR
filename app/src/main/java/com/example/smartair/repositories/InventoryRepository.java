package com.example.smartair.repositories;

import com.example.smartair.callbacks.ModelResultCallback;
import com.example.smartair.callbacks.SimpleResultCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.smartair.models.childcollections.InventoryItem;
import com.google.firebase.firestore.FieldPath;

import java.util.ArrayList;
import java.util.Arrays;
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

        String itemId;
        if (item.getType().equals("rescue")){
            itemId = "rescue";
        } else if (item.getType().equals("controller")){
            itemId = "controller";
        } else {
            throw new IllegalArgumentException("Invalid item type: " + item.getType());
        }


        Map<String, Object> data = new HashMap<>();

        // Schema uses "type" ("rescue"/"controller")
        data.put("type", item.getType());

        data.put("purchaseDate", item.getPurchaseDate());
        data.put("expirationDate", item.getExpirationDate());

        // totalActuations & dosesRemaining & expectedDailyUses (schema)
        data.put("totalActuations", item.getTotalActuations());
        data.put("dosesRemaining", item.getDosesRemaining());
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
     * Fetch only "rescue" and "controller" inventory items for a child.
     */
    public void getInventoryForChild(
            final String childUid,
            final ModelResultCallback<List<InventoryItem>> callback) {

        inventoryCollection(childUid)
                .whereIn(FieldPath.documentId(), Arrays.asList("rescue", "controller"))
                .get()
                .addOnSuccessListener(query -> {
                    List<InventoryItem> result = new ArrayList<>();
                    for (DocumentSnapshot snap : query.getDocuments()) {
                        InventoryItem item = snap.toObject(InventoryItem.class);
                        if (item != null) {
                            result.add(item);
                        }
                    }
                    callback.onSuccess(result);
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to fetch inventory: " + e.getMessage()));
    }
}
