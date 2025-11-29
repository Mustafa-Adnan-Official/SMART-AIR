package com.example.smartair.logic;

import com.example.smartair.models.Badge;
import com.example.smartair.models.BadgeType;
import com.example.smartair.models.Streak;
import com.example.smartair.models.StreakType;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose: R3 Motivation logic — streaks & badges.
 * Layer: Logic/Service
 */
public class MotivationManager {

    private final List<Streak> streaks = new ArrayList<>();

    private final List<Badge> badges = new ArrayList<>();

    // -------- Controller streak & badge --------

    public void recordControllerDay(String childUid,
                                    boolean controllerTakenToday) {

        Streak streak = getOrCreateStreak(
                childUid,
                StreakType.CONTROLLER_DAYS
        );

        updateStreak(streak, controllerTakenToday);

        // First perfect controller week = 7 in a row
        if (controllerTakenToday && streak.getCurrentCount() >= 7) {
            unlockBadge(childUid, BadgeType.FIRST_PERFECT_CONTROLLER_WEEK);
        }
    }

    // -------- Technique streak --------

    public void recordTechniqueDay(String childUid,
                                   boolean didTechniqueToday) {

        Streak streak = getOrCreateStreak(
                childUid,
                StreakType.TECHNIQUE_DAYS
        );

        updateStreak(streak, didTechniqueToday);
    }

    /**
     * Call this when total high-quality technique sessions hits >= 10.
     */
    public void onHighQualitySessionCount(String childUid, int count) {
        if (count >= 10) {
            unlockBadge(childUid, BadgeType.TEN_HIGH_QUALITY_TECH_SESSIONS);
        }
    }

    /**
     * Call this when you’ve computed rescue-days for the last 30 days.
     */
    public void evaluateLowRescueMonth(String childUid,
                                       int rescueDaysLast30) {

        if (rescueDaysLast30 <= 4) {
            unlockBadge(childUid, BadgeType.LOW_RESCUE_MONTH);
        }
    }

    public List<Badge> getBadgesForChild(String childUid) {
        List<Badge> result = new ArrayList<>();

        for (Badge b : badges) {
            if (childUid.equals(b.getChildUid())) {
                result.add(b);
            }
        }

        return result;
    }

    public Streak getStreak(String childUid, String streakType) {
        return getOrCreateStreak(childUid, streakType);
    }

    // -------- Internal helpers --------

    private Streak getOrCreateStreak(String childUid, String streakType) {

        for (Streak s : streaks) {
            if (childUid.equals(s.getChildUid())
                    && streakType.equals(s.getStreakType())) {
                return s;
            }
        }

        Streak s = new Streak(childUid, streakType);
        s.setCurrentCount(0);
        s.setLongestCount(0);

        streaks.add(s);

        return s;
    }

    private void updateStreak(Streak streak, boolean successToday) {

        Timestamp now = Timestamp.now();
        streak.setLastUpdated(now);

        if (!successToday) {
            streak.setCurrentCount(0);
            return;
        }

        int newCount = streak.getCurrentCount() + 1;
        streak.setCurrentCount(newCount);

        if (newCount > streak.getLongestCount()) {
            streak.setLongestCount(newCount);
        }
    }

    private void unlockBadge(String childUid, String badgeType) {

        Badge existing = findBadge(childUid, badgeType);

        if (existing != null && existing.isUnlocked()) {
            return;
        }

        Badge badge = (existing != null)
                ? existing
                : new Badge(childUid, badgeType, false, null);

        badge.setUnlocked(true);
        badge.setUnlockedAt(Timestamp.now());

        if (existing == null) {
            badges.add(badge);
        }
    }

    private Badge findBadge(String childUid, String badgeType) {

        for (Badge b : badges) {
            if (childUid.equals(b.getChildUid())
                    && badgeType.equals(b.getBadgeType())) {
                return b;
            }
        }

        return null;
    }
}
