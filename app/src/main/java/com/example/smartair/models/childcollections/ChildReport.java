package com.example.smartair.models.childcollections;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Purpose: Represents a frozen report snapshot stored in
 *          children/{childUid}/reports/{reportId}.
 * Layer: Model
 * Used For: Provider PDFs, parent report history, and sharing
 *           configuration.
 */
public class ChildReport {

    // Metadata
    private String reportId;
    private String childUid;
    private Timestamp generatedAt;
    private String generatedByUid;
    private Timestamp rangeStart;
    private Timestamp rangeEnd;
    private String rangeLabel;

    // Controller summary
    private Long expectedControllerDailyUsageCount;
    private Long controllerTimesUsed;
    private Double avgControllerAdherencePercentage;
    private Long personalBestAtGeneration;
    private Double avgPEF;
    private Long minPEF;
    private Long maxPEF;
    private Double avgPEFPercentOfPB;

    // Rescue & triage summary
    private Long rescueAttemptsCount;
    private Long rescueDaysCount;
    private List<Timestamp> rescueAttemptDates;
    private Timestamp lastRescueTimeInRange;
    private Long triageIncidentsCount;
    private boolean hasEscalatedIncident;

    // Symptom burden & zones
    private SymptomBurden symptomBurden;
    private ZoneDistribution zoneDistribution;
    private Long redZoneDaysCount;
    private Long rapidRescueRepeatEvents;

    // Optional trend data & notes
    private Map<String, Object> trendData;
    private String notes;

    /** Required for Firebase deserialization. */
    public ChildReport() {
    }

    // (You can add a full-args constructor later if you want; keeping this compact.)

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getChildUid() {
        return childUid;
    }

    public void setChildUid(String childUid) {
        this.childUid = childUid;
    }

    public Timestamp getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Timestamp generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getGeneratedByUid() {
        return generatedByUid;
    }

    public void setGeneratedByUid(String generatedByUid) {
        this.generatedByUid = generatedByUid;
    }

    public Timestamp getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(Timestamp rangeStart) {
        this.rangeStart = rangeStart;
    }

    public Timestamp getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(Timestamp rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public String getRangeLabel() {
        return rangeLabel;
    }

    public void setRangeLabel(String rangeLabel) {
        this.rangeLabel = rangeLabel;
    }

    public Long getExpectedControllerDailyUsageCount() {
        return expectedControllerDailyUsageCount;
    }

    public void setExpectedControllerDailyUsageCount(Long expectedControllerDailyUsageCount) {
        this.expectedControllerDailyUsageCount = expectedControllerDailyUsageCount;
    }

    public Long getControllerTimesUsed() {
        return controllerTimesUsed;
    }

    public void setControllerTimesUsed(Long controllerTimesUsed) {
        this.controllerTimesUsed = controllerTimesUsed;
    }

    public Double getAvgControllerAdherencePercentage() {
        return avgControllerAdherencePercentage;
    }

    public void setAvgControllerAdherencePercentage(Double avgControllerAdherencePercentage) {
        this.avgControllerAdherencePercentage = avgControllerAdherencePercentage;
    }

    public Long getPersonalBestAtGeneration() {
        return personalBestAtGeneration;
    }

    public void setPersonalBestAtGeneration(Long personalBestAtGeneration) {
        this.personalBestAtGeneration = personalBestAtGeneration;
    }

    public Double getAvgPEF() {
        return avgPEF;
    }

    public void setAvgPEF(Double avgPEF) {
        this.avgPEF = avgPEF;
    }

    public Long getMinPEF() {
        return minPEF;
    }

    public void setMinPEF(Long minPEF) {
        this.minPEF = minPEF;
    }

    public Long getMaxPEF() {
        return maxPEF;
    }

    public void setMaxPEF(Long maxPEF) {
        this.maxPEF = maxPEF;
    }

    public Double getAvgPEFPercentOfPB() {
        return avgPEFPercentOfPB;
    }

    public void setAvgPEFPercentOfPB(Double avgPEFPercentOfPB) {
        this.avgPEFPercentOfPB = avgPEFPercentOfPB;
    }

    public Long getRescueAttemptsCount() {
        return rescueAttemptsCount;
    }

    public void setRescueAttemptsCount(Long rescueAttemptsCount) {
        this.rescueAttemptsCount = rescueAttemptsCount;
    }

    public Long getRescueDaysCount() {
        return rescueDaysCount;
    }

    public void setRescueDaysCount(Long rescueDaysCount) {
        this.rescueDaysCount = rescueDaysCount;
    }

    public List<Timestamp> getRescueAttemptDates() {
        return rescueAttemptDates;
    }

    public void setRescueAttemptDates(List<Timestamp> rescueAttemptDates) {
        this.rescueAttemptDates = rescueAttemptDates;
    }

    public Timestamp getLastRescueTimeInRange() {
        return lastRescueTimeInRange;
    }

    public void setLastRescueTimeInRange(Timestamp lastRescueTimeInRange) {
        this.lastRescueTimeInRange = lastRescueTimeInRange;
    }

    public Long getTriageIncidentsCount() {
        return triageIncidentsCount;
    }

    public void setTriageIncidentsCount(Long triageIncidentsCount) {
        this.triageIncidentsCount = triageIncidentsCount;
    }

    public boolean isHasEscalatedIncident() {
        return hasEscalatedIncident;
    }

    public void setHasEscalatedIncident(boolean hasEscalatedIncident) {
        this.hasEscalatedIncident = hasEscalatedIncident;
    }

    public SymptomBurden getSymptomBurden() {
        return symptomBurden;
    }

    public void setSymptomBurden(SymptomBurden symptomBurden) {
        this.symptomBurden = symptomBurden;
    }

    public ZoneDistribution getZoneDistribution() {
        return zoneDistribution;
    }

    public void setZoneDistribution(ZoneDistribution zoneDistribution) {
        this.zoneDistribution = zoneDistribution;
    }

    public Long getRedZoneDaysCount() {
        return redZoneDaysCount;
    }

    public void setRedZoneDaysCount(Long redZoneDaysCount) {
        this.redZoneDaysCount = redZoneDaysCount;
    }

    public Long getRapidRescueRepeatEvents() {
        return rapidRescueRepeatEvents;
    }

    public void setRapidRescueRepeatEvents(Long rapidRescueRepeatEvents) {
        this.rapidRescueRepeatEvents = rapidRescueRepeatEvents;
    }

    public Map<String, Object> getTrendData() {
        return trendData;
    }

    public void setTrendData(Map<String, Object> trendData) {
        this.trendData = trendData;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}