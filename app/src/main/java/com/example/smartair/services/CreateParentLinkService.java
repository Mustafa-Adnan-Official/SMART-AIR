package com.example.smartair.services;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CreateParentLinkService {

    public interface Callback {
        void onSuccess(String parentUid);
        void onFailure();
    }

    private final FirebaseFirestore db;

    public CreateParentLinkService() {
        db = FirebaseFirestore.getInstance();
    }

    public void linkParentToProvider(String providerUid, String accessCode, Callback cb) {
        db.collection("parents")
                .get()
                .addOnSuccessListener(snaps -> {
                    for (QueryDocumentSnapshot d : snaps) {
                        String code = d.getString("parentAccessCode");
                        if (code != null && code.equals(accessCode)) {
                            String parentUid = d.getId();
                            String name = d.getString("name");
                            String email = d.getString("email");

                            db.collection("providers")
                                    .document(providerUid)
                                    .collection("parentLinks")
                                    .document(parentUid)
                                    .set(new ParentLink(parentUid, email, name))
                                    .addOnSuccessListener(r -> cb.onSuccess(parentUid))
                                    .addOnFailureListener(e -> cb.onFailure());
                            return;
                        }
                    }
                    cb.onFailure();
                })
                .addOnFailureListener(e -> cb.onFailure());
    }

    public static class ParentLink {
        public String parentUid;
        public String parentEmail;
        public String parentName;

        public ParentLink() {}

        public ParentLink(String parentUid, String parentEmail, String parentName) {
            this.parentUid = parentUid;
            this.parentEmail = parentEmail;
            this.parentName = parentName;
        }
    }
}
