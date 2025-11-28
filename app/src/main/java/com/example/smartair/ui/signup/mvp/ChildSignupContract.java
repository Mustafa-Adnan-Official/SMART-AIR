package com.example.smartair.ui.signup.mvp;

/**
 * Purpose: Defines the View ↔ Presenter contract for child signup.
 * Layer: MVP (Signup)
 */
public interface ChildSignupContract {

    interface View {
        // Child input
        String getChildName();
        String getChildPassword();
        boolean isParentConnectionEnabled();

        // Independent child
        String getChildEmail();

        // Child under parent
        String getChildParentPAC();
        String getParentEmailForPAC();

        // Errors
        void showNameError(String msg);
        void showEmailError(String msg);
        void showPACError(String msg);
        void showPasswordError(String msg);

        void showLoading(boolean show);
        void showGenericError(String msg);

        // Navigation
        void navigateToEmailVerificationScreen();
        void navigateToParentLinkedSuccessScreen();
    }

    interface Presenter {
        void onSignupClicked();
        void onDestroy();
    }
}