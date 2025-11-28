package com.example.smartair.ui.signup;
import com.example.smartair.ui.signup.mvp.ProviderSignupContract;
import com.example.smartair.ui.signup.mvp.ProviderSignupPresenterImpl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.services.AuthService;
import com.example.smartair.ui.login.ProviderLoginActivity;

/**
 * Purpose: Provider registration screen.
 * Layer: View (UI)
 * Used For: Collecting provider details and delegating signup to ProviderSignupPresenterImpl.
 */
public class ProviderSignupActivity extends AppCompatActivity implements ProviderSignupContract.View {

    private EditText inputPrefix;
    private EditText inputLastName;
    private EditText inputEmail;
    private EditText inputPassword;

    private TextView emailErrorText;
    private TextView passwordErrorText;
    private TextView passwordRequirementsText;

    private Button btnRegister;
    private Button btnExistingUser;

    private ProviderSignupContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_su);

        inputPrefix = findViewById(R.id.input_prefix);
        inputLastName = findViewById(R.id.input_lastname);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);

        emailErrorText = findViewById(R.id.email_error);
        passwordErrorText = findViewById(R.id.pass_error);
        passwordRequirementsText = findViewById(R.id.password_requirements);

        btnRegister = findViewById(R.id.btn_register);
        btnExistingUser = findViewById(R.id.btn_existing_user);

        presenter = new ProviderSignupPresenterImpl(this, new AuthService());

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearErrors();
                presenter.onSignupClicked();
            }
        });

        btnExistingUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        ProviderSignupActivity.this,
                        ProviderLoginActivity.class
                ));
            }
        });
    }

    private void clearErrors() {
        inputPrefix.setError(null);
        inputLastName.setError(null);
        inputEmail.setError(null);
        inputPassword.setError(null);

        emailErrorText.setVisibility(View.GONE);
        passwordErrorText.setVisibility(View.GONE);
        passwordRequirementsText.setVisibility(View.GONE);
    }

    // --------- Contract.View ---------

    @Override
    public String getProviderPrefix() {
        return inputPrefix.getText().toString().trim();
    }

    @Override
    public String getProviderName() {
        // Design choice: just use the last name for now.
        return inputLastName.getText().toString().trim();
    }

    @Override
    public String getProviderEmail() {
        return inputEmail.getText().toString().trim();
    }

    @Override
    public String getProviderPassword() {
        return inputPassword.getText().toString();
    }

    @Override
    public void showPrefixError(String msg) {
        inputPrefix.setError(msg);
        inputPrefix.requestFocus();
    }

    @Override
    public void showNameError(String msg) {
        inputLastName.setError(msg);
        inputLastName.requestFocus();
    }

    @Override
    public void showEmailError(String msg) {
        emailErrorText.setText("! " + msg);
        emailErrorText.setVisibility(View.VISIBLE);
        inputEmail.requestFocus();
    }

    @Override
    public void showPasswordError(String msg) {
        passwordErrorText.setText("! " + msg);
        passwordErrorText.setVisibility(View.VISIBLE);
        passwordRequirementsText.setVisibility(View.VISIBLE);
        inputPassword.requestFocus();
    }

    @Override
    public void showLoading(boolean show) {
        btnRegister.setEnabled(!show);
    }

    @Override
    public void showGenericError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToEmailVerificationScreen() {
        Toast.makeText(
                this,
                "Account created. Please check your email to verify.",
                Toast.LENGTH_LONG
        ).show();
        startActivity(new Intent(this, ProviderLoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}