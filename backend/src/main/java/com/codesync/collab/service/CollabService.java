package com.codesync.collab.service;

import com.codesync.collab.entity.CollabSession;
import com.codesync.collab.entity.Participant;
import java.util.List;

public interface CollabService {
    CollabSession createSession(CollabSession session);
    CollabSession getSessionById(String sessionId);
    List<CollabSession> getSessionsByProject(String projectId);
    Participant joinSession(String sessionId, String userId, String password);
    void leaveSession(String sessionId, String userId);
    void endSession(String sessionId);
    List<Participant> getParticipants(String sessionId);
    void updateCursor(String sessionId, String userId, int line, int col);
    void kickParticipant(String sessionId, String participantId);
    List<CollabSession> getActiveSessionsAll();
    void endInactiveSessions();
}
