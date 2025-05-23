package pet.project.wish.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pet.project.wish.dto.PresentFullDto;
import pet.project.wish.dto.PresentSmallDto;
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
    public Mono<Present> create(Present present) {
        return null;
    }

    @Override
    public Mono<Present> update(Present present) {
        return null;
    }

    @Override
    public Mono<PresentFullDto> getId(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Present not found")))
                .flatMap(mapper::mapToPresentFull);
    }

    @Override
    public Flux<Present> getAll() {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Flux<PresentSmallDto> getPresentsUser(Flux<Long> ids) {
        return repository.findByIdsCustom(ids)
                .switchIfEmpty(Flux.error(new NotFoundException("Present not found")))
                .transform(mapper::mapToPresentSmall);
    }
}
