package com.example.smartair.callbacks;

public interface ChildUnderParentLoginCallback {
    void onSuccess(String childUid, boolean onboarded);
    void onFailure(String errorMessage);
}
