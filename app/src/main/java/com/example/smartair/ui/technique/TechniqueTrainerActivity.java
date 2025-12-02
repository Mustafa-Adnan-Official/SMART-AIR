package com.example.smartair.ui.technique;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.models.childcollections.AchievementSummary;
import com.example.smartair.services.AchievementService;
import com.google.firebase.Timestamp;

/**
 * R3: Technique helper screen.
 * Shows steps + embedded video.
 * When the child taps SUBMIT, we log a "high-quality technique session"
 * into the achievements/summary document.
 */
public class TechniqueTrainerActivity extends AppCompatActivity {

    private VideoView videoTechnique;
    private Button btnSubmitTechnique;
    private Button btnBackATechnique;

    private String childUid;
    private AchievementService achievementService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicine_dialog_technique_trainer);

        // Get child UID from intent
        childUid = getIntent().getStringExtra("childUid");
        if (childUid == null || childUid.trim().isEmpty()) {
            Toast.makeText(this, "Missing child UID for technique session.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        achievementService = new AchievementService();

        bindViews();
        setupVideo();
        setupListeners();
    }

    private void bindViews() {
        videoTechnique = findViewById(R.id.videoTechnique);
        btnSubmitTechnique = findViewById(R.id.btnSubmitTechnique);
        btnBackATechnique = findViewById(R.id.btnBackATechnique);
    }

    private void setupVideo() {
        // Play grok_technique_video.mp4 from res/raw
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.grok_technique_video);
        videoTechnique.setVideoURI(videoUri);
        videoTechnique.start();
    }

    private void setupListeners() {
        // SUBMIT = count this as a high-quality technique session
        btnSubmitTechnique.setOnClickListener(v -> {
            btnSubmitTechnique.setEnabled(false);

            achievementService.updateAfterTechniqueSession(
                    childUid,
                    Timestamp.now(),
                    new AchievementService.AchievementCallback() {
                        @Override
                        public void onSuccess(AchievementSummary summary) {
                            Toast.makeText(
                                    TechniqueTrainerActivity.this,
                                    "Great job! Technique session logged ✅",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            btnSubmitTechnique.setEnabled(true);
                            Toast.makeText(
                                    TechniqueTrainerActivity.this,
                                    "Could not save technique session.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        });

        // BACK = just close without logging anything
        btnBackATechnique.setOnClickListener(v -> finish());
    }
}