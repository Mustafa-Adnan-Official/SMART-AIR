package com.example.smartair.ui.login.mvp;

import com.example.smartair.callbacks.ChildUnderParentLoginCallback;
import com.example.smartair.callbacks.LoginResultCallback;
import com.example.smartair.callbacks.SimpleResultCallback;
import com.example.smartair.models.users.RoleType;
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

        // CHILD-UNDER-PARENT FLOW (child screen + switch ON)
        if (view.isChildMode() && view.isChildUnderParentMode()) {
            handleChildUnderParentLogin();
        } else {
            // All other flows: parent / provider / independent child
            handleEmailPasswordLogin();
        }
    }

    /**
     * Handles parent, provider, and independent-child login.
     * IMPORTANT:
     *  - If we're on the CHILD login screen (isChildMode() == true)
     *    and NOT in "child-under-parent" mode, we will ONLY allow
     *    RoleType.CHILD to proceed. If the account is actually a
     *    PARENT or PROVIDER, we show an error instead of navigating
     *    to their home screens from the child UI.
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

                // If NOT onboarded → send them to onboarding for their role
                if (!onboarded) {
                    view.navigateToOnboarding(role.name().toLowerCase());
                    return;
                }

                // -----------------------------
                // CHILD LOGIN SCREEN BEHAVIOUR
                // -----------------------------
                if (view.isChildMode()) {
                    // This is the child login screen (independent child mode),
                    // so we ONLY allow CHILD accounts through.
                    if (role == RoleType.CHILD) {
                        view.navigateToChildHome(uid);
                    } else if (role == RoleType.PARENT) {
                        view.showGenericError(
                                "This email belongs to a parent account. " +
                                        "Please use the Parent login screen."
                        );
                    } else if (role == RoleType.PROVIDER) {
                        view.showGenericError(
                                "This email belongs to a provider account. " +
                                        "Please use the Provider login screen."
                        );
                    }
                    return;
                }

                // -----------------------------
                // NON-CHILD SCREENS (parent/provider login UIs)
                // -----------------------------
                switch (role) {
                    case PARENT:
                        view.navigateToParentHome();
                        break;
                    case PROVIDER:
                        view.navigateToProviderHome();
                        break;
                    case CHILD:
                    default:
                        view.navigateToChildHome(uid);
                        break;
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
     *
     * UI:
     *  - Screen: ChildLoginActivity
     *  - isChildMode()          == true
     *  - isChildUnderParentMode()== true (switch ON)
     *
     * Back-end:
     *  - AuthService.loginChildUnderParent(...)
     *    internally finds the child alias email and signs in as THAT child.
     */
    private void handleChildUnderParentLogin() {
        final String childName   = view.getChildName();
        final String parentEmail = view.getParentEmailForChildMode();
        final String password    = view.getPassword();

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
                            // Always go to child home for this flow
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

        // CHILD-UNDER-PARENT RESET FLOW
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

        // NORMAL RESET FLOW (parent / provider / independent child)
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