package pet.project.wish.error;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ErrorResponse> handleNotFoundException(RuntimeException ex) {
        return Mono.just(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleGenericException(Exception ex) {
        return Mono.just(new ErrorResponse("Internal server error: " + ex.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
        return Mono.just(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        return Mono.just(new ErrorResponse(ex.getMessage()));
    }

    record ErrorResponse(String message) {}
}
