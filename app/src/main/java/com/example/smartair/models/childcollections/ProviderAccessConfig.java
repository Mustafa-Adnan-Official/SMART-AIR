package com.example.smartair.models.childcollections;

/**
 * Purpose: Represents a providerAccess document stored in
 *          children/{childUid}/reports/{reportId}/providerAccess/{providerUid}.
 * Layer: Model
 * Used For: Controlling which pieces of the report a provider can view.
 */
public class ProviderAccessConfig {

    private String access; // "shared" or "revoked"

    private boolean canSeeControllerAdherence;
    private boolean canSeeIncidentLog;
    private boolean canSeeLastRescueTime;
    private boolean canSeePeakFlow;
    private boolean canSeeRescueAttempts;
    private boolean canSeeSymptoms;
    private boolean canSeeTodaysPefZone;
    private boolean canSeeTrends;
    private boolean canSeeTriggers;
    private boolean canSeeWeeklyRescueCount;
    private boolean canSeeTriageIncidents;

    /** Required for Firebase deserialization. */
    public ProviderAccessConfig() {
    }

    public ProviderAccessConfig(String access,
                                boolean canSeeControllerAdherence,
                                boolean canSeeIncidentLog,
                                boolean canSeeLastRescueTime,
                                boolean canSeePeakFlow,
                                boolean canSeeRescueAttempts,
                                boolean canSeeSymptoms,
                                boolean canSeeTodaysPefZone,
                                boolean canSeeTrends,
                                boolean canSeeTriggers,
                                boolean canSeeWeeklyRescueCount,
                                boolean canSeeTriageIncidents) {
        this.access = access;
        this.canSeeControllerAdherence = canSeeControllerAdherence;
        this.canSeeIncidentLog = canSeeIncidentLog;
        this.canSeeLastRescueTime = canSeeLastRescueTime;
        this.canSeePeakFlow = canSeePeakFlow;
        this.canSeeRescueAttempts = canSeeRescueAttempts;
        this.canSeeSymptoms = canSeeSymptoms;
        this.canSeeTodaysPefZone = canSeeTodaysPefZone;
        this.canSeeTrends = canSeeTrends;
        this.canSeeTriggers = canSeeTriggers;
        this.canSeeWeeklyRescueCount = canSeeWeeklyRescueCount;
        this.canSeeTriageIncidents = canSeeTriageIncidents;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public boolean isCanSeeControllerAdherence() {
        return canSeeControllerAdherence;
    }

    public void setCanSeeControllerAdherence(boolean canSeeControllerAdherence) {
        this.canSeeControllerAdherence = canSeeControllerAdherence;
    }

    public boolean isCanSeeIncidentLog() {
        return canSeeIncidentLog;
    }

    public void setCanSeeIncidentLog(boolean canSeeIncidentLog) {
        this.canSeeIncidentLog = canSeeIncidentLog;
    }

    public boolean isCanSeeLastRescueTime() {
        return canSeeLastRescueTime;
    }

    public void setCanSeeLastRescueTime(boolean canSeeLastRescueTime) {
        this.canSeeLastRescueTime = canSeeLastRescueTime;
    }

    public boolean isCanSeePeakFlow() {
        return canSeePeakFlow;
    }

    public void setCanSeePeakFlow(boolean canSeePeakFlow) {
        this.canSeePeakFlow = canSeePeakFlow;
    }

    public boolean isCanSeeRescueAttempts() {
        return canSeeRescueAttempts;
    }

    public void setCanSeeRescueAttempts(boolean canSeeRescueAttempts) {
        this.canSeeRescueAttempts = canSeeRescueAttempts;
    }

    public boolean isCanSeeSymptoms() {
        return canSeeSymptoms;
    }

    public void setCanSeeSymptoms(boolean canSeeSymptoms) {
        this.canSeeSymptoms = canSeeSymptoms;
    }

    public boolean isCanSeeTodaysPefZone() {
        return canSeeTodaysPefZone;
    }

    public void setCanSeeTodaysPefZone(boolean canSeeTodaysPefZone) {
        this.canSeeTodaysPefZone = canSeeTodaysPefZone;
    }

    public boolean isCanSeeTrends() {
        return canSeeTrends;
    }

    public void setCanSeeTrends(boolean canSeeTrends) {
        this.canSeeTrends = canSeeTrends;
    }

    public boolean isCanSeeTriggers() {
        return canSeeTriggers;
    }

    public void setCanSeeTriggers(boolean canSeeTriggers) {
        this.canSeeTriggers = canSeeTriggers;
    }

    public boolean isCanSeeWeeklyRescueCount() {
        return canSeeWeeklyRescueCount;
    }

    public void setCanSeeWeeklyRescueCount(boolean canSeeWeeklyRescueCount) {
        this.canSeeWeeklyRescueCount = canSeeWeeklyRescueCount;
    }

    public boolean isCanSeeTriageIncidents() {
        return canSeeTriageIncidents;
    }

    public void setCanSeeTriageIncidents(boolean canSeeTriageIncidents) {
        this.canSeeTriageIncidents = canSeeTriageIncidents;
    }
}