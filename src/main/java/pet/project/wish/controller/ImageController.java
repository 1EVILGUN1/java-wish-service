package pet.project.wish.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.ImageRequestUploadDto;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private static final String BASE_PATH = "image";
    private static final String USERS_PATH = BASE_PATH + "/users";

    @PostMapping(value = "/{username}/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> uploadProfileImage(
            @PathVariable String username,
            @RequestBody ImageRequestUploadDto dto) {
        return Mono.fromCallable(() -> {
            try {
                log.info("Uploading profile image for user: {}", username);

                // Декодируем Base64
                String base64Image = cleanBase64(dto.image());
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                // Создаем директории, если они не существуют
                Path userDir = Paths.get(USERS_PATH, username);
                Files.createDirectories(userDir);
                log.debug("Created or verified user directory: {}", userDir);

                // Удаляем старое изображение профиля, если оно существует
                String extension = dto.extension() != null ? dto.extension().toLowerCase() : "jpg";
                String newFilename = username + "." + extension;
                Path oldFilePath = userDir.resolve(newFilename);
                if (Files.exists(oldFilePath)) {
                    Files.delete(oldFilePath);
                    log.info("Deleted existing profile image: {}", oldFilePath);
                }

                // Сохраняем новое изображение
                Path filePath = userDir.resolve(newFilename);
                Files.write(filePath, imageBytes);
                log.info("Successfully saved profile image: {}", filePath);

                // Возвращаем URL
                String imageUrl = String.format("/image/users/%s/%s", username, newFilename);
                log.debug("Returning image URL: {}", imageUrl);
                return imageUrl;
            } catch (IOException e) {
                log.error("Failed to save profile image for user: {}", username, e);
                throw new RuntimeException("Failed to save profile image", e);
            } catch (IllegalArgumentException e) {
                log.error("Invalid Base64 image data for user: {}", username, e);
                throw new RuntimeException("Invalid Base64 image data", e);
            }
        });
    }

    @PostMapping(value = "/{username}/presents", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> uploadPresentImage(
            @PathVariable String username,
            @RequestBody ImageRequestUploadDto dto,
            @RequestParam("title") String title) {
        return Mono.fromCallable(() -> {
            try {
                log.info("Uploading present image for user: {}, title: {}", username, title);

                // Декодируем Base64
                String base64Image = cleanBase64(dto.image());
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                // Создаем директории
                Path presentsDir = Paths.get(USERS_PATH, username, "presents");
                Files.createDirectories(presentsDir);
                log.debug("Created or verified presents directory: {}", presentsDir);

                // Очищаем title и формируем имя файла
                String cleanTitle = title.replaceAll("[^a-zA-Z0-9]", "_");
                String extension = dto.extension() != null ? dto.extension().toLowerCase() : "jpg";
                String newFilename = cleanTitle + "_" + UUID.randomUUID() + "." + extension;

                // Сохраняем файл
                Path filePath = presentsDir.resolve(newFilename);
                Files.write(filePath, imageBytes);
                log.info("Successfully saved present image: {}", filePath);

                // Возвращаем URL
                String imageUrl = String.format("/image/users/%s/presents/%s", username, newFilename);
                log.debug("Returning present image URL: {}", imageUrl);
                return imageUrl;
            } catch (IOException e) {
                log.error("Failed to save present image for user: {}, title: {}", username, title, e);
                throw new RuntimeException("Failed to save present image", e);
            } catch (IllegalArgumentException e) {
                log.error("Invalid Base64 image data for user: {}, title: {}", username, title, e);
                throw new RuntimeException("Invalid Base64 image data", e);
            }
        });
    }

    @GetMapping("/{username}/{filename:.+}")
    public Mono<Resource> getImage(
            @PathVariable String username,
            @PathVariable String filename) {
        return Mono.fromCallable(() -> {
            log.info("Retrieving profile image for user: {}, filename: {}", username, filename);
            Path filePath = Paths.get(USERS_PATH, username, filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                log.debug("Successfully retrieved image: {}", filePath);
                return resource;
            }
            log.warn("Profile image not found: {}", filePath);
            throw new RuntimeException("Image not found");
        });
    }

    @GetMapping("/{username}/presents/{filename:.+}")
    public Mono<Resource> getPresentImage(
            @PathVariable String username,
            @PathVariable String filename) {
        return Mono.fromCallable(() -> {
            log.info("Retrieving present image for user: {}, filename: {}", username, filename);
            Path filePath = Paths.get(USERS_PATH, username, "presents", filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                log.debug("Successfully retrieved present image: {}", filePath);
                return resource;
            }
            log.warn("Present image not found: {}", filePath);
            throw new RuntimeException("Present image not found");
        });
    }

    @DeleteMapping("/{username}/presents/{filename:.+}")
    public Mono<Void> deletePresentImage(
            @PathVariable String username,
            @PathVariable String filename) {
        return Mono.fromCallable(() -> {
            try {
                log.info("Deleting present image for user: {}, filename: {}", username, filename);
                Path filePath = Paths.get(USERS_PATH, username, "presents", filename);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("Successfully deleted present image: {}", filePath);
                    return null;
                }
                log.warn("Present image not found for deletion: {}", filePath);
                throw new RuntimeException("Present image not found");
            } catch (IOException e) {
                log.error("Failed to delete present image for user: {}, filename: {}", username, filename, e);
                throw new RuntimeException("Failed to delete present image", e);
            }
        });
    }

    private String cleanBase64(String base64Image) {
        if (base64Image == null) {
            log.error("Base64 image string is null");
            throw new IllegalArgumentException("Base64 image string is null");
        }
        if (base64Image.startsWith("data:image")) {
            log.debug("Cleaning Base64 prefix from image data");
            return base64Image.substring(base64Image.indexOf(",") + 1);
        }
        log.debug("Base64 string already clean");
        return base64Image.trim();
    }
}