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
        log.info("Создание нового пользователя: {}", dto.name());
        return Mono.defer(() -> mapper.mapToUserCreatedDto(dto))
                .doOnNext(user -> log.debug("Преобразование DTO в сущность User"))
                .flatMap(repository::save)
                .doOnNext(user -> log.debug("Пользователь сохранен с ID: {}", user.getId()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Ошибка сохранения пользователя в БД");
                    return Mono.error(new NotFoundException("Ошибка создания пользователя"));
                }))
                .flatMap(user -> mapper.mapToUserResponseDto(Mono.just(user)))
                .doOnSuccess(user -> log.info("Пользователь {} успешно создан. ID: {}", user.name(), user.id()))
                .doOnError(e -> log.error("Ошибка при создании пользователя", e))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<UserResponseDto> update(UserRequestCreatedDto dto, Long id) {
        log.info("Обновление данных пользователя с ID: {}", id);
        return Mono.defer(() -> mapper.mapToUserCreatedDto(dto))
                .doOnNext(user -> {
                    user.setId(id);
                    log.debug("Установлен ID пользователя для обновления: {}", id);
                })
                .flatMap(repository::save)
                .doOnNext(user -> log.debug("Данные пользователя обновлены"))
                .flatMap(user -> mapper.mapToUserResponseDto(Mono.just(user)))
                .doOnSuccess(user -> log.info("Данные пользователя {} успешно обновлены", id))
                .doOnError(e -> log.error("Ошибка при обновлении пользователя", e))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<UserResponseDto> getId(Long id) {
        log.info("Запрос пользователя с ID: {}", id);
        return repository.findById(id)
                .doOnNext(user -> log.debug("Найден пользователь в БД: {}", user))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return Mono.error(new NotFoundException("Пользователь не найден"));
                }))
                .flatMap(user -> mapper.mapToUserResponseDto(Mono.just(user)))
                .doOnSuccess(user -> log.debug("Успешно получены данные пользователя {}", id))
                .doOnError(e -> log.error("Ошибка при получении пользователя", e));
    }

    @Override
    public Flux<UserResponseDto> getAll() {
        log.info("Запрос списка всех пользователей");
        return repository.findAll()
                .flatMap(user -> mapper.mapToUserResponseDto(Mono.just(user)))
                .doOnComplete(() -> log.info("Получено {} пользователей", "N")) // Замените N на реальное количество при необходимости
                .doOnError(e -> log.error("Ошибка при получении списка пользователей", e));
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Удаление пользователя с ID: {}", id);
        return repository.deleteById(id)
                .doOnSuccess(v -> log.warn("Пользователь {} успешно удален", id))
                .doOnError(e -> log.error("Ошибка при удалении пользователя", e));
    }

    @Override
    public Mono<UserResponseDto> login(UserAuthDto dto) {
        log.info("Попытка входа пользователя: {}", dto.name());
        return repository.findUserByNameAndPassword(dto.name(), dto.password())
                .doOnNext(user -> log.debug("Аутентификация успешна для {}", dto.name()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Неудачная попытка входа для {}", dto.name());
                    return Mono.error(new NotFoundException("Ошибка аутентификации"));
                }))
                .flatMap(user -> mapper.mapToUserResponseDto(Mono.just(user)))
                .doOnSuccess(user -> log.info("Пользователь {} успешно авторизован", user.name()))
                .doOnError(e -> log.error("Ошибка при авторизации", e));
    }

    @Override
    public Flux<FriendUserResponseDto> getFriends(Flux<Long> friendIds) {
        log.info("Запрос списка друзей");
        return repository.findByIdsCustom(friendIds)
                .doOnNext(user -> log.debug("Обработка друга с ID: {}", user.getId()))
                .switchIfEmpty(Flux.defer(() -> {
                    log.warn("Друзья не найдены");
                    return Flux.error(new NotFoundException("Друзья не найдены"));
                }))
                .transform(mapper::mapToFriendUsers)
                .doOnComplete(() -> log.debug("Завершена обработка списка друзей"))
                .doOnError(e -> log.error("Ошибка при получении друзей", e));
    }

    @Override
    public Mono<UserResponseDto> getFriend(Long userId, Long friendId) {
        log.info("Поиск друга {} у пользователя {}", friendId, userId);
        return repository.getFriend(userId, friendId)
                .doOnNext(user -> log.debug("Найден друг {} у пользователя {}", friendId, userId))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Друг {} не найден у пользователя {}", friendId, userId);
                    return Mono.error(new NotFoundException("Друг не найден"));
                }))
                .flatMap(user -> mapper.mapToFriendUserDto(Mono.just(user)))
                .doOnError(e -> log.error("Ошибка при поиске друга", e));
    }

    @Override
    public Mono<Void> addPresent(Long userId, Long presentId) {
        log.info("Добавление подарка {} пользователю {}", presentId, userId);
        return findByIdUserInDatabase(userId)
                .doOnNext(user -> log.debug("Найден пользователь для добавления подарка"))
                .flatMap(user -> {
                    if (user.getPresentIds() == null || user.getPresentIds().isEmpty()) {
                        log.debug("Инициализация списка подарков для пользователя {}", userId);
                        user.setPresentIds(new ArrayList<>());
                    }
                    if (!user.getPresentIds().contains(presentId)) {
                        log.debug("Добавление нового подарка {} пользователю {}", presentId, userId);
                        user.getPresentIds().add(presentId);
                    }
                    return repository.save(user);
                })
                .then()
                .doOnSuccess(v -> log.info("Подарок {} успешно добавлен пользователю {}", presentId, userId))
                .doOnError(e -> log.error("Ошибка при добавлении подарка", e))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<Void> removePresent(Long userId, Long presentId) {
        log.info("Удаление подарка {} у пользователя {}", presentId, userId);
        return findByIdUserInDatabase(userId)
                .flatMap(user -> {
                    if (user.getPresentIds() == null) {
                        log.warn("Список подарков пользователя {} пуст", userId);
                        return Mono.error(new NotFoundException("Список подарков пуст"));
                    }
                    if (!user.getPresentIds().contains(presentId)) {
                        log.warn("Подарок {} не найден у пользователя {}", presentId, userId);
                        return Mono.error(new NotFoundException("Подарок не найден"));
                    }
                    log.debug("Удаление подарка {} у пользователя {}", presentId, userId);
                    user.getPresentIds().remove(presentId);
                    return repository.save(user);
                })
                .then()
                .doOnSuccess(v -> log.info("Подарок {} успешно удален у пользователя {}", presentId, userId))
                .doOnError(e -> log.error("Ошибка при удалении подарка", e))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<Void> addFriend(Long userId, Long friendId) {
        log.info("Добавление друга {} пользователю {}", friendId, userId);
        return Mono.zip(
                        findByIdUserInDatabase(userId),
                        findByIdUserInDatabase(friendId)
                )
                .doOnNext(tuple -> log.debug("Оба пользователя найдены"))
                .flatMap(tuple -> {
                    User user = tuple.getT1();
                    User friend = tuple.getT2();
                    if (user.getFriendsIds() == null) {
                        log.debug("Инициализация списка друзей для пользователя {}", userId);
                        user.setFriendsIds(new ArrayList<>());
                    }
                    if (!user.getFriendsIds().contains(friendId)) {
                        log.debug("Добавление нового друга {} пользователю {}", friendId, userId);
                        user.getFriendsIds().add(friendId);
                        return repository.save(user);
                    }
                    log.warn("Пользователь {} уже есть в друзьях у {}", friendId, userId);
                    return Mono.just(user);
                })
                .then()
                .doOnSuccess(v -> log.info("Друг {} успешно добавлен пользователю {}", friendId, userId))
                .doOnError(e -> log.error("Ошибка при добавлении друга", e))
                .as(transactionalOperator::transactional);
    }

    private Mono<User> findByIdUserInDatabase(Long id) {
        log.debug("Поиск пользователя в БД по ID: {}", id);
        return repository.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Пользователь с ID {} не найден", id);
                    return Mono.error(new NotFoundException("Пользователь не найден"));
                }));
    }
}