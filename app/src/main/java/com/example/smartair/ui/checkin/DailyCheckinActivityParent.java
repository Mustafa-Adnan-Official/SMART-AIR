package com.example.smartair.ui.checkin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.callbacks.ResultCallback;
import com.example.smartair.callbacks.SimpleResultCallback;
import com.example.smartair.models.childcollections.Checkin;
import com.example.smartair.models.childcollections.EntryAuthor;
import com.example.smartair.models.users.Child;
import com.example.smartair.repositories.ChildRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.example.smartair.services.CheckinService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyCheckinActivityParent extends AppCompatActivity {

    private Button btnFeelingGreat, btnFeelingOkay, btnFeelingBad;
    private Button btnSymptomNight;
    private Button btnSymptomCough;
    private Button btnSymptomActivity;
    private Button btnSymptomBreath;
    private Button btnTriggerCold;
    private Button btnTriggerDust;
    private Button btnTriggerExercise;
    private Button btnTriggerPets;
    private Button btnTriggerIllness;
    private Button btnTriggerSmoke;
    private TextInputEditText etPeakFlow, etOther;
    private Button btnSave;
    
    private AutoCompleteTextView childSelectionDropdown;

    private String selectedFeeling = "Great"; // Default
    private List<String> selectedSymptoms = new ArrayList<>();
    private List<String> selectedTriggers = new ArrayList<>();

    private String userRole;
    private String name;
    
    private String selectedChildUid;
    private Map<String, String> childNameToUidMap = new HashMap<>();

    private ChildRepository childRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_daily_checkin); // Assuming this matches the layout file name

        childRepository = new ChildRepository();

        // Initialize Views
        childSelectionDropdown = findViewById(R.id.child_selection_autocomplete);
        
        btnFeelingGreat = findViewById(R.id.checkin_feeling_great_button);
        btnFeelingOkay = findViewById(R.id.checkin_feeling_okay_button);
        btnFeelingBad = findViewById(R.id.checkin_feeling_bad_button);

        btnSymptomNight = findViewById(R.id.symptom_night_waking_button);
        btnSymptomCough = findViewById(R.id.symptom_cough_wheeze_button);
        btnSymptomActivity = findViewById(R.id.symptom_activity_difficulty_button);
        btnSymptomBreath = findViewById(R.id.symptom_shortness_breath_button);

        btnTriggerCold = findViewById(R.id.trigger_cold_air_button);
        btnTriggerDust = findViewById(R.id.trigger_dust_button);
        btnTriggerExercise = findViewById(R.id.trigger_exercise_button);
        btnTriggerPets = findViewById(R.id.trigger_pets_button);
        btnTriggerIllness = findViewById(R.id.trigger_illness_button);
        btnTriggerSmoke = findViewById(R.id.trigger_smoke_button);

        etPeakFlow = findViewById(R.id.peakflow_edit_text);
        etOther = findViewById(R.id.other_edit_text);
        btnSave = findViewById(R.id.checkin_save_button);

        userRole = getIntent().getStringExtra("USER_ROLE");
        name = getIntent().getStringExtra("USER_NAME");

        CheckinService checkinService = new CheckinService();
        
        // Setup Child Dropdown
        setupChildDropdown();

        // Feeling Logic (Single Selection)
        View.OnClickListener feelingListener = v -> {
            // Reset all
            btnFeelingGreat.setAlpha(0.5f);
            btnFeelingOkay.setAlpha(0.5f);
            btnFeelingBad.setAlpha(0.5f);

            // Select clicked
            v.setAlpha(1.0f);

            if (v.getId() == R.id.checkin_feeling_great_button) selectedFeeling = "Great";
            else if (v.getId() == R.id.checkin_feeling_okay_button) selectedFeeling = "Okay";
            else if (v.getId() == R.id.checkin_feeling_bad_button) selectedFeeling = "Bad";
        };

        btnFeelingGreat.setOnClickListener(feelingListener);
        btnFeelingOkay.setOnClickListener(feelingListener);
        btnFeelingBad.setOnClickListener(feelingListener);

        // Default selection visual
        btnFeelingGreat.setAlpha(1.0f);
        btnFeelingOkay.setAlpha(0.5f);
        btnFeelingBad.setAlpha(0.5f);

        // Symptoms Logic (Multi Selection)
        setupMultiSelectButton(btnSymptomNight, "Night Waking", selectedSymptoms);
        setupMultiSelectButton(btnSymptomCough, "Cough/Wheeze", selectedSymptoms);
        setupMultiSelectButton(btnSymptomActivity, "Activity Difficulty", selectedSymptoms);
        setupMultiSelectButton(btnSymptomBreath, "Shortness of Breath", selectedSymptoms);

        // Triggers Logic (Multi Selection)
        setupMultiSelectButton(btnTriggerCold, "Cold Air", selectedTriggers);
        setupMultiSelectButton(btnTriggerDust, "Dust", selectedTriggers);
        setupMultiSelectButton(btnTriggerExercise, "Exercise", selectedTriggers);
        setupMultiSelectButton(btnTriggerPets, "Pets", selectedTriggers);
        setupMultiSelectButton(btnTriggerIllness, "Illness", selectedTriggers);
        setupMultiSelectButton(btnTriggerSmoke, "Smoke", selectedTriggers);

        // Save Button
        btnSave.setOnClickListener(v -> saveCheckin(checkinService));
    }
    
    private void setupChildDropdown() {
        String parentUid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (parentUid != null) {
            childRepository.getChildrenForParent(parentUid, new ResultCallback<List<Child>>() {
                @Override
                public void onSuccess(List<Child> children) {
                    List<String> childNames = new ArrayList<>();
                    childNameToUidMap.clear();
                    
                    for (Child child : children) {
                        childNames.add(child.getName());
                        childNameToUidMap.put(child.getName(), child.getChildUid());
                    }
                    
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(DailyCheckinActivityParent.this, android.R.layout.simple_dropdown_item_1line, childNames);
                    childSelectionDropdown.setAdapter(adapter);
                    
                    childSelectionDropdown.setOnItemClickListener((parent, view, position, id) -> {
                        String selectedName = adapter.getItem(position);
                        selectedChildUid = childNameToUidMap.get(selectedName);
                        // Toast.makeText(DailyCheckinActivityParent.this, "Selected: " + selectedName, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(DailyCheckinActivityParent.this, "Failed to load children: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupMultiSelectButton(Button btn, String value, List<String> list) {
        btn.setSelected(false);
        btn.setAlpha(0.6f);

        btn.setOnClickListener(v -> {
            if (btn.isSelected()) {
                btn.setSelected(false);
                btn.setAlpha(0.6f);
                list.remove(value);
            } else {
                btn.setSelected(true);
                btn.setAlpha(1.0f);
                list.add(value);
            }
        });
    }

    private void saveCheckin(CheckinService checkInService) {
        if (selectedChildUid == null) {
            Toast.makeText(this, "Please select a child first", Toast.LENGTH_SHORT).show();
            return;
        }

        String peakFlowStr = etPeakFlow.getText().toString();
        String otherStr = etOther.getText().toString();

        // Use Parent's name (fetched from intent) as Author
        EntryAuthor entryAuthor = new EntryAuthor(name, userRole);

        int peakFlow = 0;
        if (!peakFlowStr.isEmpty()) {
            try {
                peakFlow = Integer.parseInt(peakFlowStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid Peak Flow value", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Checkin checkin = new Checkin(Timestamp.now(), selectedFeeling, new ArrayList<>(selectedSymptoms), new ArrayList<>(selectedTriggers), peakFlow, entryAuthor);
        
        checkInService.addCheckin(selectedChildUid, checkin, new SimpleResultCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(DailyCheckinActivityParent.this, "Check-in Saved!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(DailyCheckinActivityParent.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
