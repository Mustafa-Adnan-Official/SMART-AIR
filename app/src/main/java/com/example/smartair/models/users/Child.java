package com.example.smartair.models.users;

import com.google.firebase.Timestamp;

import java.util.List;

/**
 * Purpose: Represents a child document stored in /children/{childUid}.
 * Layer: Model
 * Used For: Moving child data between Firestore and app logic.
 *
 * Firestore fields (R1 core):
 *  - childUid
 *  - name
 *  - childEmail
 *  - parentEmail
 *  - hasOwnEmail
 *  - parentUid
 *  - parentAccessCode
 *  - role
 *  - personalBest
 *  - onboarded
 *  - createdAt
 */
public class Child {

    private String childUid;
    private String name;

    private String childEmail;
    private String parentEmail;
    private boolean hasOwnEmail;
    private String parentAccessCode;
    private String parentUid;
    private String role;          // "child"

    private int personalBest;
    private boolean onboarded;
    private int controllerAdherence;  // optional summary field
    private Timestamp createdAt;

    private List<String> providers;

    // --- R3 Achievements / Badges (for later, but you can leave these as plain fields if needed) ---

    private int currentControllerStreak;
    private int highestControllerStreak;

    private boolean perfectControllerWeekBadgeEarned;
    private boolean perfectControllerMonthBadgeEarned;
    private boolean perfectControllerYearBadgeEarned;

    private int currentHQTechniqueSessionsStreak;

    private boolean tenHQTechniqueSessionsBadgeEarned;
    private boolean thirtyHQTechniqueSessionsBadgeEarned;
    private boolean hundredHQTechniqueSessionsBadgeEarned;
    private boolean yearHundredHQTechniqueSessionsBadgeEarned;

    private int lowRescueMonthBadgeCount;

    /** Required for Firebase deserialization. */
    public Child() {
    }

    public Child(String childUid,
                 String name,
                 String childEmail,
                 String parentEmail,
                 boolean hasOwnEmail,
                 String parentAccessCode,
                 String parentUid,
                 String role,
                 int personalBest,
                 int controllerAdherence,
                 boolean onboarded,
                 Timestamp createdAt,
                List<String> providers
    ) {

        this.childUid = childUid;
        this.name = name;
        this.childEmail = childEmail;
        this.parentEmail = parentEmail;
        this.hasOwnEmail = hasOwnEmail;
        this.parentAccessCode = parentAccessCode;
        this.parentUid = parentUid;
        this.role = role;
        this.personalBest = personalBest;
        this.onboarded = onboarded;
        this.createdAt = createdAt;
        this.controllerAdherence = controllerAdherence;
        this.providers = providers;
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

    public String getChildEmail() {
        return childEmail;
    }

    public void setChildEmail(String childEmail) {
        this.childEmail = childEmail;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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


    public List<String> getProviders() {
        return providers;
    }

    public void setProviders(List<String> providers) {
        this.providers = providers;
    }

}
