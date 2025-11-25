package com.example.smartair.models;

import com.google.firebase.Timestamp;

/**
 * Purpose: Represents one guided inhaler technique session.
 * Stored Under (planned): /children/{childUid}/techniqueSessions/{sessionId}
 * Layer: Model
 */
public class TechniqueSession {

    private String sessionId;

    private String childUid;

    private Timestamp startedAt;

    // Step completion flags

    private boolean stepSealLips;

    private boolean stepSlowDeepBreath;

    private boolean stepHoldTenSeconds;

    private boolean stepWaitBetweenPuffs;

    private boolean stepSpacerTips;

    // High-quality = all steps done with positive feedback

    private boolean highQuality;

    /** Required for Firebase. */
    public TechniqueSession() {
    }

    public TechniqueSession(String sessionId,
                            String childUid,
                            Timestamp startedAt) {

        this.sessionId = sessionId;
        this.childUid = childUid;
        this.startedAt = startedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getChildUid() {
        return childUid;
    }

    public void setChildUid(String childUid) {
        this.childUid = childUid;
    }

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    public boolean isStepSealLips() {
        return stepSealLips;
    }

    public void setStepSealLips(boolean stepSealLips) {
        this.stepSealLips = stepSealLips;
    }

    public boolean isStepSlowDeepBreath() {
        return stepSlowDeepBreath;
    }

    public void setStepSlowDeepBreath(boolean stepSlowDeepBreath) {
        this.stepSlowDeepBreath = stepSlowDeepBreath;
    }

    public boolean isStepHoldTenSeconds() {
        return stepHoldTenSeconds;
    }

    public void setStepHoldTenSeconds(boolean stepHoldTenSeconds) {
        this.stepHoldTenSeconds = stepHoldTenSeconds;
    }

    public boolean isStepWaitBetweenPuffs() {
        return stepWaitBetweenPuffs;
    }

    public void setStepWaitBetweenPuffs(boolean stepWaitBetweenPuffs) {
        this.stepWaitBetweenPuffs = stepWaitBetweenPuffs;
    }

    public boolean isStepSpacerTips() {
        return stepSpacerTips;
    }

    public void setStepSpacerTips(boolean stepSpacerTips) {
        this.stepSpacerTips = stepSpacerTips;
    }

    public boolean isHighQuality() {
        return highQuality;
    }

    public void setHighQuality(boolean highQuality) {
        this.highQuality = highQuality;
    }

    /** Convenience: all steps completed. */
    public boolean allStepsCompleted() {
        return stepSealLips
                && stepSlowDeepBreath
                && stepHoldTenSeconds
                && stepWaitBetweenPuffs
                && stepSpacerTips;
    }
}
