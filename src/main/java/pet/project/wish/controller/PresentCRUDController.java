package pet.project.wish.controller;

import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.PresentFullDto;
import pet.project.wish.dto.UserDto;
import pet.project.wish.error.InvalidTokenException;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.service.JwtUtil;
import pet.project.wish.service.PresentService;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Mono;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
public class PresentCRUDController {
    private final PresentService service;
    private final UserService userService;
    private final JwtUtil jwt;


    @PutMapping(value = "/upd/present", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PresentFullDto> update(@RequestHeader("Authorization") @NotBlank String token,
                                       @RequestBody @Valid PresentFullDto dto) {
        jwt.validateToken(token);
        return service.update(dto);
    }

    @PostMapping(value = "/create/present", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PresentFullDto> create(@RequestHeader("Authorization") @NotBlank String token,
                                       @RequestBody @Valid PresentFullDto dto) {
        jwt.validateToken(token);
        return service.create(dto);
    }

    @DeleteMapping( "/remove/present/{id}")
    public Mono<Void> remove(@RequestHeader("Authorization") @NotBlank String token,
                             @PathVariable("id") @NotNull @Positive Long id) {
        jwt.validateToken(token);
        return service.delete(id).onErrorResume(NotFoundException.class, e -> Mono.empty());
    }
}

