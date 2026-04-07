package work.trade.auth.dto.response;

public record AccessTokenResponseDto(
        String accessToken,
        Long accessTokenExpiresIn) {
}
