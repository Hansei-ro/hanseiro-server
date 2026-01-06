# 매칭 (Matching) API

> Base URL: `/api/v1/matching`  
> 담당자: Backend Team  
> 최종 수정일: 2025.01.06

---

## API 목록

1. [매칭 요청](#post-apiv1matching)
2. [매칭 대기열 상태 조회](#get-apiv1matchingqueue)
3. [매칭 룸 상태 조회](#get-apiv1matchingroomroomid)
4. [준비 완료 토글](#patch-apiv1matchingroomroomidready)
5. [매칭 취소/나가기](#delete-apiv1matching)

---

## `POST` /api/v1/matching

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 택시 카풀 매칭 대기열에 등록 |
| **인증** | Required |
| **권한** | USER |

### Method 선택 이유
> POST - 매칭 대기열에 새로운 요청(리소스)을 생성하는 행위이므로 POST 사용.  
> 동일한 요청을 여러 번 보내면 중복 등록될 수 있으므로 멱등성이 없음.

---

### Request

#### Headers
| Key | Value | Required | Description |
|-----|-------|----------|-------------|
| Authorization | Bearer {token} | O | JWT 액세스 토큰 |
| Content-Type | application/json | O | - |

#### Request Body
```json
{
  "departureStation": "SANBON",
  "expectedDepartureTime": "2025-01-15T09:00:00"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| departureStation | String | O | 출발역 (SANBON / GEUMJEONG) |
| expectedDepartureTime | DateTime | X | 예상 출발 시간 (미입력 시 즉시 매칭) |

---

### Response

#### 성공 (201 Created)
```json
{
  "code": "SUCCESS",
  "message": "매칭 대기열에 등록되었습니다.",
  "data": {
    "matchingRequestId": 123,
    "departureStation": "SANBON",
    "status": "WAITING",
    "queuePosition": 3,
    "estimatedWaitTime": 5,
    "createdAt": "2025-01-15T08:55:00"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| matchingRequestId | Long | 매칭 요청 ID |
| departureStation | String | 출발역 (SANBON / GEUMJEONG) |
| status | String | 상태 (WAITING / MATCHED / CANCELLED) |
| queuePosition | Integer | 대기열 순서 |
| estimatedWaitTime | Integer | 예상 대기 시간 (분) |
| createdAt | DateTime | 요청 생성 시간 |

#### 실패 케이스

| Status | Code | Message | Description |
|--------|------|---------|-------------|
| 400 | INVALID_STATION | 올바르지 않은 출발역입니다 | SANBON, GEUMJEONG 외 값 입력 |
| 401 | UNAUTHORIZED | 인증이 필요합니다 | 토큰 없음 또는 만료 |
| 409 | ALREADY_IN_QUEUE | 이미 대기열에 등록되어 있습니다 | 중복 매칭 요청 |
| 409 | ALREADY_IN_ROOM | 이미 매칭룸에 참여 중입니다 | 매칭 완료 상태에서 재요청 |

```json
{
  "code": "INVALID_STATION",
  "message": "올바르지 않은 출발역입니다",
  "errors": [
    {
      "field": "departureStation",
      "message": "출발역은 SANBON 또는 GEUMJEONG이어야 합니다"
    }
  ]
}
```

---

## `GET` /api/v1/matching/queue

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 출발역별 매칭 대기열 현황 조회 (홈 화면용) |
| **인증** | Required |
| **권한** | USER |

### Method 선택 이유
> GET - 대기열 현황 데이터를 조회만 하므로 GET 사용.  
> 서버 상태를 변경하지 않으며, 멱등성이 보장됨.

---

### Request

#### Headers
| Key | Value | Required | Description |
|-----|-------|----------|-------------|
| Authorization | Bearer {token} | O | JWT 액세스 토큰 |

#### Query Parameters
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| station | String | X | ALL | 조회할 출발역 (SANBON / GEUMJEONG / ALL) |

---

### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "대기열 현황을 조회했습니다.",
  "data": {
    "sanbon": {
      "station": "SANBON",
      "waitingCount": 3,
      "updatedAt": "2025-01-15T08:55:00"
    },
    "geumjeong": {
      "station": "GEUMJEONG",
      "waitingCount": 5,
      "updatedAt": "2025-01-15T08:55:00"
    },
    "myStatus": {
      "isInQueue": true,
      "station": "SANBON",
      "queuePosition": 2,
      "matchingRequestId": 123
    }
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| sanbon.station | String | 산본역 출발역 코드 |
| sanbon.waitingCount | Integer | 산본역 대기 인원 수 |
| sanbon.updatedAt | DateTime | 마지막 업데이트 시간 |
| geumjeong.station | String | 금정역 출발역 코드 |
| geumjeong.waitingCount | Integer | 금정역 대기 인원 수 |
| geumjeong.updatedAt | DateTime | 마지막 업데이트 시간 |
| myStatus.isInQueue | Boolean | 현재 사용자의 대기열 등록 여부 |
| myStatus.station | String | 현재 사용자가 대기 중인 출발역 (대기 중일 경우) |
| myStatus.queuePosition | Integer | 현재 사용자의 대기열 순서 (대기 중일 경우) |
| myStatus.matchingRequestId | Long | 현재 사용자의 매칭 요청 ID (대기 중일 경우) |

#### 실패 케이스

| Status | Code | Message | Description |
|--------|------|---------|-------------|
| 400 | INVALID_STATION | 올바르지 않은 출발역입니다 | SANBON, GEUMJEONG, ALL 외 값 |
| 401 | UNAUTHORIZED | 인증이 필요합니다 | 토큰 없음 또는 만료 |

---

## `GET` /api/v1/matching/room/{roomId}

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 매칭 룸 상태 및 참여자 목록 조회 (대기 화면용) |
| **인증** | Required |
| **권한** | USER |

### Method 선택 이유
> GET - 매칭 룸 정보를 조회만 하므로 GET 사용.  
> 서버 상태를 변경하지 않으며, 멱등성이 보장됨.

---

### Request

#### Headers
| Key | Value | Required | Description |
|-----|-------|----------|-------------|
| Authorization | Bearer {token} | O | JWT 액세스 토큰 |

#### Path Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| roomId | Long | O | 매칭 룸 고유 ID |

---

### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "매칭 룸 정보를 조회했습니다.",
  "data": {
    "roomId": 456,
    "departureStation": "SANBON",
    "status": "WAITING",
    "maxParticipants": 4,
    "currentParticipants": 3,
    "hostUserId": 101,
    "createdAt": "2025-01-15T08:55:00",
    "expiresAt": "2025-01-15T09:25:00",
    "participants": [
      {
        "userId": 101,
        "name": "김철수",
        "major": "컴퓨터공학과",
        "isReady": true,
        "isHost": true,
        "joinedAt": "2025-01-15T08:55:00"
      },
      {
        "userId": 102,
        "name": "이영희",
        "major": "경영학과",
        "isReady": true,
        "isHost": false,
        "joinedAt": "2025-01-15T08:56:00"
      },
      {
        "userId": 103,
        "name": "박민수",
        "major": "디자인학과",
        "isReady": false,
        "isHost": false,
        "joinedAt": "2025-01-15T08:57:00"
      }
    ]
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| roomId | Long | 매칭 룸 고유 ID |
| departureStation | String | 출발역 (SANBON / GEUMJEONG) |
| status | String | 룸 상태 (WAITING / READY / DEPARTED / CANCELLED) |
| maxParticipants | Integer | 최대 참여 인원 (기본 4명) |
| currentParticipants | Integer | 현재 참여 인원 |
| hostUserId | Long | 방장 사용자 ID |
| createdAt | DateTime | 룸 생성 시간 |
| expiresAt | DateTime | 룸 만료 시간 |
| participants | Array | 참여자 목록 |
| participants[].userId | Long | 참여자 사용자 ID |
| participants[].name | String | 참여자 이름 |
| participants[].major | String | 참여자 학과 |
| participants[].isReady | Boolean | 준비 완료 상태 |
| participants[].isHost | Boolean | 방장 여부 |
| participants[].joinedAt | DateTime | 참여 시간 |

#### 실패 케이스

| Status | Code | Message | Description |
|--------|------|---------|-------------|
| 401 | UNAUTHORIZED | 인증이 필요합니다 | 토큰 없음 또는 만료 |
| 403 | FORBIDDEN | 접근 권한이 없습니다 | 해당 룸의 참여자가 아님 |
| 404 | ROOM_NOT_FOUND | 매칭 룸을 찾을 수 없습니다 | 존재하지 않는 룸 ID 또는 만료된 룸 |

```json
{
  "code": "ROOM_NOT_FOUND",
  "message": "매칭 룸을 찾을 수 없습니다",
  "errors": [
    {
      "field": "roomId",
      "message": "ID 456에 해당하는 매칭 룸이 존재하지 않거나 만료되었습니다"
    }
  ]
}
```

---

## `PATCH` /api/v1/matching/room/{roomId}/ready

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 매칭 룸 내 사용자의 준비 완료 상태 토글 |
| **인증** | Required |
| **권한** | USER |

### Method 선택 이유
> PATCH - 사용자의 준비 상태라는 부분적인 정보만 수정하므로 PATCH 사용.  
> 동일한 요청을 여러 번 보내도 결과가 같으므로 멱등성이 보장됨.

---

### Request

#### Headers
| Key | Value | Required | Description |
|-----|-------|----------|-------------|
| Authorization | Bearer {token} | O | JWT 액세스 토큰 |
| Content-Type | application/json | O | - |

#### Path Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| roomId | Long | O | 매칭 룸 고유 ID |

#### Request Body
```json
{
  "isReady": true
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| isReady | Boolean | O | 준비 완료 상태 (true: 준비 완료, false: 준비 취소) |

---

### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "준비 상태가 변경되었습니다.",
  "data": {
    "userId": 102,
    "isReady": true,
    "updatedAt": "2025-01-15T09:00:00"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| userId | Long | 사용자 ID |
| isReady | Boolean | 변경된 준비 상태 |
| updatedAt | DateTime | 상태 변경 시간 |

#### 실패 케이스

| Status | Code | Message | Description |
|--------|------|---------|-------------|
| 400 | INVALID_INPUT | 입력값이 올바르지 않습니다 | isReady 필드 누락 또는 형식 오류 |
| 401 | UNAUTHORIZED | 인증이 필요합니다 | 토큰 없음 또는 만료 |
| 403 | FORBIDDEN | 접근 권한이 없습니다 | 해당 룸의 참여자가 아님 |
| 404 | ROOM_NOT_FOUND | 매칭 룸을 찾을 수 없습니다 | 존재하지 않는 룸 ID |
| 409 | ROOM_ALREADY_DEPARTED | 이미 출발한 매칭 룸입니다 | 출발 완료 상태의 룸 |

---

## `DELETE` /api/v1/matching

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 매칭 대기열 취소 또는 매칭 룸 나가기 |
| **인증** | Required |
| **권한** | USER |

### Method 선택 이유
> DELETE - 대기열 또는 매칭 룸에서 사용자의 참여를 삭제하는 행위이므로 DELETE 사용.  
> 동일한 요청을 여러 번 보내도 결과가 같으므로 멱등성이 보장됨.

---

### Request

#### Headers
| Key | Value | Required | Description |
|-----|-------|----------|-------------|
| Authorization | Bearer {token} | O | JWT 액세스 토큰 |

---

### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "매칭이 취소되었습니다.",
  "data": {
    "userId": 102,
    "previousStatus": "WAITING",
    "cancelledAt": "2025-01-15T09:00:00"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| userId | Long | 사용자 ID |
| previousStatus | String | 취소 전 상태 (WAITING / IN_ROOM) |
| cancelledAt | DateTime | 취소 시간 |

#### 실패 케이스

| Status | Code | Message | Description |
|--------|------|---------|-------------|
| 401 | UNAUTHORIZED | 인증이 필요합니다 | 토큰 없음 또는 만료 |
| 404 | NOT_IN_QUEUE_OR_ROOM | 대기열 또는 매칭 룸에 참여 중이지 않습니다 | 취소할 매칭이 없음 |
| 409 | ROOM_ALREADY_DEPARTED | 이미 출발한 매칭 룸입니다 | 출발 완료 상태의 룸에서는 나갈 수 없음 |
| 409 | HOST_CANNOT_LEAVE | 방장은 나갈 수 없습니다 | 방장은 매칭 룸을 해체해야 함 |

```json
{
  "code": "HOST_CANNOT_LEAVE",
  "message": "방장은 나갈 수 없습니다",
  "errors": [
    {
      "field": "userId",
      "message": "방장은 매칭 룸을 해체하거나 방장 권한을 위임해야 합니다"
    }
  ]
}
```

---

## 표준 에러 코드

매칭 API에서 사용되는 표준 에러 코드 목록입니다.

| Code | HTTP Status | Description | 발생 시나리오 |
|------|-------------|-------------|---------------|
| INVALID_STATION | 400 | 올바르지 않은 출발역입니다 | SANBON, GEUMJEONG 외 값 입력 |
| INVALID_INPUT | 400 | 입력값이 올바르지 않습니다 | 필수 필드 누락 또는 형식 오류 |
| UNAUTHORIZED | 401 | 인증이 필요합니다 | 토큰 없음 또는 만료 |
| FORBIDDEN | 403 | 접근 권한이 없습니다 | 해당 룸의 참여자가 아님 |
| ROOM_NOT_FOUND | 404 | 매칭 룸을 찾을 수 없습니다 | 존재하지 않는 룸 ID 또는 만료된 룸 |
| NOT_IN_QUEUE_OR_ROOM | 404 | 대기열 또는 매칭 룸에 참여 중이지 않습니다 | 취소할 매칭이 없음 |
| ALREADY_IN_QUEUE | 409 | 이미 대기열에 등록되어 있습니다 | 중복 매칭 요청 |
| ALREADY_IN_ROOM | 409 | 이미 매칭룸에 참여 중입니다 | 매칭 완료 상태에서 재요청 |
| ROOM_ALREADY_DEPARTED | 409 | 이미 출발한 매칭 룸입니다 | 출발 완료 상태의 룸 수정 시도 |
| HOST_CANNOT_LEAVE | 409 | 방장은 나갈 수 없습니다 | 방장의 매칭 룸 나가기 시도 |

---

## 상태 코드 (Status)

### 매칭 요청 상태
| Status | Description |
|--------|-------------|
| WAITING | 대기 중 |
| MATCHED | 매칭 완료 |
| CANCELLED | 취소됨 |

### 매칭 룸 상태
| Status | Description |
|--------|-------------|
| WAITING | 참여자 대기 중 |
| READY | 모든 참여자 준비 완료 |
| DEPARTED | 출발 완료 |
| CANCELLED | 취소됨 |

### 출발역 코드
| Code | Description |
|------|-------------|
| SANBON | 산본역 |
| GEUMJEONG | 금정역 |

---

## 실시간 동기화 고려사항

### 홈 화면 (대기열 현황)
- **폴링 방식**: 3~5초 간격으로 `GET /api/v1/matching/queue` 호출
- **표시 정보**: 산본역/금정역별 대기 인원 수, 사용자의 대기열 참여 여부

### 대기 화면 (매칭 룸)
- **폴링 방식**: 2~3초 간격으로 `GET /api/v1/matching/room/{roomId}` 호출
- **표시 정보**: 참여자 목록, 각 참여자의 준비 상태, 방장 표시
- **준비 완료 체크**: 사용자가 체크박스를 클릭하면 `PATCH /api/v1/matching/room/{roomId}/ready` 호출

### 향후 개선 방안
- WebSocket 또는 Server-Sent Events(SSE)를 통한 실시간 푸시 구현 고려
- 불필요한 폴링 요청 최소화를 위한 변경 감지 메커니즘 도입

---

## 작성 시 체크리스트

- [x] 엔드포인트 URL이 RESTful 규칙을 따르는가?
- [x] HTTP Method 선택 이유가 명확한가?
- [x] Request/Response 예시가 실제 데이터와 유사한가?
- [x] 모든 필수/선택 필드가 명시되어 있는가?
- [x] 에러 케이스가 충분히 정의되어 있는가?
- [x] 인증/권한 정보가 명시되어 있는가?
- [x] 참여자 '학과(major)' 정보가 포함되어 있는가?
- [x] '준비 완료(Ready)' 상태 관리 필드가 포함되어 있는가?
- [x] 방장 권한 구분이 명시되어 있는가?
- [x] 홈 화면 및 대기 화면 UI 로직이 고려되어 있는가?
