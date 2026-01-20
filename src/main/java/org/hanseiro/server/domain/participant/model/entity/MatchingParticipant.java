package org.hanseiro.server.domain.participant.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.apache.catalina.User;
import org.hanseiro.server.domain.matchingroom.model.entity.MatchingRoom; // [중요] 타 패키지 Import
import org.hanseiro.server.domain.user.model.entity.UserEntity;
import org.hanseiro.server.global.model.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "matching_participant")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingParticipant extends BaseTimeEntity {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "matching_participant_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "matching_room_id")
  private MatchingRoom matchingRoom;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private UserEntity user;

  private boolean isReady; // UI: 준비 완료 체크박스

  private LocalDateTime joinedAt;

  public static MatchingParticipant create(MatchingRoom room, UserEntity user) {
    MatchingParticipant participant = new MatchingParticipant();
    participant.matchingRoom = room;
    participant.user = user;
    participant.isReady = false; // 기본값: 미준비
    participant.joinedAt = LocalDateTime.now();

    room.addParticipant(participant); // 연관관계 편의 메소드 호출
    return participant;
  }

  public void toggleReady() {
    this.isReady = !this.isReady;
  }

  public void setReady(boolean ready){
    this.isReady = ready;
  }
}