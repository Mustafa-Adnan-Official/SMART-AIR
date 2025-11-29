package com.example.smartair.ui.dev;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.callbacks.AuthResultCallback;
import com.example.smartair.services.AuthService;

/**
 * Purpose: Temporary dev/testing screen for quickly creating test parent/provider/child accounts.
 * Layer: UI (dev only)
 * Used For: Manual Firestore population during development.
 *
 * Note:
 * - Not used in production. Safe to remove when no longer needed.
 */
public class DevSignupActivity extends AppCompatActivity {

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_signup);
        authService = new AuthService();

        Button btnCreateParent           = findViewById(R.id.btnCreateParent);
        Button btnCreateProvider         = findViewById(R.id.btnCreateProvider);
        Button btnCreateChildIndependent = findViewById(R.id.btnCreateChildIndependent);
        Button btnCreateChildWithParent  = findViewById(R.id.btnCreateChildWithParent);

        btnCreateParent.setOnClickListener(v -> createTestParent());
        btnCreateProvider.setOnClickListener(v -> createTestProvider());
        btnCreateChildIndependent.setOnClickListener(v -> createTestChildIndependent());
        btnCreateChildWithParent.setOnClickListener(v -> createTestChildWithParent());
    }

    private void createTestParent() {
        authService.registerParent(
                "Dev Parent",
                "devparent+" + System.currentTimeMillis() + "@test.com",
                "Test123!",
                new AuthResultCallback() {
                    @Override
                    public void onSuccess(String uid) {
                        Toast.makeText(DevSignupActivity.this,
                                "Parent created: " + uid, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(DevSignupActivity.this,
                                "Parent error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void createTestProvider() {
        authService.registerProvider(
                "Dr.",
                "Dev Provider",
                "devprovider+" + System.currentTimeMillis() + "@test.com",
                "Test123!",
                new AuthResultCallback() {
                    @Override
                    public void onSuccess(String uid) {
                        Toast.makeText(DevSignupActivity.this,
                                "Provider created: " + uid, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(DevSignupActivity.this,
                                "Provider error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void createTestChildIndependent() {
        authService.registerChildIndependent(
                "Dev Child (indep)",
                "devchild+" + System.currentTimeMillis() + "@test.com",
                "Test123!",
                new AuthResultCallback() {
                    @Override
                    public void onSuccess(String uid) {
                        Toast.makeText(DevSignupActivity.this,
                                "Child (indep) created: " + uid, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(DevSignupActivity.this,
                                "Child (indep) error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void createTestChildWithParent() {
        // Replace with a real test PAC in Firestore when using this dev tool
        String testPac = "PA-7FQ2-91";

        authService.registerChildUnderParent(
                "Dev Child (with parent)",
                "Test123!",
                testPac,
                new AuthResultCallback() {
                    @Override
                    public void onSuccess(String childUid) {
                        Toast.makeText(DevSignupActivity.this,
                                "Child (with parent) created: " + childUid, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(DevSignupActivity.this,
                                "Child (with parent) error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
}