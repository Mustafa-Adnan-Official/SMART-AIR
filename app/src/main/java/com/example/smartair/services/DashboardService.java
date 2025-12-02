package com.example.smartair.services;

import androidx.annotation.NonNull;

import com.example.smartair.models.childcollections.MedLog;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class DashboardService {

    private String childrenPath = "children";
    private String medLogsPath = "medLogs";
    private String checkinsPath = "checkins";

    private FirebaseFirestore db;

    public DashboardService() {
        db = FirebaseFirestore.getInstance();
    }

    public interface ZoneCallback {
        void onSuccess(String zone);
        void onError(Exception e);
    }

    public interface LastRescueCallback {
        void onSuccess(Timestamp lastRescueTime);
        void onError(Exception e);
    }

    public interface WeeklyRescueCallback {
        void onSuccess(int weeklyCount);
        void onError(Exception e);
    }

    public interface TrendSnippetCallback {
        void onSuccess(List<TrendPoint> points);
        void onError(Exception e);
    }

    public class TrendPoint {
        public String dayLabel;
        public int rescueCount;
        public int symptomScore;
        public String zone;

        public TrendPoint(String dayLabel, int rescueCount, int symptomScore, String zone) {
            this.dayLabel = dayLabel;
            this.rescueCount = rescueCount;
            this.symptomScore = symptomScore;
            this.zone = zone;
        }
    }

    private Timestamp startOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTime());
    }

    private Timestamp daysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -days);
        return new Timestamp(cal.getTime());
    }

    private String dayLabel(Timestamp ts) {
        Date d = ts.toDate();
        SimpleDateFormat fmt = new SimpleDateFormat("EEE", Locale.US);
        return fmt.format(d);
    }

    public void getTodayZone(String childUid, @NonNull ZoneCallback callback) {
        db.collection(childrenPath)
                .document(childUid)
                .get()
                .addOnSuccessListener(childSnap -> {
                    Long pbLong = childSnap.getLong("personalBest");
                    long tempPb = 0;
                    if (pbLong != null) tempPb = pbLong;
                    final long pb = tempPb;

                    final Timestamp todayStart = startOfToday();

                    db.collection(childrenPath)
                            .document(childUid)
                            .collection(checkinsPath)
                            .whereGreaterThanOrEqualTo("createdAt", todayStart)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(checkinQuery -> {
                                if (checkinQuery.isEmpty() || pb <= 0) {
                                    callback.onSuccess("none");
                                    return;
                                }

                                DocumentSnapshot doc = checkinQuery.getDocuments().get(0);
                                Long pefLong = doc.getLong("peakFlow");

                                if (pefLong == null) {
                                    callback.onSuccess("none");
                                    return;
                                }

                                double percent = (pefLong * 100.0) / pb;

                                if (percent >= 80.0) callback.onSuccess("green");
                                else if (percent >= 50.0) callback.onSuccess("yellow");
                                else callback.onSuccess("red");
                            })
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getLastRescueTime(String childUid, @NonNull LastRescueCallback callback) {
        db.collection(childrenPath)
                .document(childUid)
                .collection(medLogsPath)
                .whereEqualTo("medicineType", "rescue")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    Timestamp out = null;
                    if (!query.isEmpty()) {
                        MedLog log = query.getDocuments().get(0).toObject(MedLog.class);
                        if (log != null) out = log.getCreatedAt();
                    }
                    callback.onSuccess(out);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getWeeklyRescueCount(String childUid, @NonNull WeeklyRescueCallback callback) {
        Timestamp start = daysAgo(7);
        final Timestamp sevenDaysAgo = start;

        db.collection(childrenPath)
                .document(childUid)
                .collection(medLogsPath)
                .whereEqualTo("medicineType", "rescue")
                .whereGreaterThanOrEqualTo("createdAt", sevenDaysAgo)
                .get()
                .addOnSuccessListener(query -> callback.onSuccess(query.size()))
                .addOnFailureListener(callback::onError);
    }

    public void getTrendSnippet(String childUid, int dayRange, @NonNull TrendSnippetCallback callback) {
        Timestamp start = daysAgo(dayRange);
        final Timestamp rangeStart = start;

        db.collection(childrenPath)
                .document(childUid)
                .collection(medLogsPath)
                .whereGreaterThanOrEqualTo("createdAt", rangeStart)
                .get()
                .addOnSuccessListener(medQuery -> {
                    Map<String, Integer> rescueByDay = new HashMap<>();

                    for (DocumentSnapshot doc : medQuery) {
                        MedLog log = doc.toObject(MedLog.class);
                        if (log == null) continue;
                        Timestamp created = log.getCreatedAt();
                        if (created == null) continue;

                        String key = dateKey(created);

                        int count = 0;
                        if (rescueByDay.containsKey(key)) count = rescueByDay.get(key);
                        rescueByDay.put(key, count + 1);
                    }

                    db.collection(childrenPath)
                            .document(childUid)
                            .collection(checkinsPath)
                            .whereGreaterThanOrEqualTo("createdAt", rangeStart)
                            .get()
                            .addOnSuccessListener(checkinQuery -> {
                                Map<String, Integer> symptomScore = new HashMap<>();

                                for (DocumentSnapshot snap : checkinQuery) {
                                    Timestamp created = snap.getTimestamp("createdAt");
                                    if (created == null) continue;

                                    String key = dateKey(created);

                                    List<String> symptoms = (List<String>) snap.get("symptoms");

                                    int score = 0;
                                    if (symptoms != null) score = symptoms.size();

                                    symptomScore.put(key, score);
                                }

                                List<TrendPoint> result = new ArrayList<>();

                                for (int i = dayRange - 1; i >= 0; i--) {
                                    Timestamp t = daysAgo(i);
                                    String key = dateKey(t);

                                    int r = 0;
                                    if (rescueByDay.containsKey(key)) r = rescueByDay.get(key);

                                    int s = 0;
                                    if (symptomScore.containsKey(key)) s = symptomScore.get(key);

                                    String zone = "green";
                                    if (r >= 3) zone = "red";
                                    else if (r >= 1) zone = "yellow";

                                    result.add(new TrendPoint(dayLabel(t), r, s, zone));
                                }

                                callback.onSuccess(result);
                            })
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    private String dateKey(Timestamp ts) {
        Date d = ts.toDate();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return f.format(d);
    }
}
