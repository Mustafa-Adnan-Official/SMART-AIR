package com.example.smartair.models.providercollections;

import com.google.firebase.Timestamp;

/**
 * Purpose: Represents a single shared report entry inside the
 *          sharedReports array for a LinkedChild document.
 * Layer: Model
 * Used For: Keeping track of when a specific report was shared
 *           with a provider.
 */
public class SharedReportRef {

    private Timestamp reportDate;
    private Timestamp sharedDate;

    /** Required for Firebase deserialization. */
    public SharedReportRef() {
    }

    public SharedReportRef(Timestamp reportDate, Timestamp sharedDate) {
        this.reportDate = reportDate;
        this.sharedDate = sharedDate;
    }

    public Timestamp getReportDate() {
        return reportDate;
    }

    public void setReportDate(Timestamp reportDate) {
        this.reportDate = reportDate;
    }

    public Timestamp getSharedDate() {
        return sharedDate;
    }

    public void setSharedDate(Timestamp sharedDate) {
        this.sharedDate = sharedDate;
    }
}