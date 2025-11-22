package com.example.smartair.callbacks;

import com.example.smartair.models.RoleType;

/**
 * Purpose: Reports success/failure of FirebaseAuth login or signup.
 * Layer: Model → Presenter
 * Used For: When the Model wraps FirebaseAuth operations.
 */
public interface AuthResultCallback {

    /** Successful FirebaseAuth login/signup. */
    void onSuccess(String uid);

    /** Authentication failed (bad credentials, network error, etc). */
    void onFailure(String errorMessage);
}