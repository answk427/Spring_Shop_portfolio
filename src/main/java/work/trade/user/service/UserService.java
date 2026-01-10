package work.trade.user.service;

import work.trade.user.domain.User;

public interface UserService {

    User getUser(Long id);

    void createUser(User user);
}
