package com.codesync.collab.service;

import com.codesync.collab.entity.CollabSession;
import com.codesync.collab.entity.Participant;
import com.codesync.collab.repository.CollabRepository;
import com.codesync.collab.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollabServiceImpl implements CollabService {

    private final CollabRepository collabRepository;
    private final ParticipantRepository participantRepository;

    private static final String[] CURSOR_COLORS = {
        "#00d4ff", "#00ffa3", "#ff6b9d", "#ffd60a",
        "#ff6b35", "#a78bfa", "#f97316", "#ec4899"
    };

    @Override
    @Transactional
    public CollabSession createSession(CollabSession session) {
        if (session.getSessionId() == null || session.getSessionId().isBlank()) {
            session.setSessionId(UUID.randomUUID().toString());
        }
        session.setStatus(CollabSession.SessionStatus.ACTIVE);
        session.setLastActivityAt(LocalDateTime.now());
        CollabSession saved = collabRepository.save(session);

        // Auto-add owner as HOST participant
        long count = participantRepository.countBySessionIdAndIsActiveTrue(saved.getSessionId());
        Participant host = Participant.builder()
                .sessionId(saved.getSessionId())
                .userId(saved.getOwnerId())
                .role(Participant.ParticipantRole.HOST)
                .color(CURSOR_COLORS[(int)(count % CURSOR_COLORS.length)])
                .isActive(true)
                .build();
        participantRepository.save(host);
        return saved;
    }

    @Override
    public CollabSession getSessionById(String sessionId) {
        return collabRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
    }

    @Override
    public List<CollabSession> getSessionsByProject(String projectId) {
        return collabRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional
    public Participant joinSession(String sessionId, String userId, String password) {
        CollabSession session = getSessionById(sessionId);
        if (session.getStatus() == CollabSession.SessionStatus.ENDED) {
            throw new RuntimeException("Session has ended");
        }
        if (session.isPasswordProtected()) {
            if (!session.getSessionPassword().equals(password)) {
                throw new RuntimeException("Incorrect session password");
            }
        }
        long count = participantRepository.countBySessionIdAndIsActiveTrue(sessionId);
        if (count >= session.getMaxParticipants()) {
            throw new RuntimeException("Session is full");
        }

        // Check if already in session
        return participantRepository.findBySessionIdAndUserId(sessionId, userId)
                .map(p -> { p.setActive(true); p.setLeftAt(null); return participantRepository.save(p); })
                .orElseGet(() -> {
                    Participant p = Participant.builder()
                            .sessionId(sessionId)
                            .userId(userId)
                            .role(Participant.ParticipantRole.EDITOR)
                            .color(CURSOR_COLORS[(int)(count % CURSOR_COLORS.length)])
                            .isActive(true)
                            .build();
                    session.setLastActivityAt(LocalDateTime.now());
                    collabRepository.save(session);
                    return participantRepository.save(p);
                });
    }

    @Override
    @Transactional
    public void leaveSession(String sessionId, String userId) {
        participantRepository.findBySessionIdAndUserId(sessionId, userId)
                .ifPresent(p -> {
                    p.setActive(false);
                    p.setLeftAt(LocalDateTime.now());
                    participantRepository.save(p);
                });
    }

    @Override
    @Transactional
    public void endSession(String sessionId) {
        CollabSession session = getSessionById(sessionId);
        session.setStatus(CollabSession.SessionStatus.ENDED);
        session.setEndedAt(LocalDateTime.now());
        collabRepository.save(session);
        // Deactivate all participants
        participantRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .forEach(p -> { p.setActive(false); p.setLeftAt(LocalDateTime.now()); participantRepository.save(p); });
    }

    @Override
    public List<Participant> getParticipants(String sessionId) {
        return participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
    }

    @Override
    @Transactional
    public void updateCursor(String sessionId, String userId, int line, int col) {
        participantRepository.findBySessionIdAndUserId(sessionId, userId)
                .ifPresent(p -> {
                    p.setCursorLine(line);
                    p.setCursorCol(col);
                    participantRepository.save(p);
                });
        CollabSession session = getSessionById(sessionId);
        session.setLastActivityAt(LocalDateTime.now());
        collabRepository.save(session);
    }

    @Override
    @Transactional
    public void kickParticipant(String sessionId, String participantId) {
        participantRepository.findById(participantId)
                .ifPresent(p -> {
                    p.setActive(false);
                    p.setLeftAt(LocalDateTime.now());
                    participantRepository.save(p);
                });
    }

    @Override
    public List<CollabSession> getActiveSessionsAll() {
        return collabRepository.findByStatus(CollabSession.SessionStatus.ACTIVE);
    }

    // Scheduled: auto-end sessions idle for 30 minutes
    @Override
    @Scheduled(fixedDelay = 300000) // every 5 minutes
    @Transactional
    public void endInactiveSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        List<CollabSession> inactive = collabRepository.findInactiveSessions(cutoff);
        inactive.forEach(s -> {
            log.info("Auto-ending inactive session: {}", s.getSessionId());
            endSession(s.getSessionId());
        });
    }
}
