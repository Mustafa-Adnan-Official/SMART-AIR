package com.example.smartair.models.childcollections;



public class ProviderAccess {
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

    public ProviderAccess() {}
    public ProviderAccess(boolean canSeeControllerAdherence, boolean canSeeIncidentLog,
                          boolean canSeeLastRescueTime, boolean canSeePeakFlow,
                          boolean canSeeRescueAttempts, boolean canSeeSymptoms,
                          boolean canSeeTodaysPefZone, boolean canSeeTrends,
                          boolean canSeeTriggers, boolean canSeeWeeklyRescueCount,
                          boolean canSeeTriageIncidents) {

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

    public boolean canSeeControllerAdherence() {
        return canSeeControllerAdherence;
    }

    public void setCanSeeControllerAdherence(boolean canSeeControllerAdherence) {
        this.canSeeControllerAdherence = canSeeControllerAdherence;
    }

    public boolean isCanSeeIncidentLog(){
        return canSeeIncidentLog;
    }

    public void setCanSeeIncidentLog(boolean canSeeIncidentLog){
        this.canSeeIncidentLog = canSeeIncidentLog;

    }

    public void setCanSeeLastRescueTime(boolean canSeeLastRescueTime) {
        this.canSeeLastRescueTime = canSeeLastRescueTime;
    }

    public boolean canSeeLastRescueTime() {
        return canSeeLastRescueTime;
    }


    public boolean canSeePeakFlow() {
        return canSeePeakFlow;
    }

    public void setCanSeePeakFlow(boolean canSeePeakFlow) {
        this.canSeePeakFlow = canSeePeakFlow;
    }

    public boolean canSeeRescueAttempts() {
        return canSeeRescueAttempts;
    }

    public void setCanSeeRescueAttempts(boolean canSeeRescueAttempts) {
        this.canSeeRescueAttempts = canSeeRescueAttempts;
    }

    public boolean canSeeSymptoms() {
        return canSeeSymptoms;
    }

    public void setCanSeeSymptoms(boolean canSeeSymptoms) {
        this.canSeeSymptoms = canSeeSymptoms;
    }


    public boolean canSeeTodaysPefZone() {
        return canSeeTodaysPefZone;
    }

    public void setCanSeeTodaysPefZone(boolean canSeeTodaysPefZone) {
        this.canSeeTodaysPefZone = canSeeTodaysPefZone;
    }

    public boolean canSeeTrends() {
        return canSeeTrends;
    }

    public void setCanSeeTrends(boolean canSeeTrends) {
        this.canSeeTrends = canSeeTrends;
    }

    public boolean canSeeTriggers() {
        return canSeeTriggers;
    }

    public void setCanSeeTriggers(boolean canSeeTriggers) {
        this.canSeeTriggers = canSeeTriggers;
    }

    public boolean canSeeWeeklyRescueCount() {
        return canSeeWeeklyRescueCount;
    }

    public void setCanSeeWeeklyRescueCount(boolean canSeeWeeklyRescueCount) {
        this.canSeeWeeklyRescueCount = canSeeWeeklyRescueCount;
    }

    public boolean canSeeTriageIncidents() {
        return canSeeTriageIncidents;
    }

    public void setCanSeeTriageIncidents(boolean canSeeTriageIncidents) {
        this.canSeeTriageIncidents = canSeeTriageIncidents;
    }











}
