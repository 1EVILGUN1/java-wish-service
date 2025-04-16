package pet.project.wish.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserAuthDto(@NotBlank String name, @NotBlank String password) {
}
