UPDATE account_record_type
SET
    code = 'REFUND_TO_BUYER',
    name = '구매자 환불',
    description = '환불 금액이 구매자 지갑으로 반환됨'
WHERE code = 'REFUND';

INSERT INTO account_record_type (code, name, description)
VALUES (
           'REFUND_FROM_SELLER',
           '판매자 환불 차감',
           '환불 금액만큼 판매자 지갑에서 차감'
       );