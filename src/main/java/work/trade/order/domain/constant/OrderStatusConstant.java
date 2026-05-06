package work.trade.order.domain.constant;

public class OrderStatusConstant {
    public static final String PENDING = "PENDING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String SHIPPED = "SHIPPED";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";
    public static final String RETURNED = "RETURNED";

    private OrderStatusConstant() {
        throw new AssertionError("Cannot instantiate");
    }
}
