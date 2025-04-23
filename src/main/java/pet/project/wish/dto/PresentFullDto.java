package pet.project.wish.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

import java.util.List;

/**
 * DTO for {@link pet.project.wish.model.Present}
 */
@Builder
public record PresentFullDto(@Positive
                             @NotNull
                             Long id,
                             @NotBlank
                             @Pattern(regexp = "^[a-zA-Z0-9\\s\\-!?.]*$", message = "contain invalid characters")
                             String title,
                             @NotBlank
                             @Pattern(regexp = "^[a-zA-Z0-9\\s\\-!?.]*$", message = "contain invalid characters")
                             String description,
                             @NotEmpty
                             @Valid
                             List<String> links,
                             @NotBlank
                             @Size(max = 2048, message = "URL must not exceed 2048 characters")
                             @URL(protocol = "https", message = "URL must be a valid HTTPS URL")
                             String url) {
}