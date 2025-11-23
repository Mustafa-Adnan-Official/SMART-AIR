package com.example.smartair.ui.signup;

import com.example.smartair.ui.signup.mvp.ChildSignupContract;
import com.example.smartair.ui.signup.mvp.ChildSignupPresenterImpl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.smartair.R;
import com.example.smartair.services.AuthService;
import com.example.smartair.ui.login.ChildLoginActivity;

/**
 * Purpose: Child registration screen (independent or linked to parent via PAC).
 * Layer: View (UI)
 * Used For: Passing child signup data to ChildSignupPresenterImpl.
 */
public class ChildSignupActivity extends AppCompatActivity implements ChildSignupContract.View {

    private SwitchCompat switchParentLink;
    private EditText inputParentCode;

    private EditText inputName;
    private EditText inputEmail;
    private EditText inputPassword;

    private TextView emailErrorText;
    private TextView passwordErrorText;
    private TextView passwordRequirementsText;
    private TextView pacErrorText;

    private Button btnRegister;
    private Button btnExistingUser;

    private ChildSignupContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_su);

        switchParentLink = findViewById(R.id.switch_parent_link);
        inputParentCode = findViewById(R.id.input_parent_code);

        inputName = findViewById(R.id.input_name);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);

        emailErrorText = findViewById(R.id.email_error);
        passwordErrorText = findViewById(R.id.pass_error);
        passwordRequirementsText = findViewById(R.id.password_requirements);
        pacErrorText = inputParentCode;

        btnRegister = findViewById(R.id.btn_register);
        btnExistingUser = findViewById(R.id.btn_existing_user);

        presenter = new ChildSignupPresenterImpl(this, new AuthService());

        // Switch toggle logic
        switchParentLink.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                inputParentCode.setVisibility(View.VISIBLE);
            } else {
                inputParentCode.setText("");
                inputParentCode.setVisibility(View.GONE);
            }
        });

        btnRegister.setOnClickListener(v -> {
            clearErrors();
            presenter.onSignupClicked();
        });

        btnExistingUser.setOnClickListener(v ->
                startActivity(new Intent(ChildSignupActivity.this, ChildLoginActivity.class))
        );
    }

    private void clearErrors() {
        inputName.setError(null);
        inputEmail.setError(null);
        inputPassword.setError(null);
        inputParentCode.setError(null);

        emailErrorText.setVisibility(View.GONE);
        passwordErrorText.setVisibility(View.GONE);
        passwordRequirementsText.setVisibility(View.GONE);
    }

    // ---------- Contract.View ----------

    @Override
    public String getChildName() {
        return inputName.getText().toString().trim();
    }

    @Override
    public String getChildPassword() {
        return inputPassword.getText().toString();
    }

    @Override
    public boolean isParentConnectionEnabled() {
        return switchParentLink.isChecked();
    }

    @Override
    public String getChildEmail() {
        return inputEmail.getText().toString().trim();
    }

    @Override
    public String getChildParentPAC() {
        return inputParentCode.getText().toString().trim();
    }

    @Override
    public String getParentEmailForPAC() {
        return "";
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
    public void showPACError(String msg) {
        inputParentCode.setError(msg);
        inputParentCode.requestFocus();
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
        Toast.makeText(this, "Account created. Please check your email to verify.", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, ChildLoginActivity.class));
        finish();
    }

    @Override
    public void navigateToParentLinkedSuccessScreen() {
        Toast.makeText(this, "Linked to parent successfully. You can log in now.", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, ChildLoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}