package pet.project.wish.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pet.project.wish.dto.user.UserAuthDto;
import pet.project.wish.dto.user.UserCreatedDto;
import pet.project.wish.dto.user.UserDto;
import pet.project.wish.dto.FriendUserDto;
import pet.project.wish.model.User;
import pet.project.wish.service.JwtUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final JwtUtil jwt;

    public Mono<UserDto> mapToUser(User user) {
        return Mono.just(UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .lastName(user.getLastName())
                .birthday(user.getBirthday())
                .friendsIds(user.getFriendsIds())
                .presentIds(user.getPresentIds())
                .url(user.getUrl())
                .build());
    }

    public Flux<FriendUserDto> mapToFriendUsers(Flux<User> users) {
        return users.map(user -> FriendUserDto.builder()
                .name(user.getName())
                .lastName(user.getLastName())
                .url(user.getUrl())
                .build());
    }

    public Mono<User> mapToUserCreatedDto(UserCreatedDto dto) {
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

    public Mono<UserDto> mapToFriendUserDto(Mono<User> user) {
        return user.map(userDb -> UserDto.builder()
                .id(userDb.getId())
                .name(userDb.getName())
                .lastName(userDb.getLastName())
                .birthday(userDb.getBirthday())
                .friendsIds(userDb.getFriendsIds())
                .presentIds(userDb.getPresentIds())
                .url(userDb.getUrl())
                .build());
    }

    public Mono<User> mapToUserDto(UserDto userDto) {
            User userDb = new User();
            userDb.setId(userDto.id());
            userDb.setName(userDto.name());
            userDb.setLastName(userDto.lastName());
            userDb.setBirthday(userDto.birthday());
            userDb.setUrl(userDto.url());
            userDb.setFriendsIds(userDto.friendsIds());
            userDb.setPresentIds(userDto.presentIds());
            return Mono.just(userDb);
    }
}


