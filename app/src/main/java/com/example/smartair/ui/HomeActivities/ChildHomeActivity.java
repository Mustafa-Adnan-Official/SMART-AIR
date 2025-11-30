package com.example.smartair.ui.HomeActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

public class ChildHomeActivity extends AppCompatActivity {

    private ImageButton btnSettings;
    private ImageButton btnProfilePicture;

    private Button btnViewHistory;
    private Button btnDailyCheckin;
    private Button btnTroubleBreathing;
    private Button btnRescueInhaler;
    private Button btnControllerMedicine;
    private Button btnAchievements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);

        // Top bar
        btnSettings = findViewById(R.id.btn_settings);
        btnProfilePicture = findViewById(R.id.btn_profile_picture);

        // Main action buttons
        btnViewHistory = findViewById(R.id.btn_view_history);
        btnDailyCheckin = findViewById(R.id.btn_daily_checkin);
        btnTroubleBreathing = findViewById(R.id.btn_trouble_breathing);
        btnRescueInhaler = findViewById(R.id.btn_rescue_inhaler);
        btnControllerMedicine = findViewById(R.id.btn_controller_medicine);
        btnAchievements = findViewById(R.id.btn_achievements);

        // Example click listeners (you can change these later)
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: open settings screen
            }
        });

        btnProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: open profile popup later
            }
        });

        btnViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: go to history screen
            }
        });

        btnDailyCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: open daily check-in flow
            }
        });

        btnTroubleBreathing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: open emergency instructions
            }
        });

        btnRescueInhaler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: log rescue inhaler use
            }
        });

        btnControllerMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: log controller medicine use
            }
        });

        btnAchievements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: open achievements screen
            }
        });
    }
}
