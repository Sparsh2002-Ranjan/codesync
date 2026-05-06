package com.codesync.collab.repository;

import com.codesync.collab.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, String> {

    List<Participant> findBySessionId(String sessionId);

    List<Participant> findBySessionIdAndIsActiveTrue(String sessionId);

    Optional<Participant> findBySessionIdAndUserId(String sessionId, String userId);

    long countBySessionIdAndIsActiveTrue(String sessionId);

    List<Participant> findByUserId(String userId);
}
