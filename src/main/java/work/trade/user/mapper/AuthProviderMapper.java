package work.trade.user.mapper;

import org.mapstruct.Mapper;
import work.trade.user.domain.AuthProvider;
import work.trade.user.dto.response.AuthProviderDto;

@Mapper(componentModel = "spring")
public interface AuthProviderMapper {

    AuthProviderDto toDto(AuthProvider authProvider);
    AuthProvider toEntity(AuthProviderDto authProviderDto);
}
