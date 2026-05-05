package work.trade.wallet.domain.constant;

public class AccountRecordTypeConstant {
    public static final String SALE = "SALE"; //상품 판매
    public static final String WITHDRAWAL = "WITHDRAWAL"; //출금
    public static final String REFUND = "REFUND"; //환불

    private AccountRecordTypeConstant() {
        throw new AssertionError("Cannot instantiate");
    }
}
