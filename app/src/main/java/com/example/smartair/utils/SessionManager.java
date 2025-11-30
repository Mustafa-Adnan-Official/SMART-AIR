package com.example.smartair.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smartair.models.users.RoleType;

/**
 * Purpose: Simple SharedPreferences wrapper for storing session-related metadata.
 * Layer: Utils
 * Used For:
 *  - Remembering the last signed-in role (Parent / Child / Provider)
 *  - Helping redirects when reopening the app (optional future requirement)
 */
public class SessionManager {

    private static final String PREF_NAME = "smartair_session";
    private static final String KEY_LAST_ROLE = "last_role";

    private final SharedPreferences prefs;

    /** Creates a SharedPreferences instance scoped to the app. */
    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Saves the last known role as a string (PARENT / CHILD / PROVIDER). */
    public void saveLastRole(RoleType role) {
        if (role == null) {
            return;
        }
        prefs.edit()
                .putString(KEY_LAST_ROLE, role.name())
                .apply();
    }

    /** Retrieves the last stored role, or null if none was saved. */
    public RoleType getLastRole() {
        String stored = prefs.getString(KEY_LAST_ROLE, null);
        if (stored == null) {
            return null;
        }
        try {
            return RoleType.valueOf(stored);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Clears all stored session data (logout or cleanup). */
    public void clear() {
        prefs.edit().clear().apply();
    }
}