package pet.project.wish.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record UserAuthDto(@NotBlank
                          @Pattern(regexp = "^[a-zA-Z0-9\\s\\-!?.]*$", message = "contain invalid characters")
                          String name,
                          @NotBlank
                          @Pattern(regexp = "^[a-zA-Z0-9\\s\\-!?.]*$", message = "contain invalid characters")
                          String password) {
}
