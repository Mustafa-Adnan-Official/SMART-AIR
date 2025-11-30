package com.example.smartair.ui.r3.meds;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.models.childcollections.MedLog;
import com.example.smartair.services.AchievementService;
import com.example.smartair.services.InventoryService;
import com.example.smartair.services.MedLogService;

import java.util.Collections;
import java.util.List;

/**
 * R3: Child logs a CONTROLLER dose.
 * Uses ChildMedLogPresenter which talks to MedLogService + InventoryService + AchievementService.
 */
public class ControllerSessionActivity extends AppCompatActivity
        implements ChildMedLogPresenter.View {

    // UI
    private Button btnGood, btnOkay, btnBad;
    private Button btnDoseValue, btnDosePlus, btnDoseMinus;
    private Button btnNowBetter, btnNowSame, btnNowWorse;
    private Button btnOpenTrainer;
    private Button btnLogYes, btnLogNo;

    // State
    private long doseCount = 1;
    @Nullable private String feelingBefore = null; // "good" | "okay" | "bad"
    @Nullable private String feelingAfter = null;  // "better" | "same" | "worse"
    private boolean techniqueTrainerUsed = false;

    private ChildMedLogPresenter presenter;
    private String childUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller_session);

        // Get childUid from Intent
        childUid = getIntent().getStringExtra("childUid");
        if (childUid == null) {
            Toast.makeText(this, "Missing childUid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Init presenter & services
        presenter = new ChildMedLogPresenter(
                this,
                new MedLogService(),
                new InventoryService(),
                new AchievementService()
        );

        bindViews();
        setupListeners();
    }

    private void bindViews() {
        btnGood = findViewById(R.id.btnGood);
        btnOkay = findViewById(R.id.btnOkay);
        btnBad = findViewById(R.id.btnBad);

        btnDoseValue = findViewById(R.id.btn_dose_value);
        btnDosePlus = findViewById(R.id.btn_dose_plus);
        btnDoseMinus = findViewById(R.id.btn_dose_minus);

        btnOpenTrainer = findViewById(R.id.btn_open_trainer);

        btnNowBetter = findViewById(R.id.btnNowBetter);
        btnNowSame = findViewById(R.id.btnNowSame);
        btnNowWorse = findViewById(R.id.btnNowWorse);

        btnLogYes = findViewById(R.id.btn_log_yes);
        btnLogNo = findViewById(R.id.btn_log_no);

        btnDoseValue.setText(String.valueOf(doseCount));
    }

    private void setupListeners() {
        // Before feeling
        btnGood.setOnClickListener(v -> feelingBefore = "good");
        btnOkay.setOnClickListener(v -> feelingBefore = "okay");
        btnBad.setOnClickListener(v -> feelingBefore = "bad");

        // Dose counter
        btnDosePlus.setOnClickListener(v -> {
            doseCount++;
            btnDoseValue.setText(String.valueOf(doseCount));
        });

        btnDoseMinus.setOnClickListener(v -> {
            if (doseCount > 1) {
                doseCount--;
                btnDoseValue.setText(String.valueOf(doseCount));
            }
        });

        // Technique trainer button
        btnOpenTrainer.setOnClickListener(v -> {
            techniqueTrainerUsed = true;  // mark that the trainer was viewed

            Intent i = new Intent(this, com.example.smartair.ui.r3.technique.TechniqueTrainerActivity.class);
            startActivity(i);
        });


        // After feeling
        btnNowBetter.setOnClickListener(v -> feelingAfter = "better");
        btnNowSame.setOnClickListener(v -> feelingAfter = "same");
        btnNowWorse.setOnClickListener(v -> feelingAfter = "worse");

        // Log session?
        btnLogYes.setOnClickListener(v -> onLogSessionClicked());
        btnLogNo.setOnClickListener(v -> finish()); // just close screen
    }

    private void onLogSessionClicked() {
        if (feelingBefore == null || feelingAfter == null) {
            Toast.makeText(this, "Please select how you felt before and after.", Toast.LENGTH_SHORT).show();
            return;
        }

        // No PEF/symptoms on this screen yet; can extend later
        Long peakFlow = null;
        List<String> symptoms = Collections.emptyList();

        presenter.logControllerDose(
                childUid,
                doseCount,
                techniqueTrainerUsed,
                peakFlow,
                symptoms,
                feelingBefore,
                feelingAfter,
                null  // breathRating placeholder
        );
    }

    // ------------ ChildMedLogPresenter.View ------------

    @Override
    public void showLoading() {
        btnLogYes.setEnabled(false);
        btnLogNo.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        btnLogYes.setEnabled(true);
        btnLogNo.setEnabled(true);
    }

    @Override
    public void showSuccess(MedLog log) {
        Toast.makeText(this, "Controller dose logged ✅", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message != null ? message : "Something went wrong", Toast.LENGTH_SHORT).show();
    }
}
