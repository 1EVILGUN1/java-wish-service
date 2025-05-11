package pet.project.wish.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.Token;
import pet.project.wish.dto.user.UserRequestCreatedDto;
import pet.project.wish.dto.user.UserResponseDto;
import pet.project.wish.dto.user.UserSignUpResponseDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.util.JwtUtil;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Mono;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
public class UserCRUDController {
    private final UserService service;
    private final JwtUtil jwt;

    @PostMapping(value = "/sign-up", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserSignUpResponseDto> signUpUser(@RequestBody @Valid UserRequestCreatedDto dto) {
        log.info("Регистрация нового пользователя с именем: {}", dto.name());
        return service.create(dto)
                .flatMap(userDto -> {
                    log.debug("Пользователь {} успешно зарегистрирован. ID: {}", userDto.name(), userDto.id());
                    return Mono.just(UserSignUpResponseDto.builder()
                            .user(userDto)
                            .token(new Token(
                                    jwt.generateRefreshToken(userDto.id()),
                                    jwt.generateAccessToken(userDto.id())))
                            .build());
                })
                .doOnError(e -> log.error("Ошибка при регистрации пользователя", e));
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserResponseDto> update(
            @RequestHeader("Authorization") @NotBlank String token,
            @RequestBody @Valid UserRequestCreatedDto dto) {
        try {
            jwt.validateToken(token);
            Long userId = jwt.getUserIdFromToken(token);
            log.info("Обновление данных пользователя с ID: {}", userId);
            return service.update(dto, userId)
                    .doOnSuccess(u -> log.info("Данные пользователя {} успешно обновлены", userId))
                    .doOnError(e -> log.error("Ошибка при обновлении данных пользователя", e));
        } catch (Exception e) {
            log.error("Ошибка валидации токена при обновлении данных", e);
            return Mono.error(e);
        }
    }

    @DeleteMapping("/remove")
    public Mono<Void> delete(@RequestHeader("Authorization") @NotBlank String token) {
        try {
            jwt.validateToken(token);
            Long userId = jwt.getUserIdFromToken(token);
            log.info("Удаление пользователя с ID: {}", userId);
            return service.delete(userId)
                    .doOnSuccess(v -> log.warn("Пользователь {} успешно удален", userId))
                    .doOnError(e -> log.error("Ошибка при удалении пользователя", e))
                    .onErrorResume(NotFoundException.class, ex -> {
                        log.warn("Пользователь не найден при попытке удаления");
                        return Mono.empty();
                    });
        } catch (Exception e) {
            log.error("Ошибка валидации токена при удалении пользователя", e);
            return Mono.error(e);
        }
    }

    @GetMapping("/user")
    public Mono<UserResponseDto> getUser(@RequestHeader("Authorization") @NotBlank String token) {
        try {
            jwt.validateToken(token);
            Long userId = jwt.getUserIdFromToken(token);
            log.info("Запрос данных пользователя с ID: {}", userId);
            return service.getId(userId)
                    .doOnSuccess(u -> log.debug("Данные пользователя {} успешно получены", userId))
                    .doOnError(e -> log.error("Ошибка при получении данных пользователя", e));
        } catch (Exception e) {
            log.error("Ошибка валидации токена при запросе данных пользователя", e);
            return Mono.error(e);
        }
    }
}