package com.example.smartair.callbacks;

/*
 * Generic callback for Firebase operations that don't return a value.
 *
 * You will use this when:
 * - Registering users (parent / child / provider)
 * - Writing reports, incidents, logs, etc.
 *
 * TODO:
 *  - Implement both methods where you call AccountService / ParentService / etc.
 *  - onSuccess(): do something in the Activity (e.g., show Toast, navigate)
 *  - onError(Exception e): show an error message.
 */
public interface OperationCallback {
    void onSuccess();
    void onError(Exception e);
}