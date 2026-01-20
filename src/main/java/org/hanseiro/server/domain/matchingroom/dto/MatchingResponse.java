package org.hanseiro.server.domain.matchingroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hanseiro.server.domain.matchingroom.model.DepartureStation;
import org.hanseiro.server.domain.matchingroom.model.MatchingStatus;

import java.time.LocalDateTime;
import java.util.List;

public class MatchingResponse {

  @Data
  @AllArgsConstructor
  public static class StatusDto {
    private DepartureStation station;
    private Long currentCount;
  }

  @Data
  @Builder
  public static class RequestResultDto {
    private Long matchingId;
    private Long participantId;
    private String myStatus; // WAITING, MATCHING_COMPLETED
  }

  @Data
  @Builder
  public static class RoomDetailDto {
    private Long matchingId;
    private DepartureStation station;
    private MatchingStatus status;
    private LocalDateTime createdAt;
    private List<MemberDto> members;
  }

  @Data
  @Builder
  public static class MemberDto {
    private Long participantId;
    private Long userId;
    private String name;
    private String major;
    private boolean isReady;
    private LocalDateTime joinedAt;
  }

  @Data
  @Builder
  public static class MyStatusDto {
    private boolean isMatching;
    private Long matchingId;
    private MatchingStatus status;
  }
}