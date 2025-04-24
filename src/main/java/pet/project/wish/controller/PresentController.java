package pet.project.wish.controller;

import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.present.PresentFullDto;
import pet.project.wish.dto.present.PresentSmallDto;
import pet.project.wish.dto.user.UserDto;
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
    public Flux<PresentSmallDto> getPresents(@RequestHeader("Authorization") @NotBlank String token) {
        log.info("userid: {}",jwt.getUserIdFromToken(token));
        return userService.getId(jwt.getUserIdFromToken(token))
                .flatMapMany(present -> {
                    Iterable<Long> ids = present.presentIds() != null ? present.presentIds() : Collections.emptyList();
                    return service.getPresentsUser(Flux.fromIterable(ids))
                            .onErrorResume(NotFoundException.class, e -> Flux.empty());
                });
    }

    @GetMapping(value = "/present/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PresentFullDto> getPresent(@RequestHeader("Authorization") @NotBlank String token,
                                           @PathVariable("id") @NotNull @Positive Long id) {
            jwt.validateToken(token);
            return service.getId(id);

    }
}
