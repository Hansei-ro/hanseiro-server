package org.hanseiro.server.domain.participant.repository;

import org.hanseiro.server.domain.participant.model.entity.MatchingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MatchingParticipantRepository extends JpaRepository<MatchingParticipant, Long> {

  // 현재 진행 중인(끝나지 않은) 매칭 찾기
  @Query("SELECT p FROM MatchingParticipant p JOIN FETCH p.matchingRoom m " +
      "WHERE p.user.id = :userId AND m.status <> 'FINISHED' AND m.status <> 'CANCELLED'")
  Optional<MatchingParticipant> findByUserIdAndRoomStatusNotFinished(@Param("userId") Long userId);
}