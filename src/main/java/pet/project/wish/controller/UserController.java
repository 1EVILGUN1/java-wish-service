package pet.project.wish.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.*;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.service.JwtUtil;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final JwtUtil jwt;

    @CrossOrigin
    @PostMapping("/sing-in")
    public Mono<UserDto> signInUser(@RequestBody @Valid UserAuthDto dto) {
        return service.login(dto);
    }

    @CrossOrigin
    @GetMapping("/refresh")
    public Token refreshToken(@RequestHeader("Authorization") String token) {
        return new Token(jwt.generateAccessToken(jwt.getUserIdFromToken(token)),
                jwt.generateAccessToken(jwt.getUserIdFromToken(token)));
    }

    @CrossOrigin
    @PostMapping("/sign-up")
    public Mono<UserDto> signUpUser(@RequestBody @Valid UserCreatedDto dto) {
        return service.create(dto);
    }

    @CrossOrigin
    @GetMapping("/friends")
    public Flux<FriendUserDto> getFriends(@RequestHeader("Authorization") String token) {
        if (jwt.validateToken(token)) {
            return service.getId(jwt.getUserIdFromToken(token))
                    .flatMapMany(user ->
                            service.getFriends(Flux.fromIterable(user.friendsIds()))
                                    .onErrorResume(NotFoundException.class, e -> Flux.empty()));
        }
        return Flux.empty();
    }

    @CrossOrigin
    @GetMapping("/friend/{id}")
    public Mono<UserDto> getFriend(@RequestHeader("Authorization") String token,
                                   @PathVariable("id") Long id) {
        if (jwt.validateToken(token)) {
            return service.getId(id);
        }
        return Mono.empty();
    }


}
