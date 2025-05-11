package pet.project.wish.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pet.project.wish.dto.FriendUserResponseDto;
import pet.project.wish.dto.user.UserAuthDto;
import pet.project.wish.dto.user.UserRequestCreatedDto;
import pet.project.wish.dto.user.UserResponseDto;
import pet.project.wish.model.User;
import pet.project.wish.util.JwtUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final JwtUtil jwt;

    public Mono<UserResponseDto> mapToUser(User user) {
        return Mono.just(UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .lastName(user.getLastName())
                .birthday(user.getBirthday())
                .friendsIds(user.getFriendsIds())
                .presentIds(user.getPresentIds())
                .url(user.getUrl())
                .build());
    }

    public Flux<FriendUserResponseDto> mapToFriendUsers(Flux<User> users) {
        return users.map(user -> FriendUserResponseDto.builder()
                .name(user.getName())
                .lastName(user.getLastName())
                .url(user.getUrl())
                .build());
    }

    public Mono<User> mapToUserCreatedDto(UserRequestCreatedDto dto) {
        User user = new User();
        user.setName(dto.name());
        user.setLastName(dto.lastName());
        user.setBirthday(dto.birthday());
        user.setPassword(dto.password());
        return Mono.just(user);
    }

    public Mono<User> mapToUserAuthDto(UserAuthDto dto) {
        User user = new User();
        user.setName(dto.name());
        user.setPassword(dto.password());
        return Mono.just(user);
    }

    public Mono<UserResponseDto> mapToFriendUserDto(Mono<User> user) {
        return user.map(userDb -> UserResponseDto.builder()
                .id(userDb.getId())
                .name(userDb.getName())
                .lastName(userDb.getLastName())
                .birthday(userDb.getBirthday())
                .friendsIds(userDb.getFriendsIds())
                .presentIds(userDb.getPresentIds())
                .url(userDb.getUrl())
                .build());
    }

    public Mono<User> mapToUserDto(UserResponseDto userResponseDto) {
        User userDb = new User();
        userDb.setId(userResponseDto.id());
        userDb.setName(userResponseDto.name());
        userDb.setLastName(userResponseDto.lastName());
        userDb.setBirthday(userResponseDto.birthday());
        userDb.setUrl(userResponseDto.url());
        userDb.setFriendsIds(userResponseDto.friendsIds());
        userDb.setPresentIds(userResponseDto.presentIds());
        return Mono.just(userDb);
    }
}


