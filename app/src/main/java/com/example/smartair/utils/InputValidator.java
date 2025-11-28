package com.example.smartair.utils;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * Purpose: Centralized input validation helpers used by signup/login presenters.
 * Layer: Utils
 * Notes:
 *  - Keeps Activities/Presenters clean by isolating validation logic.
 *  - Methods return simple boolean values for MVP flow control.
 */
public class InputValidator {

    /** Valid email using Android's built-in email pattern. */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email)
                && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Valid password rules:
     *  - at least 6 characters
     *  - must contain at least one non-alphanumeric character
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        boolean hasSpecial = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                hasSpecial = true;
                break;
            }
        }
        return hasSpecial;
    }

    /**
     * Valid name rules:
     *  - not empty
     *  - cannot contain digits
     */
    public static boolean isValidName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            if (Character.isDigit(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Valid Parent Access Code (PAC) format:
     *  "PA-" + 4 alphanumeric chars + "-" + 2 alphanumeric chars.
     *  Example: PA-7FQ2-91
     */
    public static boolean isValidPAC(String pac) {
        if (TextUtils.isEmpty(pac)) {
            return false;
        }
        return pac.matches("PA-[A-Z0-9]{4}-[A-Z0-9]{2}");
    }
}