package pet.project.wish.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pet.project.wish.dto.FriendUserResponseDto;
import pet.project.wish.dto.user.UserAuthDto;
import pet.project.wish.dto.user.UserRequestCreatedDto;
import pet.project.wish.dto.user.UserResponseDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.mapper.UserMapper;
import pet.project.wish.model.User;
import pet.project.wish.repository.UserRepository;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final TransactionalOperator transactionalOperator;
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public Mono<UserResponseDto> create(UserRequestCreatedDto dto) {
        return Mono.defer(() -> mapper.mapToUserCreatedDto(dto))
                .flatMap(repository::save)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))  // Сохраняем реактивно
                .flatMap(mapper::mapToUser) // Преобразуем в UserDto
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<UserResponseDto> update(UserRequestCreatedDto dto, Long id) {
        return Mono.defer(() -> mapper.mapToUserCreatedDto(dto))
                .doOnNext(user -> user.setId(id))
                .flatMap(repository::save)
                .flatMap(mapper::mapToUser)
                .as(transactionalOperator::transactional);

    }

    @Override
    public Mono<UserResponseDto> getId(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(mapper::mapToUser);
    }

    @Override
    public Flux<UserResponseDto> getAll() {
        return repository.findAll().flatMap(mapper::mapToUser);
    }

    @Override
    public Mono<Void> delete(Long id) {
           return repository.deleteById(id);
    }

    @Override
    public Mono<UserResponseDto> login(UserAuthDto dto) {
        return repository.findFirstByNameAndPassword(dto.name(), dto.password())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(mapper::mapToUser);
    }

    @Override
    public Flux<FriendUserResponseDto> getFriends(Flux<Long> friendIds) {
        return repository.findByIdsCustom(friendIds)
                .switchIfEmpty(Flux.error(new NotFoundException("Friends not found")))
                .transform(mapper::mapToFriendUsers);
    }

    @Override
    public Mono<UserResponseDto> getFriend(Long userId, Long friendId) {
        return repository.getFriend(userId, friendId)
                .switchIfEmpty(Mono.error(new NotFoundException("Friend not found")))
                .transform(mapper::mapToFriendUserDto);
    }

    @Override
    public Mono<Void> addPresent(Long userId, Long presentId) {
        return getId(userId)
                .flatMap(userDto -> {
                    // Получаем пользователя из DTO
                    return mapper.mapToUserDto(userDto) // Предполагается, что есть метод mapToUserEntity
                            .flatMap(user -> {
                                // Добавляем presentId в список, если его там нет
                                if(user.getPresentIds() == null || user.getPresentIds().isEmpty()) {
                                    user.setPresentIds(new ArrayList<>());
                                }
                                if (!user.getPresentIds().contains(presentId)) {
                                    user.getPresentIds().add(presentId);
                                }
                                // Сохраняем обновленного пользователя
                                return repository.save(user);
                            });
                })
                .then() // Преобразуем в Mono<Void>
                .as(transactionalOperator::transactional); // Обеспечиваем транзакционность
    }

    @Override
    public Mono<Void> addFriend(Long userId, Long friendId) {
        return getId(userId)
                .flatMap(userDto ->{
                    return mapper.mapToUserDto(userDto)
                            .flatMap(user -> {
                                if(user.getFriendsIds() == null || user.getFriendsIds().isEmpty()) {
                                    user.setFriendsIds(new ArrayList<>());
                                }
                                if (!user.getFriendsIds().contains(friendId)) {
                                    user.getFriendsIds().add(friendId);
                                }
                                return repository.save(user);
                            });
                })
                .then()
                .as(transactionalOperator::transactional);
    }
}
