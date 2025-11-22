package com.example.smartair.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class InputValidator {

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email)
                && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        // At least one non-alphanumeric (special) character
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

    public static boolean isValidPAC(String pac) {
        if (TextUtils.isEmpty(pac)) {
            return false;
        }
        // Example: PA-7FQ2-91 → "PA-" + 4 chars + "-" + 2 chars
        // Tweak pattern if you want.
        return pac.matches("PA-[A-Z0-9]{4}-[A-Z0-9]{2}");
    }
}
