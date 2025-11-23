package com.example.smartair;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.ComponentActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test activity:
 *  - One button on activity_main.xml with id: btnCreateParentHardcoded
 *  - On click:
 *      1) Try to create user with email/password.
 *      2) If email already exists → sign in instead.
 *      3) In both cases, write parents/{uid} in Firestore.
 */
public class MainActivity extends ComponentActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;



    /**
     * Creates or updates a hard-coded parent:
     *  - Firebase Auth (create or sign in)
     *  - Firestore "parents/{parentUid}"
     */
    private void createOrUpdateHardcodedParent() {

        final String name     = "Parent Test 2";
        final String email    = "parent2@test.com";
        final String password = "Test1234!";
        final String pac      = "PA-7FQ2-91";

        Toast.makeText(this,
                "Creating/signing in parent: " + email,
                Toast.LENGTH_SHORT).show();

        // 1) Try to CREATE the Auth user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    // New user created
                                    String uid = task.getResult()
                                            .getUser()
                                            .getUid();
                                    writeParentDoc(uid, name, email, pac);
                                } else {
                                    Exception e = task.getException();

                                    // If email is already in use → SIGN IN instead
                                    if (e instanceof FirebaseAuthUserCollisionException) {

                                        auth.signInWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(
                                                        MainActivity.this,
                                                        new OnCompleteListener<AuthResult>() {
                                                            @Override
                                                            public void onComplete(
                                                                    @NonNull Task<AuthResult> signInTask) {

                                                                if (signInTask.isSuccessful()) {
                                                                    String uid = signInTask
                                                                            .getResult()
                                                                            .getUser()
                                                                            .getUid();
                                                                    writeParentDoc(uid, name, email, pac);
                                                                } else {
                                                                    Toast.makeText(
                                                                            MainActivity.this,
                                                                            "Sign-in failed: "
                                                                                    + signInTask.getException()
                                                                                    .getMessage(),
                                                                            Toast.LENGTH_LONG
                                                                    ).show();
                                                                }
                                                            }
                                                        });

                                    } else {
                                        // Some other Auth error
                                        Toast.makeText(
                                                MainActivity.this,
                                                "Auth error: " + e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                }
                            }
                        });
    }

    /**
     * Writes the parent document to Firestore at parents/{parentUid}.
     */
    private void writeParentDoc(String parentUid,
                                String name,
                                String email,
                                String pac) {

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("name", name);
        data.put("email", email);
        data.put("role", "parent");
        data.put("parentAccessCode", pac);
        data.put("createdAt", FieldValue.serverTimestamp());

        // empty childUID list for now
        List<String> childUID = new ArrayList<String>();
        data.put("childUID", childUID);

        db.collection("parents")
                .document(parentUid)
                .set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Firestore parent doc written for UID: " + parentUid,
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Firestore error: "
                                            + task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });
    }
}