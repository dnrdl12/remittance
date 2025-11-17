# FailCode 목록

`FailCode`는 이체(Transfer), 출금(Withdraw), 입금(Deposit) 과정에서  
**거래 실패(Failed) 원인을 기록하기 위해 사용되는 내부 코드**입니다.

`ErrorCode`는 외부 API 응답용이고,  
`FailCode`는 내부 로직에서 거래 실패 레코드를 저장할 때 사용된다는 점에서 역할이 다릅니다.

---

## FailCode 전체 목록

| FailCode | 설명 |
|----------|------------------------------------------------|
| `INSUFFICIENT_BALANCE` | 잔액 부족 |
| `ACCOUNT_SUSPENDED` | 계좌 정지 |
| `ACCOUNT_CLOSED` | 계좌 해지 |
| `INVALID_ACCOUNT` | 유효하지 않은 계좌 |
| `LIMIT_EXCEEDED` | 한도 초과 |
| `ACCOUNT_STATUS_INVALID` | 계좌 상태 오류 (정지/해지/기타) |
| `SYSTEM_ERROR` | 시스템 내부 오류 |

---

## 상황별 FailCode 사용 예시

### 1) 잔액 부족 — `INSUFFICIENT_BALANCE`

출금 또는 이체 시 **사용자 잔액 < (금액 + 수수료)** 인 경우:

```java
Transfer failed = Transfer.builder()
    .status(FAILED)
    .failCode(FailCode.INSUFFICIENT_BALANCE)
    ...
