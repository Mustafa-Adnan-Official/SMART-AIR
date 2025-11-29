package com.example.smartair.logic;

import com.example.smartair.models.InventoryItem;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Purpose: R3 Inventory logic — amount left, low canister, expiry.
 * Layer: Logic/Service
 */
public class InventoryManager {

    // 20% threshold from R3 requirements
    private static final double LOW_THRESHOLD = 0.20;

    private final List<InventoryItem> items = new ArrayList<>();

    public InventoryItem addItem(String childUid,
                                 String medicineType,
                                 String name,
                                 Timestamp purchaseDate,
                                 Timestamp expiryDate,
                                 int totalDoses) {

        String id = UUID.randomUUID().toString();

        InventoryItem item = new InventoryItem(
                id,
                childUid,
                medicineType,
                name,
                purchaseDate,
                expiryDate,
                totalDoses,
                totalDoses
        );

        items.add(item);

        return item;
    }

    /**
     * Subtract doses after a medicine log.
     */
    public void recordUse(String itemId, int dosesUsed) {

        InventoryItem item = findById(itemId);

        if (item != null) {
            int remaining = Math.max(0, item.getRemainingDoses() - dosesUsed);
            item.setRemainingDoses(remaining);
        }
    }

    public List<InventoryItem> getItemsForChild(String childUid) {
        List<InventoryItem> result = new ArrayList<>();

        for (InventoryItem i : items) {
            if (childUid.equals(i.getChildUid())) {
                result.add(i);
            }
        }

        return result;
    }

    /**
     * Returns human-readable alert messages (for UI or notifications).
     */
    public List<String> getAlerts(Timestamp now) {
        List<String> alerts = new ArrayList<>();

        for (InventoryItem item : items) {

            // Low canister
            if (item.getTotalDoses() > 0) {
                double ratio = (double) item.getRemainingDoses()
                        / (double) item.getTotalDoses();

                if (ratio <= LOW_THRESHOLD) {
                    alerts.add("Low canister for " + item.getName());
                }
            }

            // Expired medication
            if (item.getExpiryDate() != null
                    && item.getExpiryDate().compareTo(now) <= 0) {

                alerts.add("Expired medication: " + item.getName());
            }
        }

        return alerts;
    }

    private InventoryItem findById(String id) {
        for (InventoryItem i : items) {
            if (id.equals(i.getItemId())) {
                return i;
            }
        }
        return null;
    }
}
