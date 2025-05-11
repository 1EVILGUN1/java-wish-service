package pet.project.wish.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import pet.project.wish.dto.present.PresentFullResponseDto;
import pet.project.wish.dto.present.PresentRequestCreatedDto;
import pet.project.wish.dto.present.PresentRequestUpdatedDto;
import pet.project.wish.dto.present.PresentSmallResponseDto;
import pet.project.wish.model.Present;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PresMapper {

    // Синхронные маппинги
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "url", source = "url")
    PresentSmallResponseDto toPresentSmallResponseDto(Present present);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "links", source = "links")
    @Mapping(target = "url", source = "url")
    PresentFullResponseDto toPresentFullResponseDto(Present present);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "links", source = "links")
    @Mapping(target = "url", source = "url")
    Present toEntity(PresentRequestUpdatedDto dto);

    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "links", source = "links")
    @Mapping(target = "url", source = "url")
    @Mapping(target = "reserved", source = "reserved")
    Present toEntity(PresentRequestCreatedDto dto);

    // Реактивные методы с ручной обработкой
    default Mono<PresentSmallResponseDto> toPresentSmallResponseDtoMono(Mono<Present> presentMono) {
        return presentMono.map(this::toPresentSmallResponseDto);
    }

    default Mono<PresentFullResponseDto> toPresentFullResponseDtoMono(Mono<Present> presentMono) {
        return presentMono.map(this::toPresentFullResponseDto);
    }

    default Flux<PresentSmallResponseDto> toPresentSmallResponseDtos(Flux<Present> presents) {
        return presents.map(this::toPresentSmallResponseDto);
    }

    default Mono<Present> toEntityMono(PresentRequestUpdatedDto dto) {
        return Mono.just(toEntity(dto));
    }

    default Mono<Present> toEntityMono(PresentRequestCreatedDto dto) {
        return Mono.just(toEntity(dto));
    }
}