package com.example.smartair.models;

import com.google.firebase.Timestamp;

/**
 * Purpose: Represents a single unlocked badge for a child.
 * Stored Under (planned): /children/{childUid}/badges/{badgeType}
 * Layer: Model
 */
public class Badge {

    private String childUid;

    private String badgeType;      // BadgeType.*

    private boolean unlocked;

    private Timestamp unlockedAt;

    public Badge() {
    }

    public Badge(String childUid,
                 String badgeType,
                 boolean unlocked,
                 Timestamp unlockedAt) {

        this.childUid = childUid;
        this.badgeType = badgeType;
        this.unlocked = unlocked;
        this.unlockedAt = unlockedAt;
    }

    public String getChildUid() {
        return childUid;
    }

    public void setChildUid(String childUid) {
        this.childUid = childUid;
    }

    public String getBadgeType() {
        return badgeType;
    }

    public void setBadgeType(String badgeType) {
        this.badgeType = badgeType;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public Timestamp getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(Timestamp unlockedAt) {
        this.unlockedAt = unlockedAt;
    }
}
