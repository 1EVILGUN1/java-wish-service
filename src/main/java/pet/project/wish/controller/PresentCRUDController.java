package pet.project.wish.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pet.project.wish.dto.present.PresentFullResponseDto;
import pet.project.wish.dto.present.PresentRequestCreatedDto;
import pet.project.wish.dto.present.PresentRequestUpdatedDto;
import pet.project.wish.error.NotFoundException;
import pet.project.wish.service.JwtUtil;
import pet.project.wish.service.PresentService;
import pet.project.wish.service.UserService;
import reactor.core.publisher.Mono;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/presents")
@RequiredArgsConstructor
@Tag(name = "Present CRUD", description = "API for managing presents")
public class PresentCRUDController {
    private final PresentService service;
    private final UserService userService;
    private final JwtUtil jwt;

    @Operation(summary = "Update a present", description = "Updates an existing present for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Present updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Present not found")
    })
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PresentFullResponseDto> update(
            @RequestHeader("Authorization") @NotBlank String token,
            @PathVariable("id") @NotNull @Positive Long id,
            @RequestBody @Valid PresentRequestUpdatedDto dto) {
        log.info("Updating present with ID: {} for user with token: {}", id, maskToken(token));
        return validateAndGetUserId(token)
                .flatMap(userId -> {
                    if (!dto.id().equals(id)) {
                        log.warn("Mismatch between path ID: {} and DTO ID: {}", id, dto.id());
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path and DTO must match"));
                    }
                    return service.getId(id)
                            .switchIfEmpty(Mono.error(new NotFoundException("Present with ID: " + id + " not found")))
                            .flatMap(present -> service.update(dto));
                })
                .doOnSuccess(present -> log.info("Successfully updated present with ID: {}", id))
                .doOnError(NotFoundException.class, e -> log.warn("Present with ID: {} not found", id, e))
                .doOnError(e -> log.error("Error updating present with ID: {}", id, e))
                .onErrorMap(NotFoundException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @Operation(summary = "Create a present", description = "Creates a new present and associates it with the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Present created successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PresentFullResponseDto> create(
            @RequestHeader("Authorization") @NotBlank String token,
            @RequestBody @Valid PresentRequestCreatedDto dto) {
        log.info("Creating new present for user with token: {}", maskToken(token));
        return validateAndGetUserId(token)
                .flatMap(userId -> service.create(dto)
                        .flatMap(presentFullDto -> userService.addPresent(userId, presentFullDto.id())
                                .thenReturn(presentFullDto)))
                .doOnSuccess(presentFullDto -> log.info("Successfully created present with ID: {}", presentFullDto.id()))
                .doOnError(e -> log.error("Error creating present", e));
    }

    @Operation(summary = "Delete a present", description = "Deletes a present if the authenticated user is authorized")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Present deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Present not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> remove(
            @RequestHeader("Authorization") @NotBlank String token,
            @PathVariable("id") @NotNull @Positive Long id) {
        log.info("Deleting present with ID: {} for user with token: {}", id, maskToken(token));
        return validateAndGetUserId(token)
                .flatMap(userId -> service.getId(id)
                        .switchIfEmpty(Mono.error(new NotFoundException("Present with ID: " + id + " not found")))
                        .flatMap(present -> service.delete(id)))
                .doOnSuccess(v -> log.info("Successfully deleted present with ID: {}", id))
                .doOnError(NotFoundException.class, e -> log.warn("Present with ID: {} not found for deletion", id, e))
                .doOnError(e -> log.error("Error deleting present with ID: {}", id, e))
                .onErrorResume(NotFoundException.class, e -> Mono.empty());
    }

    private Mono<Long> validateAndGetUserId(String token) {
        try {
            jwt.validateToken(token);
            Long userId = jwt.getUserIdFromToken(token);
            log.debug("Token validated successfully, user ID: {}", userId);
            return Mono.just(userId);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("Expired token", e);
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired"));
        } catch (io.jsonwebtoken.JwtException e) {
            log.error("Invalid token", e);
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token validation failed"));
        }
    }

    private String maskToken(String token) {
        return token.substring(0, Math.min(token.length(), 10)) + "...";
    }
}