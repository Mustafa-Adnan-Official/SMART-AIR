package com.example.smartair.ui.HomeActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.ui.signup.ChildSignupActivity;
import com.example.smartair.ui.signup.ParentSignupActivity;
import com.example.smartair.ui.signup.ProviderSignupActivity;

/**
 * Purpose: First screen a user sees. Lets them choose Child / Parent / Provider.
 * Layer: View (UI)
 * Used For: Routing into the correct signup flow.
 */
public class WelcomeRoleSelectionActivity extends AppCompatActivity {

    private Button btnChild;
    private Button btnParent;
    private Button btnProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity_role_selection);

        btnChild = findViewById(R.id.btn_child);
        btnParent = findViewById(R.id.btn_parent);
        btnProvider = findViewById(R.id.btn_provider);

        btnChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        WelcomeRoleSelectionActivity.this,
                        ChildSignupActivity.class
                ));
            }
        });

        btnParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        WelcomeRoleSelectionActivity.this,
                        ParentSignupActivity.class
                ));
            }
        });

        btnProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        WelcomeRoleSelectionActivity.this,
                        ProviderSignupActivity.class
                ));
            }
        });
    }
}