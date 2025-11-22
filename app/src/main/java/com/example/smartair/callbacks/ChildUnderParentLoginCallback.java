package com.example.smartair.callbacks;

/**
 * Purpose: Callback for selecting a child profile under a Parent’s account.
 * Layer: Model → Presenter
 * Used For: Parent choosing which child profile to open.
 */
public interface ChildUnderParentLoginCallback {

    /** Child profile found and ready to open. */
    void onSuccess(String childUid, boolean onboarded);

    /** Child profile could not be loaded. */
    void onFailure(String errorMessage);
}