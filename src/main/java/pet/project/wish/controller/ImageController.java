package pet.project.wish.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pet.project.wish.dto.ImageUploadDto;
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

    // Загрузка основного изображения пользователя
    @PostMapping(value = "/{username}/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> uploadProfileImage(
            @PathVariable String username,
            @RequestBody ImageUploadDto dto) {
        return Mono.fromCallable(() -> {
            try {
                // Декодируем Base64
                String base64Image = cleanBase64(dto.image());
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                // Создаем директории, если они не существуют
                Path userDir = Paths.get(USERS_PATH, username);
                Files.createDirectories(userDir);

                // Формируем имя файла
                String extension = dto.extension() != null ? dto.extension().toLowerCase() : "jpg";
                String newFilename = username + "." + extension;

                // Сохраняем файл
                Path filePath = userDir.resolve(newFilename);
                Files.write(filePath, imageBytes);

                // Возвращаем URL
                return String.format("/image/users/%s/%s", username, newFilename);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save profile image", e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid Base64 image data", e);
            }
        });
    }

    // Загрузка изображения подарка
    @PostMapping(value = "/{username}/presents", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> uploadPresentImage(
            @PathVariable String username,
            @RequestBody ImageUploadDto dto,
            @RequestParam("title") String title) {
        return Mono.fromCallable(() -> {
            try {
                // Декодируем Base64
                String base64Image = cleanBase64(dto.image());
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                // Создаем директории
                Path presentsDir = Paths.get(USERS_PATH, username, "presents");
                Files.createDirectories(presentsDir);

                // Очищаем title и формируем имя файла
                String cleanTitle = title.replaceAll("[^a-zA-Z0-9]", "_");
                String extension = dto.extension() != null ? dto.extension().toLowerCase() : "jpg";
                String newFilename = cleanTitle + "_" + UUID.randomUUID() + "." + extension;

                // Сохраняем файл
                Path filePath = presentsDir.resolve(newFilename);
                Files.write(filePath, imageBytes);

                // Возвращаем URL
                return String.format("/image/users/%s/presents/%s", username, newFilename);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save present image", e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid Base64 image data", e);
            }
        });
    }

    // Получение изображения
    @GetMapping("/{username}/{filename:.+}")
    public Mono<Resource> getImage(
            @PathVariable String username,
            @PathVariable String filename) {
        return Mono.fromCallable(() -> {
            Path filePath = Paths.get(USERS_PATH, username, filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("Image not found");
        });
    }

    // Получение изображения подарка
    @GetMapping("/{username}/presents/{filename:.+}")
    public Mono<Resource> getPresentImage(
            @PathVariable String username,
            @PathVariable String filename) {
        return Mono.fromCallable(() -> {
            Path filePath = Paths.get(USERS_PATH, username, "presents", filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("Present image not found");
        });
    }

    // Очистка Base64 (удаление префикса data:image/*;base64,)
    private String cleanBase64(String base64Image) {
        if (base64Image == null) {
            throw new IllegalArgumentException("Base64 image string is null");
        }
        // Удаляем префикс, если он есть (например, data:image/jpeg;base64,)
        if (base64Image.startsWith("data:image")) {
            return base64Image.substring(base64Image.indexOf(",") + 1);
        }
        return base64Image.trim();
    }
}