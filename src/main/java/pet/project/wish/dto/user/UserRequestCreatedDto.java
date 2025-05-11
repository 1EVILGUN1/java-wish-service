package pet.project.wish.dto.user;

import jakarta.validation.constraints.*;
import lombok.Builder;
import pet.project.wish.model.User;

import java.time.LocalDate;

/**
 * DTO for {@link User}
 */
@Builder
public record UserRequestCreatedDto(
                             @NotBlank(message = "empty name")
                             @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s\\-!?.]*$", message = "contain invalid characters")
                             String name,
                             @NotBlank(message = "empty lastName")
                             @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s\\-!?.]*$", message = "contain invalid characters")
                             String lastName,
                             @NotBlank(message = "empty password")
                             @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s\\-!?.]*$", message = "contain invalid characters")
                             String password,
                             @Past(message = "birthday future or present")
                             LocalDate birthday) {
}