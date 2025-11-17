# 인증 방식

Remittance API는 안전한 호출을 위해 **API Key + Client 식별 + 멱등키(Idempotency Key)** 기반 인증 방식을 사용합니다.

---

## 공통 헤더

모든 보호된 API 엔드포인트는 다음 헤더를 요구합니다.

```http
X-Client-Id: {CLIENT_ID}
X-Idempotency-Key: {UNIQUE_KEY}
Content-Type: application/json
