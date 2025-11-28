package com.example.smartair.repositories;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.android.gms.tasks.Tasks;
import java.util.concurrent.ExecutionException;
import java.lang.InterruptedException;

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

    public boolean getAccessBool(String childUid, String providerUid) {

        try {
            DocumentSnapshot documentSnapshot = Tasks.await(
                    db.collection("providers")
                            .document(providerUid)
                            .collection("childLink")
                            .document(childUid)
                            .get()
            );

            if (documentSnapshot.exists()) {
                Boolean accessStatus = documentSnapshot.getBoolean("accessField");
                return accessStatus != null && accessStatus;
            }

            return false;

        } catch (ExecutionException e) {
            System.err.println("Firestore Execution Error: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread Interrupted: " + e.getMessage());
            return false;
        }
    }


    public void setAccessBool(String childUid, String providerUid, boolean Value) {
        db.collection("providers")
                .document(providerUid)
                .collection("childLink")
                .document(childUid)
                .update("accessField", Value);


    }
}
