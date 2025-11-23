package com.example.smartair;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.smartair.callbacks.AuthResultCallback;
import com.example.smartair.services.AuthService;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class AuthServiceInstrumentedTest {

    private AuthService authService;
    private FirebaseFirestore db;

    // CHANGED THESE TWO TO MATCH AN EXISTING PARENT IN FIRESTORE MANUALLY FOR TESTING
    private static final String EXISTING_PARENT_PAC   = "PA-7FQ2-91";
    private static final String EXISTING_PARENT_EMAIL = "devparent+1763758724962@test.com";
    private static final String EXISTING_PARENT_PASSWORD  = "Test123!"; // whatever that parent’s real password is

    @Before
    public void setup() {
        Context ctx = androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        FirebaseApp.initializeApp(ctx);
        authService = new AuthService();
        db = FirebaseFirestore.getInstance();
    }

    // 1) PARENT SIGNUP DEMO
    @Test
    public void registerParent_createsAuthUserAndParentDoc() throws Exception {
        String unique = String.valueOf(System.currentTimeMillis());
        String email = "devparent+" + unique + "@test.com";
        String password = "Test123!";

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> uidRef = new AtomicReference<>(null);
        AtomicReference<String> errorRef = new AtomicReference<>(null);

        authService.registerParent(
                "JUnit Parent",
                email,
                password,
                new AuthResultCallback() {
                    @Override
                    public void onSuccess(String uid) {
                        uidRef.set(uid);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        errorRef.set(errorMessage);
                        latch.countDown();
                    }
                }
        );

        assertTrue("Callback never called", latch.await(20, TimeUnit.SECONDS));
        assertNull("registerParent failed: " + errorRef.get(), errorRef.get());

        String uid = uidRef.get();
        assertNotNull("Parent UID is null", uid);

        Task<DocumentSnapshot> task =
                db.collection("parents").document(uid).get();
        DocumentSnapshot snap = Tasks.await(task, 15, TimeUnit.SECONDS);

        assertTrue("Parent document does not exist", snap.exists());
        assertEquals("parent", snap.getString("role"));
        assertEquals(email, snap.getString("email"));
        assertEquals("JUnit Parent", snap.getString("name"));
    }

    // 2) PROVIDER SIGNUP DEMO
    @Test
    public void registerProvider_createsAuthUserAndProviderDoc() throws Exception {
        String unique = String.valueOf(System.currentTimeMillis());
        String email = "devprovider+" + unique + "@test.com";
        String password = "Test123!";

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> uidRef = new AtomicReference<>(null);
        AtomicReference<String> errorRef = new AtomicReference<>(null);

        authService.registerProvider(
                "Dr.",
                "JUnit Provider",
                email,
                password,
                new AuthResultCallback() {
                    @Override
                    public void onSuccess(String uid) {
                        uidRef.set(uid);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        errorRef.set(errorMessage);
                        latch.countDown();
                    }
                }
        );

        assertTrue("Callback never called", latch.await(20, TimeUnit.SECONDS));
        assertNull("registerProvider failed: " + errorRef.get(), errorRef.get());

        String uid = uidRef.get();
        assertNotNull("Provider UID is null", uid);

        Task<DocumentSnapshot> task =
                db.collection("providers").document(uid).get();
        DocumentSnapshot snap = Tasks.await(task, 15, TimeUnit.SECONDS);

        assertTrue("Provider document does not exist", snap.exists());
        assertEquals("provider", snap.getString("role"));
        assertEquals(email, snap.getString("email"));
        assertEquals("JUnit Provider", snap.getString("name"));
    }

    // 3) INDEPENDENT CHILD SIGNUP DEMO
    @Test
    public void registerChildIndependent_createsAuthUserAndChildDoc() throws Exception {
        String unique = String.valueOf(System.currentTimeMillis());
        String email = "devchild+" + unique + "@test.com";
        String password = "Test123!";

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> uidRef = new AtomicReference<>(null);
        AtomicReference<String> errorRef = new AtomicReference<>(null);

        authService.registerChildIndependent(
                "JUnit Child Indep",
                email,
                password,
                new AuthResultCallback() {
                    @Override
                    public void onSuccess(String uid) {
                        uidRef.set(uid);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        errorRef.set(errorMessage);
                        latch.countDown();
                    }
                }
        );

        assertTrue("Callback never called", latch.await(20, TimeUnit.SECONDS));
        assertNull("registerChildIndependent failed: " + errorRef.get(), errorRef.get());

        String uid = uidRef.get();
        assertNotNull("Child UID is null", uid);

        Task<DocumentSnapshot> task =
                db.collection("children").document(uid).get();
        DocumentSnapshot snap = Tasks.await(task, 15, TimeUnit.SECONDS);

        assertTrue("Child document does not exist", snap.exists());
        assertEquals("child", snap.getString("role"));
        assertEquals(email, snap.getString("email"));
        assertEquals(true, snap.getBoolean("hasOwnEmail"));
    }

    // 4) CHILD UNDER EXISTING PARENT (PAC) DEMO
    @Test
    public void registerChildUnderParent_createsChildLinkedToExistingParent() throws Exception {

        // Added this — REQUIRED for Firestore permissions
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Task<AuthResult> signInTask =
                auth.signInWithEmailAndPassword(EXISTING_PARENT_EMAIL, EXISTING_PARENT_PASSWORD);
        Tasks.await(signInTask, 15, TimeUnit.SECONDS);
        assertNotNull("Parent sign-in failed", signInTask.getResult());

        // Now your parent lookup is allowed
        Task<QuerySnapshot> parentTask =
                db.collection("parents")
                        .whereEqualTo("parentAccessCode", EXISTING_PARENT_PAC)
                        .limit(1)
                        .get();

        QuerySnapshot parentQuery = Tasks.await(parentTask, 10, TimeUnit.SECONDS);
        assertFalse(
                "No parent found with PAC = " + EXISTING_PARENT_PAC +
                        ". Set EXISTING_PARENT_PAC to match Firestore.",
                parentQuery.isEmpty()
        );

        DocumentSnapshot parentDoc = parentQuery.getDocuments().get(0);
        String parentUid = parentDoc.getString("parentUid");
        String parentEmailFromDoc = parentDoc.getString("email");
        assertNotNull("parentUid field missing in parent doc", parentUid);

        assertEquals("Parent email mismatch", EXISTING_PARENT_EMAIL, parentEmailFromDoc);

        String childName = "JUnit Child (PAC) " + System.currentTimeMillis();
        String password = "Test123!";

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> childUidRef = new AtomicReference<>(null);
        AtomicReference<String> errorRef = new AtomicReference<>(null);

        authService.registerChildUnderParent(
                childName,
                password,
                EXISTING_PARENT_PAC,
                new AuthResultCallback() {
                    @Override
                    public void onSuccess(String childUid) {
                        childUidRef.set(childUid);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        errorRef.set(errorMessage);
                        latch.countDown();
                    }
                }
        );

        assertTrue("Callback never called", latch.await(20, TimeUnit.SECONDS));
        assertNull("registerChildUnderParent failed: " + errorRef.get(), errorRef.get());

        String childUid = childUidRef.get();
        assertNotNull("Child UID is null", childUid);

        Task<DocumentSnapshot> childTask =
                db.collection("children").document(childUid).get();
        DocumentSnapshot childSnap = Tasks.await(childTask, 10, TimeUnit.SECONDS);

        assertTrue("Child doc missing", childSnap.exists());
        assertEquals(childName, childSnap.getString("name"));
        assertEquals("child", childSnap.getString("role"));
        assertEquals(false, childSnap.getBoolean("hasOwnEmail"));
        assertEquals(EXISTING_PARENT_PAC, childSnap.getString("parentAccessCode"));
        assertEquals(parentUid, childSnap.getString("parentUid"));
    }
}