package com.example.smartair.callbacks;

/**
 * Generic callback for Firestore operations that return a model or value.
 */
public interface ModelResultCallback<T> {

    void onSuccess(T result);

    void onFailure(String errorMessage);
}
