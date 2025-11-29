package com.example.smartair.repositories;

import com.example.smartair.models.ProviderSharing;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.smartair.models.ChildToggles;
import com.example.smartair.callbacks.ResultCallback;
import com.example.smartair.models.Child;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

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

    public void getChildrenForParent(String parentUid, ResultCallback<List<Child>> callback) {
        db.collection("children")
                .whereEqualTo("parentUid", parentUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Child> children = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Child child = document.toObject(Child.class);
                        child.setChildUid(document.getId());
                        children.add(child);
                    }
                    callback.onSuccess(children);
                })
                .addOnFailureListener(callback::onError);
    }


}
