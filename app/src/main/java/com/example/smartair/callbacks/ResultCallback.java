package com.example.smartair.callbacks;

/**
 * Purpose: Generic callback for Firebase operations that return data.
 * Layer: Model → Presenter
 * Used For: Fetching objects/lists from Firestore.
 */
public interface ResultCallback<T> {

    /** Data successfully fetched. */
    void onSuccess(T result);

    /** Data fetch failed. */
    void onError(Exception e);
}