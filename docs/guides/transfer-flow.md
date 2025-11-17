
---

## `docs/guides/transfer-flow.md`

```markdown
# 이체 흐름 가이드

이 페이지에서는 **계좌 간 이체**를 구현할 때의 전체 흐름을 설명합니다.  
(내부적으로는 `TransferService`를 중심으로 처리된다고 가정합니다.)

---

## 1. 전체 흐름 개요

1. **클라이언트**가 이체 요청 생성 (from 계좌, to 계좌, amount, idempotencyKey 등)
2. **API Gateway / Controller**에서 인증 및 기본 파라미터 검증
3. **TransferService**에서 비즈니스 로직 처리
   - 출금 계좌/입금 계좌 조회 및 락(`SELECT ... FOR UPDATE`) 획득
   - 계좌 상태 검증 (NORMAL 여부)
   - 잔액 조회 및 부족 여부 체크
   - 수수료 정책에 따른 수수료 계산
   - `Transfer` 엔티티 생성 (PENDING → POSTED)
   - `Ledger` 엔티티 생성 (Debit/Credit 기록)
   - `BalanceSnapshot` 업데이트
4. 결과를 **클라이언트로 응답**

---

## 2. 시퀀스 다이어그램 (텍스트 버전)

```text
[Client]
   |
   | POST /v1/transfers
   v
[API Server - Controller]
   |
   | 인증/멱등성 검사
   v
[TransferService]
   |
   | 1) from/to Account 조회 및 락
   | 2) 계좌 상태 검증
   | 3) 잔액/한도 검증
   | 4) 수수료 계산
   | 5) Transfer(PENDING) 생성
   | 6) Ledger 레코드 생성
   | 7) Snapshot 잔액 반영
   | 8) Transfer 상태 POSTED로 변경
   v
[DB]
   |
   | 커밋 후
   v
[Client 응답]
