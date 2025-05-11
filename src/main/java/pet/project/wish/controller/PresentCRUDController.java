package pet.project.wish.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pet.project.wish.dto.present.PresentFullResponseDto;
import pet.project.wish.dto.present.PresentRequestCreatedDto;
import pet.project.wish.dto.present.PresentRequestUpdatedDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.util.JwtUtil;
import pet.project.wish.service.PresentService;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Mono;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/presents")
@RequiredArgsConstructor
public class PresentCRUDController {
    private final PresentService service;
    private final UserService userService;
    private final JwtUtil jwt;

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PresentFullResponseDto> update(
            @RequestHeader("Authorization") @NotBlank String token,
            @PathVariable("id") @NotNull @Positive Long id,
            @RequestBody @Valid PresentRequestUpdatedDto dto) {
        log.info("Обновление подарка с ID: {} для пользователя с токеном: {}", id, maskToken(token));
        return validateAndGetUserId(token)
                .flatMap(userId -> {
                    if (!dto.id().equals(id)) {
                        log.warn("Несоответствие между ID в пути: {} и ID в DTO: {}", id, dto.id());
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID в пути и DTO должны совпадать"));
                    }
                    return service.getId(id)
                            .switchIfEmpty(Mono.error(new NotFoundException("Подарок с ID: " + id + " не найден")))
                            .flatMap(present -> service.update(dto));
                })
                .doOnSuccess(present -> log.info("Подарок с ID: {} успешно обновлен", id))
                .doOnError(NotFoundException.class, e -> log.warn("Подарок с ID: {} не найден", id, e))
                .doOnError(e -> log.error("Ошибка при обновлении подарка с ID: {}", id, e))
                .onErrorMap(NotFoundException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PresentFullResponseDto> create(
            @RequestHeader("Authorization") @NotBlank String token,
            @RequestBody @Valid PresentRequestCreatedDto dto) {
        log.info("Создание нового подарка для пользователя с токеном: {}", maskToken(token));
        return validateAndGetUserId(token)
                .flatMap(userId -> service.create(dto)
                        .flatMap(presentFullDto -> userService.addPresent(userId, presentFullDto.id())
                                .thenReturn(presentFullDto)))
                .doOnSuccess(presentFullDto -> log.info("Подарок с ID: {} успешно создан", presentFullDto.id()))
                .doOnError(e -> log.error("Ошибка при создании подарка", e));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> remove(
            @RequestHeader("Authorization") @NotBlank String token,
            @PathVariable("id") @NotNull @Positive Long id) {
        log.info("Удаление подарка с ID: {} для пользователя с токеном: {}", id, maskToken(token));
        return validateAndGetUserId(token)
                .flatMap(userId -> service.getId(id)
                        .switchIfEmpty(Mono.error(new NotFoundException("Подарок с ID: " + id + " не найден")))
                        .flatMap(present -> service.delete(id)))
                .doOnSuccess(v -> log.info("Подарок с ID: {} успешно удален", id))
                .doOnError(NotFoundException.class, e -> log.warn("Подарок с ID: {} не найден для удаления", id, e))
                .doOnError(e -> log.error("Ошибка при удалении подарка с ID: {}", id, e))
                .onErrorResume(NotFoundException.class, e -> Mono.empty());
    }

    private Mono<Long> validateAndGetUserId(String token) {
        try {
            jwt.validateToken(token);
            Long userId = jwt.getUserIdFromToken(token);
            log.debug("Токен успешно проверен, ID пользователя: {}", userId);
            return Mono.just(userId);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("Срок действия токена истек", e);
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Срок действия токена истек"));
        } catch (io.jsonwebtoken.JwtException e) {
            log.error("Неверный токен", e);
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный токен"));
        } catch (Exception e) {
            log.error("Ошибка проверки токена", e);
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ошибка проверки токена"));
        }
    }

    private String maskToken(String token) {
        return token.substring(0, Math.min(token.length(), 10)) + "...";
    }
}