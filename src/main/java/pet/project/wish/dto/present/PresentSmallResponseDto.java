package pet.project.wish.dto.present;

import lombok.Builder;
import pet.project.wish.model.Present;

/**
 * DTO for {@link Present}
 */
@Builder
public record PresentSmallResponseDto(Long id, String title, String url) {
}