package com.example.smartair.callbacks;

import com.example.smartair.models.RoleType;

/**
 * Purpose: Returns the user’s role (parent/provider/child) after login.
 * Layer: Model → Presenter
 * Used For: Mapping Firebase UID → role + onboarding state.
 */
public interface FetchRoleCallback {

    /** Role successfully determined from Firestore. */
    void onSuccess(RoleType role, boolean onboarded);

    /** Failed to fetch role (missing doc, network error, etc). */
    void onFailure(String errorMessage);
}