package com.example.smartair.models.childcollections;

/**
 * Purpose: Represents the streakMasterBadge map in the
 *          achievements summary document.
 * Layer: Model
 * Used For: Tracking current and highest controller streaks.
 */
public class StreakMasterBadge {

    private int currentControllerStreak;
    private int highestControllerStreak;

    /** Required for Firebase deserialization. */
    public StreakMasterBadge() {
    }

    public StreakMasterBadge(int currentControllerStreak, int highestControllerStreak) {
        this.currentControllerStreak = currentControllerStreak;
        this.highestControllerStreak = highestControllerStreak;
    }

    public int getCurrentControllerStreak() {
        return currentControllerStreak;
    }

    public void setCurrentControllerStreak(int currentControllerStreak) {
        this.currentControllerStreak = currentControllerStreak;
    }

    public int getHighestControllerStreak() {
        return highestControllerStreak;
    }

    public void setHighestControllerStreak(int highestControllerStreak) {
        this.highestControllerStreak = highestControllerStreak;
    }
}