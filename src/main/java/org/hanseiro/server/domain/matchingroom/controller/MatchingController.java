package org.hanseiro.server.domain.matchingroom.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.hanseiro.server.domain.matchingroom.dto.MatchingResponse;
import org.hanseiro.server.domain.matchingroom.model.DepartureStation;
import org.hanseiro.server.domain.matchingroom.service.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/matching")
@RequiredArgsConstructor
public class MatchingController {

  private final MatchingService matchingService;

  @Operation(summary = "실시간 매칭 현황 조회", description = "특정 역의 현재 대기 인원 수를 반환합니다.")
  @GetMapping("/status")
  public ResponseEntity<?> getStatus(@RequestParam DepartureStation station) {
    Long count = matchingService.getWaitingCount(station);
    return ResponseEntity.ok(new MatchingResponse.StatusDto(station, count));
  }

  @Operation(summary = "매칭 대기열 등록", description = "사용자가 매칭 대기열에 진입합니다.")
  @PostMapping("/requests")
  public ResponseEntity<?> requestMatching(@RequestBody Map<String, String> body) {
    Long userId = 1L; // TODO: SecurityUtils.getCurrentUserId();
    DepartureStation station = DepartureStation.valueOf(body.get("station"));

    MatchingResponse.RequestResultDto result = matchingService.joinQueue(userId, station);
    return ResponseEntity.ok(Map.of("code", "SUCCESS", "data", result));
  }

  // 3. [DELETE] 매칭 취소 (방 나가기)
  @Operation(summary = "매칭 취소", description = "사용자가 매칭")
  @DeleteMapping("/rooms/{matchingId}/members/me")
  public ResponseEntity<?> cancelMatching(@PathVariable Long matchingId) {
    Long userId = 1L; // TODO: SecurityUtils.getCurrentUserId();
    matchingService.cancelMatching(matchingId, userId);
    return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "매칭이 취소되었습니다."));
  }

  // 4. [PATCH] 준비 완료 상태 변경
  @PatchMapping("/rooms/{matchingId}/status")
  public ResponseEntity<?> toggleReady(@PathVariable Long matchingId, @RequestBody Map<String, Boolean> body) {
    Long userId = 1L;
    boolean isReady = body.get("isReady");
    matchingService.toggleReady(matchingId, userId, isReady);
    return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "상태가 변경되었습니다."));
  }

  // 5. [POST] 강제 출발 (현재 인원으로 확정)
  @PostMapping("/rooms/{matchingId}/force-start")
  public ResponseEntity<?> forceStart(@PathVariable Long matchingId) {
    Long userId = 1L;
    matchingService.forceStart(matchingId, userId);
    return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "매칭이 확정되었습니다."));
  }

  // 6. [GET] 매칭방 상세 조회 (Polling)
  @GetMapping("/rooms/{matchingId}")
  public ResponseEntity<?> getMatchingRoom(@PathVariable Long matchingId) {
    Long userId = 1L;
    MatchingResponse.RoomDetailDto detail = matchingService.getMatchingRoomDetail(matchingId, userId);
    return ResponseEntity.ok(Map.of("code", "SUCCESS", "data", detail));
  }

  // 7. [GET] 내 매칭 상태 확인 (앱 실행 시)
  @GetMapping("/my-status")
  public ResponseEntity<?> getMyStatus() {
    Long userId = 1L;
    MatchingResponse.MyStatusDto status = matchingService.getMyMatchingStatus(userId);
    return ResponseEntity.ok(Map.of("code", "SUCCESS", "data", status));
  }

  // 8. [GET] 매칭 이력 조회 (마이페이지) - 구현 생략 (Repository 단순 조회)
  @GetMapping("/history")
  public ResponseEntity<?> getHistory() {
    return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "이력 조회 기능은 준비 중입니다."));
  }
}