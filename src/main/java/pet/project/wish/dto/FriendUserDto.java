package pet.project.wish.dto;

import lombok.Builder;
import pet.project.wish.model.User;

/**
 * DTO for {@link User}
 */
@Builder
public record FriendUserDto(String name, String lastName, String url) {
}