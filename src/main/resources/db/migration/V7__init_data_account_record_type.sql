-- ===== ACCOUNT RECORD 초기 데이터 =====
INSERT IGNORE INTO account_record_type (code, name, description) VALUES
('SALE', '상품 판매', '판매가 확정되어 지갑에 돈이 입금됨'),
('WITHDRAWAL', '출금', '지갑에서 돈을 정상적으로 출금'),
('REFUND', '환불', '환불한 금액만큼 지갑에서 차감');