package com.example.smartair.ui.signup.mvp;

import com.example.smartair.callbacks.AuthResultCallback;
import com.example.smartair.services.AuthService;
import com.example.smartair.utils.InputValidator;

public class ParentSignupPresenterImpl implements ParentSignupContract.Presenter {

    private ParentSignupContract.View view;
    private final AuthService authService;

    public ParentSignupPresenterImpl(ParentSignupContract.View view, AuthService authService) {
        this.view = view;
        this.authService = authService;
    }

    @Override
    public void onSignupClicked() {
        if (view == null) return;

        String name = view.getParentName();
        String email = view.getParentEmail();
        String password = view.getParentPassword();

        boolean valid = true;

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

        authService.registerParent(name, email, password, new AuthResultCallback() {
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
