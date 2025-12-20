# API Documentation Convention Guide

> 이 문서는 한세로 프로젝트의 API 문서 작성 컨벤션입니다.  
> 모든 API 문서는 이 형식을 따라 작성해주세요.

---

## 문서 구조
```
docs/
├── API_DOCS_EXAMPLE.md    # 이 파일 (컨벤션 가이드)
├── USER_API.md            # 사용자 관련 API
├── MATCHING_API.md        # 매칭 관련 API
├── MATCHING_ROOM_API.md   # 매칭룸 관련 API
├── BUS_ROUTE_API.md       # 버스 노선 관련 API
└── CHAT_API.md            # 채팅 관련 API (P2)
```

---

## API 문서 작성 템플릿

### 기본 정보
```markdown
# [도메인명] API

> Base URL: `/api/v1/[도메인]`  
> 담당자: [이름]  
> 최종 수정일: YYYY.MM.DD
```

---

### 엔드포인트 작성 형식

각 엔드포인트는 아래 형식을 따릅니다.

---

## `[METHOD]` /api/v1/[resource]

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 이 API가 하는 일을 한 줄로 설명 |
| **인증** | Required / Optional / None |
| **권한** | USER / ADMIN / ALL |

### Method 선택 이유
> 왜 이 HTTP Method를 선택했는지 간단히 설명  
> 예: POST - 새로운 리소스(매칭 요청)를 생성하기 때문

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
| id | Long | O | 리소스 고유 ID |

#### Query Parameters
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | X | 0 | 페이지 번호 |
| size | Integer | X | 10 | 페이지 크기 |

#### Request Body
```json
{
  "field1": "string",
  "field2": 0,
  "field3": true
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| field1 | String | O | 필드 설명 |
| field2 | Integer | O | 필드 설명 |
| field3 | Boolean | X | 필드 설명 (기본값: false) |

---

### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {
    "id": 1,
    "field1": "value",
    "createdAt": "2025-01-01T12:00:00"
  }
}
```

#### 실패 케이스

| Status | Code | Message | Description |
|--------|------|---------|-------------|
| 400 | INVALID_INPUT | 입력값이 올바르지 않습니다 | 필수 필드 누락 또는 형식 오류 |
| 401 | UNAUTHORIZED | 인증이 필요합니다 | 토큰 없음 또는 만료 |
| 404 | NOT_FOUND | 리소스를 찾을 수 없습니다 | 존재하지 않는 ID |
| 409 | CONFLICT | 이미 존재하는 리소스입니다 | 중복 요청 |
```json
{
  "code": "INVALID_INPUT",
  "message": "입력값이 올바르지 않습니다",
  "errors": [
    {
      "field": "email",
      "message": "이메일 형식이 올바르지 않습니다"
    }
  ]
}
```

---

## 실제 예시: 매칭 요청 API

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
| departureStation | String | 출발역 |
| status | String | 상태 (WAITING / MATCHED / CANCELLED) |
| queuePosition | Integer | 대기열 순서 |
| estimatedWaitTime | Integer | 예상 대기 시간 (분) |
| createdAt | DateTime | 요청 생성 시간 |

#### 실패 케이스

| Status | Code | Message | Description |
|--------|------|---------|-------------|
| 400 | INVALID_STATION | 올바르지 않은 출발역입니다 | SANBON, GEUMJEONG 외 값 |
| 401 | UNAUTHORIZED | 인증이 필요합니다 | 토큰 없음 또는 만료 |
| 409 | ALREADY_IN_QUEUE | 이미 대기열에 등록되어 있습니다 | 중복 매칭 요청 |

---

## 작성 시 체크리스트

- [ ] 엔드포인트 URL이 RESTful 규칙을 따르는가?
- [ ] HTTP Method 선택 이유가 명확한가?
- [ ] Request/Response 예시가 실제 데이터와 유사한가?
- [ ] 모든 필수/선택 필드가 명시되어 있는가?
- [ ] 에러 케이스가 충분히 정의되어 있는가?
- [ ] 인증/권한 정보가 명시되어 있는가?

---

## HTTP Method 가이드

| Method | 용도 | 멱등성 | 예시 |
|--------|------|--------|------|
| GET | 리소스 조회 | O | 매칭 상태 조회 |
| POST | 리소스 생성 | X | 매칭 요청, 회원가입 |
| PUT | 리소스 전체 수정 | O | 프로필 전체 수정 |
| PATCH | 리소스 부분 수정 | O | 프로필 일부 수정 |
| DELETE | 리소스 삭제 | O | 매칭 취소 |

---

## 공통 Response 형식

### 성공 응답
```json
{
  "code": "SUCCESS",
  "message": "성공 메시지",
  "data": { ... }
}
```

### 에러 응답
```json
{
  "code": "ERROR_CODE",
  "message": "에러 메시지",
  "errors": [
    {
      "field": "필드명",
      "message": "상세 에러 메시지"
    }
  ]
}
```

### 페이징 응답
```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false
  }
}
```