package com.example.smartair.ui.login.mvp;

/**
 * Purpose: Defines the View ↔ Presenter contract for login.
 * Layer: MVP (interfaces)
 */
public interface LoginContract {

    interface View {

        // Presenter pulls input from UI
        String getEmail();
        String getPassword();
        String getChildName();
        String getParentEmailForChildMode();

        boolean isChildMode();
        boolean isChildUnderParentMode();

        // Error and state feedback
        void showEmailError(String msg);
        void showPasswordError(String msg);
        void showChildNameError(String msg);
        void showGenericError(String msg);
        void showLoading(boolean show);

        // Navigation callbacks
        void navigateToParentHome();
        void navigateToProviderHome();
        void navigateToChildHome(String childUid);
        void navigateToOnboarding(String roleString);

        void showEmailNotVerifiedMessage();
        void showPasswordResetSent(String email);
    }

    interface Presenter {
        void onLoginClicked();
        void onForgotPasswordClicked();
        void onDestroy();
    }
}