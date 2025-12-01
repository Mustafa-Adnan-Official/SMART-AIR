package com.example.smartair.ui.HomeActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.ui.checkin.DailyCheckinActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChildHomeActivity extends AppCompatActivity {

    private ImageButton btnSettings;
    private ImageButton btnProfilePicture;

    private Button btnViewHistory;
    private Button btnDailyCheckin;
    private Button btnTroubleBreathing;
    private Button btnRescueInhaler;
    private Button btnControllerMedicine;
    private Button btnAchievements;
    
    private String childName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);

        // Fetch Child Name
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("children")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            childName = documentSnapshot.getString("name");
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(ChildHomeActivity.this, "Failed to load name", Toast.LENGTH_SHORT).show());
        }

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
                Intent intent = new Intent(ChildHomeActivity.this, DailyCheckinActivity.class);
                intent.putExtra("USER_ROLE", "Child");
                intent.putExtra("USER_NAME", childName);
                intent.putExtra("USER_ID", user.getUid());
                startActivity(intent);
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
