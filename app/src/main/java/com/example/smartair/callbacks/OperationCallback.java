package com.example.smartair.callbacks;

/**
 * Purpose: Generic success/failure for write-only Firebase operations.
 * Layer: Model → Presenter
 * Used For: Creating/updating docs when no return value is needed.
 */
public interface OperationCallback {

    /** Write succeeded. */
    void onSuccess();

    /** Write failed. */
    void onError(Exception e);
}