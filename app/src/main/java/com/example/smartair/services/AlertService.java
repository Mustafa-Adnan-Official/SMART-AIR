package com.example.smartair.services;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AlertService {

    private FirebaseFirestore db;
    private ListenerRegistration checkinListener;
    private ListenerRegistration medLogListener;
    private ListenerRegistration incidentListener;
    private ListenerRegistration inventoryListener;

    public AlertService() {
        db = FirebaseFirestore.getInstance();
    }

    public void start(String childUid) {
        listenCheckins(childUid);
        listenMedLogs(childUid);
        listenIncidents(childUid);
        listenInventory(childUid);
    }

    public void stop() {
        if (checkinListener != null) checkinListener.remove();
        if (medLogListener != null) medLogListener.remove();
        if (incidentListener != null) incidentListener.remove();
        if (inventoryListener != null) inventoryListener.remove();
    }

    private void listenCheckins(String childUid) {
        checkinListener = db.collection("children")
                .document(childUid)
                .collection("checkins")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null || snap.isEmpty()) return;
                    DocumentSnapshot d = snap.getDocuments().get(0);
                    Number pef = (Number) d.get("peakFlow");
                    if (pef == null) return;
                    db.collection("children").document(childUid).get().addOnSuccessListener(c -> {
                        Number pb = (Number) c.get("personalBest");
                        if (pb == null) return;
                        boolean red = AlertEvaluator.isRedZone(pb.doubleValue(), pef.doubleValue());
                        if (red) writeAlert(childUid, true, false, false, false, false);
                    });
                });
    }

    private void listenMedLogs(String childUid) {
        medLogListener = db.collection("children")
                .document(childUid)
                .collection("medLogs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null || snap.isEmpty()) return;

                    ArrayList<Timestamp> rescueTimes = new ArrayList<>();
                    boolean worseAfter = false;

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String type = d.getString("medicineType");
                        String before = d.getString("feelingBefore");
                        String after = d.getString("feelingAfter");

                        if (AlertEvaluator.isWorseAfterDose(before, after)) worseAfter = true;

                        if (type != null && type.equals("rescue")) {
                            Timestamp t = d.getTimestamp("createdAt");
                            if (t != null) rescueTimes.add(t);
                        }
                    }

                    Timestamp[] arr = rescueTimes.toArray(new Timestamp[0]);
                    boolean rapid = AlertEvaluator.isRapidRescue(arr);

                    if (rapid) writeAlert(childUid, false, true, false, false, false);
                    if (worseAfter) writeAlert(childUid, false, false, false, true, false);
                });
    }

    private void listenIncidents(String childUid) {
        incidentListener = db.collection("children")
                .document(childUid)
                .collection("incidents")
                .orderBy("timeOfIncident", Query.Direction.DESCENDING)
                .limit(3)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Boolean esc = d.getBoolean("escalatedIncident");
                        if (esc != null && esc) {
                            writeAlert(childUid, false, false, true, false, false);
                            return;
                        }
                    }
                });
    }

    private void listenInventory(String childUid) {
        inventoryListener = db.collection("children")
                .document(childUid)
                .collection("inventory")
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Number rem = (Number) d.get("dosesRemaining");
                        Number tot = (Number) d.get("totalAcutations");
                        if (rem == null || tot == null) continue;
                        boolean low = AlertEvaluator.isLowInventory(rem.intValue(), tot.intValue());
                        if (low) {
                            writeAlert(childUid, false, false, false, false, true);
                            return;
                        }
                    }
                });
    }

    private void writeAlert(String childUid, boolean red, boolean rapid, boolean triage, boolean worse, boolean low) {
        Map<String, Object> m = new HashMap<>();
        m.put("createdAt", Timestamp.now());
        m.put("redZoneDay", red);
        m.put("rapidRescueRepeats", rapid);
        m.put("triageEscalation", triage);
        m.put("worseAfterDose", worse);
        m.put("inventoryLowOrExpired", low);

        db.collection("children")
                .document(childUid)
                .collection("alerts")
                .add(m);
    }
}
