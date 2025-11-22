package com.example.smartair.ui.signup.mvp;

/**
 * Purpose: Defines View ↔ Presenter contract for parent signup.
 * Layer: MVP (Signup)
 */
public interface ParentSignupContract {

    interface View {
        String getParentName();
        String getParentEmail();
        String getParentPassword();

        void showNameError(String msg);
        void showEmailError(String msg);
        void showPasswordError(String msg);

        void showLoading(boolean show);
        void showGenericError(String msg);

        void navigateToEmailVerificationScreen();
    }

    interface Presenter {
        void onSignupClicked();
        void onDestroy();
    }
}