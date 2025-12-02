package com.example.smartair.services;

import com.google.firebase.Timestamp;

public class AlertEvaluator {

    public static boolean isRedZone(double personalBest, double pef) {
        if (personalBest <= 0) return false;
        double ratio = pef / personalBest;
        return ratio < 0.50;
    }

    public static boolean isWorseAfterDose(String before, String after) {
        int b = toScale(before);
        int a = toScale(after);
        return a < b;
    }

    private static int toScale(String f) {
        if (f == null) return 0;
        f = f.toLowerCase();
        if (f.equals("great")) return 4;
        if (f.equals("good")) return 3;
        if (f.equals("okay")) return 2;
        if (f.equals("bad")) return 1;
        if (f.equals("awful")) return 0;
        return 0;
    }

    public static boolean isRapidRescue(Timestamp[] times) {
        for (int i = 0; i < times.length; i++) {
            int count = 1;
            long base = times[i].toDate().getTime();
            for (int j = i + 1; j < times.length; j++) {
                long d = Math.abs(times[j].toDate().getTime() - base);
                if (d <= 10800000) count++;
            }
            if (count >= 3) return true;
        }
        return false;
    }

    public static boolean isLowInventory(int remaining, int total) {
        if (total <= 0) return false;
        double pct = (double) remaining / total;
        return pct <= 0.20;
    }
}
