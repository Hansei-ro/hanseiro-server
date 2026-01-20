package org.hanseiro.server.domain.matchingroom.model;

public enum MatchingStatus {
  WAITING,            // 대기 중
  MATCHING_COMPLETED, // 매칭 완료 (인원 충족/강제 출발)
  STARTED,            // 운행 시작
  FINISHED,           // 운행 종료
  CANCELLED           // 취소됨
}