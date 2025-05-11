package pet.project.wish.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PresentServiceImpl implements PresentService {
    private final TransactionalOperator transactionalOperator;
    private final PresentRepository repository;
    private final PresMapper mapper;

    @Override
    public Mono<PresentFullResponseDto> create(PresentRequestCreatedDto dto) {
        log.info("Создание нового подарка: {}", dto.title());
        return Mono.defer(() -> mapper.toEntityMono(dto))
                .doOnNext(p -> log.debug("Преобразование DTO в сущность Present"))
                .flatMap(repository::save)
                .doOnNext(p -> log.debug("Подарок сохранен в БД с ID: {}", p.getId()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Ошибка сохранения подарка в БД");
                    return Mono.error(new NotFoundException("Ошибка сохранения подарка в БД"));
                }))
                .flatMap(present -> mapper.toPresentFullResponseDtoMono(Mono.just(present)))
                .doOnSuccess(p -> log.info("Подарок успешно создан. ID: {}", p.id()))
                .doOnError(e -> log.error("Ошибка при создании подарка", e))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<PresentFullResponseDto> update(PresentRequestUpdatedDto dto) {
        log.info("Обновление подарка с ID: {}", dto.id());
        return Mono.defer(() -> mapper.toEntityMono(dto))
                .doOnNext(p -> log.debug("Преобразование DTO в сущность для обновления"))
                .flatMap(repository::save)
                .doOnNext(p -> log.debug("Подарок обновлен в БД"))
                .flatMap(present -> mapper.toPresentFullResponseDtoMono(Mono.just(present)))
                .doOnSuccess(present -> log.info("Подарок с ID: {} успешно обновлен", present.id()))
                .doOnError(e -> log.error("Ошибка при обновлении подарка", e))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<PresentFullResponseDto> getId(Long id) {
        log.info("Получение подарка по ID: {}", id);
        return repository.findById(id)
                .doOnNext(p -> log.debug("Найден подарок в БД: {}", p))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Подарок с ID: {} не найден", id);
                    return Mono.error(new NotFoundException("Подарок не найден"));
                }))
                .flatMap(present -> mapper.toPresentFullResponseDtoMono(Mono.just(present)))
                .doOnSuccess(p -> log.debug("Успешно получен подарок с ID: {}", p.id()))
                .doOnError(e -> log.error("Ошибка при получении подарка", e));
    }

    @Override
    public Flux<Present> getAll() {
        log.warn("Метод getAll() не реализован");
        return Flux.empty();
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Удаление подарка с ID: {}", id);
        return repository.deleteById(id)
                .doOnSuccess(v -> log.info("Подарок с ID: {} успешно удален", id))
                .doOnError(e -> log.error("Ошибка при удалении подарка с ID: {}", id, e))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<PresentSmallResponseDto> getPresentsUser(Flux<Long> ids) {
        log.info("Получение списка подарков пользователя");
        return ids.collectList()
                .doOnNext(idList -> log.debug("Получено {} ID подарков", idList.size()))
                .flatMapMany(repository::findByIdsCustom)
                .doOnNext(p -> log.debug("Обработка подарка с ID: {}", p.getId()))
                .switchIfEmpty(Flux.defer(() -> {
                    log.warn("Подарки не найдены");
                    return Flux.error(new NotFoundException("Подарки не найдены"));
                }))
                .transform(mapper::toPresentSmallResponseDtos)
                .doOnComplete(() -> log.debug("Завершена обработка списка подарков"))
                .doOnError(e -> log.error("Ошибка при получении списка подарков", e));
    }
}