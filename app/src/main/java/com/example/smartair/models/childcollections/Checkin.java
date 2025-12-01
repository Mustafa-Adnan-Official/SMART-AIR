package com.example.smartair.models.childcollections;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.HashMap; // <-- New Import Needed
import java.util.Map;

/**
 * Purpose: Represents a daily check-in document stored in
 *          children/{childUid}/checkins/{checkinId}.
 * Layer: Model
 * Used For: History browser, symptom burden summaries, and
 *           report generation.
 */
public class Checkin {

    private Timestamp createdAt;
    private String feeling;
    private List<String> symptoms;
    private List<String> triggers;
    private int peakFlow;          // may be null
    private EntryAuthor entryAuthor;

    /** Required for Firebase deserialization. */
    public Checkin() {
    }

    public Checkin(Timestamp createdAt,
                   String feeling,
                   List<String> symptoms,
                   List<String> triggers,
                   int peakFlow,
                   EntryAuthor entryAuthor) {
        this.createdAt = createdAt;
        this.feeling = feeling;
        this.symptoms = symptoms;
        this.triggers = triggers;
        this.peakFlow = peakFlow;
        this.entryAuthor = entryAuthor;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getFeeling() {
        return feeling;
    }

    public void setFeeling(String feeling) {
        this.feeling = feeling;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public List<String> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<String> triggers) {
        this.triggers = triggers;
    }

    public int getPeakFlow() {
        return peakFlow;
    }

    public void setPeakFlow(int peakFlow) {
        this.peakFlow = peakFlow;
    }

    public EntryAuthor getEntryAuthor() {
        return entryAuthor;
    }

    public void setEntryAuthor(EntryAuthor entryAuthor) {
        this.entryAuthor = entryAuthor;
    }


}