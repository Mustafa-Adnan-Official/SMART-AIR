package com.example.smartair.ui.triage;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

import java.util.ArrayList;

public class TriageStep2Activity extends AppCompatActivity {

    private TextView textRescueCount;
    private Button buttonRescuePlus;
    private Button buttonRescueMinus;

    private TextView textControllerCount;
    private Button buttonControllerPlus;
    private Button buttonControllerMinus;

    private TextView textTimer;
    private Button buttonBack;
    private Button buttonNext;

    private int rescueCount = 0;
    private int controllerCount = 0;

    private int currentPef = 0;
    private ArrayList<String> symptomsStep1 = new ArrayList<>();

    private CountDownTimer countDownTimer;
    private static final long TEN_MINUTES_MS = 10 * 60 * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.triage_dialog_step2);

        bindViews();
        initFromIntent();
        setupClickListeners();
        startTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }

    private void bindViews() {
        textRescueCount = findViewById(R.id.textRescueCount);
        buttonRescuePlus = findViewById(R.id.buttonRescuePlus);
        buttonRescueMinus = findViewById(R.id.buttonRescueMinus);

        textControllerCount = findViewById(R.id.textControllerCount);
        buttonControllerPlus = findViewById(R.id.buttonControllerPlus);
        buttonControllerMinus = findViewById(R.id.buttonControllerMinus);

        textTimer = findViewById(R.id.textTimer);
        buttonBack = findViewById(R.id.buttonBackStep2);
        buttonNext = findViewById(R.id.buttonNextStep2);
    }

    private void initFromIntent() {
        Intent intent = getIntent();

        // Project requirement only; not used for Firestore
        int initialRescueAttempts = intent.getIntExtra("initialRescueAttempts", 0);
        // We purposely ignore initialRescueAttempts as starting value (requirement #3).

        currentPef = intent.getIntExtra("currentPef", 0);

        ArrayList<String> incomingSymptoms =
                intent.getStringArrayListExtra("symptomsStep1");
        if (incomingSymptoms != null) {
            symptomsStep1 = incomingSymptoms;
        } else {
            symptomsStep1 = new ArrayList<>();
        }

        // Start counters at 0 on Step 2
        rescueCount = 0;
        controllerCount = 0;

        updateRescueText();
        updateControllerText();
    }

    private void setupClickListeners() {

        buttonRescuePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rescueCount++;
                updateRescueText();
            }
        });

        buttonRescueMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rescueCount > 0) {
                    rescueCount--;
                    updateRescueText();
                }
            }
        });

        buttonControllerPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controllerCount++;
                updateControllerText();
            }
        });

        buttonControllerMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controllerCount > 0) {
                    controllerCount--;
                    updateControllerText();
                }
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTimer();
                goToStep3();
            }
        });
    }

    private void updateRescueText() {
        textRescueCount.setText(String.valueOf(rescueCount));
    }

    private void updateControllerText() {
        textControllerCount.setText(String.valueOf(controllerCount));
    }

    private void startTimer() {
        cancelTimer();

        countDownTimer = new CountDownTimer(TEN_MINUTES_MS, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long totalSeconds = millisUntilFinished / 1000L;
                long minutes = totalSeconds / 60L;
                long seconds = totalSeconds % 60L;
                String text = String.format("%d:%02d", minutes, seconds);
                textTimer.setText(text);
            }

            @Override
            public void onFinish() {
                textTimer.setText("0:00");
                goToStep3();
            }
        };
        countDownTimer.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void goToStep3() {
        Intent intent = new Intent(this, TriageStep3Activity.class);
        intent.putExtra("currentPef", currentPef);
        intent.putExtra("rescueCount", rescueCount);
        intent.putExtra("controllerCount", controllerCount);
        intent.putStringArrayListExtra("symptomsStep1", symptomsStep1);
        startActivity(intent);
    }
}
