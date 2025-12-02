package com.example.smartair.services;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.Map;

public class InAppAlertsListener {

    private FirebaseFirestore db;
    private ListenerRegistration reg;

    public interface AlertCallback {
        void onAlert(Map<String, Object> data);
    }

    public InAppAlertsListener() {
        db = FirebaseFirestore.getInstance();
    }

    public void startListening(String childUid, AlertCallback cb) {
        stopListening();
        reg = db.collection("children")
                .document(childUid)
                .collection("alerts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            DocumentSnapshot d = dc.getDocument();
                            Map<String, Object> m = d.getData();
                            if (m != null) cb.onAlert(m);
                        }
                    }
                });
    }

    public void stopListening() {
        if (reg != null) reg.remove();
        reg = null;
    }
}
