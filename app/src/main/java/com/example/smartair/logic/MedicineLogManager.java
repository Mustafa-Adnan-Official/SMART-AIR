package com.example.smartair.logic;

import com.example.smartair.models.MedicineLog;
import com.example.smartair.models.PrePostStatus;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Purpose: R3 Medicine logs + Pre/Post checks (in-memory implementation).
 * Layer: Logic/Service
 */
public class MedicineLogManager {

    private final List<MedicineLog> logs = new ArrayList<>();

    /**
     * Log a new rescue or controller dose.
     */
    public MedicineLog logDose(String childUid,
                               String medicineType,
                               int doseCount,
                               Timestamp takenAt) {

        String logId = UUID.randomUUID().toString();

        MedicineLog log = new MedicineLog(
                logId,
                childUid,
                medicineType,
                doseCount,
                takenAt
        );

        logs.add(log);

        return log;
    }

    /**
     * Attach the "before dose" check.
     */
    public void setPreCheck(String logId,
                            String status,
                            Integer breathRating) {

        MedicineLog log = findById(logId);

        if (log != null) {
            log.setPreStatus(status);
            log.setPreBreathRating(breathRating);
        }
    }

    /**
     * Attach the "after dose" check.
     */
    public void setPostCheck(String logId,
                             String status,
                             Integer breathRating) {

        MedicineLog log = findById(logId);

        if (log != null) {
            log.setPostStatus(status);
            log.setPostBreathRating(breathRating);
        }
    }

    public List<MedicineLog> getLogsForChild(String childUid) {
        List<MedicineLog> result = new ArrayList<>();

        for (MedicineLog log : logs) {
            if (childUid.equals(log.getChildUid())) {
                result.add(log);
            }
        }

        return result;
    }

    private MedicineLog findById(String id) {
        for (MedicineLog log : logs) {
            if (id.equals(log.getLogId())) {
                return log;
            }
        }
        return null;
    }
}
