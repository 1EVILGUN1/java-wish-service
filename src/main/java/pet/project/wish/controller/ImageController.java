package pet.project.wish.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private static final String BASE_PATH = "image";
    private static final String USERS_PATH = BASE_PATH + "/users";

    // Загрузка основного изображения пользователя
    @PostMapping(value = "/{username}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> uploadProfileImage(
            @PathVariable String username,
            @RequestPart("file") Mono<FilePart> filePartMono) {
        return filePartMono.flatMap(filePart -> {
            try {
                // Создаем директории, если они не существуют
                Path userDir = Paths.get(USERS_PATH, username);
                Files.createDirectories(userDir);

                // Формируем имя файла
                String originalFilename = filePart.filename();
                String extension = getFileExtension(originalFilename);
                String newFilename = username + "." + extension;

                // Сохраняем файл
                Path filePath = userDir.resolve(newFilename);
                filePart.transferTo(filePath).block();

                // Возвращаем URL
                return Mono.just(String.format("/image/users/%s/%s", username, newFilename));
            } catch (IOException e) {
                return Mono.error(new RuntimeException("Failed to save profile image", e));
            }
        });
    }

    // Загрузка изображения подарка
    @PostMapping(value = "/{username}/presents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> uploadPresentImage(
            @PathVariable String username,
            @RequestPart("file") Mono<FilePart> filePartMono,
            @RequestParam("title") String title) {
        return filePartMono.flatMap(filePart -> {
            try {
                // Создаем директории
                Path presentsDir = Paths.get(USERS_PATH, username, "presents");
                Files.createDirectories(presentsDir);

                // Очищаем title и формируем имя файла
                String cleanTitle = title.replaceAll("[^a-zA-Z0-9]", "_");
                String extension = getFileExtension(filePart.filename());
                String newFilename = cleanTitle + "_" + UUID.randomUUID() + "." + extension;

                // Сохраняем файл
                Path filePath = presentsDir.resolve(newFilename);
                filePart.transferTo(filePath).block();

                // Возвращаем URL
                return Mono.just(String.format("/image/users/%s/presents/%s", username, newFilename));
            } catch (IOException e) {
                return Mono.error(new RuntimeException("Failed to save present image", e));
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

    // Вспомогательный метод для получения расширения файла
    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
