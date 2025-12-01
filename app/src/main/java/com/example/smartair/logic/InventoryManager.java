package com.example.smartair.logic;

import com.google.firebase.Timestamp;
import com.example.smartair.models.childcollections.InventoryItem;


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
                                 int totalDoses,
                                 int expectedUses
    ) {

        // Enforce at most two items: "rescue" and "controller"
        // Check if item of this type already exists
        InventoryItem existingItem = null;
        for (InventoryItem i : items) {
            if (i.getType().equalsIgnoreCase(medicineType)) {
                existingItem = i;
                break;
            }
        }

        if (existingItem != null) {
            // Update existing item
            existingItem.setPurchaseDate(purchaseDate);
            existingItem.setExpirationDate(expiryDate);
            existingItem.setTotalActuations(totalDoses);
            existingItem.setExpectedDailyUses(expectedUses);
            existingItem.setDosesRemaining(totalDoses); // Reset remaining doses for new canister
            return existingItem;
        } else {
            // Create new item
            InventoryItem item = new InventoryItem(medicineType, expiryDate, purchaseDate, expectedUses, totalDoses, totalDoses);
            items.add(item);
            return item;
        }
    }

    /**
     * Subtract doses after a medicine log.
     */
    public void recordUse(String type, int dosesUsed) {

        if (type == null) return;

        InventoryItem item = null;
        for (InventoryItem i : items) {
            if (i.getType().equalsIgnoreCase(type)) {
                item = i;
                break;
            }
        }

        if (item != null) {
            int remaining = Math.max(0, item.getDosesRemaining() - dosesUsed);
            item.setDosesRemaining(remaining);
        }
    }

    public List<InventoryItem> getItemsForChild(String childUid) {
        return new ArrayList<>(items);
    }


    public List<String> getAlerts(Timestamp now) {
        List<String> alerts = new ArrayList<>();

        for (InventoryItem item : items) {

            // Low canister
            if (item.getTotalActuations() > 0) {
                double ratio = (double) item.getDosesRemaining()
                        / (double) item.getTotalActuations();

                if (ratio <= LOW_THRESHOLD) {
                    alerts.add("Low canister for " + item.getType());
                }
            }

            // Expired medication
            if (item.getExpirationDate() != null
                    && item.getExpirationDate().compareTo(now) <= 0) {

                alerts.add("Expired medication: " + item.getType());
            }
        }

        return alerts;
    }


}
