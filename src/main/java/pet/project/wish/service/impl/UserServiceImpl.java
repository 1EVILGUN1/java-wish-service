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
import pet.project.wish.mapper.UserMapper;
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
    private final UserMapper mapper;

    @Override
    public Mono<UserResponseDto> create(UserRequestCreatedDto dto) {
        log.info("Создание нового пользователя с данными: {}", dto);
        return Mono.defer(() -> {
                    log.debug("Преобразование DTO в сущность пользователя");
                    return mapper.mapToUserCreatedDto(dto);
                })
                .flatMap(user -> {
                    log.debug("Сохранение пользователя в базе данных: {}", user);
                    return repository.save(user);
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь не найден")))
                .flatMap(user -> {
                    log.debug("Преобразование сохраненного пользователя в DTO: {}", user);
                    return mapper.mapToUser(user);
                })
                .as(transactionalOperator::transactional)
                .doOnSuccess(result -> log.info("Успешно создан пользователь: {}", result))
                .doOnError(error -> log.error("Ошибка при создании пользователя: {}", error.getMessage(), error));
    }

    @Override
    public Mono<UserResponseDto> update(UserRequestCreatedDto dto, Long id) {
        log.info("Обновление пользователя с идентификатором: {}, данными: {}", id, dto);
        return Mono.defer(() -> {
                    log.debug("Преобразование DTO в сущность пользователя для обновления");
                    return mapper.mapToUserCreatedDto(dto);
                })
                .doOnNext(user -> {
                    log.debug("Установка идентификатора {} для обновляемого пользователя", id);
                    user.setId(id);
                })
                .flatMap(user -> {
                    log.debug("Сохранение обновленного пользователя в базе данных: {}", user);
                    return repository.save(user);
                })
                .flatMap(user -> {
                    log.debug("Преобразование обновленного пользователя в DTO: {}", user);
                    return mapper.mapToUser(user);
                })
                .as(transactionalOperator::transactional)
                .doOnSuccess(result -> log.info("Успешно обновлен пользователь: {}", result))
                .doOnError(error -> log.error("Ошибка при обновлении пользователя: {}", error.getMessage(), error));
    }

    @Override
    public Mono<UserResponseDto> getId(Long id) {
        log.info("Получение пользователя по идентификатору: {}", id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь не найден")))
                .flatMap(user -> {
                    log.debug("Преобразование найденного пользователя в DTO: {}", user);
                    return mapper.mapToUser(user);
                })
                .doOnSuccess(result -> log.info("Успешно получен пользователь: {}", result))
                .doOnError(error -> log.error("Ошибка при получении пользователя: {}", error.getMessage(), error));
    }

    @Override
    public Flux<UserResponseDto> getAll() {
        log.info("Получение всех пользователей");
        return repository.findAll()
                .flatMap(user -> {
                    log.debug("Преобразование пользователя в DTO: {}", user);
                    return mapper.mapToUser(user);
                })
                .doOnComplete(() -> log.info("Успешно получены все пользователи"))
                .doOnError(error -> log.error("Ошибка при получении всех пользователей: {}", error.getMessage(), error));
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Удаление пользователя по идентификатору: {}", id);
        return repository.deleteById(id)
                .doOnSuccess(v -> log.info("Успешно удален пользователь с идентификатором: {}", id))
                .doOnError(error -> log.error("Ошибка при удалении пользователя: {}", error.getMessage(), error));
    }

    @Override
    public Mono<UserResponseDto> login(UserAuthDto dto) {
        log.info("Попытка входа пользователя: {}", dto);
        return repository.findUserByNameAndPassword(dto.name(), dto.password())
                .log("Вход пользователя: " + dto)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь не найден")))
                .flatMap(user -> {
                    log.debug("Преобразование пользователя в DTO после входа: {}", user);
                    return mapper.mapToUser(user);
                })
                .doOnSuccess(result -> log.info("Успешный вход пользователя: {}", result))
                .doOnError(error -> log.error("Ошибка при входе пользователя: {}", error.getMessage(), error));
    }

    @Override
    public Flux<FriendUserResponseDto> getFriends(Flux<Long> friendIds) {
        log.info("Получение друзей пользователя по идентификаторам: {}", friendIds);
        return repository.findByIdsCustom(friendIds)
                .switchIfEmpty(Mono.error(new NotFoundException("Друзья не найдены")))
                .transform(presents -> {
                    log.debug("Преобразование друзей в DTO: {}", presents);
                    return mapper.mapToFriendUsers(presents);
                })
                .doOnComplete(() -> log.info("Успешно получены друзья пользователя"))
                .doOnError(error -> log.error("Ошибка при получении друзей пользователя: {}", error.getMessage(), error));
    }

    @Override
    public Mono<UserResponseDto> getFriend(Long userId, Long friendId) {
        log.info("Получение друга для пользователя {} с идентификатором друга: {}", userId, friendId);
        return repository.getFriend(userId, friendId)
                .switchIfEmpty(Mono.error(new NotFoundException("Друг не найден")))
                .transform(user -> {
                    log.debug("Преобразование друга в DTO: {}", user);
                    return mapper.mapToFriendUserDto(user);
                })
                .doOnSuccess(result -> log.info("Успешно получен друг: {}", result))
                .doOnError(error -> log.error("Ошибка при получении друга: {}", error.getMessage(), error));
    }

    @Override
    public Mono<Void> addPresent(Long userId, Long presentId) {
        log.info("Добавление подарка с идентификатором {} пользователю: {}", presentId, userId);
        return findByIdUserInDatabase(userId)
                .flatMap(user -> {
                    log.debug("Проверка и инициализация списка подарков для пользователя: {}", user);
                    if (user.getPresentIds() == null || user.getPresentIds().isEmpty()) {
                        user.setPresentIds(new ArrayList<>());
                    }
                    if (!user.getPresentIds().contains(presentId)) {
                        log.debug("Добавление подарка с идентификатором: {}", presentId);
                        user.getPresentIds().add(presentId);
                    } else {
                        log.debug("Подарок с идентификатором {} уже есть у пользователя", presentId);
                    }
                    log.debug("Сохранение пользователя с обновленным списком подарков: {}", user);
                    return repository.save(user);
                })
                .then()
                .as(transactionalOperator::transactional)
                .doOnSuccess(v -> log.info("Успешно добавлен подарок пользователю: {}", userId))
                .onErrorResume(e -> {
                    if (e instanceof NotFoundException) {
                        return Mono.error(e);
                    }
                    log.error("Ошибка при добавлении подарка: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("Не удалось добавить подарок", e));
                });
    }

    @Override
    public Mono<Void> removePresent(Long userId, Long presentId) {
        log.info("Удаление подарка с идентификатором {} у пользователя: {}", presentId, userId);
        return findByIdUserInDatabase(userId)
                .flatMap(user -> {
                    log.debug("Проверка списка подарков пользователя: {}", user);
                    if (user.getPresentIds() == null) {
                        user.setPresentIds(new ArrayList<>());
                    }
                    if (!user.getPresentIds().contains(presentId)) {
                        log.warn("Подарок с идентификатором {} не найден у пользователя", presentId);
                        return Mono.error(new NotFoundException("Подарок не найден"));
                    }
                    log.debug("Удаление подарка с идентификатором: {}", presentId);
                    user.getPresentIds().remove(presentId);
                    log.debug("Сохранение пользователя с обновленным списком подарков: {}", user);
                    return repository.save(user);
                })
                .then()
                .as(transactionalOperator::transactional)
                .doOnSuccess(v -> log.info("Успешно удален подарок у пользователя: {}", userId))
                .onErrorResume(e -> {
                    if (e instanceof NotFoundException) {
                        return Mono.error(e);
                    }
                    log.error("Ошибка при удалении подарка: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("Не удалось удалить подарок", e));
                });
    }

    @Override
    public Mono<Void> addFriend(Long userId, Long friendId) {
        log.info("Добавление друга с идентификатором {} пользователю: {}", friendId, userId);
        return Mono.zip(
                        findByIdUserInDatabase(userId),
                        findByIdUserInDatabase(friendId)
                )
                .flatMap(tuple -> {
                    User user = tuple.getT1();
                    User friend = tuple.getT2();
                    log.debug("Проверка и инициализация списка друзей для пользователя: {}", user);
                    if (user.getFriendsIds() == null) {
                        user.setFriendsIds(new ArrayList<>());
                    }
                    if (!user.getFriendsIds().contains(friendId)) {
                        log.debug("Добавление друга с идентификатором: {}", friendId);
                        user.getFriendsIds().add(friendId);
                        log.debug("Сохранение пользователя с обновленным списком друзей: {}", user);
                        return repository.save(user);
                    } else {
                        log.debug("Друг с идентификатором {} уже есть в списке", friendId);
                    }
                    return Mono.just(user);
                })
                .then()
                .doOnSuccess(v -> log.info("Успешно добавлен друг пользователю: {}", userId))
                .onErrorResume(e -> {
                    log.error("Ошибка при добавлении друга: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("Не удалось добавить друга"));
                })
                .as(transactionalOperator::transactional);
    }

    private Mono<User> findByIdUserInDatabase(Long id) {
        log.debug("Поиск пользователя по идентификатору в базе данных: {}", id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь не найден")))
                .doOnSuccess(user -> log.debug("Пользователь найден: {}", user))
                .doOnError(error -> log.error("Ошибка при поиске пользователя: {}", error.getMessage(), error));
    }
}