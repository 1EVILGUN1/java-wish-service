package pet.project.wish.service;

import pet.project.wish.dto.FriendUserDto;
import pet.project.wish.dto.UserAuthDto;
import pet.project.wish.dto.UserCreatedDto;
import pet.project.wish.dto.UserDto;
import pet.project.wish.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserDto> create(UserCreatedDto dto);

    Mono<UserDto> update(UserCreatedDto dto, Long id);

    Mono<UserDto> getId(Long id);

    Flux<User> getAll();

    Mono<Void> delete(Long id);

    Mono<UserDto> login(UserAuthDto dto);

    Flux<FriendUserDto> getFriends(Flux<Long> friendIds);

    Mono<UserDto> getFriend(Long userId, Long friendId);
}
