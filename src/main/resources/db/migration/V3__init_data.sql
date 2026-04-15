-- ===== Order Status 초기 데이터 =====
INSERT IGNORE INTO order_status (code, name, description) VALUES
('PENDING', '주문 대기', '주문이 접수되었으나 확정되지 않은 상태'),
('CONFIRMED', '주문 확정', '주문이 확정되어 결제가 완료된 상태'),
('SHIPPED', '배송 중', '상품이 배송 중인 상태'),
('DELIVERED', '배송 완료', '상품이 배송 완료되어 고객이 수령한 상태'),
('CANCELLED', '주문 취소', '주문이 취소된 상태');

-- ===== Auth Providers 초기 데이터 =====
INSERT IGNORE INTO auth_providers (code, name, description) VALUES
('LOCAL', '로컬 인증', '이메일/비밀번호로 로그인'),
('GOOGLE', '구글 로그인', '구글 소셜 로그인'),
('KAKAO', '카카오 로그인', '카카오 소셜 로그인');

-- ===== Account Record Type 초기 데이터 =====
INSERT IGNORE INTO account_record_type (code, name, description) VALUES
('DEPOSIT', '입금', '지갑에 돈을 입금'),
('WITHDRAWAL', '출금', '지갑에서 돈을 출금'),
('PAYMENT', '결제', '주문 결제로 출금'),
('REFUND', '환불', '주문 환불로 입금');

-- ===== Categories 초기 데이터 (선택사항) =====
INSERT IGNORE INTO categories (id, parent_id, name) VALUES
(1, NULL, '전자제품'),
(2, 1, '스마트폰'),
(3, 1, '노트북'),
(4, NULL, '의류'),
(5, 4, '상의'),
(6, 4, '하의');