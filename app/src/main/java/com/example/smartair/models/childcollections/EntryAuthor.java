package com.example.smartair.models.childcollections;

/**
 * Purpose: Represents the nested entryAuthor map in a
 *          check-in document.
 * Layer: Model
 * Used For: Tracking who submitted a check-in (child/parent).
 */
public class EntryAuthor {

    private String name;
    private String role;   // e.g. "child" or "parent"

    /** Required for Firebase deserialization. */
    public EntryAuthor() {
    }

    public EntryAuthor(String name, String role) {
        this.name = name;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}