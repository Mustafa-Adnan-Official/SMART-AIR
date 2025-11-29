package com.example.smartair.models;

import com.google.firebase.Timestamp;

/**
 * Purpose: Track running streaks per child.
 * Stored Under (planned): /children/{childUid}/streaks/{streakType}
 * Layer: Model
 */
public class Streak {

    private String childUid;

    private String streakType;     // StreakType.CONTROLLER_DAYS or TECHNIQUE_DAYS

    private int currentCount;

    private int longestCount;

    private Timestamp lastUpdated; // last time this streak was updated

    public Streak() {
    }

    public Streak(String childUid, String streakType) {
        this.childUid = childUid;
        this.streakType = streakType;
    }

    public String getChildUid() {
        return childUid;
    }

    public void setChildUid(String childUid) {
        this.childUid = childUid;
    }

    public String getStreakType() {
        return streakType;
    }

    public void setStreakType(String streakType) {
        this.streakType = streakType;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }

    public int getLongestCount() {
        return longestCount;
    }

    public void setLongestCount(int longestCount) {
        this.longestCount = longestCount;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
