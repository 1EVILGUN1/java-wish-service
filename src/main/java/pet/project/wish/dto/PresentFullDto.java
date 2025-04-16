package pet.project.wish.dto;

import lombok.Builder;

import java.util.List;

/**
 * DTO for {@link pet.project.wish.model.Present}
 */
@Builder
public record PresentFullDto(Long id, String title, String description, List<String> links, String url) {
}