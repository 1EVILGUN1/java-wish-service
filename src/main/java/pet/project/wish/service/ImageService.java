package pet.project.wish.service;

import org.springframework.core.io.Resource;
import pet.project.wish.dto.ImageRequestUploadDto;
import reactor.core.publisher.Mono;

public interface ImageService {
    Mono<String> uploadProfileImage(String username, ImageRequestUploadDto dto);

    Mono<String> uploadPresentImage(String username, ImageRequestUploadDto dto, String title);

    Mono<Resource> getImage(String username, String filename);

    Mono<Resource> getPresentImage(String username, String filename);

    Mono<Void> deletePresentImage(String username, String filename);

}
