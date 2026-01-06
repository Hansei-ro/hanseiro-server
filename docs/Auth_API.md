# Auth API

> Base URL: `/api/v1/auth`  
> 담당자: 이유진  
> 최종 수정일: 2025.12.23

---

## `GET` /api/v1/auth/google/authorize

### 개요
| 항목 | 내용 |
|------|------|
| **설명** | 구글 OAuth2 로그인을 시작하기 위한 인증 URL(authUrl)과 state를 발급합니다. (클라이언트는 이 URL로 이동) |
| **인증** | None |
| **권한** | ALL |

### Method 선택 이유
> GET - 서버 리소스를 생성/변경하기보다는 “로그인 시작에 필요한 URL/파라미터를 조회”하는 동작이므로 GET 사용.  
> (내부적으로 state를 발급/저장할 수 있으나, 외부 API 관점에서는 URL 제공이 목적)

---

### Request

#### Headers
| Key | Value | Required | Description |
|-----|-------|----------|-------------|
| Content-Type | application/json | X | - |

#### Query Parameters
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| redirectUri | String | X | 서버 기본값 | 구글 로그인 완료 후 콜백으로 돌아올 URI (운영/개발 분리용) |


### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {
    "authUrl": "https://accounts.google.com/o/oauth2/v2/auth?...&state=9f1c...&redirect_uri=https%3A%2F%2Fapi.hansei-ro.com%2Fapi%2Fv1%2Fauth%2Fgoogle%2Fcallback",
    "state": "9f1c2f1d-6c1a-4f02-bb1a-2a1e7e2a9a1b",
    "expiresIn": 300
  }
}
```

| Field     | Type    | Description                      |
| --------- | ------- | -------------------------------- |
| authUrl   | String  | 구글 로그인 페이지 URL(클라이언트는 이 URL로 이동) |
| state     | String  | CSRF 방지용 state                   |
| expiresIn | Integer | state 유효시간(초)                    |

#### 실패 케이스
| Status | Code           | Message        | Description       |
| ------ | -------------- | -------------- | ----------------- |
| 400    | INVALID_INPUT  | 입력값이 올바르지 않습니다 | redirectUri 형식 오류 |
| 500    | INTERNAL_ERROR | 서버 오류가 발생했습니다  | authUrl 생성 실패     |


## `GET` /api/v1/auth/google/callback

### 개요
| 항목     | 내용                                                                                               |
| ------ | ------------------------------------------------------------------------------------------------ |
| **설명** | 구글 OAuth2 로그인 성공 시 전달된 인가 코드(code)와 state를 검증한 뒤, JWT를 발급합니다. Refresh Token은 HttpOnly 쿠키로 설정됩니다. |
| **인증** | None                                                                                             |
| **권한** | ALL                                                                                              |

### Method 선택 이유
> GET - OAuth2 표준 콜백은 리다이렉션 기반으로 query parameter로 code/state를 전달하므로 GET 사용.
> 서버는 이 값을 검증한 뒤 토큰 발급 및 (필요 시) 프론트로 리다이렉트합니다.

---

### Request

#### Headers
| Key          | Value            | Required | Description |
| ------------ | ---------------- | -------- | ----------- |
| Content-Type | application/json | X        | -           |

#### Query Parameters
| Parameter | Type   | Required | Default | Description                        |
| --------- | ------ | -------- | ------- | ---------------------------------- |
| code      | String | O        | -       | 구글 OAuth2 인가 코드                    |
| state     | String | O        | -       | CSRF 방지용 state (authorize에서 발급된 값) |
| scope     | String | X        | -       | 구글이 전달하는 scope (참고용)               |

### Response

#### 성공 (302 Found)
#### Response Headers
| Key        | Value                                                                                | Required | Description             |
| ---------- | ------------------------------------------------------------------------------------ | -------- | ----------------------- |
| Set-Cookie | refreshToken=...; HttpOnly; Secure; SameSite=None; Path=/api/v1/auth; Max-Age=604800 | O        | Refresh Token 쿠키(7일 예시) |

```json
{
  "code": "SUCCESS",
  "message": "로그인 성공",
  "data": {
    "user": {
      "userId": 123,
      "name": "홍길동",
      "email": "student@hansei.ac.kr"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "isProfileCompleted": false,
    "createdAt": "2025-12-27T06:10:00"
  }
}
```

| Field              | Type     | Description           |
| ------------------ | -------- | --------------------- |
| user               | Object   | 사용자 기본 정보             |
| accessToken        | String   | JWT Access Token      |
| tokenType          | String   | 토큰 타입(Bearer)         |
| expiresIn          | Integer  | Access Token 만료 시간(초) |
| isProfileCompleted | Boolean  | 추가 프로필 입력 완료 여부       |
| createdAt          | DateTime | 로그인 처리 시간             |

#### 실패 케이스
| Status | Code              | Message             | Description             |
| ------ | ----------------- | ------------------- | ----------------------- |
| 400    | INVALID_INPUT     | 입력값이 올바르지 않습니다      | code/state 누락           |
| 403    | AUTH_OAUTH_003    | 잘못된 OAuth 요청입니다     | **state 불일치 또는 만료(필수)** |
| 400    | AUTH_001          | 한세대학교 이메일만 가입 가능합니다 | @hansei.ac.kr 도메인 아님    |
| 401    | AUTH_OAUTH_001    | 구글 인증에 실패했습니다       | code 교환 실패/토큰 검증 실패     |
| 429    | TOO_MANY_REQUESTS | 요청이 너무 많습니다         | 과도한 요청                  |
| 500    | INTERNAL_ERROR    | 서버 오류가 발생했습니다       | 내부 예외                   |


## `POST` /api/v1/auth/refresh

### 개요
| 항목     | 내용                                                                                              |
| ------ | ----------------------------------------------------------------------------------------------- |
| **설명** | HttpOnly 쿠키로 전달된 Refresh Token을 검증하고, 새로운 Access Token을 재발급합니다. (Refresh Token 재발급 시 기존 토큰은 폐기) |
| **인증** | None                                                                                            |
| **권한** | ALL                                                                                             |

### Method 선택 이유
> POST - 서버가 Refresh Token을 검증하고 새로운 토큰을 생성/회전(rotation)시키는 작업이므로 POST 사용.
> 토큰은 만료/폐기 여부에 따라 결과가 달라질 수 있어 멱등성이 보장되지 않습니다.

---

### Request

#### Headers
| Key          | Value            | Required | Description |
| ------------ | ---------------- | -------- | ----------- |
| Content-Type | application/json | X        | -           |

### Response

#### 성공 (200 OK)
#### Response Headers
| Key        | Value                                                                                | Required | Description                  |
| ---------- | ------------------------------------------------------------------------------------ | -------- | ---------------------------- |
| Set-Cookie | refreshToken=...; HttpOnly; Secure; SameSite=None; Path=/api/v1/auth; Max-Age=604800 | O        | ✅ **새 Refresh Token 발급(회전)** |

```json
{
  "code": "SUCCESS",
  "message": "토큰이 재발급되었습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "issuedAt": "2025-12-27T06:20:00"
  }
}
```

| Field       | Type     | Description           |
| ----------- | -------- | --------------------- |
| accessToken | String   | 새로 발급된 Access Token   |
| tokenType   | String   | 토큰 타입(Bearer)         |
| expiresIn   | Integer  | Access Token 만료 시간(초) |
| issuedAt    | DateTime | 재발급 시간                |

#### 실패 케이스
| Status | Code           | Message       | Description               |
| ------ | -------------- | ------------- | ------------------------- |
| 401    | UNAUTHORIZED   | 인증이 필요합니다     | refreshToken 쿠키 없음/만료     |
| 403    | FORBIDDEN      | 접근이 거부되었습니다   | 폐기/블랙리스트 처리된 refreshToken |
| 500    | INTERNAL_ERROR | 서버 오류가 발생했습니다 | 내부 예외                     |


## `POST` /api/v1/auth/logout`

### 개요
| 항목     | 내용                                               |
| ------ | ------------------------------------------------ |
| **설명** | 현재 사용자의 Refresh Token을 폐기하고 쿠키를 제거하여 로그아웃 처리합니다. |
| **인증** | Required                                         |
| **권한** | USER                                             |

### Method 선택 이유
> POST - 로그아웃은 Refresh Token 폐기 및 서버 인증 상태 변경이 발생하므로 POST 사용.

### Request

#### Headers
| Key           | Value            | Required | Description      |
| ------------- | ---------------- | -------- | ---------------- |
| Authorization | Bearer {token}   | O        | JWT Access Token |
| Content-Type  | application/json | X        | -                |

#### Request Body
```json
{
"refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

### Response

#### 성공 (200 OK)
#### Response Body
| Key        | Value                                                                        | Required | Description         |
| ---------- | ---------------------------------------------------------------------------- | -------- | ------------------- |
| Set-Cookie | refreshToken=; HttpOnly; Secure; SameSite=None; Path=/api/v1/auth; Max-Age=0 | O        | Refresh Token 쿠키 삭제 |

```json
{
  "code": "SUCCESS",
  "message": "로그아웃 되었습니다.",
  "data": {
    "loggedOutAt": "2025-12-27T06:30:00"
  }
}
```

#### 실패 케이스
| Status | Code           | Message       | Description       |
| ------ | -------------- | ------------- | ----------------- |
| 401    | UNAUTHORIZED   | 인증이 필요합니다     | accessToken 없음/만료 |
| 403    | FORBIDDEN      | 접근이 거부되었습니다   | 토큰 소유자 불일치        |
| 500    | INTERNAL_ERROR | 서버 오류가 발생했습니다 | 내부 예외             |

---

## 작성 시 체크리스트

- [ ] 엔드포인트 URL이 RESTful 규칙을 따르는가?
- [ ] HTTP Method 선택 이유가 명확한가?
- [ ] Request/Response 예시가 실제 데이터와 유사한가?
- [ ] 모든 필수/선택 필드가 명시되어 있는가?
- [ ] 에러 케이스가 충분히 정의되어 있는가?
- [ ] 인증/권한 정보가 명시되어 있는가?
- [ ] state 검증 실패 케이스가 포함되어 있는가?
- [ ] HttpOnly/Secure/SameSite 정책이 명시되어 있는가?

---

## HTTP Method 가이드 
| Method | 용도        | 멱등성 | 예시                    |
| ------ | --------- | --- | --------------------- |
| GET    | 리소스 조회    | O   | 로그인 URL 발급, 사용자 정보 조회 |
| POST   | 리소스 생성/처리 | X   | 소셜 로그인 처리, 토큰 재발급     |
| PUT    | 리소스 전체 수정 | O   | 프로필 전체 수정             |
| PATCH  | 리소스 부분 수정 | O   | 프로필 일부 수정             |
| DELETE | 리소스 삭제    | O   | 매칭 취소                 |

---

## 공통 Response 형식

### 성공 응답
```json
{
  "code": "SUCCESS",
  "message": "성공 메시지",
  "data": { }
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
    "content": [  ],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false
  }
}
```