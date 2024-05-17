package pl.chrapatij.backend.controller.advice;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.chrapatij.backend.exception.userExceptionError400;
import pl.chrapatij.backend.exception.userExceptionError401;
import pl.chrapatij.backend.exception.userExceptionError500;
import pl.chrapatij.backend.model.ErrorResponse;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
    @ExceptionHandler(userExceptionError400.class)
    public ResponseEntity<ErrorResponse> userExceptionError400(userExceptionError400 ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 400);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> usernameNotFoundException(UsernameNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 400);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(userExceptionError401.class)
    public ResponseEntity<ErrorResponse> userExceptionError401(userExceptionError401 ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 401);
        return ResponseEntity.status(401).body(errorResponse);
    }

    @ExceptionHandler(userExceptionError500.class)
    public ResponseEntity<ErrorResponse> userExceptionError500(userExceptionError500 ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 500);
        return new ResponseEntity<>(errorResponse, HttpStatusCode.valueOf(500));
    }
}