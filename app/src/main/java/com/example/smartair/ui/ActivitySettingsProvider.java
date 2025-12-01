package com.example.smartair.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.models.users.Child;
import com.example.smartair.models.users.Parent;
import com.example.smartair.models.users.Provider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        setContentView(R.layout.activity_settings_provider);

        providerRecyclerView = findViewById(R.id.providers_recycler_view);
        providerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadProvidersData();
        setupRecyclerView();
    }

    private void loadProvidersData() {


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String parentUid = currentUser.getUid();
            // Note: Implementation would involve fetching the Parent document, 
            // then fetching associated Provider documents, and finally populating 
            // those Provider objects with the Parent's Children list to allow access toggling.
            
            // Example structure (pseudo-code):
            // parentRepository.getParent(parentUid, parent -> {
            //      this.currentParent = parent;
            //      childRepository.getChildrenForParent(parentUid, children -> {
            //           // fetch providers...
            //           // for each provider, provider.setChildren(children);
            //           // update list and notify adapter
            //      });
            // });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

    }

    private void setupRecyclerView() {
        if (currentParent == null) {
            // Fallback or loading state
            currentParent = new Parent();
            currentParent.setParentUid("mock_parent_uid");
        }
        providerAdapter = new ProvidersAdapter(this, providerList, currentParent);
        providerRecyclerView.setAdapter(providerAdapter);
    }
}
