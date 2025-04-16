package pet.project.wish.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pet.project.wish.dto.FriendUserDto;
import pet.project.wish.dto.UserAuthDto;
import pet.project.wish.dto.UserCreatedDto;
import pet.project.wish.dto.UserDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.mapper.UserMapper;
import pet.project.wish.model.User;
import pet.project.wish.repository.UserRepository;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final TransactionalOperator transactionalOperator;
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public Mono<UserDto> create(UserCreatedDto dto) {
        return Mono.defer(() ->
                        // Преобразуем DTO в User (без блокировки)
                        mapper.mapToUserCreatedDto(dto)
                )
                .flatMap(repository::save)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))  // Сохраняем реактивно
                .flatMap(mapper::mapToUser) // Преобразуем в UserDto
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<User> update(User user) {
        return null;
    }

    @Override
    public Mono<UserDto> getId(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(mapper::mapToUser);
    }

    @Override
    public Flux<User> getAll() {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Mono<UserDto> login(UserAuthDto dto) {
        return repository.findFirstByNameAndPassword(dto.name(), dto.password())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(mapper::mapToUser);
    }

    @Override
    public Flux<FriendUserDto> getFriends(Flux<Long> friendIds) {
        return repository.findByIdsCustom(friendIds)
                .switchIfEmpty(Flux.error(new NotFoundException("Friends not found")))
                .transform(mapper::mapToFriendUsers);
    }
}
