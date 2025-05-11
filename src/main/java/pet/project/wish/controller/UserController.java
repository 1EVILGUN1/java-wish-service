package pet.project.wish.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.*;
import pet.project.wish.dto.user.UserAuthDto;
import pet.project.wish.dto.user.UserResponseDto;
import pet.project.wish.dto.user.UserSignUpResponseDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.service.JwtUtil;
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
        log.info("signInUser: {}", dto);
        return service.login(dto)
                .flatMap(userDto -> Mono.just(UserSignUpResponseDto.builder()
                        .user(userDto)
                        .token(new Token(jwt.generateRefreshToken(userDto.id()), jwt.generateAccessToken(userDto.id())))
                        .build()));
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE) // Используем POST вместо GET
    public Mono<Token> refreshToken(
            @RequestHeader("Authorization") @NotBlank String token) {
        jwt.validateToken(token);
        return Mono.just(new Token(jwt.generateRefreshToken(jwt.getUserIdFromToken(token)),
                jwt.generateAccessToken(jwt.getUserIdFromToken(token))));
    }

    @GetMapping(value = "/friends", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<FriendUserResponseDto> getFriends(@RequestHeader("Authorization") @NotBlank String token) {
        jwt.validateToken(token);
        return service.getId(jwt.getUserIdFromToken(token))
                .flatMapMany(userDto -> {
                    Iterable<Long> ids = userDto.friendsIds() != null ? userDto.friendsIds() : Collections.emptyList();
                    return service.getFriends(Flux.fromIterable(ids))
                            .onErrorResume(NotFoundException.class, e -> Flux.empty());

                });
    }

    @GetMapping(value = "/friend/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserResponseDto> getFriend(@RequestHeader("Authorization") @NotBlank String token,
                                           @PathVariable("id") @NotNull @Positive Long id) {
        jwt.validateToken(token);
        return service.getFriend(jwt.getUserIdFromToken(token), id);
    }

    @GetMapping("/users")
    public Flux<UserResponseDto> getUsers(@RequestHeader("Authorization") @NotBlank String token) {
        jwt.validateToken(token);
        return service.getAll();
    }

    @PutMapping("/friend/{id}")
    public Mono<Void> addFriend(@RequestHeader("Authorization") @NotBlank String token,
                                @PathVariable("id") @NotNull @Positive Long id){
        jwt.validateToken(token);
        if(jwt.getUserIdFromToken(token).equals(id)){
            throw new NotFoundException("Duplicate user id");
        }
        return service.addFriend(jwt.getUserIdFromToken(token), id);
    }
}
