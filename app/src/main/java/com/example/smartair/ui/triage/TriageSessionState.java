package com.example.smartair.ui.triage;

import java.io.Serializable;

/**
 * Holds all data for a single triage session.
 * UI-only; Module C will later read this and write to Firestore.
 */
public class TriageSessionState implements Serializable {

    public static final String EXTRA_KEY =
            "com.example.smartair.ui.triage.TRIAGE_SESSION_STATE";

    // When triage started
    private long startedAtMillis;

    // Step 1 inputs
    private int rescueAttemptsToday;
    private Integer currentPef; // null if not entered

    private boolean step1CantSpeak;
    private boolean step1ChestPulling;
    private boolean step1BlueLips;

    // Step 2 counters
    private int rescuePuffs;
    private int controllerPuffs;

    // Step 3 serious symptoms + breathing state
    private boolean step3CantSpeak;
    private boolean step3ChestPulling;
    private boolean step3BlueLips;
    private Boolean breathingRegulated; // null if not answered

    // Final feelings
    private String feelingBefore; // "good", "okay", "bad"
    private String feelingNow;    // "better", "same", "worse"

    // Overall emergency flag
    private boolean emergencyFlag;

    // ---------- Getters / setters ----------

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public void setStartedAtMillis(long startedAtMillis) {
        this.startedAtMillis = startedAtMillis;
    }

    public int getRescueAttemptsToday() {
        return rescueAttemptsToday;
    }

    public void setRescueAttemptsToday(int rescueAttemptsToday) {
        this.rescueAttemptsToday = rescueAttemptsToday;
    }

    public Integer getCurrentPef() {
        return currentPef;
    }

    public void setCurrentPef(Integer currentPef) {
        this.currentPef = currentPef;
    }

    public boolean isStep1CantSpeak() {
        return step1CantSpeak;
    }

    public void setStep1CantSpeak(boolean step1CantSpeak) {
        this.step1CantSpeak = step1CantSpeak;
    }

    public boolean isStep1ChestPulling() {
        return step1ChestPulling;
    }

    public void setStep1ChestPulling(boolean step1ChestPulling) {
        this.step1ChestPulling = step1ChestPulling;
    }

    public boolean isStep1BlueLips() {
        return step1BlueLips;
    }

    public void setStep1BlueLips(boolean step1BlueLips) {
        this.step1BlueLips = step1BlueLips;
    }

    public int getRescuePuffs() {
        return rescuePuffs;
    }

    public void setRescuePuffs(int rescuePuffs) {
        this.rescuePuffs = rescuePuffs;
    }

    public void incrementRescuePuffs() {
        rescuePuffs++;
    }

    public void decrementRescuePuffs() {
        if (rescuePuffs > 0) {
            rescuePuffs--;
        }
    }

    public int getControllerPuffs() {
        return controllerPuffs;
    }

    public void setControllerPuffs(int controllerPuffs) {
        this.controllerPuffs = controllerPuffs;
    }

    public void incrementControllerPuffs() {
        controllerPuffs++;
    }

    public void decrementControllerPuffs() {
        if (controllerPuffs > 0) {
            controllerPuffs--;
        }
    }

    public boolean isStep3CantSpeak() {
        return step3CantSpeak;
    }

    public void setStep3CantSpeak(boolean step3CantSpeak) {
        this.step3CantSpeak = step3CantSpeak;
    }

    public boolean isStep3ChestPulling() {
        return step3ChestPulling;
    }

    public void setStep3ChestPulling(boolean step3ChestPulling) {
        this.step3ChestPulling = step3ChestPulling;
    }

    public boolean isStep3BlueLips() {
        return step3BlueLips;
    }

    public void setStep3BlueLips(boolean step3BlueLips) {
        this.step3BlueLips = step3BlueLips;
    }

    public Boolean getBreathingRegulated() {
        return breathingRegulated;
    }

    public void setBreathingRegulated(Boolean breathingRegulated) {
        this.breathingRegulated = breathingRegulated;
    }

    public String getFeelingBefore() {
        return feelingBefore;
    }

    public void setFeelingBefore(String feelingBefore) {
        this.feelingBefore = feelingBefore;
    }

    public String getFeelingNow() {
        return feelingNow;
    }

    public void setFeelingNow(String feelingNow) {
        this.feelingNow = feelingNow;
    }

    public boolean isEmergencyFlag() {
        return emergencyFlag;
    }

    public void setEmergencyFlag(boolean emergencyFlag) {
        this.emergencyFlag = emergencyFlag;
    }
}