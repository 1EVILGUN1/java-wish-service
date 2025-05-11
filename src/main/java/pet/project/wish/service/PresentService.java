package pet.project.wish.service;

import pet.project.wish.dto.present.PresentFullResponseDto;
import pet.project.wish.dto.present.PresentRequestCreatedDto;
import pet.project.wish.dto.present.PresentRequestUpdatedDto;
import pet.project.wish.dto.present.PresentSmallResponseDto;
import pet.project.wish.model.Present;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PresentService {
    Mono<PresentFullResponseDto> create(PresentRequestCreatedDto dto);

    Mono<PresentFullResponseDto> update(PresentRequestUpdatedDto dto);

    Mono<PresentFullResponseDto> getId(Long id);

    Flux<Present> getAll();

    Mono<Void> delete(Long id);

    Flux<PresentSmallResponseDto> getPresentsUser(Flux<Long> ids);
}
