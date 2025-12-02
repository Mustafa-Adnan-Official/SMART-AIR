package com.example.smartair.services;

/**
 * Purpose: Computes the PEF zone from a personal best and a PEF value.
 * Layer: Service (pure logic)
 * Used For: Child and Parent home tiles, reports, and triage.
 */
public class ZoneService {

    public enum PefZone {
        GREEN,
        YELLOW,
        RED,
        UNKNOWN
    }

    public static class ZoneResult {
        private final PefZone zone;
        private final int percentOfPb;
        private final Long rawPef;

        public ZoneResult(PefZone zone, int percentOfPb, Long rawPef) {
            this.zone = zone;
            this.percentOfPb = percentOfPb;
            this.rawPef = rawPef;
        }

        public PefZone getZone() {
            return zone;
        }

        public int getPercentOfPb() {
            return percentOfPb;
        }

        public Long getRawPef() {
            return rawPef;
        }
    }

    /**
     * Computes the zone given a PEF and personal best.
     * PB must be > 0 and PEF must be non-null to get a real zone.
     */
    public static ZoneResult computeZone(Long peakFlow, int personalBest) {
        if (peakFlow == null || personalBest <= 0) {
            return new ZoneResult(PefZone.UNKNOWN, -1, peakFlow);
        }

        double percent = (peakFlow * 100.0) / personalBest;
        int roundedPercent = (int) Math.round(percent);

        PefZone zone;
        if (percent >= 80.0) {
            zone = PefZone.GREEN;
        } else if (percent >= 50.0) {
            zone = PefZone.YELLOW;
        } else {
            zone = PefZone.RED;
        }

        return new ZoneResult(zone, roundedPercent, peakFlow);
    }
}
