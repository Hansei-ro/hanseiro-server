package org.hanseiro.server.domain.matchingroom.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hanseiro.server.domain.matchingroom.model.DepartureStation;
import org.hanseiro.server.domain.matchingroom.model.MatchingStatus;
import org.hanseiro.server.domain.participant.model.entity.MatchingParticipant;
import org.hanseiro.server.global.model.BaseTimeEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "matching_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingRoom extends BaseTimeEntity {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "matching_room_id")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DepartureStation departureStation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MatchingStatus status;

  // 양방향 연관관계 설정
  @OneToMany(mappedBy = "matchingRoom", cascade = CascadeType.ALL)
  private List<MatchingParticipant> participants = new ArrayList<>();

  private LocalDateTime startedAt;

  // --- 생성 메서드 ---
  public static MatchingRoom create(DepartureStation station) {
    MatchingRoom room = new MatchingRoom();
    room.departureStation = station;
    room.status = MatchingStatus.WAITING;
    return room;
  }

  // --- 비즈니스 로직 ---
  public void addParticipant(MatchingParticipant participant) {
    this.participants.add(participant);
  }

  public void completeMatching() {
    this.status = MatchingStatus.MATCHING_COMPLETED;
  }
  public void updateStatus(MatchingStatus status) {
    this.status = status;
  }
}