package com.example.smartair.services;

import com.example.smartair.callbacks.CheckinListCallback;
import com.example.smartair.callbacks.SimpleResultCallback;
import com.example.smartair.callbacks.StringListCallback;
import com.example.smartair.models.childcollections.Checkin;
import com.example.smartair.utils.SimpleDateFormat;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;
public class CheckinService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addCheckin(String childUid, Checkin checkin, SimpleResultCallback callback) {

        String dateId = SimpleDateFormat.getCurrentDateId();

        db.collection("children")
                .document(childUid)
                .collection("checkins")
                .document(dateId)
                .set(checkin)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Checkin added successfully for child: " + childUid);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error adding checkin: " + e.getMessage());
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });

    }

    public void getCheckins(String childUid, CheckinListCallback callback) {

        db.collection("children")
                .document(childUid)
                .collection("checkins")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Checkin> checkinsList = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Checkin checkin = document.toObject(Checkin.class);
                        if(checkin != null){
                            checkinsList.add(checkin);

                        }
                    }

                    callback.onSuccess(checkinsList);


                })
                .addOnFailureListener(e -> {
                   callback.onFailure(e);
                });

    }

    public void getSymptoms(String childUid, StringListCallback callback){
        List<String> symptomsList;
        getCheckins(childUid, new CheckinListCallback() {
            @Override
            public void onSuccess(List<Checkin> checkins) {
                List<String> symptoms = new ArrayList<>();

                for(Checkin checkin : checkins) {
                    List<String> currentSymptoms = checkin.getSymptoms();
                    if (currentSymptoms != null) {
                        for(String symptom : currentSymptoms){
                            if (!symptoms.contains(symptom)){
                                symptoms.add(symptom);
                            }
                        }
                    }

                }
                callback.onSuccess(symptoms);

            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);


            }
        });

    }


    public void getTriggers(String childUid, StringListCallback callback){
        List<String> symptomsList;
        getCheckins(childUid, new CheckinListCallback() {
            @Override
            public void onSuccess(List<Checkin> checkins) {
                List<String> triggers = new ArrayList<>();

                for(Checkin checkin : checkins) {
                    List<String> currentTriggers = checkin.getTriggers();
                    if (currentTriggers != null) {
                        for(String currentTrigger : currentTriggers){
                            if (!triggers.contains(currentTrigger)){
                                triggers.add(currentTrigger);
                            }
                        }
                    }

                }
                callback.onSuccess(triggers);

            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);


            }
        });

    }
}
