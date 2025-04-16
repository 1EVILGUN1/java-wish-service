package pet.project.wish.mapper;



import org.springframework.stereotype.Component;
import pet.project.wish.dto.PresentFullDto;
import pet.project.wish.dto.PresentSmallDto;
import pet.project.wish.model.Present;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PresentMapper {

    public Mono<PresentSmallDto> mapToPresent(Present present) {
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
}
