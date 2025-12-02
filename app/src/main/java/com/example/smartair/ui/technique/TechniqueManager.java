package com.example.smartair.ui.technique;

/**
 * Pure UI-flow logic for the technique helper.
 * No Firebase here.
 */
public class TechniqueManager {

    public enum Step {
        START,
        SEAL_LIPS,
        SLOW_DEEP_BREATH,
        HOLD_FOR_10_SECONDS,
        WAIT_BETWEEN_PUFFS,
        SPACER_MASK_TIPS,
        DONE
    }

    public Step getFirstStep() {
        return Step.START;
    }

    public Step getNextStep(Step current) {
        switch (current) {
            case START:               return Step.SEAL_LIPS;
            case SEAL_LIPS:           return Step.SLOW_DEEP_BREATH;
            case SLOW_DEEP_BREATH:    return Step.HOLD_FOR_10_SECONDS;
            case HOLD_FOR_10_SECONDS: return Step.WAIT_BETWEEN_PUFFS;
            case WAIT_BETWEEN_PUFFS:  return Step.SPACER_MASK_TIPS;
            case SPACER_MASK_TIPS:
            default:                  return Step.DONE;
        }
    }

    public boolean isLastStep(Step step) {
        return step == Step.DONE;
    }
}
