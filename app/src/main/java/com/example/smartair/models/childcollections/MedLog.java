package com.example.smartair.models.childcollections;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * Purpose: Represents a medicine log document stored in
 *          children/{childUid}/medLogs/{logId}.
 * Layer: Model
 * Used For: Controller adherence, rescue frequency,
 *           and technique trainer stats.
 */
public class MedLog {

    private Timestamp createdAt;
    private Long peakFlow;              // may be null
    private List<String> symptoms;
    private String feelingBefore;
    private String feelingAfter;
    private Long puffsOrMeasures;
    private String medicineType;        // "rescue" or "controller"
    private Boolean techniqueTrainerUsed; // true/false for controller; null for rescue

    /** Required for Firebase deserialization. */
    public MedLog() {
    }

    public MedLog(Timestamp createdAt,
                  Long peakFlow,
                  List<String> symptoms,
                  String feelingBefore,
                  String feelingAfter,
                  Long puffsOrMeasures,
                  String medicineType,
                  Boolean techniqueTrainerUsed) {
        this.createdAt = createdAt;
        this.peakFlow = peakFlow;
        this.symptoms = symptoms;
        this.feelingBefore = feelingBefore;
        this.feelingAfter = feelingAfter;
        this.puffsOrMeasures = puffsOrMeasures;
        this.medicineType = medicineType;
        this.techniqueTrainerUsed = techniqueTrainerUsed;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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

    public Long getPuffsOrMeasures() {
        return puffsOrMeasures;
    }

    public void setPuffsOrMeasures(Long puffsOrMeasures) {
        this.puffsOrMeasures = puffsOrMeasures;
    }

    public String getMedicineType() {
        return medicineType;
    }

    public void setMedicineType(String medicineType) {
        this.medicineType = medicineType;
    }

    public Boolean getTechniqueTrainerUsed() {
        return techniqueTrainerUsed;
    }

    public void setTechniqueTrainerUsed(Boolean techniqueTrainerUsed) {
        this.techniqueTrainerUsed = techniqueTrainerUsed;
    }
}