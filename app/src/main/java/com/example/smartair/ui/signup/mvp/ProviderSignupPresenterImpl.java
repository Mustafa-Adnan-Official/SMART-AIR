package com.example.smartair.ui.signup.mvp;

import com.example.smartair.callbacks.AuthResultCallback;
import com.example.smartair.services.AuthService;
import com.example.smartair.utils.InputValidator;

/**
 * Purpose: Handles validation and signup logic for provider accounts.
 * Layer: Presenter (Signup)
 */
public class ProviderSignupPresenterImpl implements ProviderSignupContract.Presenter {

    private ProviderSignupContract.View view;
    private final AuthService authService;

    public ProviderSignupPresenterImpl(ProviderSignupContract.View view, AuthService authService) {
        this.view = view;
        this.authService = authService;
    }

    @Override
    public void onSignupClicked() {
        if (view == null) return;

        String prefix = view.getProviderPrefix();
        String name = view.getProviderName();
        String email = view.getProviderEmail();
        String password = view.getProviderPassword();

        boolean valid = true;

        if (prefix == null || prefix.trim().isEmpty()) {
            view.showPrefixError("Prefix required");
            valid = false;
        }

        if (!InputValidator.isValidName(name)) {
            view.showNameError("Invalid name");
            valid = false;
        }

        if (!InputValidator.isValidEmail(email)) {
            view.showEmailError("Invalid email");
            valid = false;
        }

        if (!InputValidator.isValidPassword(password)) {
            view.showPasswordError("Weak password");
            valid = false;
        }

        if (!valid) return;

        view.showLoading(true);

        authService.registerProvider(prefix, name, email, password, new AuthResultCallback() {
            @Override
            public void onSuccess(String uid) {
                if (view == null) return;
                view.showLoading(false);
                view.navigateToEmailVerificationScreen();
            }

            @Override
            public void onFailure(String errorMessage) {
                if (view == null) return;
                view.showLoading(false);
                view.showGenericError(errorMessage);
            }
        });
    }

    @Override
    public void onDestroy() {
        this.view = null;
    }
}