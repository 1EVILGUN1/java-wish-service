package pet.project.wish.mapper;



import org.springframework.stereotype.Component;
import pet.project.wish.dto.present.PresentFullDto;
import pet.project.wish.dto.present.PresentRequestDto;
import pet.project.wish.dto.present.PresentSmallDto;
import pet.project.wish.model.Present;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PresentMapper {

    public Mono<PresentSmallDto> mapToPresentSmallDtoMono(Present present) {
        return Mono.just(PresentSmallDto.builder()
                .id(present.getId())
                .title(present.getTitle())
                .url(present.getUrl()).build());
    }

    public Mono<PresentFullDto> mapToPresentFull(Present present) {
        return Mono.just(PresentFullDto.builder()
                .id(present.getId())
                .title(present.getTitle())
                .description(present.getDescription())
                .links(present.getLinks())
                .url(present.getUrl())
                .build()
        );
    }

    public Flux<PresentSmallDto> mapToPresentSmall(Flux<Present> presents) {
        return presents.map(present -> PresentSmallDto.builder()
                .id(present.getId())
                .title(present.getTitle())
                .url(present.getUrl())
                .build()
        );
    }

    public Mono<Present> mapToPresentMono(PresentFullDto dto) {
        Present present = new Present();
        present.setId(dto.id());
        present.setTitle(dto.title());
        present.setUrl(dto.url());
        present.setDescription(dto.description());
        present.setLinks(dto.links());
        return Mono.just(present);
    }

    public Mono<Present> mapToPresentRequestDto(PresentRequestDto dto) {
        Present present = new Present();
        present.setTitle(dto.title());
        present.setDescription(dto.description());
        present.setLinks(dto.links());
        present.setUrl(dto.url());
        present.setReserved(dto.reserved());
        return Mono.just(present);
    }
}
