package com.example.smartair.ui.login.mvp;

import com.example.smartair.callbacks.ChildUnderParentLoginCallback;
import com.example.smartair.callbacks.LoginResultCallback;
import com.example.smartair.callbacks.SimpleResultCallback;
import com.example.smartair.models.RoleType;
import com.example.smartair.services.AuthService;
import com.example.smartair.utils.InputValidator;

/**
 * Purpose: Implements login logic and validation.
 * Layer: Presenter (MVP)
 * Used For: Handling all login flows and updating the View based on results.
 */
public class LoginPresenterImpl implements LoginContract.Presenter {

    private LoginContract.View view;
    private final AuthService authService;

    public LoginPresenterImpl(LoginContract.View view, AuthService authService) {
        this.view = view;
        this.authService = authService;
    }

    @Override
    public void onLoginClicked() {
        if (view == null) return;

        if (view.isChildMode() && view.isChildUnderParentMode()) {
            handleChildUnderParentLogin();
        } else {
            handleEmailPasswordLogin();
        }
    }

    /**
     * Handles parent, provider, and independent-child login.
     */
    private void handleEmailPasswordLogin() {
        final String email = view.getEmail();
        final String password = view.getPassword();

        boolean valid = true;

        if (!InputValidator.isValidEmail(email)) {
            view.showEmailError("Invalid email format");
            valid = false;
        }

        if (!InputValidator.isValidPassword(password)) {
            view.showPasswordError("Password must be ≥ 6 chars and contain a special character");
            valid = false;
        }

        if (!valid) {
            return;
        }

        view.showLoading(true);

        authService.loginWithEmailPassword(email, password, new LoginResultCallback() {
            @Override
            public void onSuccess(String uid, RoleType role, boolean onboarded) {
                if (view == null) return;
                view.showLoading(false);

                if (!onboarded) {
                    view.navigateToOnboarding(role.name().toLowerCase());
                    return;
                }

                switch (role) {
                    case PARENT:   view.navigateToParentHome();   break;
                    case PROVIDER: view.navigateToProviderHome(); break;
                    case CHILD:
                    default:       view.navigateToChildHome(uid); break;
                }
            }

            @Override
            public void onUnverifiedEmail() {
                if (view == null) return;
                view.showLoading(false);
                view.showEmailNotVerifiedMessage();
            }

            @Override
            public void onFailure(String errorMessage) {
                if (view == null) return;
                view.showLoading(false);
                view.showGenericError(errorMessage);
            }
        });
    }

    /**
     * Handles login when child logs in under a parent email.
     */
    private void handleChildUnderParentLogin() {
        final String childName = view.getChildName();
        final String parentEmail = view.getParentEmailForChildMode();
        final String password = view.getPassword();

        boolean valid = true;

        if (!InputValidator.isValidName(childName)) {
            view.showChildNameError("Invalid child name");
            valid = false;
        }

        if (!InputValidator.isValidEmail(parentEmail)) {
            view.showEmailError("Invalid parent email");
            valid = false;
        }

        if (!InputValidator.isValidPassword(password)) {
            view.showPasswordError("Password must be ≥ 6 chars and contain a special character");
            valid = false;
        }

        if (!valid) {
            return;
        }

        view.showLoading(true);

        authService.loginChildUnderParent(
                childName,
                parentEmail,
                password,
                new ChildUnderParentLoginCallback() {
                    @Override
                    public void onSuccess(String childUid, boolean onboarded) {
                        if (view == null) return;
                        view.showLoading(false);

                        if (!onboarded) {
                            view.navigateToOnboarding("child");
                        } else {
                            view.navigateToChildHome(childUid);
                        }
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
    public void onForgotPasswordClicked() {
        if (view == null) return;

        // Child under parent flow → special logic
        if (view.isChildMode() && view.isChildUnderParentMode()) {
            final String parentEmail = view.getParentEmailForChildMode();
            final String childName   = view.getChildName();

            boolean valid = true;

            if (!InputValidator.isValidEmail(parentEmail)) {
                view.showEmailError("Enter a valid parent email");
                valid = false;
            }

            if (!InputValidator.isValidName(childName)) {
                view.showChildNameError("Enter a valid child name");
                valid = false;
            }

            if (!valid) return;

            view.showLoading(true);

            authService.sendChildPasswordResetUnderParent(
                    childName,
                    parentEmail,
                    new SimpleResultCallback() {
                        @Override
                        public void onSuccess() {
                            if (view == null) return;
                            view.showLoading(false);
                            // You can phrase this however you like
                            view.showPasswordResetSent(parentEmail);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            if (view == null) return;
                            view.showLoading(false);
                            view.showGenericError(errorMessage);
                        }
                    }
            );
            return;
        }

        // Normal flow: parent / provider / independent child
        String email = view.getEmail();

        if (!InputValidator.isValidEmail(email)) {
            view.showEmailError("Enter a valid email to reset password");
            return;
        }

        view.showLoading(true);

        authService.sendPasswordReset(email, new SimpleResultCallback() {
            @Override
            public void onSuccess() {
                if (view == null) return;
                view.showLoading(false);
                view.showPasswordResetSent(email);
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