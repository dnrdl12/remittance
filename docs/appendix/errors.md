# 에러 코드 목록

이 페이지는 Remittance API에서 공통적으로 사용되는 에러 코드를 정리한 부록입니다.  
실제 프로젝트의 `ErrorCode`, `FailCode` Enum과 맞춰 사용하는 것을 권장합니다.

---

## 1. 공통 에러

| 코드 | 설명 |
|------|------|
| `INVALID_REQUEST` | 필수값 누락 또는 유효하지 않은 파라미터 |
| `UNAUTHORIZED` | 인증 실패 (API Key 또는 토큰 문제) |
| `FORBIDDEN` | 권한 없음 |
| `NOT_FOUND` | 리소스를 찾을 수 없음 |
| `INTERNAL_ERROR` | 내부 서버 오류 |

---

## 2. 계좌 관련 에러

| 코드 | 설명 |
|------|------|
| `ACCOUNT_NOT_FOUND` | 존재하지 않는 계좌 |
| `ACCOUNT_STATUS_INVALID` | 계좌 상태가 거래 불가 (정지/해지 등) |
| `ACCOUNT_CLOSED` | 이미 해지된 계좌 |

---

## 3. 이체/출금 관련 에러

| 코드 | 설명 |
|------|------|
| `INVALID_AMOUNT` | 금액이 0 이하이거나 허용 범위를 벗어남 |
| `INSUFFICIENT_BALANCE` | 잔액 부족으로 출금/이체 불가 |
| `TRANSFER_SAME_ACCOUNT` | 출금 계좌와 입금 계좌가 동일 |
| `DAILY_LIMIT_EXCEEDED` | 일일 이체/출금 한도 초과 |

---

## 4. 멱등성(Idempotency) 관련 에러

| 코드 | 설명 |
|------|------|
| `IDEMPOTENCY_KEY_USED_DIFFERENT_PARAMS` | 동일 idempotencyKey로 다른 파라미터 요청 |
| `IDEMPOTENCY_KEY_EXPIRED` | 유효기간이 지난 idempotencyKey (선택 적용 가능) |

---

## 5. 응답 예시 포맷

```json
{
  "code": "INSUFFICIENT_BALANCE",
  "message": "출금 계좌 잔액이 부족합니다.",
  "path": "/v1/transfers",
  "timestamp": "2025-11-17T10:00:00+09:00",
  "data": null
}
