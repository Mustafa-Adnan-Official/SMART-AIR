package com.example.smartair.callbacks;

import com.example.smartair.models.users.RoleType;

/**
 * Purpose: Final wrapped login result after auth + role lookup.
 * Layer: Presenter → View
 * Used For: Telling the View how the login ended.
 */
public interface LoginResultCallback {

    /** Full successful login (uid + role + onboarding state). */
    void onSuccess(String uid, RoleType role, boolean onboarded);

    /** Email exists but is not verified. */
    void onUnverifiedEmail();

    /** Login failed for any other reason. */
    void onFailure(String errorMessage);
}