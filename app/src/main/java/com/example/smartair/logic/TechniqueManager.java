package com.example.smartair.logic;

import com.example.smartair.models.TechniqueSession;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Purpose: R3 Technique helper logic (track sessions & high-quality flags).
 * Layer: Logic/Service
 */
public class TechniqueManager {

    private final List<TechniqueSession> sessions = new ArrayList<>();

    /**
     * Start a new technique session for the child.
     */
    public TechniqueSession startSession(String childUid) {

        String id = UUID.randomUUID().toString();

        TechniqueSession session = new TechniqueSession(
                id,
                childUid,
                Timestamp.now()
        );

        sessions.add(session);

        return session;
    }

    public TechniqueSession getSession(String sessionId) {
        for (TechniqueSession s : sessions) {
            if (sessionId.equals(s.getSessionId())) {
                return s;
            }
        }
        return null;
    }

    public List<TechniqueSession> getSessionsForChild(String childUid) {
        List<TechniqueSession> result = new ArrayList<>();

        for (TechniqueSession s : sessions) {
            if (childUid.equals(s.getChildUid())) {
                result.add(s);
            }
        }

        return result;
    }
}
