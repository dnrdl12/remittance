/**
 * 초기 데이터 세팅 스크립트
 */

-- ==========================================
-- V2__init_data.sql
-- fee_policy 3개 + 시스템 member(1) + 계좌 2개
-- ==========================================

-- ------------------------------------------------------------
-- 1) FEE_POLICY (기본 + VIP_FEE + BANK_FEE)
-- ------------------------------------------------------------
INSERT INTO fee_policy (
    fee_policy_seq,
    policy_name,
    transfer_fee_rate,
    withdraw_fee_rate,
    event_flag,
    start_date,
    end_date,
    reg_id,
    mod_id
) VALUES (
  1,
  '기본 수수료 정책',
  0.00100,     -- 일반 이체 수수료율
  0.00000,     -- 출금 수수료 없음
  0,
  NULL,
  NULL,
  'SYSTEM',
  'SYSTEM'
), (
  2,
  'VIP_FEE',
  0.00000,     -- VIP 고객: 이체 수수료 없음
  0.00000,
  0,
  NULL,
  NULL,
  'SYSTEM',
  'SYSTEM'
), (
  3,
  'BANK_FEE',
  0.00150,     -- 예: 타행/은행용 수수료율 (원하면 값 조정)
  0.00000,
  0,
  NULL,
  NULL,
  'SYSTEM',
  'SYSTEM'
);

-- ------------------------------------------------------------
-- 2) 시스템 계정용 멤버 (member_seq = 1)
-- ------------------------------------------------------------
INSERT INTO member (
    member_seq,
    member_nm,
    member_phone,
    member_phone_hash,
    member_ci,
    member_ci_hash,
    member_di,
    member_di_hash,
    member_status,
    priv_consent_yn,
    msg_consent_yn,
    reg_id,
    mod_id
) VALUES (
    1,
    'SYSTEM_MEMBER',
    'AyC4kQXOUl4vtHrmDIft664QDWK5Z//zsvzelm0oMo+V0GbQBx2T',
    'psiKFUY1x3fA/5iwgIeB4tNIc+fxPgSGl+gl6VUyfX0=',
    'AJC6ctDnKyl/tSu7c2a95bzu2ipAL2sY73bUztUce8fSF9r4OQ==',
    'DxlUpIp6SwTyr8B+kBsMm3w9v+WbAXDdWNGx2a/TalE=',
    'VDmtO8uFlxhCXzTFQVGm7tr52A/FF6pTnDcHSXeuKeIGsyul0Q==',
    'eKKjvJQ59F1lavxhLkVUYg3Hc4SDbWQC5G9QUYq5oVk=',
    1,
    'Y',
    'Y',
    'SYSTEM',
    'SYSTEM'
    );

-- ------------------------------------------------------------
-- 3) ACCOUNT (시스템 계좌 2개 — member_seq = 1)
-- ------------------------------------------------------------

-- 시스템 입출금 계좌 (account_seq = 1)
INSERT INTO account (
    account_seq,
    account_number,
    account_sort,
    account_status,
    account_type,
    bank_code,
    branch_code,
    closed_date,
    created_date,
    daily_transfer_limit,
    daily_withdraw_limit,
    nickname,
    fee_policy_seq,
    member_seq,
    reg_id,
    mod_id
) VALUES (
    1,
    '999-0000-000001',
    1,
    1,                    -- 정상
    1,                    -- 일반 계좌
    '999',
    '0001',
    NULL,
    CURRENT_TIMESTAMP,
    999999999,
    999999999,
    'SYSTEM-ACCOUNT',
    1,                    -- 기본 수수료 정책
    1,                    -- 시스템 멤버
    'SYSTEM',
    'SYSTEM'
);

-- 수수료 적립 계좌 (account_seq = 2)
INSERT INTO account (
    account_seq,
    account_number,
    account_sort,
    account_status,
    account_type,
    bank_code,
    branch_code,
    closed_date,
    created_date,
    daily_transfer_limit,
    daily_withdraw_limit,
    nickname,
    fee_policy_seq,
    member_seq,
    reg_id,
    mod_id
) VALUES (
     2,
     '999-0000-000002',
     1,
     1,                    -- 정상
     1,                    -- 일반 계좌
     '999',
     '0002',
     NULL,
     CURRENT_TIMESTAMP,
     999999999,
     999999999,
     'FEE-ACCOUNT',
     3,                    -- BANK_FEE 정책 연결 (은행용 수수료 계좌)
     1,                    -- 시스템 멤버
     'SYSTEM',
     'SYSTEM'
 );

-- ------------------------------------------------------------
-- 4) BALANCE_SNAPSHOTS (시스템 계좌 초기 잔액)
-- ------------------------------------------------------------
INSERT INTO balance_snapshots (
    account_seq,
    balance,
    reg_id,
    mod_id
) VALUES
  (1, 99999999999, 'SYSTEM', 'SYSTEM'),
  (2, 0, 'SYSTEM', 'SYSTEM');
