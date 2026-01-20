package org.hanseiro.server.domain.matchingroom.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hanseiro.server.domain.matchingroom.dto.MatchingResponse;
import org.hanseiro.server.domain.matchingroom.model.DepartureStation;
import org.hanseiro.server.domain.matchingroom.model.MatchingStatus;
import org.hanseiro.server.domain.matchingroom.model.entity.MatchingRoom;
import org.hanseiro.server.domain.matchingroom.repository.MatchingRoomRepository;
import org.hanseiro.server.domain.participant.model.entity.MatchingParticipant;
import org.hanseiro.server.domain.participant.repository.MatchingParticipantRepository;
import org.hanseiro.server.domain.user.model.entity.UserEntity;
import org.hanseiro.server.domain.user.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

  private final RedisTemplate<String, String> redisTemplate;
  private final MatchingRoomRepository matchingRoomRepository;
  private final MatchingParticipantRepository matchingParticipantRepository;
  private final UserRepository userRepository;

  private static final int TARGET_COUNT = 4;

  /**
   * 매칭 요청 변경:
   * - DB의 WAITING 방을 찾지 않고 항상 Redis ZSet에 추가
   * - ZSet 크기 충족 시 Redis에서 꺼내 DB 방 생성 및 참가자 저장
   */
  @Transactional
  public MatchingResponse.RequestResultDto joinQueue(Long userId, DepartureStation station) {
    UserEntity user = getUser(userId);

    String key = "waiting:" + station.name();
    redisTemplate.opsForZSet().add(key, String.valueOf(userId), System.currentTimeMillis());
    log.info("User {} added to Redis queue {}", userId, station);

    Long count = redisTemplate.opsForZSet().zCard(key);
    if (count != null && count >= TARGET_COUNT) {
      MatchingRoom newRoom = createRoomFromRedis(station, key);
      return MatchingResponse.RequestResultDto.builder()
          .matchingId(newRoom.getId())
          .myStatus(MatchingStatus.MATCHING_COMPLETED.name())
          .build();
    }

    return MatchingResponse.RequestResultDto.builder()
        .myStatus(MatchingStatus.WAITING.name())
        .build();
  }

  /**
   * Redis -> MySQL 방 생성
   * - Redis에서 상위 TARGET_COUNT 유저를 꺼내고 제거
   * - MatchingRoom 생성 후 각 MatchingParticipant를 repository에 저장
   */
  private MatchingRoom createRoomFromRedis(DepartureStation station, String key) {
    Set<String> userIds = redisTemplate.opsForZSet().range(key, 0, TARGET_COUNT - 1);
    if (userIds == null || userIds.isEmpty()) {
      throw new IllegalStateException("No users in Redis to create room");
    }

    // Redis에서 제거 (가능한 경우에만)
    redisTemplate.opsForZSet().remove(key, userIds.toArray());
    log.info("Removed users {} from Redis key {}", userIds, key);

    MatchingRoom room = MatchingRoom.create(station);
    room.completeMatching(); // 4명 모인 방은 바로 완료 상태
    matchingRoomRepository.save(room);

    for (String idStr : userIds) {
      UserEntity u = getUser(Long.valueOf(idStr));
      MatchingParticipant participant = MatchingParticipant.create(room, u);
      matchingParticipantRepository.save(participant); // 저장 추가
    }

    // 필요하면 방을 다시 저장하여 연관관계 반영
    matchingRoomRepository.save(room);
    log.info("Created room {} with participants {}", room.getId(), userIds);
    return room;
  }

  /**
   * Redis에서 현재 있는 인원 수(최소 2 이상일 때) 만큼 방 생성 — 강제 출발용
   */
  @Transactional
  public MatchingRoom createRoomFromRedisWithCount(DepartureStation station, int createCount) {
    String key = "waiting:" + station.name();
    Set<String> userIds = redisTemplate.opsForZSet().range(key, 0, createCount - 1);
    if (userIds == null || userIds.isEmpty()) {
      throw new IllegalStateException("No users in Redis to create partial room");
    }

    redisTemplate.opsForZSet().remove(key, userIds.toArray());
    log.info("Removed users {} from Redis key {} for forced start", userIds, key);

    MatchingRoom room = MatchingRoom.create(station);
    // 강제출발로 만들어진 방은 상황에 따라 바로 MATCHING_COMPLETED로 처리하거나
    // 이후 로직에서 상태를 변경하도록 결정할 수 있음. 여기서는 바로 완료 상태로 두지 않음.
    matchingRoomRepository.save(room);

    for (String idStr : userIds) {
      UserEntity u = getUser(Long.valueOf(idStr));
      MatchingParticipant participant = MatchingParticipant.create(room, u);
      matchingParticipantRepository.save(participant);
    }

    matchingRoomRepository.save(room);
    log.info("Created partial room {} with participants {}", room.getId(), userIds);
    return room;
  }


  /**
   * Redis 대기열 취소
   */
  public void cancelQueue(Long userId, DepartureStation station) {
    String key = "waiting:" + station.name();
    redisTemplate.opsForZSet().remove(key, String.valueOf(userId));
    log.info("User {} canceled request from Redis queue {}", userId, station);
  }

  /**
   * 준비 상태 변경
   */
  @Transactional
  public void toggleReady(Long matchingId, Long userId, boolean isReady) {
    MatchingRoom room = getRoom(matchingId);
    MatchingParticipant me = findParticipant(room, userId);
    me.setReady(isReady);
  }

  /**
   * forceStart 변경:
   * - matchingId로 방 조회 실패하면 Redis에서 사용자가 속한 스테이션을 찾아 현재 대기인원(최소 2명)을 기반으로 방 생성
   * - 방 생성 후 기존 검증(최소 2명 및 전원 ready) 수행
   */
  @Transactional
  public void forceStart(Long matchingId, Long userId) {
    MatchingRoom room;
    try {
      room = getRoom(matchingId);
    } catch (IllegalArgumentException e) {
      // DB에 방이 없는 경우: 사용자가 어느 스테이션 대기열에 있는지 확인
      DepartureStation station = findUserWaitingStation(userId);
      if (station == null) {
        throw new IllegalStateException("사용자가 대기열에 없습니다.");
      }
      Long redisCount = getWaitingCount(station);
      if (redisCount < 2) {
        throw new IllegalStateException("최소 2명 이상이어야 출발 가능합니다.");
      }
      int createCount = Math.min(redisCount.intValue(), TARGET_COUNT);
      room = createRoomFromRedisWithCount(station, createCount);
    }

    if (room.getParticipants().size() < 2) {
      throw new IllegalStateException("최소 2명 이상이어야 출발 가능합니다.");
    }
    boolean allReady = room.getParticipants().stream().allMatch(MatchingParticipant::isReady);
    if (!allReady) {
      throw new IllegalStateException("모든 참가자가 준비되어야 합니다.");
    }

    room.updateStatus(MatchingStatus.MATCHING_COMPLETED);
  }

  /**
   * 상세 조회
   */
  public MatchingResponse.RoomDetailDto getMatchingRoomDetail(Long matchingId, Long userId) {
    MatchingRoom room = getRoom(matchingId);

    List<MatchingResponse.MemberDto> members = room.getParticipants().stream()
        .map(p -> MatchingResponse.MemberDto.builder()
            .participantId(p.getId())
            .userId(p.getUser().getId())
            .name(p.getUser().getName())
            .major(p.getUser().getMajor())
            .isReady(p.isReady())
            .joinedAt(p.getJoinedAt())
            .build())
        .collect(Collectors.toList());

    return MatchingResponse.RoomDetailDto.builder()
        .matchingId(room.getId())
        .station(room.getDepartureStation())
        .status(room.getStatus())
        .createdAt(room.getCreatedAt())
        .members(members)
        .build();
  }

  /**
   * 대기 인원 조회
   */
  public Long getWaitingCount(DepartureStation station) {
    String key = "waiting:" + station.name();
    Long redisCount = redisTemplate.opsForZSet().zCard(key);
    return redisCount != null ? redisCount : 0L;
  }

  /**
   * 내 상태 조회: DB 참여 확인 -> Redis 대기열 확인
   */
  public MatchingResponse.MyStatusDto getMyMatchingStatus(Long userId) {
    Optional<MatchingParticipant> p = matchingParticipantRepository.findByUserIdAndRoomStatusNotFinished(userId);
    if (p.isPresent()) {
      return MatchingResponse.MyStatusDto.builder()
          .isMatching(true)
          .matchingId(p.get().getMatchingRoom().getId())
          .status(p.get().getMatchingRoom().getStatus())
          .build();
    }

    // Redis 모든 스테이션을 조회해서 대기 여부 확인
    for (DepartureStation station : DepartureStation.values()) {
      String key = "waiting:" + station.name();
      Double score = redisTemplate.opsForZSet().score(key, String.valueOf(userId));
      if (score != null) {
        return MatchingResponse.MyStatusDto.builder()
            .isMatching(true)
            .status(MatchingStatus.WAITING)
            .build();
      }
    }

    return MatchingResponse.MyStatusDto.builder().isMatching(false).build();
  }

  // --- Helper Methods ---
  private UserEntity getUser(Long userId) {
    return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  private MatchingRoom getRoom(Long matchingId) {
    return matchingRoomRepository.findById(matchingId)
        .orElseThrow(() -> new IllegalArgumentException("Room not found"));
  }

  private MatchingParticipant findParticipant(MatchingRoom room, Long userId) {
    return room.getParticipants().stream()
        .filter(p -> p.getUser().getId().equals(userId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("참가자가 아닙니다."));
  }

  /**
   * 사용자가 어느 스테이션 대기열에 있는지 확인
   */
  private DepartureStation findUserWaitingStation(Long userId) {
    for (DepartureStation station : DepartureStation.values()) {
      String key = "waiting:" + station.name();
      Double score = redisTemplate.opsForZSet().score(key, String.valueOf(userId));
      if (score != null) {
        return station;
      }
    }
    return null;
  }

}