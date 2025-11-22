package com.example.smartair.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smartair.models.RoleType;

public class SessionManager {

    private static final String PREF_NAME = "smartair_session";
    private static final String KEY_LAST_ROLE = "last_role";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveLastRole(RoleType role) {
        if (role == null) {
            return;
        }
        prefs.edit()
                .putString(KEY_LAST_ROLE, role.name())
                .apply();
    }

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

    public void clear() {
        prefs.edit().clear().apply();
    }
}
