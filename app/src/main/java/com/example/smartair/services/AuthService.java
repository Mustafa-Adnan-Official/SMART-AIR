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

/**
 * Purpose: FirebaseAuth + Firestore service for registering users, logging in, and updating onboarding.
 * Layer: Service / Model (used by presenters).
 * Used For: Parent/child/provider signup, login (including child-under-parent), role detection, reset, sign-out.
 */
public class AuthService {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthService() {
        this(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance());
    }

    /**
     * Constructor injection for easier testing with mocks.
     */
    public AuthService(FirebaseAuth auth, FirebaseFirestore db) {
        this.auth = auth;
        this.db = db;
    }

    // ---------- SIGNUP ----------

    /**
     * Registers a new parent (auth + /parents/{parentUid} document).
     */
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
                    data.put("parentAccessCode", null);
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

    /**
     * Registers a new provider (auth + /providers/{providerUid} document).
     */
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

    /**
     * Registers an independent child (own email) as /children/{authUid}.
     */
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
                    data.put("childUid", childUid);
                    data.put("name", name);

                    // Login + reference email for this child
                    data.put("email", email);          // used for login
                    data.put("childEmail", email);     // explicit child email
                    data.put("parentEmail", null);     // no parent in this mode

                    data.put("hasOwnEmail", true);
                    data.put("parentAccessCode", null);
                    data.put("parentUid", null);
                    data.put("personalBest", 0);
                    data.put("role", "child");
                    data.put("onboarded", false);
                    data.put("createdAt", FieldValue.serverTimestamp());

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

    /**
     * Registers a child profile under an existing parent using parentAccessCode.
     * Flow:
     *  1) Find parent by PAC
     *  2) Generate alias email from parent email (local+child-<unique>@domain)
     *  3) Create FirebaseAuth user with alias email + child's password
     *  4) Create /children/{childUid} with childEmail + parentEmail references
     *  5) Append childUid to parent's childUIDs array
     */
    public void registerChildUnderParent(
            final String name,
            final String password,
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
                    final String parentUid = parentDoc.getString("parentUid");
                    final String parentEmail = parentDoc.getString("email");

                    if (parentUid == null || parentEmail == null) {
                        callback.onFailure("Parent UID or Email missing");
                        return;
                    }

                    // 2) Build alias email from parent email:
                    //    localPart + "+child-" + <unique> + "@" + domain
                    String[] parts = parentEmail.split("@", 2);
                    String local = parts[0];
                    String domain = (parts.length > 1) ? parts[1] : "";
                    String uniqueTag = "child-" + System.currentTimeMillis();
                    final String childAliasEmail = local + "+" + uniqueTag + "@" + domain;

                    // 3) Create auth user for child with alias email
                    auth.createUserWithEmailAndPassword(childAliasEmail, password)
                            .addOnSuccessListener(authResult -> {
                                FirebaseUser childUser = authResult.getUser();
                                if (childUser == null) {
                                    callback.onFailure("Child auth user is null");
                                    return;
                                }

                                // Optionally send verification to alias (goes to parent inbox)
                                childUser.sendEmailVerification();

                                final String childUid = childUser.getUid();

                                // 4) Create child doc
                                Map<String, Object> data = new HashMap<>();
                                data.put("childUid", childUid);
                                data.put("name", name);

                                // Login + reference emails
                                data.put("email", childAliasEmail);      // used for login
                                data.put("childEmail", childAliasEmail); // explicit child email
                                data.put("parentEmail", parentEmail);    // reference to real parent email

                                data.put("hasOwnEmail", false);
                                data.put("parentAccessCode", parentAccessCode);
                                data.put("parentUid", parentUid);
                                data.put("personalBest", 0);
                                data.put("role", "child");
                                data.put("onboarded", false);
                                data.put("createdAt", FieldValue.serverTimestamp());

                                db.collection("children")
                                        .document(childUid)
                                        .set(data)
                                        .addOnSuccessListener(unused -> {
                                            // 5) Append childUid to parent's childUIDs array
                                            db.collection("parents")
                                                    .document(parentUid)
                                                    .update("childUIDs", FieldValue.arrayUnion(childUid))
                                                    .addOnSuccessListener(unused2 ->
                                                            callback.onSuccess(childUid))
                                                    .addOnFailureListener(e ->
                                                            callback.onFailure("Failed to update parent.childUIDs: " + e.getMessage()));
                                        })
                                        .addOnFailureListener(e ->
                                                callback.onFailure("Firestore child error: " + e.getMessage()));
                            })
                            .addOnFailureListener(e ->
                                    callback.onFailure("Auth child error: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("PAC lookup error: " + e.getMessage()));
    }


    // ---------- LOGIN ----------

    /**
     * Logs in with email/password and then detects the user’s role.
     * Used by: parent, provider, and independent child flows.
     */
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
                    detectRoleForUid(uid, callback);
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Auth login error: " + e.getMessage()));
    }

    /**
     * Logs a child in under a parent email.
     *
     * Flow:
     *  1) Find parent doc by email
     *  2) Find child doc under that parent with matching name
     *  3) Read child's alias email from doc
     *  4) Auth with child alias email + password
     */
    public void loginChildUnderParent(
            final String childName,
            final String parentEmail,
            final String password,
            final ChildUnderParentLoginCallback callback) {

        // 1) Find parent by email
        db.collection("parents")
                .whereEqualTo("email", parentEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(parentQuery -> {
                    if (parentQuery.isEmpty()) {
                        callback.onFailure("No parent found with that email");
                        return;
                    }

                    DocumentSnapshot parentDoc = parentQuery.getDocuments().get(0);
                    final String parentUid = parentDoc.getString("parentUid");
                    if (parentUid == null) {
                        callback.onFailure("Parent UID missing");
                        return;
                    }

                    // 2) Find child under this parent with the given name
                    db.collection("children")
                            .whereEqualTo("parentUid", parentUid)
                            .whereEqualTo("name", childName)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(childQuery -> {
                                if (childQuery.isEmpty()) {
                                    callback.onFailure("No matching child profile for this parent");
                                    return;
                                }

                                DocumentSnapshot childDoc = childQuery.getDocuments().get(0);
                                final String childUid = childDoc.getString("childUid");

                                // Prefer "email" (login email); fallback to "childEmail" if needed
                                String childEmail = childDoc.getString("email");
                                if (childEmail == null) {
                                    childEmail = childDoc.getString("childEmail");
                                }

                                Boolean onboarded = childDoc.getBoolean("onboarded");

                                if (childEmail == null || childUid == null) {
                                    callback.onFailure("Child email or UID missing");
                                    return;
                                }

                                // 3) Log in as child with alias email
                                auth.signInWithEmailAndPassword(childEmail, password)
                                        .addOnSuccessListener(authResult -> {
                                            FirebaseUser child = authResult.getUser();
                                            if (child == null) {
                                                callback.onFailure("Child user is null");
                                                return;
                                            }

                                            // You could optionally enforce email verification here.
                                            callback.onSuccess(childUid, onboarded != null && onboarded);
                                        })
                                        .addOnFailureListener(e ->
                                                callback.onFailure("Child auth error: " + e.getMessage()));
                            })
                            .addOnFailureListener(e ->
                                    callback.onFailure("Child lookup error: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Parent lookup error: " + e.getMessage()));
    }

    // ---------- ROLE / ONBOARDING ----------

    /**
     * Internal helper to detect role by checking parents → providers → children.
     */
    private void detectRoleForUid(final String uid, final LoginResultCallback callback) {
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

    /**
     * Fetches the current signed-in user’s role and onboarding flag.
     */
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

    /**
     * Marks the current user as onboarded in the collection selected by role.
     */
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

    /**
     * Sends a password reset email using FirebaseAuth.
     */
    public void sendPasswordReset(String email, final SimpleResultCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onFailure("Reset email error: " + e.getMessage()));
    }

    /**
     * Sends a password reset email for a child account that is linked under a parent.
     *
     * Flow:
     *  1) Find the parent document using the parent's real email.
     *  2) From that parentUid, locate the matching child document by child name.
     *  3) Read the child’s alias email (email or childEmail field).
     *  4) Trigger FirebaseAuth password reset for the child’s alias email.
     *
     * Notes:
     *  - The reset link is delivered to the parent’s inbox because alias emails route to
     *    the parent’s mailbox.
     *  - This operation resets ONLY the child’s FirebaseAuth account; the parent’s password
     *    remains unaffected.
     *
     * Used For: Forgot-password flow in child-under-parent login mode.
     */
    public void sendChildPasswordResetUnderParent(
            final String childName,
            final String parentEmail,
            final SimpleResultCallback callback) {

        // 1) Find parent by email
        db.collection("parents")
                .whereEqualTo("email", parentEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(parentQuery -> {
                    if (parentQuery.isEmpty()) {
                        callback.onFailure("No parent found with that email");
                        return;
                    }

                    DocumentSnapshot parentDoc = parentQuery.getDocuments().get(0);
                    final String parentUid = parentDoc.getString("parentUid");
                    if (parentUid == null) {
                        callback.onFailure("Parent UID missing");
                        return;
                    }

                    // 2) Find child under this parent with the given name
                    db.collection("children")
                            .whereEqualTo("parentUid", parentUid)
                            .whereEqualTo("name", childName)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(childQuery -> {
                                if (childQuery.isEmpty()) {
                                    callback.onFailure("No matching child profile for this parent");
                                    return;
                                }

                                DocumentSnapshot childDoc = childQuery.getDocuments().get(0);

                                // Prefer "email" field, fallback to "childEmail"
                                String childEmail = childDoc.getString("email");
                                if (childEmail == null) {
                                    childEmail = childDoc.getString("childEmail");
                                }

                                if (childEmail == null) {
                                    callback.onFailure("Child email missing");
                                    return;
                                }

                                // 3) Send reset email for the CHILD account
                                sendPasswordReset(childEmail, callback);
                            })
                            .addOnFailureListener(e ->
                                    callback.onFailure("Child lookup error: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Parent lookup error: " + e.getMessage()));
    }

    /**
     * Signs out the current FirebaseAuth user.
     */
    public void signOut() {
        auth.signOut();
    }
}