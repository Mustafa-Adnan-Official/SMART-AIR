package com.example.smartair.ui.signup.mvp;

public interface ProviderSignupContract {

    interface View {
        String getProviderPrefix();
        String getProviderName();
        String getProviderEmail();
        String getProviderPassword();

        void showPrefixError(String msg);
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
