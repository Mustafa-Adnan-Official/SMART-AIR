package com.example.smartair.repositories;

import com.example.smartair.models.ProviderSharing;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.smartair.models.ChildToggles;

public class ChildRepository {
    private FirebaseFirestore db;

    public ChildRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void adjustPB(String childUID, int value) {
        db.collection("children")
                      .document(childUID)
                      .update("personalBest", value);
    }

    public void adjustControllerUses(String childUID, int value) {
        db.collection("children")
                      .document(childUID)
                      .update("controllerUses", value);
    }

    public void adjustReportDuration(String childUID, int value) {
        if (value>2 && value<7) { // check if value is between 3 and 6 months
            db.collection("children").document(childUID).update("reportDuration", value);
        }
    }

    public void adjustProviderSharing(String childUID, String providerUID, ChildToggles toggles) {
        ProviderSharing sharing = new ProviderSharing(providerUID, toggles);

        db.collection("children")
                      .document(childUID)
                      .update("providerSharingList", FieldValue.arrayUnion(sharing));
    }
}
