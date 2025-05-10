package pet.project.wish.dto.present;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

@Builder
public record PresentRequestUpdatedDto(@Positive(message = "not positive id")
                                     @NotNull(message = "null id")
                                     Long id,
                                       @NotBlank(message = "empty title")
                                     @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s\\-!?.]*$", message = "contain invalid characters")
                                     String title,
                                       @NotBlank(message = "empty description")
                                     @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s\\-!?.]*$", message = "contain invalid characters")
                                     String description,
                                       @NotEmpty(message = "empty array links")
                                     @Valid
                                     List<String> links,
                                       @NotBlank(message = "empty url image")
                                     String url) {
}
