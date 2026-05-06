package com.codesync.collab.service;

import com.codesync.collab.entity.CollabSession;
import com.codesync.collab.entity.Participant;
import java.util.List;

public interface CollabService {
    CollabSession createSession(CollabSession session);
    CollabSession getSessionById(String sessionId);
    List<CollabSession> getSessionsByProject(String projectId);
    // FIX 5: username param added so it can be persisted with the participant record
    Participant joinSession(String sessionId, String userId, String username, String password);
    void leaveSession(String sessionId, String userId);
    void endSession(String sessionId);
    List<Participant> getParticipants(String sessionId);
    void updateCursor(String sessionId, String userId, int line, int col);
    void kickParticipant(String sessionId, String participantId);
    List<CollabSession> getActiveSessionsAll();
    void endInactiveSessions();
}
