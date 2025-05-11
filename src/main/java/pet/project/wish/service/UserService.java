package pet.project.wish.service;

import pet.project.wish.dto.FriendUserResponseDto;
import pet.project.wish.dto.user.UserAuthDto;
import pet.project.wish.dto.user.UserRequestCreatedDto;
import pet.project.wish.dto.user.UserResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserResponseDto> create(UserRequestCreatedDto dto);

    Mono<UserResponseDto> update(UserRequestCreatedDto dto, Long id);

    Mono<UserResponseDto> getId(Long id);

    Flux<UserResponseDto> getAll();

    Mono<Void> delete(Long id);

    Mono<UserResponseDto> login(UserAuthDto dto);

    Flux<FriendUserResponseDto> getFriends(Flux<Long> friendIds);

    Mono<UserResponseDto> getFriend(Long userId, Long friendId);

    Mono<Void> addPresent(Long userId, Long presentId);

    Mono<Void> removePresent(Long userId, Long presentId);

    Mono<Void> addFriend(Long userId, Long friendId);
}
