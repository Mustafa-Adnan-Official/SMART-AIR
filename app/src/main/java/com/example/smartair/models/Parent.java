package com.example.smartair.models;

import java.util.List;

/**
 * Firestore model for a Parent user.
 *
 * Firestore Path:
 *   parents/{parentUid}
 *
 * Fields expected in Firestore:
 *   - name: String
 *   - email: String
 *   - role: "parent"
 *   - parentAccessCode: String
 *   - createdAtMillis: long
 *   - childUID: List<String> (list of child UIDs linked to this parent)
 *
 * Notes:
 *   - Firestore requires: public fields + empty constructor.
 *   - You may add getters/setters later if you want stricter encapsulation.
 */
public class Parent {

    // --------------------
    // Firestore-mapped fields
    // --------------------
    public String name;
    public String email;
    public String role;                // always "parent"
    public String parentAccessCode;
    public long createdAtMillis;

    // List of children linked to this parent
    // This matches your Firestore "childUID" array exactly.
    public List<String> childUID;

    // --------------------
    // Empty constructor required by Firestore
    // --------------------
    public Parent() { }

    // --------------------
    // Convenience constructor for your own usage
    // --------------------
    public Parent(String name,
                  String email,
                  String role,
                  String parentAccessCode,
                  long createdAtMillis,
                  List<String> childUID) {

        this.name = name;
        this.email = email;
        this.role = role;
        this.parentAccessCode = parentAccessCode;
        this.createdAtMillis = createdAtMillis;
        this.childUID = childUID;
    }
}