package com.example.smartair.repositories;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

public class ProviderRepository {

    private FirebaseFirestore db;

    public ProviderRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void adjustName(String providerUID, String newName) {
            db.collection("providers")
              .document(providerUID)
              .update("name", newName);
    }

    public void adjustPrefix(String providerUID, String newPrefix) {
        db.collection("providers")
                .document(providerUID)
                .update("prefix", newPrefix);
    }
}
