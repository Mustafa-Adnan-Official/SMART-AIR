package com.example.smartair.models;

import com.google.firebase.Timestamp;

public class Child {

    private String childUid;          // if independent: auth UID; if under parent: random id
    private String name;
    private String email;             // either own email or parent email
    private boolean hasOwnEmail;      // true if email is child’s own email
    private String parentAccessCode;  // null or actual PAC
    private String parentUid;         // null / empty if no parent
    private int personalBest;         // default 0
    private boolean onboarded;
    private Timestamp createdAt;

    public Child() {}

    public Child(String childUid,
                 String name,
                 String email,
                 boolean hasOwnEmail,
                 String parentAccessCode,
                 String parentUid,
                 int personalBest,
                 boolean onboarded,
                 Timestamp createdAt) {
        this.childUid = childUid;
        this.name = name;
        this.email = email;
        this.hasOwnEmail = hasOwnEmail;
        this.parentAccessCode = parentAccessCode;
        this.parentUid = parentUid;
        this.personalBest = personalBest;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
