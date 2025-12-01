package com.example.smartair.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.models.users.RoleType;
import com.example.smartair.services.AuthService;
import com.example.smartair.callbacks.SimpleResultCallback;
import com.example.smartair.ui.HomeActivities.ParentHomeActivity;

/**
 * Purpose: Four-page onboarding flow for parent users.
 * Layer: View (UI)
 * Used For: First login only; marks parent as onboarded then routes to ParentHomeActivity.
 */
public class ParentOnboardingActivity extends AppCompatActivity {

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authService = new AuthService();
        showPage1();
    }

    private void showPage1() {
        setContentView(R.layout.onboard_activity_parent_1);
        Button btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(v -> showPage2());
    }

    private void showPage2() {
        setContentView(R.layout.onboard_activity_parent_2);
        Button btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(v -> showPage3());
    }

    private void showPage3() {
        setContentView(R.layout.onboard_activity_parent_3);
        Button btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(v -> showPage4());
    }

    private void showPage4() {
        setContentView(R.layout.onboard_activity_parent_4);
        Button btnDone = findViewById(R.id.btn_next); // last one’s text is “Done”
        btnDone.setOnClickListener(v -> completeOnboarding());
    }

    private void completeOnboarding() {
        authService.markOnboarded(RoleType.PARENT, new SimpleResultCallback() {
            @Override
            public void onSuccess() {
                startActivity(new Intent(ParentOnboardingActivity.this, ParentHomeActivity.class));
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ParentOnboardingActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}