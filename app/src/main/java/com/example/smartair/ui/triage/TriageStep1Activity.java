package com.example.smartair.ui.triage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.services.TriageService;

import java.util.ArrayList;

/**
 * TRIAGE STEP 1
 *
 * Asks:
 *  - number of rescue attempts today (UI requirement only; NOT written to Firestore)
 *  - optional current PEF
 *  - serious symptoms checkboxes
 *
 * If any serious symptom is checked:
 *  - logs an escalated incident with feelingBefore = "Bad", feelingAfter = "Worse"
 *  - sends child directly to emergency card
 *
 * Otherwise:
 *  - goes to Step 2 and passes:
 *      initialRescueAttempts (for UI only)
 *      currentPef (int, maybe 0)
 *      symptomsStep1 (ArrayList<String>)
 */
public class TriageStep1Activity extends AppCompatActivity {

    private TriageService triageService;

    private EditText editRescueAttempts;
    private EditText editCurrentPef;

    private CheckBox checkboxCantSpeak;
    private CheckBox checkboxChestPulling;
    private CheckBox checkboxBlueLips;

    private Button buttonBack;
    private Button buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.triage_dialog_step1);

        triageService = new TriageService();
        bindViews();
        setupClickListeners();
    }

    private void bindViews() {
        editRescueAttempts = findViewById(R.id.editRescueAttempts);
        editCurrentPef = findViewById(R.id.editCurrentPef);

        checkboxCantSpeak = findViewById(R.id.checkboxCantSpeak);
        checkboxChestPulling = findViewById(R.id.checkboxChestPulling);
        checkboxBlueLips = findViewById(R.id.checkboxBlueLips);

        buttonBack = findViewById(R.id.buttonBackStep1);
        buttonNext = findViewById(R.id.buttonNextStep1);
    }

    private void setupClickListeners() {

        buttonBack.setOnClickListener(v -> finish());

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean hasSeriousSymptom =
                        checkboxCantSpeak.isChecked()
                                || checkboxChestPulling.isChecked()
                                || checkboxBlueLips.isChecked();

                // Project requirement only – not stored in Firestore
                String rescueStr = editRescueAttempts.getText().toString().trim();
                if (rescueStr.isEmpty()) {
                    Toast.makeText(
                            TriageStep1Activity.this,
                            "Please enter your rescue attempts for today.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                String pefStr = editCurrentPef.getText().toString().trim();
                int currentPef = pefStr.isEmpty() ? 0 : safeParseInt(pefStr);

                ArrayList<String> symptomsStep1 = buildSymptomsStep1();

                if (hasSeriousSymptom) {
                    // Convert int PEF to Long or null
                    Long peakFlowLong = (currentPef > 0) ? (long) currentPef : null;

                    triageService.logImmediateEmergencyFromStep1(
                            peakFlowLong,
                            symptomsStep1,
                            new TriageService.TriageCallback() {
                                @Override
                                public void onSuccess() {
                                    openEmergencyScreen();
                                }

                                @Override
                                public void onError(Exception e) {
                                    // Even if logging fails, still show emergency screen
                                    openEmergencyScreen();
                                }
                            }
                    );
                    return;
                }

                // Otherwise go to Step 2.
                Intent nextIntent = new Intent(
                        TriageStep1Activity.this,
                        TriageStep2Activity.class
                );

                // UI-only starting count, not used in Firestore
                int initialRescueAttempts = safeParseInt(rescueStr);
                nextIntent.putExtra("initialRescueAttempts", initialRescueAttempts);
                nextIntent.putExtra("currentPef", currentPef);
                nextIntent.putStringArrayListExtra("symptomsStep1", symptomsStep1);

                startActivity(nextIntent);
            }
        });
    }

    private void openEmergencyScreen() {
        Intent emergencyIntent = new Intent(
                TriageStep1Activity.this,
                TriageEmergencyActivity.class
        );
        emergencyIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
        );
        startActivity(emergencyIntent);
        finish();
    }

    private ArrayList<String> buildSymptomsStep1() {
        ArrayList<String> list = new ArrayList<>();
        if (checkboxCantSpeak.isChecked()) {
            list.add("cant_speak_full_sentences");
        }
        if (checkboxChestPulling.isChecked()) {
            list.add("chest_pulling_retractions");
        }
        if (checkboxBlueLips.isChecked()) {
            list.add("blue_grey_lips_or_nails");
        }
        return list;
    }

    private int safeParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}