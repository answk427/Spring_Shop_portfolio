package work.trade.user.event;


import work.trade.user.domain.User;

public record UserCreatedEvent(User user) {
}
