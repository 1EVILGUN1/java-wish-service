package pet.project.wish.service;

import pet.project.wish.dto.present.PresentFullDto;
import pet.project.wish.dto.present.PresentRequestDto;
import pet.project.wish.dto.present.PresentSmallDto;
import pet.project.wish.model.Present;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PresentService {
    Mono<PresentFullDto> create(PresentRequestDto dto);
    Mono<PresentFullDto> update(PresentFullDto dto);
    Mono<PresentFullDto> getId(Long id);
    Flux<Present> getAll();
    Mono<Void> delete(Long id);
    Flux<PresentSmallDto> getPresentsUser(Flux<Long> ids);
}
