package com.example.smartair.repositories;

import com.example.smartair.callbacks.ResultCallback;
import com.example.smartair.models.users.Child;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

    public void getChildrenForProvider(String providerUID, ResultCallback<List<Child>> callback) {
        db.collection("providers")
                .document(providerUID)
                .collection("parentLinks")
                .get()
                .addOnSuccessListener(parentSnapshots -> {
                    List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                    for (DocumentSnapshot parentDoc : parentSnapshots.getDocuments()) {
                        String pUid = parentDoc.getId();
                        tasks.add(db.collection("children")
                                .whereEqualTo("parentUid", pUid)
                                .get());
                    }

                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                        List<Child> allChildren = new ArrayList<>();
                        for (Object result : results) {
                            QuerySnapshot querySnap = (QuerySnapshot) result;
                            for (DocumentSnapshot childDoc : querySnap) {
                                Child child = childDoc.toObject(Child.class);
                                if (child != null) {
                                    child.setChildUid(childDoc.getId());
                                    allChildren.add(child);
                                }
                            }
                        }
                        callback.onSuccess(allChildren);
                    }).addOnFailureListener(callback::onError);

                })
                .addOnFailureListener(callback::onError);
    }

}
