package work.trade.auth.dto.response;


public record LoginResponseDto(
        String accessToken,
        Long accessTokenExpiresIn,
        String refreshToken,
        Long refreshTokenExpiresIn) {
}
