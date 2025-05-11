package pet.project.wish.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import pet.project.wish.dto.FriendUserResponseDto;
import pet.project.wish.dto.user.UserAuthDto;
import pet.project.wish.dto.user.UserRequestCreatedDto;
import pet.project.wish.dto.user.UserResponseDto;
import pet.project.wish.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UseMapper {
    // Синхронные маппинги
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "birthday", source = "birthday")
    @Mapping(target = "friendsIds", source = "friendsIds")
    @Mapping(target = "presentIds", source = "presentIds")
    @Mapping(target = "url", source = "url")
    UserResponseDto toUserResponseDto(User user);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "url", source = "url")
    FriendUserResponseDto toFriendUserResponseDto(User user);

    @Mapping(target = "id", ignore = true) // ID не мапится, так как создается новый пользователь
    @Mapping(target = "name", source = "name")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "birthday", source = "birthday")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "friendsIds", ignore = true) // Предполагается, что новый пользователь не имеет друзей
    @Mapping(target = "presentIds", ignore = true) // Предполагается, что новый пользователь не имеет подарков
    @Mapping(target = "url", ignore = true) // URL может быть сгенерирован позже
    User toUserFromCreatedDto(UserRequestCreatedDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "birthday", ignore = true)
    @Mapping(target = "friendsIds", ignore = true)
    @Mapping(target = "presentIds", ignore = true)
    @Mapping(target = "url", ignore = true)
    User toUserFromAuthDto(UserAuthDto dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "birthday", source = "birthday")
    @Mapping(target = "friendsIds", source = "friendsIds")
    @Mapping(target = "presentIds", source = "presentIds")
    @Mapping(target = "url", source = "url")
    @Mapping(target = "password", ignore = true) // Пароль не мапится из DTO в сущность
    User toUserFromResponseDto(UserResponseDto userResponseDto);

    // Реактивные методы
    default Mono<UserResponseDto> mapToUserResponseDto(Mono<User> user) {
        return user.map(this::toUserResponseDto);
    }

    default Flux<FriendUserResponseDto> mapToFriendUsers(Flux<User> users) {
        return users.map(this::toFriendUserResponseDto);
    }

    default Mono<User> mapToUserCreatedDto(UserRequestCreatedDto dto) {
        return Mono.just(toUserFromCreatedDto(dto));
    }

    default Mono<User> mapToUserAuthDto(UserAuthDto dto) {
        return Mono.just(toUserFromAuthDto(dto));
    }

    default Mono<UserResponseDto> mapToFriendUserDto(Mono<User> user) {
        return user.map(this::toUserResponseDto);
    }

    default Mono<User> mapToUserDto(UserResponseDto userResponseDto) {
        return Mono.just(toUserFromResponseDto(userResponseDto));
    }

}
