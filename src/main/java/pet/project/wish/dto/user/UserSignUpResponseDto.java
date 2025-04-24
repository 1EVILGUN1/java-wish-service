package pet.project.wish.dto.user;

import lombok.Builder;
import pet.project.wish.dto.Token;

@Builder
public record UserSignUpResponseDto(UserDto user, Token token) {

}
