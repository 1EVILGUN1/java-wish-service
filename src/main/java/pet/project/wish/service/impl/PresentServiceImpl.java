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
import pet.project.wish.mapper.PresentMapper;
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
    private final PresentMapper mapper;

    @Override
    public Mono<PresentFullResponseDto> create(PresentRequestCreatedDto dto) {
        log.info("Создание нового подарка с данными: {}", dto);
        return Mono.defer(() -> {
                    log.debug("Преобразование DTO в сущность подарка");
                    return mapper.mapToPresentRequestDto(dto);
                })
                .flatMap(present -> {
                    log.debug("Сохранение подарка в базе данных: {}", present);
                    return repository.save(present);
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Ошибка записи подарка в базу данных")))
                .flatMap(present -> {
                    log.debug("Преобразование сохраненного подарка в DTO: {}", present);
                    return mapper.mapToPresentFull(present);
                })
                .as(transactionalOperator::transactional)
                .doOnSuccess(result -> log.info("Успешно создан подарок: {}", result))
                .doOnError(error -> log.error("Ошибка при создании подарка: {}", error.getMessage(), error));
    }

    @Override
    public Mono<PresentFullResponseDto> update(PresentRequestUpdatedDto dto) {
        log.info("Обновление подарка с данными: {}", dto);
        return Mono.defer(() -> {
                    log.debug("Преобразование DTO в сущность подарка для обновления");
                    return mapper.mapToPresentMono(dto);
                })
                .flatMap(present -> {
                    log.debug("Сохранение обновленного подарка в базе данных: {}", present);
                    return repository.save(present);
                })
                .flatMap(present -> {
                    log.debug("Преобразование обновленного подарка в DTO: {}", present);
                    return mapper.mapToPresentFull(present);
                })
                .as(transactionalOperator::transactional)
                .doOnSuccess(result -> log.info("Успешно обновлен подарок: {}", result))
                .doOnError(error -> log.error("Ошибка при обновлении подарка: {}", error.getMessage(), error));
    }

    @Override
    public Mono<PresentFullResponseDto> getId(Long id) {
        log.info("Получение подарка по идентификатору: {}", id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Подарок не найден")))
                .flatMap(present -> {
                    log.debug("Преобразование найденного подарка в DTO: {}", present);
                    return mapper.mapToPresentFull(present);
                })
                .doOnSuccess(result -> log.info("Успешно получен подарок: {}", result))
                .doOnError(error -> log.error("Ошибка при получении подарка: {}", error.getMessage(), error));
    }

    @Override
    public Flux<Present> getAll() {
        log.info("Получение всех подарков");
        return repository.findAll()
                .doOnComplete(() -> log.info("Успешно получены все подарки"))
                .doOnError(error -> log.error("Ошибка при получении всех подарков: {}", error.getMessage(), error));
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Удаление подарка по идентификатору: {}", id);
        return repository.deleteById(id)
                .as(transactionalOperator::transactional)
                .doOnSuccess(v -> log.info("Успешно удален подарок с идентификатором: {}", id))
                .doOnError(error -> log.error("Ошибка при удалении подарка: {}", error.getMessage(), error));
    }

    @Override
    public Flux<PresentSmallResponseDto> getPresentsUser(Flux<Long> ids) {
        log.info("Получение подарков пользователя по идентификаторам: {}", ids);
        return ids.collectList()
                .flatMapMany(idList -> {
                    log.debug("Поиск подарков по идентификаторам: {}", idList);
                    return repository.findByIdsCustom(idList);
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Подарки не найдены")))
                .transform(presents -> {
                    log.debug("Преобразование подарков в DTO: {}", presents);
                    return mapper.mapToPresentSmall(presents);
                })
                .doOnComplete(() -> log.info("Успешно получены подарки пользователя"))
                .doOnError(error -> log.error("Ошибка при получении подарков пользователя: {}", error.getMessage(), error));
    }
}