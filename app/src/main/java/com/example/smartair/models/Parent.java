package com.example.smartair.models;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * Purpose: Represents a parent document stored in /parents/{parentUid}.
 * Layer: Model
 * Used For: Parent auth, linking children, and onboarding state.
 */
public class Parent {

    private String parentUid;        // FirebaseAuth UID
    private String name;
    private String email;
    private String parentAccessCode; // may be null
    private boolean onboarded;
    private Timestamp createdAt;

    // New: list of child UIDs linked to this parent
    private List<String> childUIDs;

    /** Required for Firebase deserialization. */
    public Parent() {}

    public Parent(String parentUid,
                  String name,
                  String email,
                  String parentAccessCode,
                  boolean onboarded,
                  Timestamp createdAt,
                  List<String> childUIDs) {
        this.parentUid = parentUid;
        this.name = name;
        this.email = email;
        this.parentAccessCode = parentAccessCode;
        this.onboarded = onboarded;
        this.createdAt = createdAt;
        this.childUIDs = childUIDs;
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

    public List<String> getChildUIDs() {
        return childUIDs;
    }

    public void setChildUIDs(List<String> childUIDs) {
        this.childUIDs = childUIDs;
    }
}