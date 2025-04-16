package pet.project.wish.service;

import pet.project.wish.dto.PresentFullDto;
import pet.project.wish.dto.PresentSmallDto;
import pet.project.wish.model.Present;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PresentService {
    Mono<Present> create(Present present);
    Mono<Present> update(Present present);
    Mono<PresentFullDto> getId(Long id);
    Flux<Present> getAll();
    void delete(Long id);
    Flux<PresentSmallDto> getPresentsUser(Flux<Long> ids);
}
