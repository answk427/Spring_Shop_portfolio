package work.trade.user.repository;


import work.trade.user.domain.User;
import work.trade.user.dto.request.UserUpdateDto;

import java.util.Optional;

public interface UserRepository {

    //CRUD
    public User save(User user);
    public Optional<User> findById(Long id);
    public void delete(User user);
}
