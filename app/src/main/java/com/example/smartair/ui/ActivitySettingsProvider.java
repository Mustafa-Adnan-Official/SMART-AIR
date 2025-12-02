package com.example.smartair.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.callbacks.ResultCallback;
import com.example.smartair.models.users.Child;
import com.example.smartair.models.users.Parent;
import com.example.smartair.models.users.Provider;
import com.example.smartair.repositories.ChildRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ActivitySettingsProvider extends AppCompatActivity {

    private RecyclerView providerRecyclerView;
    private ProvidersAdapter providerAdapter;

    private List<Provider> providerList = new ArrayList<>();
    private Parent currentParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_activity_providers_for_parent);

        providerRecyclerView = findViewById(R.id.providersRecyclerView);
        providerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // loadProvidersData();
        loadMockData();
        // setupRecyclerView called after data load
    }

    private void loadMockData() {
        // Mock Parent
        currentParent = new Parent();
        currentParent.setParentUid("mockParentUid");
        currentParent.setName("Mock Parent");

        // Mock Children
        List<Child> mockChildren = new ArrayList<>();
        Child child1 = new Child();
        child1.setChildUid("child1");
        child1.setName("Alice");
        child1.setParentUid("mockParentUid");
        mockChildren.add(child1);

        Child child2 = new Child();
        child2.setChildUid("child2");
        child2.setName("Bob");
        child2.setParentUid("mockParentUid");
        mockChildren.add(child2);

        // Mock Providers
        providerList.clear();
        Provider provider1 = new Provider();
        provider1.setProviderUid("provider1");
        provider1.setName("Dr. Smith");
        provider1.setChildren(mockChildren);
        providerList.add(provider1);

        Provider provider2 = new Provider();
        provider2.setProviderUid("provider2");
        provider2.setName("Nurse Joy");
        provider2.setChildren(mockChildren);
        providerList.add(provider2);

        Toast.makeText(this, "Mock data loaded", Toast.LENGTH_SHORT).show();

        setupRecyclerView();
    }


    /*
    private void loadProvidersData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String parentUid = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            ChildRepository childRepository = new ChildRepository();

            // 1. Fetch Parent Data
            db.collection("parents").document(parentUid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentParent = documentSnapshot.toObject(Parent.class);
                    
                    // 2. Fetch Children
                    childRepository.getChildrenForParent(parentUid, new ResultCallback<List<Child>>() {
                        @Override
                        public void onSuccess(List<Child> children) {
                            
                            // 3. Fetch Providers
                            List<String> providerUIDs = currentParent.getProviderUIDs(); // Assuming getter exists (it did in Parent.java)
                            
                            if (providerUIDs == null || providerUIDs.isEmpty()) {
                                setupRecyclerView(); // No providers, just show empty or headers
                                return;
                            }

                            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                            for (String pUid : providerUIDs) {
                                tasks.add(db.collection("providers").document(pUid).get());
                            }

                            Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
                                providerList.clear();
                                for (Object obj : objects) {
                                    DocumentSnapshot snap = (DocumentSnapshot) obj;
                                    if (snap.exists()) {
                                        Provider provider = snap.toObject(Provider.class);
                                        if (provider != null) {
                                            // Populate provider with the Parent's children so the adapter can show toggles
                                            provider.setChildren(children);
                                            providerList.add(provider);
                                        }
                                    }
                                }
                                setupRecyclerView();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(ActivitySettingsProvider.this, "Error fetching providers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(ActivitySettingsProvider.this, "Error fetching children: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error fetching parent: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }


*/

    private void setupRecyclerView() {
        if (currentParent == null) {
            // Fallback if parent load failed but we reached here (unlikely)
            currentParent = new Parent(); 
        }
        providerAdapter = new ProvidersAdapter(this, providerList, currentParent);
        providerRecyclerView.setAdapter(providerAdapter);
    }
}
