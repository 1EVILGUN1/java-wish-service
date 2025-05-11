package pet.project.wish.dto.present;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record PresentRequestCreatedDto(@NotBlank(message = "empty title")
                                @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s\\-!?.]*$", message = "contain invalid characters")
                                String title,
                                       @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s\\-!?.]*$", message = "contain invalid characters")
                                String description,
                                       List<String> links,
                                       boolean reserved,
                                       String url) {
}
