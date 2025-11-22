package com.example.smartair.models;

import com.google.firebase.Timestamp;

public class Parent {

    private String parentUid;          // same as FirebaseAuth UID
    private String name;
    private String email;
    private String parentAccessCode;   // may be null until later requirement
    private boolean onboarded;
    private Timestamp createdAt;

    // Firestore requires empty constructor
    public Parent() {}

    public Parent(String parentUid,
                  String name,
                  String email,
                  String parentAccessCode,
                  boolean onboarded,
                  Timestamp createdAt) {
        this.parentUid = parentUid;
        this.name = name;
        this.email = email;
        this.parentAccessCode = parentAccessCode;
        this.onboarded = onboarded;
        this.createdAt = createdAt;
    }

    public String getParentUid() {
        return parentUid;
    }

    public void setParentUid(String parentUid) {
        this.parentUid = parentUid;
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

    public String getParentAccessCode() {
        return parentAccessCode;
    }

    public void setParentAccessCode(String parentAccessCode) {
        this.parentAccessCode = parentAccessCode;
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
