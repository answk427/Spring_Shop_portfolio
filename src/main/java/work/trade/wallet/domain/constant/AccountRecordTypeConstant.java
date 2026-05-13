package work.trade.wallet.domain.constant;

public class AccountRecordTypeConstant {
    public static final String PAYMENT = "PAYMENT"; //구매자가 결제시
    public static final String SALE = "SALE"; //상품 판매 (배송 완료시 돈이 들어옴)
    public static final String WITHDRAWAL = "WITHDRAWAL"; //출금
    public static final String REFUND_TO_BUYER = "REFUND_TO_BUYER"; //환불
    public static final String REFUND_FROM_SELLER = "REFUND_FROM_SELLER"; //환불

    private AccountRecordTypeConstant() {
        throw new AssertionError("Cannot instantiate");
    }
}
