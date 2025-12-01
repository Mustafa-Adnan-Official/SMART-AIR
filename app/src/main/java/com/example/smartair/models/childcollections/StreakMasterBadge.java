package com.example.smartair.models.childcollections;

/**
 * Purpose: Represents the streakMasterBadge map in the
 *          achievements summary document:
 *          children/{childUid}/achievements/summary.
 *
 * Layer: Model
 * Used For: Tracking current and highest controller streaks
 *           and whether the Streak Master badge is unlocked.
 */
public class StreakMasterBadge {

    // Total consecutive days the child has hit their controller plan
    private int currentStreakDays;

    // Highest streak achieved so far
    private int highestStreakDays;

    // Whether the Streak Master badge is unlocked
    private boolean unlocked;

    /** Required for Firebase deserialization. */
    public StreakMasterBadge() {
    }

    public StreakMasterBadge(int currentStreakDays, int highestStreakDays, boolean unlocked) {
        this.currentStreakDays = currentStreakDays;
        this.highestStreakDays = highestStreakDays;
        this.unlocked = unlocked;
    }

    public int getCurrentStreakDays() {
        return currentStreakDays;
    }

    public void setCurrentStreakDays(int currentStreakDays) {
        this.currentStreakDays = currentStreakDays;
    }

    public int getHighestStreakDays() {
        return highestStreakDays;
    }

    public void setHighestStreakDays(int highestStreakDays) {
        this.highestStreakDays = highestStreakDays;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}