package org.hanseiro.server.domain.matchingroom.repository;

import org.hanseiro.server.domain.matchingroom.model.DepartureStation;
import org.hanseiro.server.domain.matchingroom.model.MatchingStatus;
import org.hanseiro.server.domain.matchingroom.model.entity.MatchingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchingRoomRepository extends JpaRepository<MatchingRoom, Long> {
  Optional<MatchingRoom> findFirstByDepartureStationAndStatusOrderByCreatedAtAsc(
      DepartureStation departureStation,
      MatchingStatus status
  );
}
