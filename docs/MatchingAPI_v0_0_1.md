# 매칭 (Matching) API

> Base URL: `/api/v1/matching`  
> 담당자: 김태남  
> 최종 수정일: 2025.12.24  
> 관련 스키마: `Matching Room`, `Room Participant`

---

## `GET` /api/v1/matching/status

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 특정 출발역(산본/금정)의 실시간 매칭 대기 현황(인원 수)을 조회합니다. |
| **UI 매핑** | **[홈 화면]** '산본역 매칭 현황' 카드, 진행률 바, '2/4' 텍스트 |
| **인증** | Required |
| **권한** | USER |

### Method 선택 이유
> GET - 서버의 상태를 변경하지 않고, 현재 대기열의 정보(리소스)를 조회하는 요청이기 때문입니다.

---

### Request

#### Headers
| Key | Value | Required | Description |
|-----|-------|----------|-------------|
| Authorization | Bearer {token} | O | JWT 액세스 토큰 |

#### Query Parameters
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| station | String | O | - | 출발역 (SANBON / GEUMJEONG) |

---

### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "매칭 현황 조회 성공",
  "data": {
    "station": "SANBON",
    "currentCount": 2
  }
}
```

#### 실패 케이스
| Status | Code | Message | Description |
|--------|------|---------|-------------|
| 400 | INVALID_STATION | 유효하지 않은 역 이름입니다. | SANBON, GEUMJEONG 외 입력 시 |

---

## `POST` /api/v1/matching/requests

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 사용자가 특정 역의 매칭 대기열에 진입(매칭 요청)합니다. |
| **UI 매핑** | [홈 화면] 하단 '금정역/산본역' 파란색 버튼 클릭 시 |
| **인증** | Required |
| **권한** | USER |

### Method 선택 이유
> POST - 대기열에 새로운 참가자(리소스)를 생성하는 행위이므로 POST를 사용합니다.

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
  "station": "SANBON"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| station | String | O | 출발역 (SANBON / GEUMJEONG) |

---

### Response

#### 성공 (201 Created)
```json
{
  "code": "SUCCESS",
  "message": "매칭 대기열 등록 성공",
  "data": {
    "matchingId": 101,
    "participantId": 505,
    "myStatus": "WAITING"
  }
}
```

#### 실패 케이스
| Status | Code | Message | Description |
|--------|------|---------|-------------|
| 409 | ALREADY_MATCHING | 이미 진행 중인 매칭이 있습니다. | 중복 요청 방지 |
| 400 | INVALID_STATION | 유효하지 않은 역 이름입니다. | - |

---

## `DELETE` /api/v1/matching/rooms/{matchingId}/members/me

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 현재 대기 중인 매칭방에서 나갑니다 (매칭 취소). |
| **UI 매핑** | [매칭 대기 화면] 하단 '매칭 취소' 버튼 |
| **인증** | Required |
| **권한** | USER |

### Method 선택 이유
> DELETE - 매칭방 내의 '참가자(나)'라는 리소스를 삭제하는 행위이므로 DELETE를 사용합니다.

---

### Request

#### Headers
| Key | Value | Required | Description |
|-----|-------|----------|-------------|
| Authorization | Bearer {token} | O | JWT 액세스 토큰 |

#### Path Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| matchingId | Long | O | 매칭방 ID (matching_id) |

---

### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "매칭이 취소되었습니다.",
  "data": null
}
```

#### 실패 케이스
| Status | Code | Message | Description |
|--------|------|---------|-------------|
| 404 | ROOM_NOT_FOUND | 매칭방을 찾을 수 없습니다. | 존재하지 않거나 이미 종료된 방 |

---

## `PATCH` /api/v1/matching/rooms/{matchingId}/status

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 4인이 되기 전 강제 출발을 위해 본인의 상태를 '준비 완료'로 변경합니다. |
| **UI 매핑** | [매칭 대기 화면] 리스트 내 본인 프로필의 체크박스 또는 토글 |
| **인증** | Required |
| **권한** | USER |

### Method 선택 이유
> PATCH - 참가자의 정보 중 상태(isReady) 필드만 부분 수정하므로 PATCH를 사용합니다.

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
| matchingId | Long | O | 매칭방 ID |

#### Request Body
```json
{
  "isReady": true
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| isReady | Boolean | O | 준비 완료 여부 (true: 준비됨, false: 취소) |

---

### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "상태가 변경되었습니다.",
  "data": {
    "participantId": 505,
    "userId": 12,
    "isReady": true,
    "roomStatus": "MATCHING_COMPLETED"
  }
}
```

#### 실패 케이스
| Status | Code | Message | Description |
|--------|------|---------|-------------|
| 400 | CANNOT_READY | 준비할 수 없는 상태입니다. | 이미 출발했거나 방이 유효하지 않음 |

---

## `POST` /api/v1/matching/rooms/{matchingId}/force-start

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 현재 인원으로 출발을 확정합니다. (모든 참가자가 Ready 상태여야 함) |
| **UI 매핑** | [매칭 대기 화면] '현재 인원으로 출발' 주황색 버튼 |
| **인증** | Required |
| **권한** | USER (참가자 누구나 가능하나, 서버에서 전원 동의 체크) |

### Method 선택 이유
> POST - '매칭 완료'라는 새로운 상태를 확정하고 거래(Transaction)를 트리거하는 행위이므로 POST를 사용합니다.

---

### Request

#### Headers
| Key | Value | Required | Description |
|-----|-------|----------|-------------|
| Authorization | Bearer {token} | O | JWT 액세스 토큰 |

#### Path Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| matchingId | Long | O | 매칭방 ID |

---

### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "매칭이 확정되었습니다. 곧 출발합니다!",
  "data": {
    "matchingId": 101,
    "status": "MATCHED",
    "finalMemberCount": 3
  }
}
```

#### 실패 케이스
| Status | Code | Message | Description |
|--------|------|---------|-------------|
| 409 | NOT_ALL_READY | 모든 참가자가 준비되지 않았습니다. | 만장일치 조건 미달 |
| 400 | NOT_ENOUGH_MEMBERS | 최소 출발 인원 부족 | 최소 2인 이상 등 정책 위반 시 |

---

## `GET` /api/v1/matching/rooms/{matchingId}

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 특정 매칭방의 상세 정보와 참가자 현황을 최신 상태로 조회합니다. (Polling용) |
| **UI 매핑** | [매칭 대기 화면] 전체 (타이머, 참가자 리스트, 준비 상태 등) |
| **인증** | Required |
| **권한** | USER (해당 방의 참가자만 조회 가능) |

### Method 선택 이유
> GET - 매칭방이라는 리소스의 상세 정보를 조회하는 요청이므로 GET을 사용합니다.

---

### Request

#### Headers
| Key | Value | Required | Description |
|-----|-------|----------|-------------|
| Authorization | Bearer {token} | O | JWT 액세스 토큰 |

#### Path Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| matchingId | Long | O | 매칭방 ID |

---

### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "매칭방 상세 조회 성공",
  "data": {
    "matchingId": 101,
    "station": "SANBON",
    "status": "WAITING",
    "createdAt": "2025-12-24T12:00:00",
    "members": [
      {
        "participantId": 501,
        "userId": 10,
        "name": "홍길동",
        "major": "컴퓨터공학과",
        "isReady": true,
        "joinedAt": "2025-12-24T12:01:00"
      },
      {
        "participantId": 505,
        "userId": 12,
        "name": "김한세",
        "major": "디자인학부",
        "isReady": false,
        "joinedAt": "2025-12-24T12:05:00"
      }
    ]
  }
}
```

#### 실패 케이스
| Status | Code | Message | Description |
|--------|------|---------|-------------|
| 404 | ROOM_NOT_FOUND | 매칭방을 찾을 수 없습니다. | 존재하지 않는 방 ID |
| 403 | FORBIDDEN | 조회 권한이 없습니다. | 해당 방의 참가자가 아님 |

---

## `GET` /api/v1/matching/history

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 사용자의 과거 매칭 참여 이력을 조회합니다. |
| **UI 매핑** | [마이페이지 화면] '12월 9일(목) 산본역 4인 매칭' 리스트 |
| **인증** | Required |
| **권한** | USER |

### Method 선택 이유
> GET - 사용자의 이력 데이터(컬렉션)를 조회하는 요청이므로 GET을 사용합니다.

---

### Request

#### Headers
| Key | Value | Required | Description |
|-----|-------|----------|-------------|
| Authorization | Bearer {token} | O | JWT 액세스 토큰 |

#### Query Parameters
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | X | 0 | 페이지 번호 |
| size | Integer | X | 10 | 페이지 크기 |

---

### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "매칭 이력 조회 성공",
  "data": {
    "content": [
      {
        "historyId": "1",
        "date": "2025-12-09T18:30",
        "station": "SANBON",
        "finalMemberCount": 4
      },
      {
        "historyId": "2",
        "date": "2025-12-07T09:00",
        "station": "GEUMJEONG",
        "finalMemberCount": 2
      }
    ],
    "page": 0,
    "size": 10,
    "hasNext": true
  }
}
```

---

## `GET` /api/v1/matching/my-status

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 현재 사용자가 참여 중인 매칭방이 있는지 확인합니다. (앱 실행 시 라우팅용) |
| **UI 매핑** | [앱 실행/스플래시] 결과에 따라 '홈 화면' 또는 '매칭 대기 화면'으로 이동 |
| **인증** | Required |
| **권한** | USER |

### Method 선택 이유
> GET - 사용자의 현재 상태 정보를 조회하는 요청이므로 GET을 사용합니다.

---

### Request

#### Headers
| Key | Value | Required | Description |
|-----|-------|----------|-------------|
| Authorization | Bearer {token} | O | JWT 액세스 토큰 |

---

### Response

#### 성공 (참여 중인 방 있음)
```json
{
  "code": "SUCCESS",
  "message": "현재 참여 중인 매칭이 있습니다.",
  "data": {
    "isMatching": true,
    "matchingId": 101,
    "status": "WAITING"
  }
}
```

#### 성공 (참여 중인 방 없음)
```json
{
  "code": "SUCCESS",
  "message": "참여 중인 매칭이 없습니다.",
  "data": {
    "isMatching": false,
    "matchingId": null,
    "status": null
  }
}
```
