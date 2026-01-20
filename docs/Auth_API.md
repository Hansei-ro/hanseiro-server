# Auth(인증) API - Google Social Login

> Base URL: `/api/v1/auth`  
> 담당자: 이유진  
> 최종 수정일: 2026.01.16

---

## `POST` /api/v1/auth/google

### 개요
| 항목     | 내용                                                                                |
| ------ | --------------------------------------------------------------------------------- |
| **설명** | 프론트에서 받은 구글 인가코드로 구글 토큰/사용자 정보를 조회하고, 한세대 계정(@hansei.ac.kr)만 로그인 처리 후 JWT를 발급합니다. |
| **인증** | None                                                                              |
| **권한** | ALL                                                                               |

### Method 선택 이유
> POST - 인가코드 교환 및 JWT 발급(세션성 자원 생성/인증 상태 생성)에 해당하며, 민감 정보를 URL 쿼리로 노출하지 않기 위해 Body로 전달하는 POST를 사용합니다.

### Request

#### Headers
| Key          | Value            | Required | Description |
| ------------ | ---------------- | -------- | ----------- |
| Content-Type | application/json | O        | -           |

#### Query Parameters
 X

#### Path Parameters
 X

#### Request Body
```json
{
"code": "4/0AfJohX....",
"redirectUri": "https://frontend.example.com/auth/google/callback",
"state": "random_state_value"
}
```

| Field       | Type   | Required | Description                                          |
| ----------- | ------ | -------- | ---------------------------------------------------- |
| code        | String | O        | 구글 OAuth 로그인 성공 후 프론트가 받은 인가 코드(authorization code)  |
| redirectUri | String | O        | 구글 토큰 교환 시 사용하는 redirect_uri (구글 콘솔 등록값과 정확히 일치해야 함) |
| state       | String | X        | CSRF 방지용 state 값(서버에서 검증하는 경우 필수)                |

---

### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 123,
      "email": "student@hansei.ac.kr",
      "name": "홍길동",
      "isNew": false
    }
  }
}
```

| Field        | Type    | Required | Description                            |
| ------------ | ------- | -------- | -------------------------------------- |
| accessToken  | String  | O        | API 요청 시 사용하는 JWT Access Token         |
| refreshToken | String  | O        | Access Token 재발급을 위한 JWT Refresh Token |
| user.id      | Long    | O        | 내부 사용자 ID                              |
| user.email   | String  | O        | 한세대학교 이메일(@hansei.ac.kr)               |
| user.name    | String  | O        | 사용자 이름                                 |
| user.isNew   | Boolean | O        | 신규 가입 여부                               |


#### 실패 케이스
| Status | Code                              | Message                 | Description                  |
| ------ | --------------------------------- | ----------------------- | ---------------------------- |
| 400    | INVALID_INPUT                     | 입력값이 올바르지 않습니다          | code 또는 redirectUri 누락/형식 오류 |
| 401    | AUTH_INVALID_STATE                | 로그인 요청이 유효하지 않습니다       | state 검증 실패                  |
| 401    | AUTH_GOOGLE_TOKEN_EXCHANGE_FAILED | 구글 인증 처리에 실패했습니다        | code 만료, redirectUri 불일치 등   |
| 502    | AUTH_GOOGLE_USERINFO_FAILED       | 구글 사용자 정보 조회에 실패했습니다    | userinfo 요청 실패               |
| 403    | AUTH_ONLY_HANSEI_ACCOUNT          | 한세대학교 계정으로 만 로그인 가능합니다. | 이메일 도메인 불일치                  |
| 403    | AUTH_EMAIL_NOT_VERIFIED           | 이메일 인증이 필요합니다           | email_verified=false         |
| 500    | AUTH_TOKEN_ISSUE_FAILED           | 토큰 발급에 실패했습니다           | JWT 생성 오류                    |

```json
{
  "code": "AUTH_ONLY_HANSEI_ACCOUNT",
  "message": "한세대학교 계정으로 만 로그인 가능합니다.",
  "errors": []
}

```

---

## `POST` /api/v1/auth/refresh

### 개요
| 항목     | 내용                                                |
| ------ | ------------------------------------------------- |
| **설명** | Refresh Token을 헤더로 전달받아 새로운 Access Token을 재발급합니다. |
| **인증** | Required                                          |
| **권한** | USER                                              |


### Method 선택 이유
> POST - 토큰 재발급은 인증 상태를 갱신하는 동작이며, 캐싱 및 URL 노출을 방지하기 위해 POST를 사용합니다.

### Request

#### Headers
| Key           | Value                 | Required | Description       |
| ------------- | --------------------- | -------- | ----------------- |
| Authorization | Bearer {refreshToken} | O        | JWT Refresh Token |
| Content-Type  | application/json      | X        | -                 |


#### Query Parameters
 X

#### Path Parameters
 X

#### Request Body
```json
{}
```

---

### Response

#### 성공 (200 OK)
```json
{
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

| Field       | Type   | Required | Description             |
| ----------- | ------ | -------- | ----------------------- |
| accessToken | String | O        | 새로 발급된 JWT Access Token |

#### 실패 케이스
| Status | Code                       | Message                   | Description           |
| ------ | -------------------------- | ------------------------- | --------------------- |
| 401    | AUTH_INVALID_REFRESH_TOKEN | 인증이 만료되었습니다. 다시 로그인 해주세요. | refreshToken 누락/만료/위조 |
| 500    | AUTH_TOKEN_ISSUE_FAILED    | 토큰 발급에 실패했습니다             | 재발급 처리 중 오류           |

```json
{
  "code": "AUTH_INVALID_REFRESH_TOKEN",
  "message": "인증이 만료되었습니다. 다시 로그인 해주세요.",
  "errors": []
}
```

---

## `POST` /api/v1/auth/logout

### 개요
| 항목     | 내용                               |
| ------ | -------------------------------- |
| **설명** | Refresh Token을 무효화하여 로그아웃 처리합니다. |
| **인증** | Required                         |
| **권한** | USER                             |



### Method 선택 이유
> POST - 서버에 저장된 Refresh Token을 폐기하는 상태 변경 작업이므로 POST를 사용합니다.

### Request

#### Headers
| Key           | Value                 | Required | Description           |
| ------------- | --------------------- | -------- | --------------------- |
| Authorization | Bearer {refreshToken} | O        | 폐기할 JWT Refresh Token |
| Content-Type  | application/json      | X        | -                     |



#### Query Parameters
 X

#### Path Parameters
 X

#### Request Body
```json
{}
```

---

### Response

#### 성공 (204 No Content)
 응답 바디 X

#### 실패 케이스
| Status | Code                       | Message                   | Description        |
| ------ | -------------------------- | ------------------------- | ------------------ |
| 401    | AUTH_INVALID_REFRESH_TOKEN | 인증이 만료되었습니다. 다시 로그인 해주세요. | refreshToken 없음/만료 |
| 500    | AUTH_LOGOUT_FAILED         | 로그아웃 처리에 실패했습니다           | 토큰 폐기 실패           |

```json
{
  "code": "AUTH_LOGOUT_FAILED",
  "message": "로그아웃 처리에 실패했습니다",
  "errors": []
}
```
