package com.example.smartair.ui.login.mvp;

public interface LoginContract {

    interface View {
        // Getters – presenter pulls data from the View
        String getEmail();
        String getPassword();
        String getChildName();
        String getParentEmailForChildMode();

        boolean isChildMode();             // user selected "child"
        boolean isChildUnderParentMode();  // toggle "use parent connection" ON

        // Error / state display
        void showEmailError(String msg);
        void showPasswordError(String msg);
        void showChildNameError(String msg);
        void showGenericError(String msg);
        void showLoading(boolean show);

        // Navigation hooks
        void navigateToParentHome();
        void navigateToProviderHome();
        void navigateToChildHome(String childUid);
        void navigateToOnboarding(String roleString); // "parent" / "provider" / "child"

        void showEmailNotVerifiedMessage();
        void showPasswordResetSent(String email);
    }

    interface Presenter {
        void onLoginClicked();
        void onForgotPasswordClicked();
        void onDestroy();
    }
}
