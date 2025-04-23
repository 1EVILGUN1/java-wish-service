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
import pet.project.wish.dto.FriendUserDto;
import pet.project.wish.dto.Token;
import pet.project.wish.dto.UserAuthDto;
import pet.project.wish.dto.UserDto;
import pet.project.wish.error.InvalidTokenException;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.service.JwtUtil;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final JwtUtil jwt;


    @PostMapping(value = "/sing-in", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserDto> signInUser(@RequestBody @Valid UserAuthDto dto) {
        return service.login(dto);
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE) // Используем POST вместо GET
    public Mono<Token> refreshToken(
            @RequestHeader("Authorization") @NotBlank String token) {
        jwt.validateToken(token);
        return Mono.just(new Token(jwt.generateRefreshToken(jwt.getUserIdFromToken(token)),
                jwt.generateAccessToken(jwt.getUserIdFromToken(token))));
    }

    @GetMapping(value = "/friends", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<FriendUserDto> getFriends(@RequestHeader("Authorization") @NotBlank String token) {
        jwt.validateToken(token);
        return service.getId(jwt.getUserIdFromToken(token))
                .flatMapMany(userDto -> service.getFriends(Flux.fromIterable(userDto.friendsIds())));
    }

    @GetMapping(value = "/friend/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserDto> getFriend(@RequestHeader("Authorization") @NotBlank String token,
                                   @PathVariable("id") @NotNull @Positive Long id) {
        jwt.validateToken(token);
        return service.getFriend(jwt.getUserIdFromToken(token), id);
    }
}
