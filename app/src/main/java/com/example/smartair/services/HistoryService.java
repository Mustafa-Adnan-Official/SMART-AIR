package com.example.smartair.services;

import com.example.smartair.callbacks.ResultCallback;
import com.example.smartair.models.childcollections.Checkin;
import com.example.smartair.models.childcollections.HistoryEntry;
import com.example.smartair.models.childcollections.MedLog;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class HistoryService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public String generateReport(String childUid, List<HistoryEntry> historyEntries, Date start, Date end, String duration, Map<String, Boolean> providerAccessMap){

        CollectionReference reportsRef = db.collection("children").document(childUid).collection("reports");
        String startStr = sdf.format(start);
        String documentID = "(" + startStr + ")" + "-" + duration;


        Map<String, Object> reportData = new HashMap<>();

        
        reportData.put("reportType", "history");
        reportData.put("startDate", start); 
        reportData.put("endDate", end);

        reportData.put("logs", historyEntries);

        // Save the main report document
        reportsRef.document(documentID).set(reportData);

        // Create "providerAccess" subcollection
        CollectionReference accessRef = reportsRef.document(documentID).collection("providerAccess");

        if (providerAccessMap != null) {
            for (Map.Entry<String, Boolean> entry : providerAccessMap.entrySet()) {
                Map<String, Object> accessData = new HashMap<>();
                accessData.put("hasAccess", entry.getValue());
                accessRef.document(entry.getKey()).set(accessData);
            }
        }

        return documentID;
    }

    public void getAllHistoryLogs(String childUid, Date start, Date end, ResultCallback<List<HistoryEntry>> callback){
        
        Query medLogQuery = db.collection("children")
                .document(childUid)
                .collection("medlogs")
                .whereGreaterThanOrEqualTo("createdAt", start)
                .whereLessThanOrEqualTo("createdAt", end);

        medLogQuery.get().addOnSuccessListener(medLogSnapshot -> {
            Map<String, List<MedLog>> logsByDate = new HashMap<>();
            List<String> uniqueDates = new ArrayList<>();

            if (medLogSnapshot != null) {
                for (DocumentSnapshot document : medLogSnapshot.getDocuments()){
                    try {
                        MedLog medLog = document.toObject(MedLog.class);
                        if (medLog == null || medLog.getCreatedAt() == null) continue;

                        String logDateString = sdf.format(medLog.getCreatedAt().toDate());
                        
                        if (!logsByDate.containsKey(logDateString)) {
                            logsByDate.put(logDateString, new ArrayList<>());
                            uniqueDates.add(logDateString);
                        }
                        logsByDate.get(logDateString).add(medLog);
                    } catch (Exception e) {
                        // Skip malformed documents to prevent crash
                        System.err.println("Error parsing MedLog: " + e.getMessage());
                    }
                }
            }

            List<Task<DocumentSnapshot>> checkinTasks = new ArrayList<>();
            for (String date : uniqueDates) {
                checkinTasks.add(db.collection("children")
                        .document(childUid)
                        .collection("checkins")
                        .document(date)
                        .get());
            }

            Tasks.whenAllSuccess(checkinTasks).addOnSuccessListener(checkinObjects -> {
                List<HistoryEntry> historyEntries = new ArrayList<>();
                
                for (int i = 0; i < checkinObjects.size(); i++) {
                    String date = uniqueDates.get(i);
                    DocumentSnapshot checkinSnap = (DocumentSnapshot) checkinObjects.get(i);
                    List<MedLog> logs = logsByDate.get(date);
                    
                    Checkin dailyCheckin = null;
                    if (checkinSnap.exists()) {
                        try {
                            dailyCheckin = checkinSnap.toObject(Checkin.class);
                        } catch (Exception e) {
                            System.err.println("Error parsing Checkin: " + e.getMessage());
                        }
                    }

                    MedLog medLogRescue = null;
                    MedLog medLogController = null;

                    if (logs != null) {
                        for (MedLog log : logs) {
                            if ("rescue".equalsIgnoreCase(log.getMedicineType())) {
                                medLogRescue = log;
                            } else if ("controller".equalsIgnoreCase(log.getMedicineType())) {
                                medLogController = log;
                            }
                        }
                    }

                    List<String> symptoms = new ArrayList<>();
                    List<String> triggers = new ArrayList<>();

                    if (dailyCheckin != null) {
                        if (dailyCheckin.getSymptoms() != null) symptoms = dailyCheckin.getSymptoms();
                        if (dailyCheckin.getTriggers() != null) triggers = dailyCheckin.getTriggers();
                    }

                    // Corrected order: Rescue first, then Controller (matches HistoryEntry constructor)
                    historyEntries.add(new HistoryEntry(date, medLogRescue, medLogController, symptoms, triggers));
                }
                
                callback.onSuccess(historyEntries);

            }).addOnFailureListener(callback::onError);

        }).addOnFailureListener(callback::onError);
    }


    public static List<HistoryEntry> filterSymptomsTriggers(List<HistoryEntry> historyEntries, List<String> symptoms, List<String> triggers) {
        List<HistoryEntry> filteredHistoryEntries = new ArrayList<>();

        for (HistoryEntry entry : historyEntries) {
            boolean matchesSymptoms = false;
            if (symptoms == null || symptoms.isEmpty()) {
                matchesSymptoms = true;
            } else if (entry.getSymptoms() != null) {
                for (String s : symptoms) {
                    if (entry.getSymptoms().contains(s)) {
                        matchesSymptoms = true;
                        break;
                    }
                }
            }

            boolean matchesTriggers = false;
            if (triggers == null || triggers.isEmpty()) {
                matchesTriggers = true;
            } else if (entry.getTriggers() != null) {
                for (String t : triggers) {
                    if (entry.getTriggers().contains(t)) {
                        matchesTriggers = true;
                        break;
                    }
                }
            }

            if (matchesSymptoms && matchesTriggers) {
                filteredHistoryEntries.add(entry);
            }
        }
        return filteredHistoryEntries;
    }
}
