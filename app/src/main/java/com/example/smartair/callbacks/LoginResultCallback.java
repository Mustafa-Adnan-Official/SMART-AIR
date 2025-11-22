package com.example.smartair.callbacks;

import com.example.smartair.models.RoleType;

public interface LoginResultCallback {
    void onSuccess(String uid, RoleType role, boolean onboarded);
    void onUnverifiedEmail();
    void onFailure(String errorMessage);
}
