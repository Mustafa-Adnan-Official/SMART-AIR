package com.example.smartair.ui.r3.technique;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

/**
 * R3: Technique helper screen.
 * No Firebase – just shows instructions + optional video.
 */
public class TechniqueTrainerActivity extends AppCompatActivity {

    private VideoView videoTechnique;
    private Button btnSubmitTechnique;
    private Button btnBackATechnique;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.technique_trainer);  // <-- change if your file name is different

        videoTechnique = findViewById(R.id.videoTechnique);
        btnSubmitTechnique = findViewById(R.id.btnSubmitTechnique);
        btnBackATechnique = findViewById(R.id.btnBackATechnique);

        // OPTIONAL: play a local video in res/raw (e.g. res/raw/technique.mp4)
        // Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.technique);
        // videoTechnique.setVideoURI(videoUri);
        // videoTechnique.start();

        btnSubmitTechnique.setOnClickListener(v -> {
            // You could show a Toast or mark completion in a manager later
            // For now just close and go back to controller session
            finish();
        });

        btnBackATechnique.setOnClickListener(v -> {
            // Just go back without doing anything
            finish();
        });
    }
}
