package com.example.smartair.logic;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.smartair.repositories.ParentRepository;
import com.example.smartair.utils.ParentInviteFlow;

public class ProviderLinkingFlow {
    private FirebaseFirestore db;
    private ParentRepository parentRepo;
    private ParentInviteFlow inviteFlow;

    public ProviderLinkingFlow() {
        db = FirebaseFirestore.getInstance();
        parentRepo = new ParentRepository();
        inviteFlow = new ParentInviteFlow();
    }

    public void linkProviderToParent (String providerUID, String enteredCode) {
        db.collection("parents")
          .whereEqualTo("parentAccessCode", enteredCode)
          .get()
          .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

              @Override
              public void onSuccess(QuerySnapshot querySnapshot) {
                  if (querySnapshot.isEmpty()) {
                      System.out.println("invalid Parent Access Code");
                      return;
                  }

                  QueryDocumentSnapshot doc = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);

                  long now = System.currentTimeMillis();
                  long lastUpdated = doc.getDate("__update_time__").getTime();
                  long sevenDays = 7L * 24L * 60L * 60L * 1000L;

                  if (now - lastUpdated > sevenDays) {
                      System.out.println("Parent access code expired");
                      return;
                  }

                  String parentUID = doc.getId();

                  db.collection("parents")
                    .document(parentUID)
                    .update("providerUIDs", FieldValue.arrayUnion(providerUID));

                  String newCode = inviteFlow.generateCode();
                  parentRepo.regenerateAccessCode(parentUID, newCode);

                  System.out.println("Provider linked successfully");
              }
          });
    }
}
