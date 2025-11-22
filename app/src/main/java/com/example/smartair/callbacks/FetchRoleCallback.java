package com.example.smartair.callbacks;

import com.example.smartair.models.RoleType;

public interface FetchRoleCallback {
    void onSuccess(RoleType role, boolean onboarded);
    void onFailure(String errorMessage);
}
