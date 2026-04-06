package work.trade.user.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//경량 조회용 DTO
@Getter
@Setter
@NoArgsConstructor
public class UserSummaryDto {
    private Long id;
    private String email;
    private String name;
}
