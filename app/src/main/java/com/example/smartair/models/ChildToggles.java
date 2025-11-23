package com.example.smartair.models;

public class ChildToggles {
    public boolean toggleMain;
    public boolean toggleRescueLogs;
    public boolean toggleControllerSummary;
    public boolean toggleSymptoms;
    public boolean toggleTriggers;
    public boolean togglePeakFlow;
    public boolean toggleTriageIncidents;
    public boolean toggleSummaryCharts;

    public ChildToggles() {}

    public ChildToggles(boolean toggleMain,
                        boolean toggleRescueLogs,
                        boolean toggleControllerSummary,
                        boolean toggleSymptoms,
                        boolean toggleTriggers,
                        boolean togglePeakFlow,
                        boolean toggleTriageIncidents,
                        boolean toggleSummaryCharts) {
        this.toggleMain = toggleMain;
        if (toggleMain) {
            this.toggleRescueLogs = toggleRescueLogs;
            this.toggleControllerSummary = toggleControllerSummary;
            this.toggleSymptoms = toggleSymptoms;
            this.toggleTriggers = toggleTriggers;
            this.togglePeakFlow = togglePeakFlow;
            this.toggleTriageIncidents = toggleTriageIncidents;
            this.toggleSummaryCharts = toggleSummaryCharts;
        } else {
            this.toggleRescueLogs = false;
            this.toggleControllerSummary = false;
            this.toggleSymptoms = false;
            this.toggleTriggers = false;
            this.togglePeakFlow = false;
            this.toggleTriageIncidents = false;
            this.toggleSummaryCharts = false;
        }
    }
}
