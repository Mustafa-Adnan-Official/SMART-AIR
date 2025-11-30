package com.example.smartair.models.childcollections;

/**
 * Purpose: Represents the symptomBurden map in a child report.
 * Layer: Model
 * Used For: Capturing high-level burden metrics like problem nights
 *           and activity limitation days.
 */
public class SymptomBurden {

    private Long problemNights;
    private Long activityLimitDays;
    private Long coughWheezeDays;

    /** Required for Firebase deserialization. */
    public SymptomBurden() {
    }

    public SymptomBurden(Long problemNights,
                         Long activityLimitDays,
                         Long coughWheezeDays) {
        this.problemNights = problemNights;
        this.activityLimitDays = activityLimitDays;
        this.coughWheezeDays = coughWheezeDays;
    }

    public Long getProblemNights() {
        return problemNights;
    }

    public void setProblemNights(Long problemNights) {
        this.problemNights = problemNights;
    }

    public Long getActivityLimitDays() {
        return activityLimitDays;
    }

    public void setActivityLimitDays(Long activityLimitDays) {
        this.activityLimitDays = activityLimitDays;
    }

    public Long getCoughWheezeDays() {
        return coughWheezeDays;
    }

    public void setCoughWheezeDays(Long coughWheezeDays) {
        this.coughWheezeDays = coughWheezeDays;
    }
}