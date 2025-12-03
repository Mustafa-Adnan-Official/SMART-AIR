// File: ReportExportRequest.java
package com.example.smartair.models.providercollections;

public class ReportExportRequest {

    private final String childUid;
    private final String reportId;
    private final boolean isHistory;

    public ReportExportRequest(String childUid,
                               String reportId,
                               boolean isHistory) {
        this.childUid = childUid;
        this.reportId = reportId;
        this.isHistory = isHistory;
    }

    public String getChildUid() {
        return childUid;
    }

    public String getReportId() {
        return reportId;
    }

    public boolean isHistory() {
        return isHistory;
    }
}
