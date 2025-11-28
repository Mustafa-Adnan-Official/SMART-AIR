package com.example.smartair.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.callbacks.ResultCallback;
import com.example.smartair.models.Child;
import com.example.smartair.repositories.ChildRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ActivitySettingsChild extends AppCompatActivity {

    private RecyclerView childrenRecyclerView;
    private ChildrenAdapter childrenAdapter;

    private List<Child> childList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_child);

        childrenRecyclerView = findViewById(R.id.children_recycler_view);

        setupChildrenRecyclerView();
        loadChildrenData();
    }

    private void loadChildrenData() {

        childList.clear();

        Child child1 = new Child();
        child1.setName("Test Child 1");
        child1.setPersonalBest(150);
        child1.setDailyControllerUses(5); // Assuming setter name from previous context
        child1.setChildUid("test_uid_1");
        childList.add(child1);

        Child child2 = new Child();
        child2.setName("Test Child 2");
        child2.setPersonalBest(300);
        child2.setDailyControllerUses(2);
        child2.setChildUid("test_uid_2");
        childList.add(child2);

        if (childrenAdapter != null) {
            childrenAdapter.notifyDataSetChanged();
        }
        // --- TEMPORARY TEST DATA END ---

        /* REAL DATA LOADING (Commented out for UI testing)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String parentUid = currentUser.getUid();
            ChildRepository childRepository = new ChildRepository();
            childRepository.getChildrenForParent(parentUid, new ResultCallback<List<Child>>() {
                @Override
                public void onSuccess(List<Child> result) {
                    childList.clear();
                    childList.addAll(result);
                    if (childrenAdapter != null) {
                        childrenAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(ActivitySettingsChild.this, "Error loading children: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
        */
    }

    private void setupChildrenRecyclerView() {
        childrenAdapter = new ChildrenAdapter(childList);
        childrenRecyclerView.setAdapter(childrenAdapter);
    }
}
