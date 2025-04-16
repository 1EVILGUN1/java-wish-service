package pet.project.wish.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pet.project.wish.dto.Token;
import pet.project.wish.dto.UserAuthDto;
import pet.project.wish.dto.UserCreatedDto;
import pet.project.wish.dto.UserDto;
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
                .token(new Token(jwt.generateRefreshToken(user.getId()), jwt.generateAccessToken(user.getId())))
                .build()
        );
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
}


