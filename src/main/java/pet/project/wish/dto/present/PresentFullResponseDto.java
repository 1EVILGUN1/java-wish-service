package pet.project.wish.dto.present;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

/**
 * DTO for {@link pet.project.wish.model.Present}
 */
@Builder
public record PresentFullResponseDto(
                             Long id,
                             String title,
                             String description,
                             List<String> links,
                             String url) {
}