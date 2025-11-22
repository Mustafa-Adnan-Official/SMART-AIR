package com.example.smartair.ui.signup.mvp;

public interface ChildSignupContract {

    interface View {
        // shared
        String getChildName();
        String getChildPassword();
        boolean isParentConnectionEnabled();

        // independent child
        String getChildEmail();

        // child with parent
        String getChildParentPAC();
        String getParentEmailForPAC();

        void showNameError(String msg);
        void showEmailError(String msg);
        void showPACError(String msg);
        void showPasswordError(String msg);

        void showLoading(boolean show);
        void showGenericError(String msg);

        void navigateToEmailVerificationScreen();
        void navigateToParentLinkedSuccessScreen();
    }

    interface Presenter {
        void onSignupClicked();
        void onDestroy();
    }
}
