package com.example.smartair.utils;

import java.security.SecureRandom;
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

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    int length = 8;

    private SecureRandom random;
    public PacGenerator(){
        this.random = new SecureRandom();
    }

    private String PacCode(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        String raw_code = sb.toString();
        String final_code = raw_code.substring(0, 2) + "-" +
                raw_code.substring(2, 6) + "-" +
                raw_code.substring(6, 8);


        return final_code;
    }

    public String getPacCode(){
        return PacCode();
    }




}


