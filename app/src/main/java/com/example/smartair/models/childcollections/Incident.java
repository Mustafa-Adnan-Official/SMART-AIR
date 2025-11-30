package com.example.smartair.models.childcollections;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * Purpose: Represents a triage incident document stored in
 *          children/{childUid}/incidents/{incidentId}.
 * Layer: Model
 * Used For: Provider reports, notable incident logs, and
 *           safety summaries.
 */
public class Incident {

    private Timestamp timeOfIncident;
    private Long peakFlow;          // may be null
    private List<String> symptoms;
    private String feelingBefore;
    private String feelingAfter;
    private String type;            // "rescue" or "controller"
    private Long puffsOrMeasures;   // may be null
    private boolean escalatedIncident;

    /** Required for Firebase deserialization. */
    public Incident() {
    }

    public Incident(Timestamp timeOfIncident,
                    Long peakFlow,
                    List<String> symptoms,
                    String feelingBefore,
                    String feelingAfter,
                    String type,
                    Long puffsOrMeasures,
                    boolean escalatedIncident) {
        this.timeOfIncident = timeOfIncident;
        this.peakFlow = peakFlow;
        this.symptoms = symptoms;
        this.feelingBefore = feelingBefore;
        this.feelingAfter = feelingAfter;
        this.type = type;
        this.puffsOrMeasures = puffsOrMeasures;
        this.escalatedIncident = escalatedIncident;
    }

    public Timestamp getTimeOfIncident() {
        return timeOfIncident;
    }

    public void setTimeOfIncident(Timestamp timeOfIncident) {
        this.timeOfIncident = timeOfIncident;
    }

    public Long getPeakFlow() {
        return peakFlow;
    }

    public void setPeakFlow(Long peakFlow) {
        this.peakFlow = peakFlow;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public String getFeelingBefore() {
        return feelingBefore;
    }

    public void setFeelingBefore(String feelingBefore) {
        this.feelingBefore = feelingBefore;
    }

    public String getFeelingAfter() {
        return feelingAfter;
    }

    public void setFeelingAfter(String feelingAfter) {
        this.feelingAfter = feelingAfter;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getPuffsOrMeasures() {
        return puffsOrMeasures;
    }

    public void setPuffsOrMeasures(Long puffsOrMeasures) {
        this.puffsOrMeasures = puffsOrMeasures;
    }

    public boolean isEscalatedIncident() {
        return escalatedIncident;
    }

    public void setEscalatedIncident(boolean escalatedIncident) {
        this.escalatedIncident = escalatedIncident;
    }
}