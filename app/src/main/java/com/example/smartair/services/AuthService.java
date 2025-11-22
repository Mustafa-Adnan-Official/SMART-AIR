package com.example.smartair.services;

import androidx.annotation.NonNull;

import com.example.smartair.callbacks.AuthResultCallback;
import com.example.smartair.callbacks.ChildUnderParentLoginCallback;
import com.example.smartair.callbacks.FetchRoleCallback;
import com.example.smartair.callbacks.LoginResultCallback;
import com.example.smartair.callbacks.SimpleResultCallback;
import com.example.smartair.models.RoleType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthService() {
        this(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance());
    }

    // Constructor injection → easier to mock in tests
    public AuthService(FirebaseAuth auth, FirebaseFirestore db) {
        this.auth = auth;
        this.db = db;
    }

    // ---------- SIGNUP ----------

    public void registerParent(
            final String name,
            final String email,
            final String password,
            final AuthResultCallback callback) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        callback.onFailure("Auth user is null");
                        return;
                    }

                    String parentUid = user.getUid();
                    user.sendEmailVerification();

                    Map<String, Object> data = new HashMap<>();
                    data.put("parentUid", parentUid);
                    data.put("name", name);
                    data.put("email", email);
                    data.put("parentAccessCode", null); // to be set by future requirement
                    data.put("role", "parent");
                    data.put("onboarded", false);
                    data.put("createdAt", FieldValue.serverTimestamp());
                    data.put("childUIDs", new java.util.ArrayList<String>());

                    db.collection("parents")
                            .document(parentUid)
                            .set(data)
                            .addOnSuccessListener(unused -> callback.onSuccess(parentUid))
                            .addOnFailureListener(e ->
                                    callback.onFailure("Firestore parent error: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Auth parent error: " + e.getMessage()));
    }

    public void registerProvider(
            final String prefix,
            final String name,
            final String email,
            final String password,
            final AuthResultCallback callback) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        callback.onFailure("Auth user is null");
                        return;
                    }

                    String providerUid = user.getUid();
                    user.sendEmailVerification();

                    Map<String, Object> data = new HashMap<>();
                    data.put("providerUid", providerUid);
                    data.put("prefix", prefix);
                    data.put("name", name);
                    data.put("email", email);
                    data.put("role", "provider");
                    data.put("onboarded", false);
                    data.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("providers")
                            .document(providerUid)
                            .set(data)
                            .addOnSuccessListener(unused -> callback.onSuccess(providerUid))
                            .addOnFailureListener(e ->
                                    callback.onFailure("Firestore provider error: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Auth provider error: " + e.getMessage()));
    }

    // Child with own email (independent)
    public void registerChildIndependent(
            final String name,
            final String email,
            final String password,
            final AuthResultCallback callback) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        callback.onFailure("Auth user is null");
                        return;
                    }

                    String childUid = user.getUid();
                    user.sendEmailVerification();

                    Map<String, Object> data = new HashMap<>();
                    data.put("childUid",      childUid);
                    data.put("name",          name);
                    data.put("email",         email);
                    data.put("hasOwnEmail",   true);
                    data.put("parentAccessCode", null);
                    data.put("parentUid",     null);
                    data.put("personalBest",  0);
                    data.put("role",          "child");
                    data.put("onboarded",     false);
                    data.put("createdAt",     FieldValue.serverTimestamp());

                    db.collection("children")
                            .document(childUid)
                            .set(data)
                            .addOnSuccessListener(unused -> callback.onSuccess(childUid))
                            .addOnFailureListener(e ->
                                    callback.onFailure("Firestore child error: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Auth child error: " + e.getMessage()));
    }

    // Child with parent PAC; child uses parent email to sign in.
    public void registerChildUnderParent(
            final String name,
            final String password,          // currently unused, kept for future extension
            final String parentAccessCode,
            final AuthResultCallback callback) {

        // 1) Find parent by PAC
        db.collection("parents")
                .whereEqualTo("parentAccessCode", parentAccessCode)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onFailure("Invalid Parent Access Code");
                        return;
                    }

                    DocumentSnapshot parentDoc = querySnapshot.getDocuments().get(0);
                    final String parentUid   = parentDoc.getString("parentUid");
                    final String parentEmail = parentDoc.getString("email");

                    if (parentUid == null || parentEmail == null) {
                        callback.onFailure("Parent UID or Email missing");
                        return;
                    }

                    // 2) Create child document with random ID
                    DocumentReference childRef = db.collection("children").document();
                    String childUid = childRef.getId();

                    Map<String, Object> data = new HashMap<>();
                    data.put("childUid",         childUid);
                    data.put("name",             name);
                    data.put("email",            parentEmail);   // child signs in using parent email
                    data.put("hasOwnEmail",      false);
                    data.put("parentAccessCode", parentAccessCode);
                    data.put("parentUid",        parentUid);
                    data.put("personalBest",     0);
                    data.put("role",             "child");
                    data.put("onboarded",        false);
                    data.put("createdAt",        FieldValue.serverTimestamp());

                    childRef.set(data)
                            .addOnSuccessListener(unused -> callback.onSuccess(childUid))
                            .addOnFailureListener(e ->
                                    callback.onFailure("Firestore child error: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("PAC lookup error: " + e.getMessage()));
    }


    // ---------- LOGIN ----------

    // Parent, Provider, independent child: email+password
    public void loginWithEmailPassword(
            final String email,
            final String password,
            final LoginResultCallback callback) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        callback.onFailure("User is null");
                        return;
                    }

                    if (!user.isEmailVerified()) {
                        auth.signOut();
                        callback.onUnverifiedEmail();
                        return;
                    }

                    String uid = user.getUid();
                    // After login, figure out which collection this user belongs to
                    detectRoleForUid(uid, callback);
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Auth login error: " + e.getMessage()));
    }

    // Child under parent login: childName + parentEmail + password
    public void loginChildUnderParent(
            final String childName,
            final String parentEmail,
            final String password,
            final ChildUnderParentLoginCallback callback) {

        // 1) Log in as parent
        auth.signInWithEmailAndPassword(parentEmail, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser parent = authResult.getUser();
                    if (parent == null) {
                        callback.onFailure("Parent user is null");
                        return;
                    }
                    if (!parent.isEmailVerified()) {
                        auth.signOut();
                        callback.onFailure("Parent email not verified");
                        return;
                    }

                    final String parentUid = parent.getUid();

                    // 2) Find child under this parent with the given name
                    db.collection("children")
                            .whereEqualTo("parentUid", parentUid)
                            .whereEqualTo("name", childName)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (querySnapshot.isEmpty()) {
                                    callback.onFailure("No matching child profile for this parent");
                                    return;
                                }

                                DocumentSnapshot childDoc = querySnapshot.getDocuments().get(0);
                                String childUid = childDoc.getString("childUid");
                                Boolean onboarded = childDoc.getBoolean("onboarded");
                                callback.onSuccess(childUid, onboarded != null && onboarded);
                            })
                            .addOnFailureListener(e ->
                                    callback.onFailure("Child lookup error: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Parent auth error: " + e.getMessage()));
    }

    // ---------- ROLE / ONBOARDING ----------

    private void detectRoleForUid(final String uid, final LoginResultCallback callback) {
        // Check parents → providers → children
        db.collection("parents").document(uid).get()
                .addOnSuccessListener(parentSnap -> {
                    if (parentSnap.exists()) {
                        Boolean onboarded = parentSnap.getBoolean("onboarded");
                        callback.onSuccess(uid, RoleType.PARENT,
                                onboarded != null && onboarded);
                    } else {
                        db.collection("providers").document(uid).get()
                                .addOnSuccessListener(providerSnap -> {
                                    if (providerSnap.exists()) {
                                        Boolean onboarded = providerSnap.getBoolean("onboarded");
                                        callback.onSuccess(uid, RoleType.PROVIDER,
                                                onboarded != null && onboarded);
                                    } else {
                                        db.collection("children").document(uid).get()
                                                .addOnSuccessListener(childSnap -> {
                                                    if (childSnap.exists()) {
                                                        Boolean onboarded = childSnap.getBoolean("onboarded");
                                                        callback.onSuccess(uid, RoleType.CHILD,
                                                                onboarded != null && onboarded);
                                                    } else {
                                                        callback.onFailure("No role document found for this UID");
                                                    }
                                                })
                                                .addOnFailureListener(e ->
                                                        callback.onFailure("Child role error: " + e.getMessage()));
                                    }
                                })
                                .addOnFailureListener(e ->
                                        callback.onFailure("Provider role error: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Parent role error: " + e.getMessage()));
    }

    public void fetchCurrentUserRole(final FetchRoleCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onFailure("No signed-in user");
            return;
        }
        detectRoleForUid(user.getUid(), new LoginResultCallback() {
            @Override
            public void onSuccess(String uid, RoleType role, boolean onboarded) {
                callback.onSuccess(role, onboarded);
            }

            @Override
            public void onUnverifiedEmail() {
                callback.onFailure("Email not verified");
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    public void markOnboarded(RoleType role, final SimpleResultCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onFailure("No signed-in user");
            return;
        }
        String uid = user.getUid();

        String collection;
        switch (role) {
            case PARENT:
                collection = "parents";
                break;
            case PROVIDER:
                collection = "providers";
                break;
            case CHILD:
            default:
                collection = "children";
                break;
        }

        db.collection(collection)
                .document(uid)
                .update("onboarded", true)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onFailure("Onboarded update error: " + e.getMessage()));
    }

    // ---------- MISC ----------

    public void sendPasswordReset(String email, final SimpleResultCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onFailure("Reset email error: " + e.getMessage()));
    }

    public void signOut() {
        auth.signOut();
    }
}
