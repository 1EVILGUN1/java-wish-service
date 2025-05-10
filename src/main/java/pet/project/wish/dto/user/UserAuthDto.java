package pet.project.wish.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record UserAuthDto(@NotBlank(message = "empty name")
                          @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s\\-!?.]*$", message = "contain invalid characters")
                          String name,
                          @NotBlank(message = "empty password")
                          @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s\\-!?.]*$", message = "contain invalid characters")
                          String password) {
}
