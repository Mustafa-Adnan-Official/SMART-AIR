package com.example.smartair.ui.meds;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.models.childcollections.MedLog;
import com.example.smartair.services.AchievementService;
import com.example.smartair.services.AuthService;
import com.example.smartair.services.InventoryService;
import com.example.smartair.services.MedLogService;
import com.example.smartair.ui.technique.TechniqueTrainerActivity;

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
        setContentView(R.layout.medicine_dialog_controller_session);

        // Get childUid from AuthService
        AuthService authService = new AuthService();
        childUid = authService.getCurrentUserUid();
        if (childUid == null || childUid.trim().isEmpty()) {
            Toast.makeText(this, "Missing childUid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

        clearBeforeFeelingSelection();
        clearAfterFeelingSelection();
    }

    private void setupListeners() {
        // Before feeling
        btnGood.setOnClickListener(v -> {
            feelingBefore = "good";
            setBeforeFeelingSelection(btnGood);
        });

        btnOkay.setOnClickListener(v -> {
            feelingBefore = "okay";
            setBeforeFeelingSelection(btnOkay);
        });

        btnBad.setOnClickListener(v -> {
            feelingBefore = "bad";
            setBeforeFeelingSelection(btnBad);
        });

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
            techniqueTrainerUsed = true;
            Intent i = new Intent(
                    ControllerSessionActivity.this,
                    TechniqueTrainerActivity.class
            );
            i.putExtra("childUid", childUid);
            startActivity(i);
        });

        // After feeling
        btnNowBetter.setOnClickListener(v -> {
            feelingAfter = "better";
            setAfterFeelingSelection(btnNowBetter);
        });

        btnNowSame.setOnClickListener(v -> {
            feelingAfter = "same";
            setAfterFeelingSelection(btnNowSame);
        });

        btnNowWorse.setOnClickListener(v -> {
            feelingAfter = "worse";
            setAfterFeelingSelection(btnNowWorse);
        });

        // Log session?
        btnLogYes.setOnClickListener(v -> onLogSessionClicked());
        btnLogNo.setOnClickListener(v -> finish());
    }

    // --- Selection helpers for "before" buttons ---

    private void clearBeforeFeelingSelection() {
        btnGood.setSelected(false);
        btnOkay.setSelected(false);
        btnBad.setSelected(false);
    }

    private void setBeforeFeelingSelection(Button selected) {
        btnGood.setSelected(selected == btnGood);
        btnOkay.setSelected(selected == btnOkay);
        btnBad.setSelected(selected == btnBad);
    }

    // --- Selection helpers for "after" buttons ---

    private void clearAfterFeelingSelection() {
        btnNowBetter.setSelected(false);
        btnNowSame.setSelected(false);
        btnNowWorse.setSelected(false);
    }

    private void setAfterFeelingSelection(Button selected) {
        btnNowBetter.setSelected(selected == btnNowBetter);
        btnNowSame.setSelected(selected == btnNowSame);
        btnNowWorse.setSelected(selected == btnNowWorse);
    }

    private void onLogSessionClicked() {
        if (feelingBefore == null || feelingAfter == null) {
            Toast.makeText(this, "Please select how you felt before and after.", Toast.LENGTH_SHORT).show();
            return;
        }

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
        Toast.makeText(this,
                message != null ? message : "Something went wrong",
                Toast.LENGTH_SHORT).show();
    }
}