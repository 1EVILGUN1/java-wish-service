package pet.project.wish.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.present.PresentFullResponseDto;
import pet.project.wish.dto.present.PresentSmallResponseDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.util.JwtUtil;
import pet.project.wish.service.PresentService;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
public class PresentController {
    private final PresentService service;
    private final UserService userService;
    private final JwtUtil jwt;

    @GetMapping(value = "/present", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<PresentSmallResponseDto> getPresents(@RequestHeader("Authorization") @NotBlank String token) {
        log.info("Получение списка подарков для пользователя с токеном: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        return userService.getId(jwt.getUserIdFromToken(token))
                .flatMapMany(present -> {
                    Iterable<Long> ids = present.presentIds() != null ? present.presentIds() : Collections.emptyList();
                    log.debug("Найдены идентификаторы подарков для пользователя: {}", ids);
                    return service.getPresentsUser(Flux.fromIterable(ids))
                            .doOnComplete(() -> log.info("Успешно получены подарки для пользователя"))
                            .onErrorResume(NotFoundException.class, e -> {
                                log.warn("Подарки для пользователя не найдены: {}", e.getMessage());
                                return Flux.empty();
                            })
                            .doOnError(e -> log.error("Ошибка при получении подарков для пользователя: {}", e.getMessage(), e));
                });
    }

    @GetMapping(value = "/present/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PresentFullResponseDto> getPresent(@RequestHeader("Authorization") @NotBlank String token,
                                                   @PathVariable("id") @NotNull @Positive Long id) {
        log.info("Получение подарка с идентификатором: {} для пользователя с токеном: {}", id, token.substring(0, Math.min(token.length(), 10)) + "...");
        try {
            jwt.validateToken(token);
            log.debug("Токен успешно проверен для подарка с идентификатором: {}", id);
            return service.getId(id)
                    .doOnSuccess(present -> log.info("Успешно получен подарок с идентификатором: {}", id))
                    .doOnError(NotFoundException.class, e -> log.warn("Подарок с идентификатором: {} не найден", id, e))
                    .doOnError(e -> log.error("Ошибка при получении подарка с идентификатором: {}", id, e));
        } catch (Exception e) {
            log.error("Недействительный токен при получении подарка с идентификатором: {}", id, e);
            throw e;
        }
    }
}