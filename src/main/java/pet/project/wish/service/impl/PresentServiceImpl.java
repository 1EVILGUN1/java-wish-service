package pet.project.wish.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pet.project.wish.dto.present.PresentFullResponseDto;
import pet.project.wish.dto.present.PresentRequestCreatedDto;
import pet.project.wish.dto.present.PresentRequestUpdatedDto;
import pet.project.wish.dto.present.PresentSmallResponseDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.mapper.PresentMapper;
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
    private final PresentMapper mapper;

    @Override
    public Mono<PresentFullResponseDto> create(PresentRequestCreatedDto dto) {
        return Mono.defer(()->mapper.mapToPresentRequestDto(dto))
                .flatMap(repository::save)
                .switchIfEmpty(Mono.error(new NotFoundException("Present error write database")))
                .flatMap(mapper::mapToPresentFull)
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<PresentFullResponseDto> update(PresentRequestUpdatedDto dto) {
        return Mono.defer(() -> mapper.mapToPresentMono(dto))
                .flatMap(repository::save)
                .flatMap(mapper::mapToPresentFull)
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<PresentFullResponseDto> getId(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Present not found")))
                .flatMap(mapper::mapToPresentFull);
    }

    @Override
    public Flux<Present> getAll() {
        return null;
    }

    @Override
    public Mono<Void> delete(Long id) {
        return repository.deleteById(id)
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<PresentSmallResponseDto> getPresentsUser(Flux<Long> ids) {
        return repository.findByIdsCustom(ids)
                .switchIfEmpty(Flux.error(new NotFoundException("Present not found")))
                .transform(mapper::mapToPresentSmall);
    }
}
