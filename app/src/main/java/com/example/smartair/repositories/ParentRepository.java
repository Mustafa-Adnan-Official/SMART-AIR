package com.example.smartair.repositories;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

public class ParentRepository {
    private FirebaseFirestore db;

    public ParentRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void removeChild(String parentUID, String childUID) {
        db.collection("parents")
          .document(parentUID)
          .update("childUIDs", FieldValue.arrayRemove(childUID));
    }

    public void removeProvider(String parentUID, String providerUID) {
        db.collection("parents")
                .document(parentUID)
                .update("providerUIDs", FieldValue.arrayRemove(providerUID));
    }

    public void updateAccessCode(String parentUID, String newCode) {
        db.collection("parents")
                .document(parentUID)
                .update("parentAccessCode", newCode);
    }

    public void regenerateAccessCode(String parentUID, String newCode) {
        db.collection("parents")
                .document(parentUID)
                .update("parentAccessCode", newCode);
    }
}
