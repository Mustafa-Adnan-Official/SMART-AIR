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
    private Long expectedDailyUses;
    private Long dosesRemaining;
    private Long totalActuations;

    /** Required for Firebase deserialization. */
    public InventoryItem() {
    }

    public InventoryItem(String type,
                         Timestamp expirationDate,
                         Long expectedDailyUses,
                         Long dosesRemaining,
                         Long totalActuations) {
        this.type = type;
        this.expirationDate = expirationDate;
        this.expectedDailyUses = expectedDailyUses;
        this.dosesRemaining = dosesRemaining;
        this.totalActuations = totalActuations;
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

    public Long getExpectedDailyUses() {
        return expectedDailyUses;
    }

    public void setExpectedDailyUses(Long expectedDailyUses) {
        this.expectedDailyUses = expectedDailyUses;
    }

    public Long getDosesRemaining() {
        return dosesRemaining;
    }

    public void setDosesRemaining(Long dosesRemaining) {
        this.dosesRemaining = dosesRemaining;
    }

    public Long getTotalActuations() {
        return totalActuations;
    }

    public void setTotalActuations(Long totalActuations) {
        this.totalActuations = totalActuations;
    }
}