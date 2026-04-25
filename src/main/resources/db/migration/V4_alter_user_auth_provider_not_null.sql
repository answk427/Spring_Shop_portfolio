-- 1. 기존 NULL 데이터를 기본값(예: 'LOCAL')으로 업데이트
UPDATE users SET auth_provider = 'LOCAL' WHERE auth_provider IS NULL;

-- 2. 필드에 NOT NULL 제약 조건 추가
ALTER TABLE users MODIFY auth_provider VARCHAR(20) NOT NULL;