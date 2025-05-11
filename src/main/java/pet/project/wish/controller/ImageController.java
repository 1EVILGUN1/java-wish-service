package pet.project.wish.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.ImageRequestUploadDto;
import pet.project.wish.service.ImageService;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping(value = "/{username}/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> uploadProfileImage(
            @PathVariable String username,
            @RequestBody ImageRequestUploadDto dto) {
        return imageService.uploadProfileImage(username, dto);
    }

    @PostMapping(value = "/{username}/presents", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> uploadPresentImage(
            @PathVariable String username,
            @RequestBody ImageRequestUploadDto dto,
            @RequestParam("title") String title) {
        return imageService.uploadPresentImage(username, dto, title);
    }

    @GetMapping("/{username}/{filename:.+}")
    public Mono<Resource> getImage(
            @PathVariable String username,
            @PathVariable String filename) {
        return imageService.getImage(username, filename);
    }

    @GetMapping("/{username}/presents/{filename:.+}")
    public Mono<Resource> getPresentImage(
            @PathVariable String username,
            @PathVariable String filename) {
        return imageService.getPresentImage(username, filename);
    }

    @DeleteMapping("/{username}/presents/{filename:.+}")
    public Mono<Void> deletePresentImage(
            @PathVariable String username,
            @PathVariable String filename) {
        return imageService.deletePresentImage(username, filename);
    }
}