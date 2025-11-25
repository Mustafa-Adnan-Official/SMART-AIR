package com.example.smartair.models;

import com.google.firebase.Timestamp;

/**
 * Purpose: Represents a single medicine use log.
 * Stored Under (planned): /children/{childUid}/medicineLogs/{logId}
 * Layer: Model
 * Used For: R3 Medicine logs + Pre/Post check.
 */
public class MedicineLog {

    private String logId;           // Firestore doc id (can be null before save)

    private String childUid;        // owner child

    private String medicineType;    // use MedicineType.RESCUE or MedicineType.CONTROLLER

    private int doseCount;          // puffs/measures

    private Timestamp takenAt;      // when the dose was taken

    // Pre / Post check fields

    private String preStatus;       // PrePostStatus.*

    private Integer preBreathRating;

    private String postStatus;      // PrePostStatus.*

    private Integer postBreathRating;

    /** Required for Firebase deserialization. */
    public MedicineLog() {
    }

    public MedicineLog(String logId,
                       String childUid,
                       String medicineType,
                       int doseCount,
                       Timestamp takenAt) {

        this.logId = logId;
        this.childUid = childUid;
        this.medicineType = medicineType;
        this.doseCount = doseCount;
        this.takenAt = takenAt;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
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

    public int getDoseCount() {
        return doseCount;
    }

    public void setDoseCount(int doseCount) {
        this.doseCount = doseCount;
    }

    public Timestamp getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(Timestamp takenAt) {
        this.takenAt = takenAt;
    }

    public String getPreStatus() {
        return preStatus;
    }

    public void setPreStatus(String preStatus) {
        this.preStatus = preStatus;
    }

    public Integer getPreBreathRating() {
        return preBreathRating;
    }

    public void setPreBreathRating(Integer preBreathRating) {
        this.preBreathRating = preBreathRating;
    }

    public String getPostStatus() {
        return postStatus;
    }

    public void setPostStatus(String postStatus) {
        this.postStatus = postStatus;
    }

    public Integer getPostBreathRating() {
        return postBreathRating;
    }

    public void setPostBreathRating(Integer postBreathRating) {
        this.postBreathRating = postBreathRating;
    }
}
