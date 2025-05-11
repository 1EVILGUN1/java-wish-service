package pet.project.wish.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.FriendUserResponseDto;
import pet.project.wish.dto.Token;
import pet.project.wish.dto.user.UserAuthDto;
import pet.project.wish.dto.user.UserResponseDto;
import pet.project.wish.dto.user.UserSignUpResponseDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.util.JwtUtil;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final JwtUtil jwt;

    @PostMapping(value = "/sign-in", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserSignUpResponseDto> signInUser(@RequestBody @Valid UserAuthDto dto) {
        log.info("Авторизация пользователя: {}", dto.name());
        return service.login(dto)
                .flatMap(userDto -> {
                    log.debug("Пользователь {} успешно авторизован", userDto.name());
                    return Mono.just(UserSignUpResponseDto.builder()
                            .user(userDto)
                            .token(new Token(jwt.generateRefreshToken(userDto.id()), jwt.generateAccessToken(userDto.id())))
                            .build());
                })
                .doOnError(e -> log.error("Ошибка авторизации пользователя", e));
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Token> refreshToken(
            @RequestHeader("Authorization") @NotBlank String token) {
        log.info("Обновление токена");
        try {
            jwt.validateToken(token);
            Long userId = jwt.getUserIdFromToken(token);
            log.debug("Генерация новых токенов для пользователя с ID: {}", userId);
            return Mono.just(new Token(jwt.generateRefreshToken(userId),
                    jwt.generateAccessToken(userId)));
        } catch (Exception e) {
            log.error("Ошибка при обновлении токена", e);
            return Mono.error(e);
        }
    }

    @GetMapping(value = "/friends", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<FriendUserResponseDto> getFriends(@RequestHeader("Authorization") @NotBlank String token) {
        log.info("Получение списка друзей");
        try {
            jwt.validateToken(token);
            Long userId = jwt.getUserIdFromToken(token);
            log.debug("Поиск друзей для пользователя с ID: {}", userId);

            return service.getId(userId)
                    .flatMapMany(userDto -> {
                        Iterable<Long> ids = userDto.friendsIds() != null ? userDto.friendsIds() : Collections.emptyList();
                        log.debug("Найдено {} друзей", ids.iterator().hasNext() ? ids.iterator().next() : 0);
                        return service.getFriends(Flux.fromIterable(ids))
                                .onErrorResume(NotFoundException.class, e -> {
                                    log.warn("Друзья не найдены", e);
                                    return Flux.empty();
                                });
                    });
        } catch (Exception e) {
            log.error("Ошибка при получении списка друзей", e);
            return Flux.error(e);
        }
    }

    @GetMapping(value = "/friend/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserResponseDto> getFriend(@RequestHeader("Authorization") @NotBlank String token,
                                           @PathVariable("id") @NotNull @Positive Long id) {
        log.info("Получение информации о друге с ID: {}", id);
        try {
            jwt.validateToken(token);
            Long userId = jwt.getUserIdFromToken(token);
            log.debug("Пользователь {} запросил информацию о друге {}", userId, id);
            return service.getFriend(userId, id)
                    .doOnSuccess(f -> log.debug("Информация о друге {} получена", id))
                    .doOnError(e -> log.error("Ошибка при получении информации о друге", e));
        } catch (Exception e) {
            log.error("Ошибка при запросе информации о друге", e);
            return Mono.error(e);
        }
    }

    @GetMapping("/users")
    public Flux<UserResponseDto> getUsers(@RequestHeader("Authorization") @NotBlank String token) {
        log.info("Получение списка всех пользователей");
        try {
            jwt.validateToken(token);
            log.debug("Запрос списка пользователей");
            return service.getAll()
                    .doOnError(e -> log.error("Ошибка при получении списка пользователей", e));
        } catch (Exception e) {
            log.error("Ошибка авторизации при запросе списка пользователей", e);
            return Flux.error(e);
        }
    }

    @PutMapping("/friend/{id}")
    public Mono<Void> addFriend(@RequestHeader("Authorization") @NotBlank String token,
                                @PathVariable("id") @NotNull @Positive Long id) {
        log.info("Добавление пользователя {} в друзья", id);
        try {
            jwt.validateToken(token);
            Long userId = jwt.getUserIdFromToken(token);

            if (userId.equals(id)) {
                log.warn("Попытка добавить самого себя в друзья");
                return Mono.error(new NotFoundException("Нельзя добавить самого себя в друзья"));
            }

            log.debug("Пользователь {} добавляет в друзья пользователя {}", userId, id);
            return service.addFriend(userId, id)
                    .doOnSuccess(v -> log.info("Пользователь {} успешно добавлен в друзья", id))
                    .doOnError(e -> log.error("Ошибка при добавлении в друзья", e));
        } catch (Exception e) {
            log.error("Ошибка при обработке запроса на добавление в друзья", e);
            return Mono.error(e);
        }
    }
}