package work.trade.wallet.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountRecordDto(
        AccountRecordTypeDto type,
        BigDecimal amount,
        LocalDateTime createdAt
) {

}
