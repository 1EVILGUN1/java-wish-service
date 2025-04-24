package pet.project.wish.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.Token;
import pet.project.wish.dto.user.UserCreatedDto;
import pet.project.wish.dto.user.UserDto;
import pet.project.wish.dto.user.UserSignUpResponseDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.service.JwtUtil;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class UserCRUDController {
    private final UserService service;
    private final JwtUtil jwt;

    @PostMapping(value = "/sign-up", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserSignUpResponseDto> signUpUser(@RequestBody @Valid UserCreatedDto dto) {
        log.info("signUpUser: {}", dto);
        return service.create(dto)
                .flatMap(userDto -> Mono.just(UserSignUpResponseDto.builder()
                        .user(userDto)
                        .token(new Token(jwt.generateRefreshToken(userDto.id()), jwt.generateAccessToken(userDto.id())))
                        .build()));
    }

    @PutMapping(value = "/upd/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserDto> update(@RequestHeader("Authorization") @NotBlank String token,
                                @RequestBody @Valid UserCreatedDto dto) {
        jwt.validateToken(token);
        return service.update(dto,jwt.getUserIdFromToken(token));
    }

    @DeleteMapping("/remove")
    public Mono<Void> delete(@RequestHeader("Authorization") @NotBlank String token) {
        jwt.validateToken(token);
        return service.delete(jwt.getUserIdFromToken(token))
                .onErrorResume(NotFoundException.class, ex -> Mono.empty());
    }
}
