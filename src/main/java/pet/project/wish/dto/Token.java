package pet.project.wish.dto;

import lombok.Builder;

@Builder
public record Token(String refresh, String access) {
}
