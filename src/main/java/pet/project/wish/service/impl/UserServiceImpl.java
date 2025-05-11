package pet.project.wish.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pet.project.wish.dto.FriendUserResponseDto;
import pet.project.wish.dto.user.UserAuthDto;
import pet.project.wish.dto.user.UserRequestCreatedDto;
import pet.project.wish.dto.user.UserResponseDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.mapper.UseMapper;
import pet.project.wish.model.User;
import pet.project.wish.repository.UserRepository;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final TransactionalOperator transactionalOperator;
    private final UserRepository repository;
    private final UseMapper mapper;

    @Override
    public Mono<UserResponseDto> create(UserRequestCreatedDto dto) {
        return Mono.defer(() -> mapper.mapToUserCreatedDto(dto))
                .flatMap(repository::save)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> mapper.mapToUserResponseDto(Mono.just(user)))
                .flatMap(Mono::just) // Разворачиваем Mono<Mono<UserResponseDto>> в Mono<UserResponseDto>
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<UserResponseDto> update(UserRequestCreatedDto dto, Long id) {
        return Mono.defer(() -> mapper.mapToUserCreatedDto(dto))
                .doOnNext(user -> user.setId(id))
                .flatMap(repository::save)
                .flatMap(user -> mapper.mapToUserResponseDto(Mono.just(user)))
                .flatMap(Mono::just)
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<UserResponseDto> getId(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> mapper.mapToUserResponseDto(Mono.just(user)))
                .flatMap(Mono::just);
    }

    @Override
    public Flux<UserResponseDto> getAll() {
        return repository.findAll()
                .flatMap(user -> mapper.mapToUserResponseDto(Mono.just(user)))
                .flatMap(Mono::just);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<UserResponseDto> login(UserAuthDto dto) {
        return repository.findUserByNameAndPassword(dto.name(), dto.password())
                .log("User login" + dto)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> mapper.mapToUserResponseDto(Mono.just(user)))
                .flatMap(Mono::just);
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
                .flatMap(user -> mapper.mapToFriendUserDto(Mono.just(user)))
                .flatMap(Mono::just);
    }

    @Override
    public Mono<Void> addPresent(Long userId, Long presentId) {
        return findByIdUserInDatabase(userId)
                .flatMap(user -> {
                    if (user.getPresentIds() == null || user.getPresentIds().isEmpty()) {
                        user.setPresentIds(new ArrayList<>());
                    }
                    if (!user.getPresentIds().contains(presentId)) {
                        user.getPresentIds().add(presentId);
                    }
                    return repository.save(user);
                })
                .then()
                .as(transactionalOperator::transactional)
                .onErrorResume(e -> {
                    if (e instanceof NotFoundException) {
                        return Mono.error(e);
                    }
                    log.error("Error adding present", e);
                    return Mono.error(new RuntimeException("Failed to add present", e));
                });
    }

    @Override
    public Mono<Void> removePresent(Long userId, Long presentId) {
        return findByIdUserInDatabase(userId)
                .flatMap(user -> {
                    if (user.getPresentIds() == null) {
                        user.setPresentIds(new ArrayList<>());
                    }
                    if (!user.getPresentIds().contains(presentId)) {
                        return Mono.error(new NotFoundException("Present not found"));
                    }
                    user.getPresentIds().remove(presentId);
                    return repository.save(user);
                })
                .then()
                .as(transactionalOperator::transactional)
                .onErrorResume(e -> {
                    if (e instanceof NotFoundException) {
                        return Mono.error(e);
                    }
                    log.error("Error removing present", e);
                    return Mono.error(new RuntimeException("Failed to remove present", e));
                });
    }

    @Override
    public Mono<Void> addFriend(Long userId, Long friendId) {
        return Mono.zip(
                        findByIdUserInDatabase(userId),
                        findByIdUserInDatabase(friendId)
                )
                .flatMap(tuple -> {
                    User user = tuple.getT1();
                    User friend = tuple.getT2();
                    if (user.getFriendsIds() == null) {
                        user.setFriendsIds(new ArrayList<>());
                    }
                    if (!user.getFriendsIds().contains(friendId)) {
                        user.getFriendsIds().add(friendId);
                        return repository.save(user);
                    }
                    return Mono.just(user);
                })
                .then()
                .onErrorResume(e -> {
                    log.error("Error adding friend", e);
                    return Mono.error(new RuntimeException("Failed to add friend"));
                })
                .as(transactionalOperator::transactional);
    }

    private Mono<User> findByIdUserInDatabase(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")));
    }
}