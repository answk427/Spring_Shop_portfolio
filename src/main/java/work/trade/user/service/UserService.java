package work.trade.user.service;

import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.user.domain.User;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.request.UserUpdateDto;
import work.trade.user.dto.response.UserDto;

import java.util.Optional;

public interface UserService {

    //CRUD
    UserDto createUser(UserCreateRequestDto dto);
    UserDto findUser(Long id);
    UserDto findByEmail(String email);
    UserDto updateUser(Long id, UserUpdateDto dto);
    void deleteById(Long id);
}
