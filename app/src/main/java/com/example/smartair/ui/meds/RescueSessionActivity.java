package com.example.smartair.ui.meds;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.models.childcollections.MedLog;
import com.example.smartair.services.AchievementService;
import com.example.smartair.services.AuthService;
import com.example.smartair.services.InventoryService;
import com.example.smartair.services.MedLogService;

import java.util.Collections;
import java.util.List;

/**
 * R3: Child logs a RESCUE dose.
 * Uses ChildMedLogPresenter which talks to MedLogService + InventoryService + AchievementService.
 */
public class RescueSessionActivity extends AppCompatActivity
        implements ChildMedLogPresenter.View {

    // UI
    private Button btnGood, btnOkay, btnBad;
    private Button btnDoseValue, btnDosePlus, btnDoseMinus;
    private Button btnNowBetter, btnNowSame, btnNowWorse;
    private Button btnSaveRescue;
    private EditText inputPEF;

    // State
    private long doseCount = 1;
    @Nullable private String feelingBefore = null; // "good" | "okay" | "bad"
    @Nullable private String feelingAfter = null;  // "better" | "same" | "worse"

    private ChildMedLogPresenter presenter;
    private String childUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicine_dialog_rescue_session);

        // Get childUid from AuthService (current logged-in child)
        AuthService authService = new AuthService();
        childUid = authService.getCurrentUserUid();
        if (childUid == null || childUid.trim().isEmpty()) {
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

        btnDoseValue = findViewById(R.id.btnDoseValue);
        btnDosePlus = findViewById(R.id.btnDosePlus);
        btnDoseMinus = findViewById(R.id.btnDoseMinus);

        btnNowBetter = findViewById(R.id.btnNowBetter);
        btnNowSame = findViewById(R.id.btnNowSame);
        btnNowWorse = findViewById(R.id.btnNowWorse);

        inputPEF = findViewById(R.id.inputPEF);
        btnSaveRescue = findViewById(R.id.btnSaveRescue);

        // initial dose text
        btnDoseValue.setText(String.valueOf(doseCount));

        // ensure all feeling buttons start unselected
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

        // Dose count
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

        // Save / log rescue dose
        btnSaveRescue.setOnClickListener(v -> onSaveClicked());
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

    private void onSaveClicked() {
        if (feelingBefore == null || feelingAfter == null) {
            Toast.makeText(this, "Please select how you felt before and after.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse PEF if entered
        Long peakFlow = null;
        String pefText = inputPEF.getText().toString().trim();
        if (!TextUtils.isEmpty(pefText)) {
            try {
                peakFlow = Long.parseLong(pefText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "PEF must be a number.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // No symptoms on this screen yet; you can plug them in later
        List<String> symptoms = Collections.emptyList();

        presenter.logRescueDose(
                childUid,
                doseCount,
                peakFlow,
                symptoms,
                feelingBefore,
                feelingAfter,
                null  // breathRating – you can map your wording if needed
        );
    }

    //  ChildMedLogPresenter.View

    @Override
    public void showLoading() {
        btnSaveRescue.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        btnSaveRescue.setEnabled(true);
    }

    @Override
    public void showSuccess(MedLog log) {
        Toast.makeText(this, "Rescue dose logged ✅", Toast.LENGTH_SHORT).show();
        finish(); // close screen / go back to home
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message != null ? message : "Something went wrong", Toast.LENGTH_SHORT).show();
    }
}