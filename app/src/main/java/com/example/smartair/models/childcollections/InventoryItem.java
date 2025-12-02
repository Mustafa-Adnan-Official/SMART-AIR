package com.example.smartair.models.childcollections;

import com.google.firebase.Timestamp;

/**
 * Purpose: Represents a medicine inventory document stored in
 *          children/{childUid}/inventory/{inventoryId}.
 * Layer: Model
 * Used For: Tracking controller/rescue canisters, expiry,
 *           and expected daily controller use.
 */
public class InventoryItem {

    private String type;                // "rescue" or "controller"
    private Timestamp expirationDate;
    private int expectedDailyUses;
    private int dosesRemaining;
    private int totalActuations;
    
    private Timestamp purchaseDate;

    /** Required for Firebase deserialization. */
    public InventoryItem() {
    }

    public InventoryItem(String type,
                         Timestamp expirationDate,
                         Timestamp purchaseDate,
                         int expectedDailyUses,
                         int dosesRemaining,
                         int totalActuations) {
        this.type = type;
        this.expirationDate = expirationDate;
        this.expectedDailyUses = expectedDailyUses;
        this.dosesRemaining = dosesRemaining;
        this.totalActuations = totalActuations;
        this.purchaseDate = purchaseDate;
    
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Timestamp expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public Timestamp getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Timestamp purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public int getExpectedDailyUses() {
        return expectedDailyUses;
    }

    public void setExpectedDailyUses(int expectedDailyUses) {
        this.expectedDailyUses = expectedDailyUses;
    }
    
    

    public int getDosesRemaining() {
        return dosesRemaining;
    }

    public void setDosesRemaining(int dosesRemaining) {
        this.dosesRemaining = dosesRemaining;
    }

    public int getTotalActuations() {
        return totalActuations;
    }

    public void setTotalActuations(int totalActuations) {
        this.totalActuations = totalActuations;
    }
}