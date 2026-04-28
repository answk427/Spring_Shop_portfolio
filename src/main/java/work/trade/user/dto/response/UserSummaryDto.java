package work.trade.user.dto.response;

//경량 조회용 DTO
public record UserSummaryDto(
        Long id,
        String email,
        String name
) {

}
