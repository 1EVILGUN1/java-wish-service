package pet.project.wish.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.PresentFullDto;
import pet.project.wish.dto.PresentSmallDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.service.JwtUtil;
import pet.project.wish.service.PresentService;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PresentController {
    private final PresentService service;
    private final UserService userService;
    private final JwtUtil jwt;

    @CrossOrigin
    @GetMapping("/present")
    public Flux<PresentSmallDto> getPresents(@RequestHeader("Authorization") String token){
        if (jwt.validateToken(token)) {
            return userService.getId(jwt.getUserIdFromToken(token))
                    .flatMapMany(present ->
                            service.getPresentsUser(Flux.fromIterable(present.presentIds()))
                                    .onErrorResume(NotFoundException.class, e -> Flux.empty()));
        }
        return Flux.empty();
    }

    @CrossOrigin
    @GetMapping("/present/{id}")
    public Mono<PresentFullDto> getPresent(@RequestHeader("Authorization") String token,
                                           @PathVariable("id") Long id) {
        if (jwt.validateToken(token)) {
            return service.getId(id);
        }
        return Mono.empty();
    }
}
