package com.example.smartair.models.childcollections;

/**
 * Purpose: Represents the achievements summary document stored in
 *          children/{childUid}/achievements/summary.
 * Layer: Model
 * Used For: Fast loading of badges and streaks in the UI without
 *           scanning raw logs.
 */
public class AchievementSummary {

    private StreakMasterBadge streakMasterBadge;

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
    public AchievementSummary() {
    }

    public AchievementSummary(StreakMasterBadge streakMasterBadge,
                              boolean perfectControllerWeekBadgeEarned,
                              boolean perfectControllerMonthBadgeEarned,
                              boolean perfectControllerYearBadgeEarned,
                              int currentHQTechniqueSessionsStreak,
                              boolean tenHQTechniqueSessionsBadgeEarned,
                              boolean thirtyHQTechniqueSessionsBadgeEarned,
                              boolean hundredHQTechniqueSessionsBadgeEarned,
                              boolean yearHundredHQTechniqueSessionsBadgeEarned,
                              int lowRescueMonthBadgeCount) {
        this.streakMasterBadge = streakMasterBadge;
        this.perfectControllerWeekBadgeEarned = perfectControllerWeekBadgeEarned;
        this.perfectControllerMonthBadgeEarned = perfectControllerMonthBadgeEarned;
        this.perfectControllerYearBadgeEarned = perfectControllerYearBadgeEarned;
        this.currentHQTechniqueSessionsStreak = currentHQTechniqueSessionsStreak;
        this.tenHQTechniqueSessionsBadgeEarned = tenHQTechniqueSessionsBadgeEarned;
        this.thirtyHQTechniqueSessionsBadgeEarned = thirtyHQTechniqueSessionsBadgeEarned;
        this.hundredHQTechniqueSessionsBadgeEarned = hundredHQTechniqueSessionsBadgeEarned;
        this.yearHundredHQTechniqueSessionsBadgeEarned = yearHundredHQTechniqueSessionsBadgeEarned;
        this.lowRescueMonthBadgeCount = lowRescueMonthBadgeCount;
    }

    public StreakMasterBadge getStreakMasterBadge() {
        return streakMasterBadge;
    }

    public void setStreakMasterBadge(StreakMasterBadge streakMasterBadge) {
        this.streakMasterBadge = streakMasterBadge;
    }

    public boolean isPerfectControllerWeekBadgeEarned() {
        return perfectControllerWeekBadgeEarned;
    }

    public void setPerfectControllerWeekBadgeEarned(boolean perfectControllerWeekBadgeEarned) {
        this.perfectControllerWeekBadgeEarned = perfectControllerWeekBadgeEarned;
    }

    public boolean isPerfectControllerMonthBadgeEarned() {
        return perfectControllerMonthBadgeEarned;
    }

    public void setPerfectControllerMonthBadgeEarned(boolean perfectControllerMonthBadgeEarned) {
        this.perfectControllerMonthBadgeEarned = perfectControllerMonthBadgeEarned;
    }

    public boolean isPerfectControllerYearBadgeEarned() {
        return perfectControllerYearBadgeEarned;
    }

    public void setPerfectControllerYearBadgeEarned(boolean perfectControllerYearBadgeEarned) {
        this.perfectControllerYearBadgeEarned = perfectControllerYearBadgeEarned;
    }

    public int getCurrentHQTechniqueSessionsStreak() {
        return currentHQTechniqueSessionsStreak;
    }

    public void setCurrentHQTechniqueSessionsStreak(int currentHQTechniqueSessionsStreak) {
        this.currentHQTechniqueSessionsStreak = currentHQTechniqueSessionsStreak;
    }

    public boolean isTenHQTechniqueSessionsBadgeEarned() {
        return tenHQTechniqueSessionsBadgeEarned;
    }

    public void setTenHQTechniqueSessionsBadgeEarned(boolean tenHQTechniqueSessionsBadgeEarned) {
        this.tenHQTechniqueSessionsBadgeEarned = tenHQTechniqueSessionsBadgeEarned;
    }

    public boolean isThirtyHQTechniqueSessionsBadgeEarned() {
        return thirtyHQTechniqueSessionsBadgeEarned;
    }

    public void setThirtyHQTechniqueSessionsBadgeEarned(boolean thirtyHQTechniqueSessionsBadgeEarned) {
        this.thirtyHQTechniqueSessionsBadgeEarned = thirtyHQTechniqueSessionsBadgeEarned;
    }

    public boolean isHundredHQTechniqueSessionsBadgeEarned() {
        return hundredHQTechniqueSessionsBadgeEarned;
    }

    public void setHundredHQTechniqueSessionsBadgeEarned(boolean hundredHQTechniqueSessionsBadgeEarned) {
        this.hundredHQTechniqueSessionsBadgeEarned = hundredHQTechniqueSessionsBadgeEarned;
    }

    public boolean isYearHundredHQTechniqueSessionsBadgeEarned() {
        return yearHundredHQTechniqueSessionsBadgeEarned;
    }

    public void setYearHundredHQTechniqueSessionsBadgeEarned(boolean yearHundredHQTechniqueSessionsBadgeEarned) {
        this.yearHundredHQTechniqueSessionsBadgeEarned = yearHundredHQTechniqueSessionsBadgeEarned;
    }

    public int getLowRescueMonthBadgeCount() {
        return lowRescueMonthBadgeCount;
    }

    public void setLowRescueMonthBadgeCount(int lowRescueMonthBadgeCount) {
        this.lowRescueMonthBadgeCount = lowRescueMonthBadgeCount;
    }
}