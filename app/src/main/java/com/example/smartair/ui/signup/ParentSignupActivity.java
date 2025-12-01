package com.example.smartair.ui.signup;

import com.example.smartair.ui.signup.mvp.ParentSignupContract;
import com.example.smartair.ui.signup.mvp.ParentSignupPresenterImpl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.ui.login.ParentLoginActivity;
import com.example.smartair.services.AuthService;

/**
 * Purpose: Parent registration screen.
 * Layer: View (UI)
 * Used For: Collecting parent details and delegating signup to ParentSignupPresenterImpl.
 */
public class ParentSignupActivity extends AppCompatActivity implements ParentSignupContract.View {

    private EditText inputName;
    private EditText inputEmail;
    private EditText inputPassword;

    private TextView emailErrorText;
    private TextView passwordErrorText;
    private TextView passwordRequirementsText;

    private Button btnRegister;
    private Button btnExistingUser;

    private ParentSignupContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_acitivity_parent_su);

        inputName = findViewById(R.id.input_name);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);

        emailErrorText = findViewById(R.id.email_error);
        passwordErrorText = findViewById(R.id.pass_error);
        passwordRequirementsText = findViewById(R.id.password_requirements);

        btnRegister = findViewById(R.id.btn_register);
        btnExistingUser = findViewById(R.id.btn_existing_user);

        presenter = new ParentSignupPresenterImpl(this, new AuthService());

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        ParentSignupActivity.this,
                        "Parent Register clicked",
                        Toast.LENGTH_SHORT
                ).show();

                clearErrors();
                presenter.onSignupClicked();
            }
        });

        btnExistingUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        ParentSignupActivity.this,
                        ParentLoginActivity.class
                ));
            }
        });
    }

    private void clearErrors() {
        inputName.setError(null);
        inputEmail.setError(null);
        inputPassword.setError(null);

        emailErrorText.setVisibility(View.GONE);
        passwordErrorText.setVisibility(View.GONE);
        passwordRequirementsText.setVisibility(View.GONE);
    }

    // --------- Contract.View implementation ---------

    @Override
    public String getParentName() {
        return inputName.getText().toString().trim();
    }

    @Override
    public String getParentEmail() {
        return inputEmail.getText().toString().trim();
    }

    @Override
    public String getParentPassword() {
        return inputPassword.getText().toString();
    }

    @Override
    public void showNameError(String msg) {
        inputName.setError(msg);
        inputName.requestFocus();
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
        // After signup, send them to Parent login
        startActivity(new Intent(this, ParentLoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}