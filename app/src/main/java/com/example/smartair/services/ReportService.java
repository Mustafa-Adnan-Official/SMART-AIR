package com.example.smartair.services;

import com.example.smartair.models.childcollections.ChildReport;
import com.example.smartair.models.childcollections.HistoryEntry;
import com.example.smartair.models.childcollections.Incident;
import com.example.smartair.models.childcollections.InventoryItem;
import com.example.smartair.models.childcollections.SymptomBurden;
import com.example.smartair.models.childcollections.ZoneDistribution;
import com.example.smartair.models.users.Child;
import com.example.smartair.models.childcollections.ProviderAccess; // <-- NEW IMPORT
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ReportService {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SimpleDateFormat sdfReport = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat sdfHistory = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public String generateReport(
            String childUid, ChildReport report, String duration, Date start, Date end,
            Map<String, ProviderAccess> providerAccessMap,
            Child child, InventoryItem inventory,
            List<HistoryEntry> historyEntries, List<Incident> incidents) {

        CollectionReference reportsRef = db.collection("children").document(childUid).collection("reports");
        String startStr = sdfReport.format(start);
        String documentID = "(" + startStr + ")" + "-" + duration;

        Map<String, Object> reportData = createReportDataMap(
                childUid, documentID, start, end, duration, child, inventory, historyEntries, incidents
        );

        reportsRef.document(documentID).set(reportData);

        // --- UPDATED PROVIDER ACCESS LOGIC ---
        CollectionReference accessRef = reportsRef.document(documentID).collection("providerAccess");
        if (providerAccessMap != null) {
            for (Map.Entry<String, ProviderAccess> entry : providerAccessMap.entrySet()) {
                String providerUid = entry.getKey();
                ProviderAccess accessObject = entry.getValue();

                Map<String, Object> accessData = new HashMap<>();

                accessData.put("access", "shared"); // Defaulting to "shared" if the object is present
                accessData.put("canSeeControllerAdherence", accessObject.canSeeControllerAdherence());
                accessData.put("canSeeIncidentLog", accessObject.isCanSeeIncidentLog());
                accessData.put("canSeeLastRescueTime", accessObject.canSeeLastRescueTime());
                accessData.put("canSeePeakFlow", accessObject.canSeePeakFlow());
                accessData.put("canSeeRescueAttempts", accessObject.canSeeRescueAttempts());
                accessData.put("canSeeSymptoms", accessObject.canSeeSymptoms());
                accessData.put("canSeeTodaysPefZone", accessObject.canSeeTodaysPefZone());
                accessData.put("canSeeTrends", accessObject.canSeeTrends());
                accessData.put("canSeeTriggers", accessObject.canSeeTriggers());
                accessData.put("canSeeWeeklyRescueCount", accessObject.canSeeWeeklyRescueCount());
                accessData.put("canSeeTriageIncidents", accessObject.canSeeTriageIncidents());

                accessRef.document(providerUid).set(accessData);
            }
        }
        // --- END UPDATED PROVIDER ACCESS LOGIC ---

        return documentID;
    }

    public Map<String, Object> createReportDataMap(
            String childUid, String reportId, Date rangeStart, Date rangeEnd,
            String rangeLabel, Child child, InventoryItem inventory,
            List<HistoryEntry> historyEntries, List<Incident> incidents) {

        Map<String, Object> reportData = new HashMap<>();

        // Metadata
        reportData.put("reportId", reportId);
        reportData.put("childUid", childUid);
        reportData.put("generatedAt", Timestamp.now());
        reportData.put("rangeStart", new Timestamp(rangeStart));
        reportData.put("rangeEnd", new Timestamp(rangeEnd));
        reportData.put("rangeLabel", rangeLabel);
        reportData.put("reportType", "Report");

        // Calculations
        int[] allPefs = getAllPef(historyEntries, incidents);
        int personalBest = getPersonalBest(child);
        int pefAverage = (int) calculateAveragePEF(allPefs);

        // Controller Adherence Summary
        reportData.put("expectedControllerDailyUsageCount", expectedDailyUsageCount(inventory));
        reportData.put("controllerTimesUsed", controllerTimesUsed(historyEntries));
        reportData.put("avgControllerAdherencePercentage", avgControllerAdherencePercentage(historyEntries, inventory));
        reportData.put("personalBest", personalBest);
        reportData.put("avgPEF", pefAverage);
        reportData.put("minPEF", calculateMinPEF(allPefs));
        reportData.put("maxPEF", calculateMaxPEF(allPefs));
        reportData.put("avgPEFPercentOfPB", avgPEFPercentOfPB(pefAverage, personalBest));

        // Rescue & Triage Summary
        reportData.put("rescueAttemptsCount", rescueAttemptsCount(historyEntries));
        reportData.put("rescueDaysCount", rescueDaysCount(historyEntries));
        reportData.put("rescueAttemptDates", rescueAttemptDates(historyEntries));
        reportData.put("lastRescueTimeInRange", lastRescueTime(historyEntries));
        reportData.put("triageIncidentsCount", triageIncidentsCount(incidents));
        reportData.put("EscalatedIncidents", escalateIncidentsCount(incidents));

        // Symptom Burden & Zones
        SymptomBurden symptomBurden = CalculateSymptomBurden(historyEntries);

        Map<String, Object> sbMap = new HashMap<>();
        sbMap.put("problemNights", symptomBurden.getProblemNights());
        sbMap.put("activityLimitDays", symptomBurden.getActivityLimitDays());
        sbMap.put("coughWheezeDays", symptomBurden.getCoughWheezeDays());
        reportData.put("symptomBurden", sbMap);

        ZoneDistribution zoneDistribution = calculateZoneDistribution(child, historyEntries);
        Map<String, Object> zdMap = new HashMap<>();
        zdMap.put("greenDays", zoneDistribution.getGreenDays());
        zdMap.put("yellowDays", zoneDistribution.getYellowDays());
        zdMap.put("redDays", zoneDistribution.getRedDays());
        reportData.put("zoneDistribution", zdMap);

        reportData.put("rapidRescueRepeatEvent", rapidRescueRepeatEvent(historyEntries));

        return reportData;
    }

    public int expectedDailyUsageCount(InventoryItem item) {
        return item != null ? item.getExpectedDailyUses() : 0;
    }

    public int controllerTimesUsed(List<HistoryEntry> historyEntries){
        int controllerTimesUsed = 0;
        if(historyEntries == null) return 0;

        for(HistoryEntry entry : historyEntries) {
            if (entry.getMedlogController() != null) {
                controllerTimesUsed++;
            }
        }
        return controllerTimesUsed;
    }

    public int avgControllerAdherencePercentage(List<HistoryEntry> historyEntries, InventoryItem item){
        if (historyEntries == null || item == null) return 0;

        int totalPuffsOrMeasures = 0;
        Set<String> distinctDays = new HashSet<>();

        for(HistoryEntry entry : historyEntries) {
            if (entry.getMedlogController() != null && entry.getMedlogController().getPuffsOrMeasures() != null) {
                totalPuffsOrMeasures += entry.getMedlogController().getPuffsOrMeasures();
            }
            Date d = parseHistoryDate(entry.getDate());
            if (d != null) {
                distinctDays.add(sdfReport.format(d));
            }
        }

        int numberOfDays = distinctDays.size();
        if (numberOfDays == 0) return 0;

        int expectedTotal = numberOfDays * item.getExpectedDailyUses();
        if (expectedTotal == 0) return 0;

        double adherence = ((double)totalPuffsOrMeasures / expectedTotal) * 100;
        return (int) Math.min(adherence, 100);
    }

    public int getPersonalBest(Child child){
        if (child != null && child.getPersonalBest() != 0) {
            return child.getPersonalBest();
        }
        return 0;
    }

    public int[] getAllPef(List<HistoryEntry> historyEntries, List<Incident> incidents){
        List<Integer> pefList = new ArrayList<>();
        if (historyEntries != null) {
            for (HistoryEntry entry : historyEntries) {
                if (entry.getMedlogRescue() != null && entry.getMedlogRescue().getPeakFlow() != null) {
                    pefList.add(entry.getMedlogRescue().getPeakFlow().intValue());
                }
                if (entry.getMedlogController() != null && entry.getMedlogController().getPeakFlow() != null) {
                    pefList.add(entry.getMedlogController().getPeakFlow().intValue());
                }
            }
        }
        if (incidents != null) {
            for (Incident incident : incidents) {
                if (incident.getPeakFlow() != null) {
                    pefList.add(incident.getPeakFlow().intValue());
                }
            }
        }
        int[] allPef = new int[pefList.size()];
        for (int i = 0; i < pefList.size(); i++) {
            allPef[i] = pefList.get(i);
        }
        return allPef;
    }

    public double calculateAveragePEF(int[] pefs) {
        if (pefs == null || pefs.length == 0) return 0.0;
        long sum = 0;
        for (int p : pefs) {
            sum += p;
        }
        return (double) sum / pefs.length;
    }

    public int calculateMinPEF(int[] pefs) {
        if (pefs == null || pefs.length == 0) return 0;
        int min = Integer.MAX_VALUE;
        for (int p : pefs) {
            if (p < min) min = p;
        }
        return min;
    }

    public int calculateMaxPEF(int[] pefs) {
        if (pefs == null || pefs.length == 0) return 0;
        int max = Integer.MIN_VALUE;
        for (int p : pefs) {
            if (p > max) max = p;
        }
        return max;
    }

    public int avgPEFPercentOfPB(int pefAverage, int pb){
        if (pb == 0) return 0;
        return (int)((double)pefAverage/pb*100);
    }

    public int rescueAttemptsCount(List<HistoryEntry> historyEntries) {
        int rescueAttemptCounts = 0;
        if (historyEntries == null) return 0;
        for(HistoryEntry entry : historyEntries) {
            if (entry.getMedlogRescue() != null) {
                rescueAttemptCounts++;
            }
        }
        return rescueAttemptCounts;
    }

    public int rescueDaysCount(List<HistoryEntry> historyEntries){
        if (historyEntries == null) return 0;
        Set<String> distinctRescueDays = new HashSet<>();

        for(HistoryEntry entry : historyEntries) {
            if(entry.getMedlogRescue() != null && entry.getDate() != null){
                Date d = parseHistoryDate(entry.getDate());
                if (d != null) {
                    distinctRescueDays.add(sdfReport.format(d));
                }
            }
        }
        return distinctRescueDays.size();
    }

    public List<Timestamp> rescueAttemptDates(List<HistoryEntry> historyEntries){
        if (historyEntries == null) return new ArrayList<>();
        List<Timestamp> timestamps = new ArrayList<>();

        for(HistoryEntry entry : historyEntries){
            if(entry.getMedlogRescue() != null && entry.getDate() != null){
                Date d = parseHistoryDate(entry.getDate());
                if(d != null){
                    timestamps.add(new Timestamp(d));
                }
            }
        }
        return timestamps;
    }

    public Timestamp lastRescueTime(List<HistoryEntry> historyEntries){
        if (historyEntries == null || historyEntries.isEmpty()) return null;
        Date maxDate = null;

        for(HistoryEntry entry : historyEntries){
            if(entry.getMedlogRescue() != null && entry.getDate() != null){
                Date current = parseHistoryDate(entry.getDate());
                if(current != null){
                    if(maxDate == null || current.after(maxDate)){
                        maxDate = current;
                    }
                }
            }
        }
        return maxDate != null ? new Timestamp(maxDate) : null;
    }

    public int triageIncidentsCount(List<Incident> incidents){
        int triageIncidentsCount = 0;
        if(incidents == null) return 0;
        for(Incident incident : incidents) {
            if (incident != null) {
                triageIncidentsCount++;
            }
        }
        return triageIncidentsCount;
    }

    public int escalateIncidentsCount(List<Incident> incidents) {
        int escalatedIncidentsCount = 0;
        if(incidents == null) return 0;
        for(Incident incident : incidents) {
            if (incident.isEscalatedIncident()) {
                escalatedIncidentsCount++;
            }
        }
        return escalatedIncidentsCount;
    }

    public SymptomBurden CalculateSymptomBurden(List<HistoryEntry> historyEntries){
        long coughWheeze = 0;
        long problemNights = 0;
        long activityLimitDays = 0;

        if (historyEntries != null) {
            for(HistoryEntry historyEntry : historyEntries) {


                if (historyEntry.getSymptoms() != null) {
                    if(historyEntry.getSymptoms().contains("Cough/Wheeze")){
                        coughWheeze++;
                    }

                    if(historyEntry.getSymptoms().contains("Night Waking")){
                        problemNights++;
                    }

                    if(historyEntry.getSymptoms().contains("Activity Difficulty")){
                        activityLimitDays++;
                    }
                }
            }
        }
        return new SymptomBurden(problemNights, activityLimitDays, coughWheeze);
    }

    public ZoneDistribution calculateZoneDistribution(Child child, List<HistoryEntry> historyEntries) {
        if (child == null || historyEntries == null) return new ZoneDistribution((long)0,(long)0,(long)0);

        int[] allPefs = getAllPef(historyEntries, null);
        long redDays = 0;
        long yellowDays = 0;
        long greenDays = 0;
        int personalBest = getPersonalBest(child);

        if (personalBest == 0) return new ZoneDistribution((long)0,(long)0,(long)0);

        for (int pef : allPefs) {
            double percent = (double) pef / personalBest;

            if (percent > 0.8){
                greenDays++;
            } else if (percent > 0.6) {
                yellowDays++;
            } else {
                redDays++;
            }


        }
        return new ZoneDistribution(greenDays, yellowDays, redDays);
    }

    public int rapidRescueRepeatEvent(List<HistoryEntry> historyEntries){
        if (historyEntries == null || historyEntries.size() < 3) return 0;

        List<Date> rescueTimes = new ArrayList<>();

        for (HistoryEntry entry : historyEntries) {
            if (entry.getMedlogRescue() != null && entry.getDate() != null) {
                Date d = parseHistoryDate(entry.getDate());

                if (d != null) {
                    rescueTimes.add(d);
                }
            }
        }

        Collections.sort(rescueTimes);

        int rapidEvents = 0;

        int i = 0;
        while (i <= rescueTimes.size() - 3) {
            Date start = rescueTimes.get(i);
            Date end = rescueTimes.get(i + 2);

            long diffInMillis = end.getTime() - start.getTime();

            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);

            if (diffInHours <= 3) {
                rapidEvents++;
                i += 3;
            } else {
                i++;
            }
        }

        return rapidEvents;
    }

    private Date parseHistoryDate(String dateStr) {
        try {
            return sdfHistory.parse(dateStr);
        } catch (ParseException e) {
            try {
                return sdfReport.parse(dateStr);
            } catch (ParseException ex) {
                return null;
            }
        }
    }
}
