package pet.project.wish.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;
import pet.project.wish.model.User;

import java.time.LocalDate;

/**
 * DTO for {@link User}
 */
@Builder
public record UserCreatedDto(@Size(min = 6)
                             @NotBlank
                             @Pattern(regexp = "^[a-zA-Z0-9\\s\\-!?.]*$", message = "contain invalid characters")
                             String name,
                             @Size(min = 6)
                             @NotBlank
                             @Pattern(regexp = "^[a-zA-Z0-9\\s\\-!?.]*$", message = "contain invalid characters")
                             String lastName,
                             @Size(min = 6)
                             @NotBlank
                             @Pattern(regexp = "^[a-zA-Z0-9\\s\\-!?.]*$", message = "contain invalid characters")
                             String password,
                             @Future
                             @DateTimeFormat(pattern = "dd.MM.yyyy")
                             LocalDate birthday) {
}