package pet.project.wish.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;
import pet.project.wish.model.User;

import java.time.LocalDate;

/**
 * DTO for {@link User}
 */
@Builder
public record UserCreatedDto(
                             @NotBlank
                             @Pattern(regexp = "^[a-zA-Z0-9\\s\\-!?.]*$", message = "contain invalid characters")
                             String name,
                             @NotBlank
                             @Pattern(regexp = "^[a-zA-Z0-9\\s\\-!?.]*$", message = "contain invalid characters")
                             String lastName,
                             @NotBlank
                             @Pattern(regexp = "^[a-zA-Z0-9\\s\\-!?.]*$", message = "contain invalid characters")
                             String password,
                             @Future
                             LocalDate birthday) {
}