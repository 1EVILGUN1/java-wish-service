package pet.project.wish.dto.user;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for {@link pet.project.wish.model.User}
 */
@Builder
public record UserDto(Long id, String name, String lastName, LocalDate birthday, List<Long> friendsIds,
                      List<Long> presentIds, String url) {
}