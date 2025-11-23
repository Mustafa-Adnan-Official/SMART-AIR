package com.example.smartair.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;

import java.util.ArrayList;
import java.util.List;

public class ActivitySettingsChild extends AppCompatActivity {

    private RecyclerView childrenRecyclerView;
    private ChildrenAdapter childrenAdapter;

    // --- Placeholder Child Data/Model (Move to com.example.smartair.models) ---
    private List<Child> childList = new ArrayList<>();

    // Placeholder Child class definition
    // NOTE: This placeholder class is kept for the mock data generation
    public static class Child {
        private String name;
        private int pbSetting;
        private int controllerUses;

        public Child(String name, int pbSetting, int controllerUses) {
            this.name = name;
            this.pbSetting = pbSetting;
            this.controllerUses = controllerUses;
        }

        public String getName() { return name; }
        public int getPbSetting() { return pbSetting; }
        public int getControllerUses() { return controllerUses; }
        public void setPbSetting(int pbSetting) { this.pbSetting = pbSetting; }
        public void setDailyControllerUses(int controllerUses) { this.controllerUses = controllerUses; }
    }
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the new, dedicated child layout
        setContentView(R.layout.activity_settings_child);

        // 1. Initialize Views
        // Note: The 'add_new_child_button' is now removed from the layout and code.
        childrenRecyclerView = findViewById(R.id.children_recycler_view);

        // 2. Load Data (Mock Data)
        loadMockData();

        // 3. Configure RecyclerView
        setupChildrenRecyclerView();

        // 4. Removed the button listener as per your instruction.
    }

    private void loadMockData() {
        // Mock Children data (existing children for management)
        childList.add(new Child("Sarah", 350, 2));
        childList.add(new Child("David", 400, 1));
        // You would replace this with actual database loading logic (e.g., from Firebase)
    }

    private void setupChildrenRecyclerView() {
        childrenAdapter = new ChildrenAdapter(childList);
        childrenRecyclerView.setAdapter(childrenAdapter);
    }

    // The showAddChildDialog() method was removed since this activity is for management only.
}