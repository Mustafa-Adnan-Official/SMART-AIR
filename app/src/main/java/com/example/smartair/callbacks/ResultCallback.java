package com.example.smartair.callbacks;

/*
 * Generic callback for Firebase operations that DO return data.
 *
 * T is the type you expect back, e.g.:
 * - ResultCallback<Parent>
 * - ResultCallback<List<Child>>
 *
 * TODO:
 *  - Use this in service methods that need to pass back objects from Firestore:
 *      - Fetching children list for a parent
 *      - Fetching provider’s parents/children lists
 *      - Fetching a DailyReport, etc.
 */
public interface ResultCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}