package com.example.smartair.models.childcollections;

/**
 * Purpose: Represents the zoneDistribution map in a child report.
 * Layer: Model
 * Used For: Showing the count of green, yellow, and red zone days.
 */
public class ZoneDistribution {

    private Long greenDays;
    private Long yellowDays;
    private Long redDays;

    /** Required for Firebase deserialization. */
    public ZoneDistribution() {
    }

    public ZoneDistribution(Long greenDays, Long yellowDays, Long redDays) {
        this.greenDays = greenDays;
        this.yellowDays = yellowDays;
        this.redDays = redDays;
    }

    public Long getGreenDays() {
        return greenDays;
    }

    public void setGreenDays(Long greenDays) {
        this.greenDays = greenDays;
    }

    public Long getYellowDays() {
        return yellowDays;
    }

    public void setYellowDays(Long yellowDays) {
        this.yellowDays = yellowDays;
    }

    public Long getRedDays() {
        return redDays;
    }

    public void setRedDays(Long redDays) {
        this.redDays = redDays;
    }
}