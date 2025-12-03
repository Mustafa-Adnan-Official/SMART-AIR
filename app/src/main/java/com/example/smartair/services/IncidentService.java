package com.example.smartair.services;

import com.example.smartair.callbacks.ResultCallback;
import com.example.smartair.models.childcollections.Incident;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class IncidentService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public void getAllIncidents(String childUid, Date start, Date end, ResultCallback<List<Incident>> callback) {

        Query incidentQuery = db.collection("children")
                .document(childUid)
                .collection("incidents")
                .whereGreaterThanOrEqualTo("timeOfIncident", new Timestamp(start))
                .whereLessThanOrEqualTo("timeOfIncident", new Timestamp(end));

        incidentQuery.get().addOnSuccessListener(incidentSnapshot -> {
            List<Incident> incidents = new ArrayList<>();

            if (incidentSnapshot != null) {
                for (com.google.firebase.firestore.DocumentSnapshot document : incidentSnapshot.getDocuments()){
                    try {
                        Incident incident = document.toObject(Incident.class);
                        if (incident != null) {
                            incidents.add(incident);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing Incident document: " + e.getMessage());
                    }
                }
            }

            callback.onSuccess(incidents);

        }).addOnFailureListener(callback::onError);
    }
}