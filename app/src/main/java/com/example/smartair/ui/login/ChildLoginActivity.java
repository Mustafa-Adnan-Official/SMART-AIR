package com.example.smartair.ui.login;

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
import com.example.smartair.ui.ChildHomeActivity;
import com.example.smartair.ui.ParentHomeActivity;
import com.example.smartair.ui.ProviderHomeActivity;
import com.example.smartair.ui.login.mvp.LoginContract;
import com.example.smartair.ui.login.mvp.LoginPresenterImpl;

/**
 * Purpose: Child login screen.
 * Layer: View (UI)
 * Used For: Independent child login or child-under-parent login.
 */
public class ChildLoginActivity extends AppCompatActivity implements LoginContract.View {

    private SwitchCompat switchParentEmail;
    private EditText inputParentEmail;

    private EditText inputName;
    private EditText inputEmail;
    private EditText inputPassword;

    private TextView emailErrorText;
    private TextView passwordErrorText;
    private TextView passwordRequirementsText;

    private Button btnLogin;
    private Button btnForgotPassword;

    private LoginContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_li);

        switchParentEmail = findViewById(R.id.switch_parent_email);
        inputParentEmail = findViewById(R.id.input_parent_email);

        inputName = findViewById(R.id.input_name);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);

        emailErrorText = findViewById(R.id.email_error);
        passwordErrorText = findViewById(R.id.pass_error);
        passwordRequirementsText = findViewById(R.id.password_requirements);

        btnLogin = findViewById(R.id.btn_login);
        btnForgotPassword = findViewById(R.id.btn_forgot_pswrd);

        presenter = new LoginPresenterImpl(this, new AuthService());

        switchParentEmail.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                inputParentEmail.setVisibility(View.VISIBLE);
            } else {
                inputParentEmail.setText("");
                inputParentEmail.setVisibility(View.GONE);
            }
        });

        btnLogin.setOnClickListener(v -> {
            clearErrors();
            presenter.onLoginClicked();
        });

        btnForgotPassword.setOnClickListener(v -> presenter.onForgotPasswordClicked());
    }

    private void clearErrors() {
        inputName.setError(null);
        inputEmail.setError(null);
        inputPassword.setError(null);
        inputParentEmail.setError(null);

        emailErrorText.setVisibility(View.GONE);
        passwordErrorText.setVisibility(View.GONE);
        passwordRequirementsText.setVisibility(View.GONE);
    }

    // -------- LoginContract.View --------

    @Override
    public String getEmail() {
        return inputEmail.getText().toString().trim();
    }

    @Override
    public String getPassword() {
        return inputPassword.getText().toString();
    }

    @Override
    public String getChildName() {
        return inputName.getText().toString().trim();
    }

    @Override
    public String getParentEmailForChildMode() {
        return inputParentEmail.getText().toString().trim();
    }

    @Override
    public boolean isChildMode() {
        return true;
    }

    @Override
    public boolean isChildUnderParentMode() {
        return switchParentEmail.isChecked();
    }

    @Override
    public void showEmailError(String msg) {
        emailErrorText.setText("! " + msg);
        emailErrorText.setVisibility(View.VISIBLE);

        if (isChildUnderParentMode()) {
            inputParentEmail.requestFocus();
        } else {
            inputEmail.requestFocus();
        }
    }

    @Override
    public void showPasswordError(String msg) {
        passwordErrorText.setText("! " + msg);
        passwordErrorText.setVisibility(View.VISIBLE);
        passwordRequirementsText.setVisibility(View.VISIBLE);
        inputPassword.requestFocus();
    }

    @Override
    public void showChildNameError(String msg) {
        inputName.setError(msg);
        inputName.requestFocus();
    }

    @Override
    public void showGenericError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showLoading(boolean show) {
        btnLogin.setEnabled(!show);
        btnForgotPassword.setEnabled(!show);
    }

    @Override
    public void navigateToParentHome() {
        startActivity(new Intent(this, ParentHomeActivity.class));
        finish();
    }

    @Override
    public void navigateToProviderHome() {
        startActivity(new Intent(this, ProviderHomeActivity.class));
        finish();
    }

    @Override
    public void navigateToChildHome(String childUid) {
        Intent i = new Intent(this, ChildHomeActivity.class);
        i.putExtra("childUid", childUid);
        startActivity(i);
        finish();
    }

    @Override
    public void navigateToOnboarding(String roleString) {
        Toast.makeText(this, "First login detected (" + roleString + "). Onboarding flow TODO.", Toast.LENGTH_LONG).show();
        navigateToChildHome("currentChild");
    }

    @Override
    public void showEmailNotVerifiedMessage() {
        Toast.makeText(this, "Please ask your parent to verify the email before logging in.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void showPasswordResetSent(String email) {
        Toast.makeText(this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}