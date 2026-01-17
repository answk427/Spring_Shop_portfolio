package work.trade.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import work.trade.user.domain.User;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.request.UserUpdateDto;
import work.trade.user.dto.response.SellerDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.dto.response.UserSummaryDto;

@Mapper(componentModel = "spring", uses = {AuthProviderMapper.class})
//@Mapper(componentModel = "spring")
public interface UserMapper {

//Request -> Entity
//-------------------------------------//
    User toEntity(UserCreateRequestDto dto);
    //Password는 서비스에서 Hash로 변경
    User updateEntityFromDto(UserUpdateDto dto, @MappingTarget User user);

//Entity -> Response
//-------------------------------------//
    UserDto toDto(User user);
    UserSummaryDto toSummaryDto(User user);
    SellerDto toSellerDto(User user);

}
