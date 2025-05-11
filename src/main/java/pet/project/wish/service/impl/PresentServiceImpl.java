package pet.project.wish.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pet.project.wish.dto.present.PresentFullResponseDto;
import pet.project.wish.dto.present.PresentRequestCreatedDto;
import pet.project.wish.dto.present.PresentRequestUpdatedDto;
import pet.project.wish.dto.present.PresentSmallResponseDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.mapper.PresMapper;
import pet.project.wish.model.Present;
import pet.project.wish.repository.PresentRepository;
import pet.project.wish.service.PresentService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PresentServiceImpl implements PresentService {
    private final TransactionalOperator transactionalOperator;
    private final PresentRepository repository;
    private final PresMapper mapper;

    @Override
    public Mono<PresentFullResponseDto> create(PresentRequestCreatedDto dto) {
        return Mono.defer(() -> mapper.toEntityMono(dto))
                .flatMap(repository::save)
                .switchIfEmpty(Mono.error(new NotFoundException("Present error write database")))
                .flatMap(present -> mapper.toPresentFullResponseDtoMono(Mono.just(present)))
                .flatMap(Mono::just) // Разворачиваем Mono<Mono<PresentFullResponseDto>> в Mono<PresentFullResponseDto>
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<PresentFullResponseDto> update(PresentRequestUpdatedDto dto) {
        return Mono.defer(() -> mapper.toEntityMono(dto))
                .flatMap(repository::save)
                .flatMap(present -> mapper.toPresentFullResponseDtoMono(Mono.just(present)))
                .flatMap(Mono::just) // Разворачиваем Mono<Mono<PresentFullResponseDto>> в Mono<PresentFullResponseDto>
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<PresentFullResponseDto> getId(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Present not found")))
                .flatMap(present -> mapper.toPresentFullResponseDtoMono(Mono.just(present)))
                .flatMap(Mono::just); // Разворачиваем Mono<Mono<PresentFullResponseDto>> в Mono<PresentFullResponseDto>
    }

    @Override
    public Flux<Present> getAll() {
        return null; // Реализуйте или удалите, если не требуется
    }

    @Override
    public Mono<Void> delete(Long id) {
        return repository.deleteById(id)
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<PresentSmallResponseDto> getPresentsUser(Flux<Long> ids) {
        return ids.collectList()
                .flatMapMany(repository::findByIdsCustom)
                .switchIfEmpty(Flux.error(new NotFoundException("Present not found")))
                .transform(mapper::toPresentSmallResponseDtos);
    }
}