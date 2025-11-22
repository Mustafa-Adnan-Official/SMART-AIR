package com.example.smartair.ui.signup.mvp;

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
