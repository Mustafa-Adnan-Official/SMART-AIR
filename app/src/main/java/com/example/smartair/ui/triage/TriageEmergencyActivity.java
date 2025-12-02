package com.example.smartair.ui.triage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.ui.HomeActivities.ChildHomeActivity;

public class TriageEmergencyActivity extends AppCompatActivity {

    private Button buttonCloseEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.triage_dialog_emergency);

        buttonCloseEmergency = findViewById(R.id.buttonCloseEmergency);

        buttonCloseEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Always go back to child home screen
                Intent intent = new Intent(
                        TriageEmergencyActivity.this,
                        ChildHomeActivity.class
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}