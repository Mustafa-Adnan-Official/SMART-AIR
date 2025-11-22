package com.example.smartair.callbacks;

/**
 * Purpose: Lightweight success/failure callback using string errors.
 * Layer: Model → Presenter
 * Used For: When Model already converts exceptions to user messages.
 */
public interface SimpleResultCallback {

    /** Operation succeeded. */
    void onSuccess();

    /** Operation failed with a user-readable message. */
    void onFailure(String errorMessage);
}