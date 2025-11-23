package com.example.smartair.utils; // i.e. the following code belongs to  com.example.smartair.models

import java.util.Random;

public class ParentInviteFlow {
    private String characters;
    private Random random;

    public ParentInviteFlow() {
        characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ23456789";
        random = new Random();
    }

    public String generateCode() {
        StringBuilder code = new StringBuilder();
        int length = 7;

        int i;
        int index;
        for (i=0; i<length; i++) {
            index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }
}