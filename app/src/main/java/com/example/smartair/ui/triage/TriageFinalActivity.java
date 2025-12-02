package com.example.smartair.ui.triage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.services.TriageService;
import com.example.smartair.ui.HomeActivities.ChildHomeActivity;

import java.util.ArrayList;

public class TriageFinalActivity extends AppCompatActivity {

    private Button btnGood;
    private Button btnOkay;
    private Button btnBad;

    private Button btnNowBetter;
    private Button btnNowSame;
    private Button btnNowWorse;

    private Button buttonDone;

    private String beforeFeeling = null;
    private String nowFeeling = null;

    private int currentPef = 0;
    private int rescueCount = 0;
    private int controllerCount = 0;
    private ArrayList<String> symptomsCombined = new ArrayList<>();

    private final TriageService triageService = new TriageService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.triage_dialog_final);

        bindViews();
        initFromIntent();
        setupClickListeners();
    }

    private void bindViews() {
        btnGood = findViewById(R.id.btnGood);
        btnOkay = findViewById(R.id.btnOkay);
        btnBad = findViewById(R.id.btnBad);

        btnNowBetter = findViewById(R.id.btnNowBetter);
        btnNowSame = findViewById(R.id.btnNowSame);
        btnNowWorse = findViewById(R.id.btnNowWorse);

        buttonDone = findViewById(R.id.buttonDone);
    }

    private void initFromIntent() {
        currentPef = getIntent().getIntExtra("currentPef", 0);
        rescueCount = getIntent().getIntExtra("rescueCount", 0);
        controllerCount = getIntent().getIntExtra("controllerCount", 0);

        ArrayList<String> incomingSymptoms =
                getIntent().getStringArrayListExtra("symptomsCombined");
        if (incomingSymptoms != null) {
            symptomsCombined = incomingSymptoms;
        } else {
            symptomsCombined = new ArrayList<>();
        }
    }

    private void setupClickListeners() {

        // BEFORE feelings
        btnGood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beforeFeeling = "good";
                updateBeforeButtons();
            }
        });

        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beforeFeeling = "okay";
                updateBeforeButtons();
            }
        });

        btnBad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beforeFeeling = "bad";
                updateBeforeButtons();
            }
        });

        // NOW feelings
        btnNowBetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFeeling = "better";
                updateNowButtons();
            }
        });

        btnNowSame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFeeling = "same";
                updateNowButtons();
            }
        });

        btnNowWorse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFeeling = "worse";
                updateNowButtons();
            }
        });

        // DONE → write incident + medLogs (non-emergency) and go home
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // If user leaves them empty, give safe defaults
                if (beforeFeeling == null) {
                    beforeFeeling = "okay";
                }
                if (nowFeeling == null) {
                    nowFeeling = "same";
                }

                triageService.recordTriageResult(
                        beforeFeeling,
                        nowFeeling,
                        rescueCount,
                        controllerCount,
                        (currentPef > 0 ? currentPef : null),
                        false,
                        symptomsCombined
                );

                // Navigate back to child home, similar to emergency flow
                Intent intent = new Intent(
                        TriageFinalActivity.this,
                        ChildHomeActivity.class
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void updateBeforeButtons() {
        btnGood.setSelected("good".equals(beforeFeeling));
        btnOkay.setSelected("okay".equals(beforeFeeling));
        btnBad.setSelected("bad".equals(beforeFeeling));
    }

    private void updateNowButtons() {
        btnNowBetter.setSelected("better".equals(nowFeeling));
        btnNowSame.setSelected("same".equals(nowFeeling));
        btnNowWorse.setSelected("worse".equals(nowFeeling));
    }
}