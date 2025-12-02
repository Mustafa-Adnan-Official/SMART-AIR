package com.example.smartair.services;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class ProviderReportService {

    private String childrenPath = "children";
    private String medLogsPath = "medLogs";
    private String alertsPath = "alerts";
    private String checkinsPath = "checkins";
    private String reportsPath = "reports";

    private FirebaseFirestore db;

    public interface ReportCallback {
        void onSuccess(String reportId);
        void onError(Exception e);
    }

    public ProviderReportService() {
        db = FirebaseFirestore.getInstance();
    }

    private Map<String, Object> baseReport(Timestamp start, Timestamp end) {
        Map<String, Object> m = new HashMap<>();
        m.put("start", start);
        m.put("end", end);
        m.put("rescues", 0L);
        m.put("controllers", 0L);
        m.put("controllerAdherencePct", 0.0);
        m.put("symptoms", new ArrayList<>());
        m.put("zoneGreen", 0L);
        m.put("zoneYellow", 0L);
        m.put("zoneRed", 0L);
        m.put("triageCount", 0L);
        m.put("rapidRescueAlerts", 0L);
        m.put("redZoneAlerts", 0L);
        m.put("inventoryAlerts", 0L);
        m.put("worseAfterDoseAlerts", 0L);
        return m;
    }

    private void writeReport(String childUid, Map<String,Object> data, ReportCallback cb) {
        db.collection(childrenPath)
                .document(childUid)
                .collection(reportsPath)
                .add(data)
                .addOnSuccessListener(ref -> cb.onSuccess(ref.getId()))
                .addOnFailureListener(cb::onError);
    }

    private String zoneFromPEF(long pef, long pb) {
        if (pef <= 0 || pb <= 0) return "none";
        double pct = (pef * 100.0) / pb;
        if (pct >= 80) return "green";
        else if (pct >= 50) return "yellow";
        return "red";
    }

    public void generateReport(String childUid, Timestamp start, Timestamp end, @NonNull ReportCallback callback) {
        Map<String,Object> report = baseReport(start, end);

        db.collection(childrenPath)
                .document(childUid)
                .get()
                .addOnSuccessListener(childSnap -> {
                    Long pbLong = childSnap.getLong("personalBest");
                    long pb = pbLong != null ? pbLong : 0L;

                    db.collection(childrenPath)
                            .document(childUid)
                            .collection(medLogsPath)
                            .whereGreaterThanOrEqualTo("createdAt", start)
                            .whereLessThanOrEqualTo("createdAt", end)
                            .get()
                            .addOnSuccessListener(medLogs -> {
                                long rescues = 0;
                                long controllers = 0;
                                List<String> symptomsList = new ArrayList<>();

                                for (DocumentSnapshot d : medLogs) {
                                    String type = d.getString("medicineType");
                                    if ("rescue".equalsIgnoreCase(type)) rescues++;
                                    if ("controller".equalsIgnoreCase(type)) controllers++;

                                    List<String> sym = (List<String>) d.get("symptoms");
                                    if (sym != null) symptomsList.addAll(sym);
                                }

                                report.put("rescues", rescues);
                                report.put("controllers", controllers);

                                long totalDays = Math.max(
                                        1,
                                        (end.toDate().getTime() - start.toDate().getTime()) / (1000L * 60L * 60L * 24L)
                                );

                                double adherence = (double) controllers / (double) totalDays;
                                report.put("controllerAdherencePct", adherence * 100.0);
                                report.put("symptoms", symptomsList);

                                db.collection(childrenPath)
                                        .document(childUid)
                                        .collection(checkinsPath)
                                        .whereGreaterThanOrEqualTo("createdAt", start)
                                        .whereLessThanOrEqualTo("createdAt", end)
                                        .get()
                                        .addOnSuccessListener(checkins -> {
                                            long green = 0;
                                            long yellow = 0;
                                            long red = 0;

                                            for (DocumentSnapshot c : checkins) {
                                                Long pef = c.getLong("peakFlow");
                                                if (pef == null || pef <= 0) continue;
                                                String z = zoneFromPEF(pef, pb);
                                                if ("green".equals(z)) green++;
                                                if ("yellow".equals(z)) yellow++;
                                                if ("red".equals(z)) red++;
                                            }

                                            report.put("zoneGreen", green);
                                            report.put("zoneYellow", yellow);
                                            report.put("zoneRed", red);

                                            db.collection(childrenPath)
                                                    .document(childUid)
                                                    .collection(alertsPath)
                                                    .whereGreaterThanOrEqualTo("createdAt", start)
                                                    .whereLessThanOrEqualTo("createdAt", end)
                                                    .orderBy("createdAt", Query.Direction.ASCENDING)
                                                    .get()
                                                    .addOnSuccessListener(alerts -> {
                                                        long rapid = 0;
                                                        long rz = 0;
                                                        long triage = 0;
                                                        long inventory = 0;
                                                        long worse = 0;

                                                        for (DocumentSnapshot a : alerts) {
                                                            Boolean rr = a.getBoolean("rapidRescueRepeats");
                                                            Boolean redZone = a.getBoolean("redZoneDay");
                                                            Boolean tr = a.getBoolean("triageEscalation");
                                                            Boolean low = a.getBoolean("inventoryLowOrExpired");
                                                            Boolean w = a.getBoolean("worseAfterDose");

                                                            if (rr != null && rr) rapid++;
                                                            if (redZone != null && redZone) rz++;
                                                            if (tr != null && tr) triage++;
                                                            if (low != null && low) inventory++;
                                                            if (w != null && w) worse++;
                                                        }

                                                        report.put("rapidRescueAlerts", rapid);
                                                        report.put("redZoneAlerts", rz);
                                                        report.put("triageCount", triage);
                                                        report.put("inventoryAlerts", inventory);
                                                        report.put("worseAfterDoseAlerts", worse);

                                                        writeReport(childUid, report, callback);
                                                    })
                                                    .addOnFailureListener(callback::onError);
                                        })
                                        .addOnFailureListener(callback::onError);
                            })
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }
}
