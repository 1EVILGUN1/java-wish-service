package pet.project.wish.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import pet.project.wish.model.User;

import java.time.LocalDate;

/**
 * DTO for {@link User}
 */
@Builder
public record UserCreatedDto(@Size(min = 6) @NotBlank String name, @Size(min = 6) @NotBlank String lastName,
                             @Size(min = 6) @NotBlank String password, @Future LocalDate birthday) {
}