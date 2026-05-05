-- 1. 기존 외래 키 제약 조건 삭제
ALTER TABLE wallets DROP FOREIGN KEY fk_wallets_user;

-- 2. user_id 컬럼의 NOT NULL 제약 조건 제거 (NULL 허용으로 변경)
ALTER TABLE wallets MODIFY COLUMN user_id bigint UNSIGNED NULL;

-- 3. ON DELETE SET NULL 옵션을 적용하여 외래 키 재설정
ALTER TABLE wallets
    ADD CONSTRAINT fk_wallets_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;