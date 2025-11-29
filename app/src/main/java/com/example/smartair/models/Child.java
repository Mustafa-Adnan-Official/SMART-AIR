package com.example.smartair.models;

import com.google.firebase.Timestamp;

/**
 * Purpose: Represents a child document stored in /children/{childUid}.
 * Layer: Model
 * Used For: Moving child data between Firestore and app logic.
 */
public class Child {

    private String childUid;          // auth UID for both independent and under-parent
    private String name;

    // Login email (child's own email or alias based on parent email)
    private String email;

    // Explicit fields matching Firestore schema
    private String childEmail;        // child's own or alias email
    private String parentEmail;       // null for independent child

    private boolean hasOwnEmail;
    private String parentAccessCode;  // null or actual PAC used during linking
    private String parentUid;         // null if no parent

    private int personalBest;         // default 0
    private boolean onboarded;
    private int controllerAdherence;  // I called this ControllerUses
    private Timestamp createdAt;

    /** Required for Firebase deserialization. */
    public Child() {}

    public Child(String childUid,
                 String name,
                 String email,
                 String childEmail,
                 String parentEmail,
                 boolean hasOwnEmail,
                 String parentAccessCode,
                 String parentUid,
                 int personalBest,
                 int controllerAdherence,
                 boolean onboarded,
                 Timestamp createdAt) {
        this.childUid = childUid;
        this.name = name;
        this.email = email;
        this.childEmail = childEmail;
        this.parentEmail = parentEmail;
        this.hasOwnEmail = hasOwnEmail;
        this.parentAccessCode = parentAccessCode;
        this.parentUid = parentUid;
        this.personalBest = personalBest;
        this.controllerAdherence = controllerAdherence;
        this.onboarded = onboarded;
        this.createdAt = createdAt;
    }

    public String getChildUid() {
        return childUid;
    }

    public void setChildUid(String childUid) {
        this.childUid = childUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Login email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Explicit child email
    public String getChildEmail() {
        return childEmail;
    }

    public void setChildEmail(String childEmail) {
        this.childEmail = childEmail;
    }

    // Parent email reference
    public String getParentEmail() {
        return parentEmail;
    }

    public void setParentEmail(String parentEmail) {
        this.parentEmail = parentEmail;
    }

    public boolean isHasOwnEmail() {
        return hasOwnEmail;
    }

    public void setHasOwnEmail(boolean hasOwnEmail) {
        this.hasOwnEmail = hasOwnEmail;
    }

    public String getParentAccessCode() {
        return parentAccessCode;
    }

    public void setParentAccessCode(String parentAccessCode) {
        this.parentAccessCode = parentAccessCode;
    }

    public String getParentUid() {
        return parentUid;
    }

    public void setParentUid(String parentUid) {
        this.parentUid = parentUid;
    }

    public int getPersonalBest() {
        return personalBest;
    }

    public void setPersonalBest(int personalBest) {
        this.personalBest = personalBest;
    }

    public boolean isOnboarded() {
        return onboarded;
    }

    public void setOnboarded(boolean onboarded) {
        this.onboarded = onboarded;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getControllerUses() {
        return controllerAdherence;
    }

    public void setDailyControllerUses(int newValue) {
        this.controllerAdherence = newValue;
    }
}