package pet.project.wish.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.PresentFullDto;
import pet.project.wish.service.JwtUtil;
import pet.project.wish.service.PresentService;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PresentCRUDController {
    private final PresentService service;
    private final JwtUtil jwt;

    @CrossOrigin
    @PutMapping("/upd/present")
    public Mono<PresentFullDto> update(@RequestHeader("Authorization") String token,
                                       @RequestBody PresentFullDto dto) {
        return service.update(dto);
    }

    @CrossOrigin
    @PostMapping("/create/present")
    public Mono<PresentFullDto> create(@RequestHeader("Authorization") String token,
                                       @RequestBody PresentFullDto dto) {
        return service.create(dto);
    }

    @CrossOrigin
    @DeleteMapping("/remove/present/{id}")
    public Mono<Void> remove(@RequestHeader("Authorization") String token,
                             @PathVariable("id") Long id) {
        if(jwt.validateToken(token)){
            return service.delete(id);
        }
        return Mono.empty();
    }

}

