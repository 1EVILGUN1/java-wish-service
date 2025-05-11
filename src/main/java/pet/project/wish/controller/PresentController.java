package pet.project.wish.controller;

import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.present.PresentFullResponseDto;
import pet.project.wish.dto.present.PresentSmallResponseDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.service.JwtUtil;
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
        log.info("Retrieving presents for user with token: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        return userService.getId(jwt.getUserIdFromToken(token))
                .flatMapMany(present -> {
                    Iterable<Long> ids = present.presentIds() != null ? present.presentIds() : Collections.emptyList();
                    log.debug("Found present IDs for user: {}", ids);
                    return service.getPresentsUser(Flux.fromIterable(ids))
                            .doOnComplete(() -> log.info("Successfully retrieved presents for user"))
                            .onErrorResume(NotFoundException.class, e -> {
                                log.warn("No presents found for user: {}", e.getMessage());
                                return Flux.empty();
                            })
                            .doOnError(e -> log.error("Error retrieving presents for user", e));
                });
    }

    @GetMapping(value = "/present/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PresentFullResponseDto> getPresent(@RequestHeader("Authorization") @NotBlank String token,
                                                   @PathVariable("id") @NotNull @Positive Long id) {
        log.info("Retrieving present with ID: {} for user with token: {}", id, token.substring(0, Math.min(token.length(), 10)) + "...");
        try {
            jwt.validateToken(token);
            log.debug("Token validated successfully for present ID: {}", id);
            return service.getId(id)
                    .doOnSuccess(present -> log.info("Successfully retrieved present with ID: {}", id))
                    .doOnError(NotFoundException.class, e -> log.warn("Present with ID: {} not found", id, e))
                    .doOnError(e -> log.error("Error retrieving present with ID: {}", id, e));
        } catch (Exception e) {
            log.error("Invalid token for retrieving present with ID: {}", id, e);
            throw e;
        }
    }
}