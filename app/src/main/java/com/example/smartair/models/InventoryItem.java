package com.example.smartair.models;

import com.google.firebase.Timestamp;

/**
 * Purpose: Track inhaler inventory for a child.
 * Stored Under (planned): /children/{childUid}/inventory/{itemId}
 * Layer: Model
 * Used For: Low-canister + expiry alerts (R3).
 */
public class InventoryItem {

    private String itemId;

    private String childUid;

    private String medicineType;      // MedicineType.RESCUE or CONTROLLER

    private String name;              // e.g. "Ventolin HFA"

    private Timestamp purchaseDate;

    private Timestamp expiryDate;

    private int totalDoses;           // e.g. 200 puffs

    private int remainingDoses;       // updated as child/parent logs usage

    /** Required for Firebase. */
    public InventoryItem() {
    }

    public InventoryItem(String itemId,
                         String childUid,
                         String medicineType,
                         String name,
                         Timestamp purchaseDate,
                         Timestamp expiryDate,
                         int totalDoses,
                         int remainingDoses) {

        this.itemId = itemId;
        this.childUid = childUid;
        this.medicineType = medicineType;
        this.name = name;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.totalDoses = totalDoses;
        this.remainingDoses = remainingDoses;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getChildUid() {
        return childUid;
    }

    public void setChildUid(String childUid) {
        this.childUid = childUid;
    }

    public String getMedicineType() {
        return medicineType;
    }

    public void setMedicineType(String medicineType) {
        this.medicineType = medicineType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Timestamp purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Timestamp getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Timestamp expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getTotalDoses() {
        return totalDoses;
    }

    public void setTotalDoses(int totalDoses) {
        this.totalDoses = totalDoses;
    }

    public int getRemainingDoses() {
        return remainingDoses;
    }

    public void setRemainingDoses(int remainingDoses) {
        this.remainingDoses = remainingDoses;
    }
}
