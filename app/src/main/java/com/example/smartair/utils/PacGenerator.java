package com.example.smartair.utils;

/*
 * Utility class for generating Parent Access Codes (PAC).
 *
 * Responsibilities:
 *  - Generate a random string in a format like: "AB3D-9K2L-7P0X"
 *  - Ensure it's reasonably hard to guess (use SecureRandom later).
 *
 * Usage (later):
 *  - When a parent registers, AccountService will call:
 *        String pac = PacGenerator.generatePac();
 *    and store it in the "parents/{parentUid}" document.
 *
 * TODO:
 *  - Implement a static method:
 *        public static String generatePac()
 *    that returns a new random PAC each time.
 */
public class PacGenerator {
    // TODO: Add characters set and implementation here later.
}