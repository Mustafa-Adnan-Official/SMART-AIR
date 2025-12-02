package com.example.smartair.ui.triage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.services.TriageService;

import java.util.ArrayList;

public class TriageStep3Activity extends AppCompatActivity {

    private CheckBox checkboxCantSpeak2;
    private CheckBox checkboxChestPulling2;
    private CheckBox checkboxBlueLips2;

    private Button buttonYes;
    private Button buttonNo;

    private int currentPef = 0;
    private int rescueCount = 0;
    private int controllerCount = 0;
    private ArrayList<String> symptomsStep1 = new ArrayList<>();

    private final TriageService triageService = new TriageService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.triage_dialog_step3);

        bindViews();
        initFromIntent();
        setupClickListeners();
    }

    private void bindViews() {
        checkboxCantSpeak2 = findViewById(R.id.checkboxCantSpeak2);
        checkboxChestPulling2 = findViewById(R.id.checkboxChestPulling2);
        checkboxBlueLips2 = findViewById(R.id.checkboxBlueLips2);

        buttonYes = findViewById(R.id.buttonYes);
        buttonNo = findViewById(R.id.buttonNo);
    }

    private void initFromIntent() {
        Intent intent = getIntent();

        currentPef = intent.getIntExtra("currentPef", 0);
        rescueCount = intent.getIntExtra("rescueCount", 0);
        controllerCount = intent.getIntExtra("controllerCount", 0);

        ArrayList<String> incomingSymptoms =
                intent.getStringArrayListExtra("symptomsStep1");
        if (incomingSymptoms != null) {
            symptomsStep1 = incomingSymptoms;
        } else {
            symptomsStep1 = new ArrayList<>();
        }
    }

    private void setupClickListeners() {

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean serious =
                        checkboxCantSpeak2.isChecked()
                                || checkboxChestPulling2.isChecked()
                                || checkboxBlueLips2.isChecked();

                if (serious) {
                    // Emergency with Bad/Worse feelings
                    recordEmergencyAndGo();
                } else {
                    goToFinal();
                }
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Breathing not regulated → emergency
                recordEmergencyAndGo();
            }
        });
    }

    private ArrayList<String> buildCombinedSymptoms() {
        ArrayList<String> combined = new ArrayList<>(symptomsStep1);

        if (checkboxCantSpeak2.isChecked()) {
            combined.add("cant_speak_full_sentences");
        }
        if (checkboxChestPulling2.isChecked()) {
            combined.add("chest_pulling_retractions");
        }
        if (checkboxBlueLips2.isChecked()) {
            combined.add("blue_grey_lips_or_nails");
        }

        return combined;
    }

    private void recordEmergencyAndGo() {
        ArrayList<String> combinedSymptoms = buildCombinedSymptoms();

        // Requirement #6: feelingBefore = Bad, feelingAfter = Worse
        triageService.recordTriageResult(
                "Bad",
                "Worse",
                rescueCount,
                controllerCount,
                (currentPef > 0 ? currentPef : null),
                true,
                combinedSymptoms
        );

        goToEmergency();
    }

    private void goToEmergency() {
        Intent intent = new Intent(this, TriageEmergencyActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToFinal() {
        Intent intent = new Intent(this, TriageFinalActivity.class);
        intent.putExtra("currentPef", currentPef);
        intent.putExtra("rescueCount", rescueCount);
        intent.putExtra("controllerCount", controllerCount);
        intent.putStringArrayListExtra("symptomsCombined", buildCombinedSymptoms());
        startActivity(intent);
        finish();
    }
}
