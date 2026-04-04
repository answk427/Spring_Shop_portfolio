package work.trade.user.repository;


import work.trade.user.domain.User;

import java.util.Optional;

public interface UserRepositoryOld {

    //CRUD
    public User save(User user);
    public Optional<User> findById(Long id);
    public Optional<User> findByEmail(String email);
    public void delete(User user);
    public void deleteAll();
}
