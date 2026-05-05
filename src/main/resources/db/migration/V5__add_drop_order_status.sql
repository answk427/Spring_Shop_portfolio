-- ===== Order 테이블에서 status_code 제거 =====
ALTER TABLE orders DROP FOREIGN KEY fk_orders_status;
ALTER TABLE orders DROP COLUMN status_code;

-- ===== Order_item 테이블에 status_code 추가 =====
ALTER TABLE order_items ADD COLUMN status_code VARCHAR(20) NOT NULL DEFAULT 'PENDING';

ALTER TABLE order_items ADD CONSTRAINT fk_order_items_status
    FOREIGN KEY (status_code)
        REFERENCES order_status (code)
        ON DELETE RESTRICT
        ON UPDATE CASCADE;

-- ===== 환불, 반품 관련 초기 데이터 =====
INSERT IGNORE INTO order_status (code, name, description) VALUES
('RETURNED', '반품 완료', '상품이 반품되었고 환불이 완료된 상태');