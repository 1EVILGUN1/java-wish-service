package pet.project.wish.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.UserCreatedDto;
import pet.project.wish.dto.UserDto;
import pet.project.wish.service.JwtUtil;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserCRUDController {
    private final UserService service;
    private final JwtUtil jwt;

    @CrossOrigin
    @PutMapping("/upd/user")
    public Mono<UserDto> update(@RequestHeader("Authorization") String token,
                                @RequestBody UserCreatedDto dto) {
        return service.update(dto,jwt.getUserIdFromToken(token));
    }

    @CrossOrigin
    @DeleteMapping("/remove")
    public Mono<Void> delete(@RequestHeader("Authorization") String token) {
        if(jwt.validateToken(token)){
            return service.delete(jwt.getUserIdFromToken(token));
        }
        return Mono.empty();
    }

}
