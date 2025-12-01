package com.example.smartair.ui.login;

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
import com.example.smartair.ui.HomeActivities.ChildHomeActivity;
import com.example.smartair.ui.HomeActivities.ParentHomeActivity;
import com.example.smartair.ui.HomeActivities.ProviderHomeActivity;
import com.example.smartair.ui.login.mvp.LoginContract;
import com.example.smartair.ui.login.mvp.LoginPresenterImpl;

/**
 * Purpose: Parent login screen (email + password).
 * Layer: View (UI)
 * Used For: Delegating login logic to LoginPresenterImpl, then routing to parent home/onboarding.
 */
public class ParentLoginActivity extends AppCompatActivity implements LoginContract.View {

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
        setContentView(R.layout.account_activity_parent_li);

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);

        emailErrorText = findViewById(R.id.user_error);
        passwordErrorText = findViewById(R.id.pass_error);
        passwordRequirementsText = findViewById(R.id.password_requirements);

        btnLogin = findViewById(R.id.btn_login);
        btnForgotPassword = findViewById(R.id.btn_forgot_password);

        presenter = new LoginPresenterImpl(this, new AuthService());

        btnLogin.setOnClickListener(v -> {
            clearErrors();
            presenter.onLoginClicked();
        });

        btnForgotPassword.setOnClickListener(v -> presenter.onForgotPasswordClicked());
    }


    private void clearErrors() {
        inputEmail.setError(null);
        inputPassword.setError(null);
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
        return ""; // Not used for parent login
    }

    @Override
    public String getParentEmailForChildMode() {
        return ""; // Not used for parent login
    }

    @Override
    public boolean isChildMode() {
        return false;
    }

    @Override
    public boolean isChildUnderParentMode() {
        return false;
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
    public void showChildNameError(String msg) {
        // no child name field on this screen → fallback toast
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
        // Should not normally happen from this screen, but handle gracefully.
        startActivity(new Intent(this, ProviderHomeActivity.class));
        finish();
    }

    @Override
    public void navigateToChildHome(String childUid) {
        // Should not normally happen, but keep consistent behavior.
        Intent i = new Intent(this, ChildHomeActivity.class);
        i.putExtra("childUid", childUid);
        startActivity(i);
        finish();
    }

    @Override
    public void navigateToOnboarding(String roleString) {
        // For parent logins, we always go to parent onboarding
        Intent i = new Intent(this,
                com.example.smartair.ui.onboarding.ParentOnboardingActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void showEmailNotVerifiedMessage() {
        Toast.makeText(
                this,
                "Please verify your email before logging in.",
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public void showPasswordResetSent(String email) {
        Toast.makeText(
                this,
                "Password reset email sent to " + email,
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}