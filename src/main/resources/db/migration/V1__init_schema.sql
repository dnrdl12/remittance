-- ===============================
-- V1__init_schema.sql
-- 전체 DROP → 전체 CREATE (로컬 전용)
-- 운영 환경에서는 flyway clean() 금지
-- ===============================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS ledger;
DROP TABLE IF EXISTS balance_snapshots;
DROP TABLE IF EXISTS outbox_events;
DROP TABLE IF EXISTS transfers;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS fee_policy;
DROP TABLE IF EXISTS member;

SET FOREIGN_KEY_CHECKS = 1;

-- ===========================================================
-- 1) MEMBER 테이블
-- ===========================================================
CREATE TABLE member (
    member_seq        BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '회원 고유 식별자',
    member_nm         VARCHAR(100) NOT NULL COMMENT '회원 이름',
    member_phone      VARCHAR(512) NOT NULL COMMENT '휴대폰 번호(암호화 대상 원문)',
    member_phone_hash VARCHAR(88) NULL COMMENT '휴대폰 번호 해시',
    member_ci         VARCHAR(512) NOT NULL COMMENT 'CI(암호화 대상 원문)',
    member_ci_hash    VARCHAR(88) NULL COMMENT 'CI 해시',
    member_di         VARCHAR(512) NOT NULL COMMENT 'DI(암호화 대상 원문)',
    member_di_hash    VARCHAR(88) NULL COMMENT 'DI 해시',
    member_status     TINYINT NOT NULL COMMENT '회원 상태 코드',
    priv_consent_yn   CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '개인정보 동의 여부',
    msg_consent_yn    CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '알림/문자 수신 동의 여부',

    reg_id   VARCHAR(50)  NULL COMMENT '등록자 ID',
    reg_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    mod_id   VARCHAR(50)  NULL COMMENT '수정자 ID',
    mod_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    CONSTRAINT ck_member_priv_consent CHECK (priv_consent_yn IN ('Y', 'N')),
    CONSTRAINT ck_member_msg_consent  CHECK (msg_consent_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원 테이블';


-- ===========================================================
-- 2) FEE_POLICY 테이블
-- ===========================================================
CREATE TABLE fee_policy (
                            fee_policy_seq    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '수수료 정책 고유 식별자',
                            policy_name       VARCHAR(50) NOT NULL COMMENT '수수료 정책명',
                            transfer_fee_rate DECIMAL(10,5) NOT NULL COMMENT '이체 수수료율',
                            withdraw_fee_rate DECIMAL(10,5) NOT NULL COMMENT '출금 수수료율',
                            event_flag        TINYINT(1) NOT NULL DEFAULT 0 COMMENT '이벤트 플래그 (0: 일반, 1: 이벤트)',
                            start_date        DATETIME NULL COMMENT '정책 시작일시',
                            end_date          DATETIME NULL COMMENT '정책 종료일시',

                            reg_id   VARCHAR(50)  NULL COMMENT '등록자 ID',
                            reg_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
                            mod_id   VARCHAR(50)  NULL COMMENT '수정자 ID',
                            mod_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='이체 수수료 정책 테이블';


-- ===========================================================
-- 3) ACCOUNT 테이블
--   - Account 엔티티와 1:1 매핑
-- ===========================================================
CREATE TABLE account (
                         account_seq           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '계좌 고유 식별자',
                         account_number        VARCHAR(30) NOT NULL UNIQUE COMMENT '계좌번호',

                         member_seq            BIGINT NOT NULL COMMENT '회원 고유 식별자 (FK)',

                         account_status        TINYINT NOT NULL COMMENT '계좌 상태: 1 정상, 2 정지, 3 해지',
                         nickname              VARCHAR(50) NULL COMMENT '계좌 별칭',
                         account_type          TINYINT NOT NULL COMMENT '계좌 종류: 1 일반, 2 월급통장, 3 한도통장',

                         bank_code             VARCHAR(10) NULL COMMENT '은행 코드',
                         branch_code           VARCHAR(10) NULL COMMENT '지점 코드',

                         created_date          DATETIME NOT NULL COMMENT '계좌 개설일시',
                         closed_date           DATETIME NULL COMMENT '계좌 해지일시',

                         account_sort          INT NULL COMMENT '계좌 순번 (사용자 계좌 목록 정렬용)',

                         fee_policy_seq        BIGINT NULL COMMENT '수수료 정책 (FK)',

                         daily_transfer_limit  BIGINT NOT NULL COMMENT '1일 이체 한도',
                         daily_withdraw_limit  BIGINT NOT NULL COMMENT '1일 출금 한도',

                         reg_id   VARCHAR(50)  NULL COMMENT '등록자 ID',
                         reg_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
                         mod_id   VARCHAR(50)  NULL COMMENT '수정자 ID',
                         mod_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

                         CONSTRAINT fk_account_member
                             FOREIGN KEY (member_seq) REFERENCES member(member_seq),
                         CONSTRAINT fk_account_fee_policy
                             FOREIGN KEY (fee_policy_seq) REFERENCES fee_policy(fee_policy_seq),

                         CONSTRAINT ck_account_status
                             CHECK (account_status IN (1, 2, 3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='계좌 테이블';

CREATE INDEX ix_account_member ON account(member_seq);


-- ===========================================================
-- 4) BALANCE_SNAPSHOTS 테이블
--   - 계좌별 최신 잔액 스냅샷
-- ===========================================================
CREATE TABLE balance_snapshots (
                                   account_seq BIGINT PRIMARY KEY COMMENT '계좌 고유 식별자 (FK, PK 겸용)',
                                   balance     BIGINT NOT NULL COMMENT '현재 잔액',

                                   reg_id   VARCHAR(50)  NULL COMMENT '등록자 ID',
                                   reg_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
                                   mod_id   VARCHAR(50)  NULL COMMENT '수정자 ID',
                                   mod_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

                                   CONSTRAINT fk_balance_snapshot_account
                                       FOREIGN KEY (account_seq) REFERENCES account(account_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='계좌 잔액 스냅샷 테이블';


-- ===========================================================
-- 5) TRANSFERS 테이블 (이체 트랜잭션)
-- ===========================================================
CREATE TABLE transfers (
                           transfer_seq     BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이체 고유 식별자',
                           client_id        VARCHAR(64) NOT NULL COMMENT '클라이언트 ID(호출 시스템 식별자)',
                           idempotency_key  VARCHAR(128) NOT NULL COMMENT '멱등 키 (client_id와 조합하여 유니크)',
                           from_account_seq BIGINT NOT NULL COMMENT '출금 계좌 (FK)',
                           to_account_seq   BIGINT NOT NULL COMMENT '입금 계좌 (FK)',
                           amount           BIGINT NOT NULL COMMENT '이체 금액',
                           fee              BIGINT NOT NULL COMMENT '수수료 금액',
                           currency         CHAR(3) NOT NULL COMMENT '통화 코드 (예: KRW)',
                           status           VARCHAR(16) NOT NULL COMMENT '이체 상태 (예: REQUESTED, COMPLETED, FAILED)',
                           fail_code        VARCHAR(64) NULL COMMENT '실패 사유 코드',
                           memo             VARCHAR(255) NULL COMMENT '이체 메모',
                           requested_date   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '요청 일시',
                           posted_date      DATETIME NULL COMMENT '실제 반영(전표) 일시',

                           reg_id   VARCHAR(50)  NULL COMMENT '등록자 ID',
                           reg_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
                           mod_id   VARCHAR(50)  NULL COMMENT '수정자 ID',
                           mod_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

                           CONSTRAINT fk_transfers_from_account
                               FOREIGN KEY (from_account_seq) REFERENCES account(account_seq),
                           CONSTRAINT fk_transfers_to_account
                               FOREIGN KEY (to_account_seq) REFERENCES account(account_seq),

                           CONSTRAINT uk_transfers_client_id_idem
                               UNIQUE (client_id, idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='이체 이력 테이블';

CREATE INDEX ix_transfers_status_requested_date
    ON transfers(status, requested_date);


-- ===========================================================
-- 6) LEDGER 테이블 (복식부기 전표)
-- ===========================================================
CREATE TABLE ledger (
                        ledger_seq    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '전표(원장) 고유 식별자',
                        transfer_seq  BIGINT NOT NULL COMMENT '이체 고유 식별자 (FK)',
                        account_seq   BIGINT NOT NULL COMMENT '관련 계좌 (FK)',
                        amount        BIGINT NOT NULL COMMENT '전표 금액 (양수/음수 포함 가능)',
                        entry_type    VARCHAR(10) NOT NULL COMMENT '엔트리 타입 (DEBIT/CREDIT 등)',
                        currency      CHAR(3) NOT NULL COMMENT '통화 코드',
                        entry_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '전표 생성 시각',

                        reg_id   VARCHAR(50)  NULL COMMENT '등록자 ID',
                        reg_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
                        mod_id   VARCHAR(50)  NULL COMMENT '수정자 ID',
                        mod_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

                        CONSTRAINT fk_ledger_transfer
                            FOREIGN KEY (transfer_seq) REFERENCES transfers(transfer_seq),
                        CONSTRAINT fk_ledger_account
                            FOREIGN KEY (account_seq) REFERENCES account(account_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='원장(전표) 테이블';

CREATE INDEX ix_ledger_account_entry_time
    ON ledger(account_seq, entry_time);

CREATE INDEX ix_ledger_transfer
    ON ledger(transfer_seq);


-- ===========================================================
-- 7) OUTBOX_EVENTS (Outbox 패턴용 이벤트 저장소)
-- ===========================================================
CREATE TABLE outbox_events (
                               outbox_seq     BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '아웃박스 이벤트 고유 식별자',
                               aggregate_type VARCHAR(50) NOT NULL COMMENT '집계 루트 타입 (예: TRANSFER)',
                               aggregate_id   BIGINT NOT NULL COMMENT '집계 루트 ID',
                               event_type     VARCHAR(50) NOT NULL COMMENT '이벤트 타입',
                               payload        JSON NOT NULL COMMENT '이벤트 페이로드(JSON)',
                               created_date   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '이벤트 생성 시각',
                               published_date DATETIME NULL COMMENT '외부 발행 시각',

                               reg_id   VARCHAR(50)  NULL COMMENT '등록자 ID',
                               reg_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
                               mod_id   VARCHAR(50)  NULL COMMENT '수정자 ID',
                               mod_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Outbox 이벤트 테이블';

CREATE INDEX ix_outbox_pub_create
    ON outbox_events(published_date, created_date);
