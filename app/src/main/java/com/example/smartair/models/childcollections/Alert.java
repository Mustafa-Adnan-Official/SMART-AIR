package com.example.smartair.models.childcollections;

import com.google.firebase.Timestamp;

/**
 * Purpose: Represents a safety alert document stored in
 *          children/{childUid}/alerts/{alertId}.
 * Layer: Model
 * Used For: Showing and analyzing important safety events
 *           like rapid rescue repeats and red-zone days.
 */
public class Alert {

    private Timestamp createdAt;
    private boolean rapidRescueRepeats;
    private boolean redZoneDay;
    private boolean triageEscalation;
    private boolean worseAfterDose;
    private boolean inventoryLowOrExpired;

    /** Required for Firebase deserialization. */
    public Alert() {
    }

    public Alert(Timestamp createdAt,
                 boolean rapidRescueRepeats,
                 boolean redZoneDay,
                 boolean triageEscalation,
                 boolean worseAfterDose,
                 boolean inventoryLowOrExpired) {
        this.createdAt = createdAt;
        this.rapidRescueRepeats = rapidRescueRepeats;
        this.redZoneDay = redZoneDay;
        this.triageEscalation = triageEscalation;
        this.worseAfterDose = worseAfterDose;
        this.inventoryLowOrExpired = inventoryLowOrExpired;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRapidRescueRepeats() {
        return rapidRescueRepeats;
    }

    public void setRapidRescueRepeats(boolean rapidRescueRepeats) {
        this.rapidRescueRepeats = rapidRescueRepeats;
    }

    public boolean isRedZoneDay() {
        return redZoneDay;
    }

    public void setRedZoneDay(boolean redZoneDay) {
        this.redZoneDay = redZoneDay;
    }

    public boolean isTriageEscalation() {
        return triageEscalation;
    }

    public void setTriageEscalation(boolean triageEscalation) {
        this.triageEscalation = triageEscalation;
    }

    public boolean isWorseAfterDose() {
        return worseAfterDose;
    }

    public void setWorseAfterDose(boolean worseAfterDose) {
        this.worseAfterDose = worseAfterDose;
    }

    public boolean isInventoryLowOrExpired() {
        return inventoryLowOrExpired;
    }

    public void setInventoryLowOrExpired(boolean inventoryLowOrExpired) {
        this.inventoryLowOrExpired = inventoryLowOrExpired;
    }
}