package work.trade.user.mapper;

import org.mapstruct.Mapper;
import work.trade.user.domain.User;
import work.trade.user.dto.response.SellerDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.dto.response.UserSummaryDto;

@Mapper(componentModel = "spring", uses = {AuthProviderMapper.class})
public interface UserMapper {

//Entity -> Response
//-------------------------------------//
    UserDto toDto(User user);
    UserSummaryDto toSummaryDto(User user);
    SellerDto toSellerDto(User user);

//DTO -> DTO
//-------------------------------------//
    UserSummaryDto toSummaryDto(UserDto userDto);
}
