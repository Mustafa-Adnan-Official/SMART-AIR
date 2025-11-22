package com.example.smartair.ui.signup.mvp;

import com.example.smartair.callbacks.AuthResultCallback;
import com.example.smartair.services.AuthService;
import com.example.smartair.utils.InputValidator;

public class ChildSignupPresenterImpl implements ChildSignupContract.Presenter {

    private ChildSignupContract.View view;
    private final AuthService authService;

    public ChildSignupPresenterImpl(ChildSignupContract.View view, AuthService authService) {
        this.view = view;
        this.authService = authService;
    }

    @Override
    public void onSignupClicked() {
        if (view == null) return;

        if (view.isParentConnectionEnabled()) {
            signupWithParent();
        } else {
            signupIndependent();
        }
    }

    private void signupIndependent() {
        String name = view.getChildName();
        String email = view.getChildEmail();
        String password = view.getChildPassword();

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

        authService.registerChildIndependent(
                name, email, password,
                new AuthResultCallback() {
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
                }
        );
    }


    private void signupWithParent() {
        String name = view.getChildName();
        String pac = view.getChildParentPAC();
        String password = view.getChildPassword();

        boolean valid = true;

        if (!InputValidator.isValidName(name)) {
            view.showNameError("Invalid name");
            valid = false;
        }

        if (!InputValidator.isValidPAC(pac)) {
            view.showPACError("Invalid PAC format");
            valid = false;
        }

        if (!InputValidator.isValidPassword(password)) {
            view.showPasswordError("Weak password");
            valid = false;
        }

        if (!valid) return;

        view.showLoading(true);

        authService.registerChildUnderParent(
                name,
                password,
                pac,
                new AuthResultCallback() {
                    @Override
                    public void onSuccess(String childUid) {
                        if (view == null) return;
                        view.showLoading(false);
                        view.navigateToParentLinkedSuccessScreen();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (view == null) return;
                        view.showLoading(false);
                        view.showGenericError(errorMessage);
                    }
                }
        );
    }


    @Override
    public void onDestroy() {
        this.view = null;
    }
}
