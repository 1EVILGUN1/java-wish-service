package pet.project.wish.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import pet.project.wish.dto.ImageRequestUploadDto;
import pet.project.wish.service.ImageService;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    private static final String BASE_PATH = "image";
    private static final String USERS_PATH = BASE_PATH + "/users";

    @Override
    public Mono<String> uploadProfileImage(String username, ImageRequestUploadDto dto) {
        return Mono.fromCallable(() -> {
            try {
                log.info("Загрузка изображения профиля для пользователя: {}", username);

                // Декодируем Base64
                String base64Image = cleanBase64(dto.image());
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                // Создаем директории
                Path userDir = Paths.get(USERS_PATH, username);
                Files.createDirectories(userDir);
                log.debug("Создано или проверено директории пользователя: {}", userDir);

                // Удаляем старое изображение профиля
                String extension = dto.extension() != null ? dto.extension().toLowerCase() : "jpg";
                String newFilename = username + "." + extension;
                Path oldFilePath = userDir.resolve(newFilename);
                if (Files.exists(oldFilePath)) {
                    Files.delete(oldFilePath);
                    log.info("Удалено существующее изображение профиля: {}", oldFilePath);
                }

                // Сохраняем новое изображение
                Path filePath = userDir.resolve(newFilename);
                Files.write(filePath, imageBytes);
                log.info("Успешно сохранено изображение профиля: {}", filePath);

                // Возвращаем URL
                String imageUrl = String.format("/image/users/%s/%s", username, newFilename);
                log.debug("Возвращен URL изображения: {}", imageUrl);
                return imageUrl;
            } catch (IOException e) {
                log.error("Не удалось сохранить изображение профиля для пользователя: {}", username, e);
                throw new RuntimeException("Не удалось сохранить изображение профиля", e);
            } catch (IllegalArgumentException e) {
                log.error("Неверные данные Base64 изображения для пользователя: {}", username, e);
                throw new RuntimeException("Неверные данные Base64 изображения", e);
            }
        });
    }

    @Override
    public Mono<String> uploadPresentImage(String username, ImageRequestUploadDto dto, String title) {
        return Mono.fromCallable(() -> {
            try {
                log.info("Загрузка изображения подарка для пользователя: {}, название: {}", username, title);

                // Декодируем Base64
                String base64Image = cleanBase64(dto.image());
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                // Создаем директории
                Path presentsDir = Paths.get(USERS_PATH, username, "presents");
                Files.createDirectories(presentsDir);
                log.debug("Создано или проверено директории подарков: {}", presentsDir);

                // Очищаем title и формируем имя файла
                String cleanTitle = title.replaceAll("[^a-zA-Z0-9]", "_");
                String extension = dto.extension() != null ? dto.extension().toLowerCase() : "jpg";
                String newFilename = cleanTitle + "_" + UUID.randomUUID() + "." + extension;

                // Сохраняем файл
                Path filePath = presentsDir.resolve(newFilename);
                Files.write(filePath, imageBytes);
                log.info("Успешно сохранено изображение подарка: {}", filePath);

                // Возвращаем URL
                String imageUrl = String.format("/image/users/%s/presents/%s", username, newFilename);
                log.debug("Возвращен URL изображения подарка: {}", imageUrl);
                return imageUrl;
            } catch (IOException e) {
                log.error("Не удалось сохранить изображение подарка для пользователя: {}, название: {}", username, title, e);
                throw new RuntimeException("Не удалось сохранить изображение подарка", e);
            } catch (IllegalArgumentException e) {
                log.error("Неверные данные Base64 изображения для пользователя: {}, название: {}", username, title, e);
                throw new RuntimeException("Неверные данные Base64 изображения", e);
            }
        });
    }

    @Override
    public Mono<Resource> getImage(String username, String filename) {
        return Mono.fromCallable(() -> {
            log.info("Получение изображения профиля для пользователя: {}, имя файла: {}", username, filename);
            Path filePath = Paths.get(USERS_PATH, username, filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                log.debug("Успешно получено изображение: {}", filePath);
                return resource;
            }
            log.warn("Изображение профиля не найдено: {}", filePath);
            throw new RuntimeException("Изображение не найдено");
        });
    }

    public Mono<Resource> getPresentImage(String username, String filename) {
        return Mono.fromCallable(() -> {
            log.info("Получение изображения подарка для пользователя: {}, имя файла: {}", username, filename);
            Path filePath = Paths.get(USERS_PATH, username, "presents", filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                log.debug("Успешно получено изображение подарка: {}", filePath);
                return resource;
            }
            log.warn("Изображение подарка не найдено: {}", filePath);
            throw new RuntimeException("Изображение подарка не найдено");
        });
    }

    @Override
    public Mono<Void> deletePresentImage(String username, String filename) {
        return Mono.fromCallable(() -> {
            try {
                log.info("Удаление изображения подарка для пользователя: {}, имя файла: {}", username, filename);
                Path filePath = Paths.get(USERS_PATH, username, "presents", filename);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("Успешно удалено изображение подарка: {}", filePath);
                    return null;
                }
                log.warn("Изображение подарка не найдено для удаления: {}", filePath);
                throw new RuntimeException("Изображение подарка не найдено");
            } catch (IOException e) {
                log.error("Не удалось удалить изображение подарка для пользователя: {}, имя файла: {}", username, filename, e);
                throw new RuntimeException("Не удалось удалить изображение подарка", e);
            }
        });
    }

    private String cleanBase64(String base64Image) {
        if (base64Image == null) {
            log.error("Строка Base64 изображения равна null");
            throw new IllegalArgumentException("Строка Base64 изображения равна null");
        }
        if (base64Image.startsWith("data:image")) {
            log.debug("Очистка префикса Base64 из данных изображения");
            return base64Image.substring(base64Image.indexOf(",") + 1);
        }
        log.debug("Строка Base64 уже очищена");
        return base64Image.trim();
    }
}