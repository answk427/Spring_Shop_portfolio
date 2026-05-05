package work.trade.wallet.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletDto (
        BigDecimal balance,
        LocalDateTime createdAt
){

}
