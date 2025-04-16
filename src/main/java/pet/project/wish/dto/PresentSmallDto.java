package pet.project.wish.dto;

import lombok.Builder;
import pet.project.wish.model.Present;

/**
 * DTO for {@link Present}
 */
@Builder
public record PresentSmallDto(Long id, String title, String url) {
}