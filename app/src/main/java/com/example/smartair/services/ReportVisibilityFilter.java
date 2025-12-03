// File: ReportVisibilityFilter.java
package com.example.smartair.services;

import com.google.firebase.firestore.DocumentSnapshot;
import com.example.smartair.services.ProviderReportExporter;
import com.example.smartair.models.providercollections.ReportExportRequest;
import java.util.HashMap;
import java.util.Map;

public final class ReportVisibilityFilter {

    private ReportVisibilityFilter() {}

    public static Map<String, Object> apply(
            DocumentSnapshot report,
            DocumentSnapshot access
    ) {
        Map<String, Object> out = new HashMap<>();

        addAlways(out, report, "generatedAt", "rangeStart", "rangeEnd", "reportType");

        addIfTrue(out, report, access, "canSeeControllerAdherence",
                "controllerTimesUsed", "avgControllerAdherencePercentage");

        addIfTrue(out, report, access, "canSeeRescueAttempts",
                "rescueAttemptsCount", "rescueDaysCount");

        addIfTrue(out, report, access, "canSeeTriageIncidents",
                "triageIncidentsCount", "escalatedIncidents");

        addIfTrue(out, report, access, "canSeeSymptoms", "symptomBurden");
        addIfTrue(out, report, access, "canSeePeakFlow",
                "avgPEF", "minPEF", "maxPEF");

        return out;
    }

    private static void addAlways(
            Map<String, Object> out,
            DocumentSnapshot src,
            String... keys
    ) {
        for (String k : keys) {
            if (src.contains(k)) out.put(k, src.get(k));
        }
    }

    private static void addIfTrue(
            Map<String, Object> out,
            DocumentSnapshot src,
            DocumentSnapshot access,
            String flag,
            String... keys
    ) {
        Boolean allowed = access.getBoolean(flag);
        if (allowed != null && allowed) {
            for (String k : keys) {
                if (src.contains(k)) out.put(k, src.get(k));
            }
        }
    }
}
