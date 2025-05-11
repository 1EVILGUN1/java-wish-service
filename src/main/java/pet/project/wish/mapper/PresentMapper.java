package pet.project.wish.mapper;


import org.springframework.stereotype.Component;
import pet.project.wish.dto.present.PresentFullResponseDto;
import pet.project.wish.dto.present.PresentRequestCreatedDto;
import pet.project.wish.dto.present.PresentRequestUpdatedDto;
import pet.project.wish.dto.present.PresentSmallResponseDto;
import pet.project.wish.model.Present;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PresentMapper {

    public Mono<PresentSmallResponseDto> mapToPresentSmallDtoMono(Present present) {
        return Mono.just(PresentSmallResponseDto.builder()
                .id(present.getId())
                .title(present.getTitle())
                .url(present.getUrl()).build());
    }

    public Mono<PresentFullResponseDto> mapToPresentFull(Present present) {
        return Mono.just(PresentFullResponseDto.builder()
                .id(present.getId())
                .title(present.getTitle())
                .description(present.getDescription())
                .links(present.getLinks())
                .url(present.getUrl())
                .build()
        );
    }

    public Flux<PresentSmallResponseDto> mapToPresentSmall(Flux<Present> presents) {
        return presents.map(present -> PresentSmallResponseDto.builder()
                .id(present.getId())
                .title(present.getTitle())
                .url(present.getUrl())
                .build()
        );
    }

    public Mono<Present> mapToPresentMono(PresentRequestUpdatedDto dto) {
        Present present = new Present();
        present.setId(dto.id());
        present.setTitle(dto.title());
        present.setUrl(dto.url());
        present.setDescription(dto.description());
        present.setLinks(dto.links());
        return Mono.just(present);
    }

    public Mono<Present> mapToPresentRequestDto(PresentRequestCreatedDto dto) {
        Present present = new Present();
        present.setTitle(dto.title());
        present.setDescription(dto.description());
        present.setLinks(dto.links());
        present.setUrl(dto.url());
        present.setReserved(dto.reserved());
        return Mono.just(present);
    }
}
