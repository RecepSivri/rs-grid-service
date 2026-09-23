package com.rs.gridservice.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.lang.reflect.Method;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesResourceNotFound() {
        ResponseEntity<ApiError> response = handler.handleNotFound(new ResourceNotFoundException("bulunamadi"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("bulunamadi");
    }

    @Test
    void handlesUserAlreadyExists() {
        ResponseEntity<ApiError> response = handler.handleAlreadyExists(new UserAlreadyExistsException("zaten var"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo("zaten var");
    }

    @Test
    void handlesInvalidCredentials() {
        ResponseEntity<ApiError> response = handler.handleInvalidCredentials(new InvalidCredentialsException("gecersiz"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).isEqualTo("gecersiz");
    }

    @Test
    void handlesKeycloakOperationError() {
        ResponseEntity<ApiError> response = handler.handleKeycloakError(
                new KeycloakOperationException("baglanti kurulamadi", new RuntimeException()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().message()).contains("baglanti kurulamadi");
    }

    @Test
    void handlesValidationErrorsWithAndWithoutDefaultMessage() throws NoSuchMethodException {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "username", "username bos olamaz"));
        bindingResult.addError(new FieldError("request", "email", null, false, null, null, null));

        Method dummy = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyTarget", String.class);
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(new MethodParameter(dummy, 0), bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().fieldErrors())
                .containsEntry("username", "username bos olamaz")
                .containsEntry("email", "gecersiz deger");
    }

    @Test
    void handlesValidationErrorsWithDuplicateFieldKeepsFirstMessage() throws NoSuchMethodException {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "username", "ilk hata"));
        bindingResult.addError(new FieldError("request", "username", "ikinci hata"));

        Method dummy = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyTarget", String.class);
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(new MethodParameter(dummy, 0), bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(ex);

        assertThat(response.getBody().fieldErrors()).containsEntry("username", "ilk hata");
    }

    @Test
    void handlesMissingRequestParameter() {
        ResponseEntity<ApiError> response = handler.handleMissingParameter(
                new MissingServletRequestParameterException("userId", "String"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("userId").contains("zorunlu");
    }

    @Test
    void handlesConstraintViolation() {
        ResponseEntity<ApiError> response = handler.handleConstraintViolation(
                new ConstraintViolationException("gecersiz parametre", new HashSet<>()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("gecersiz parametre");
    }

    @Test
    void handlesNotReadableBody() {
        ResponseEntity<ApiError> response = handler.handleNotReadable(
                new HttpMessageNotReadableException("bad json", (org.springframework.http.HttpInputMessage) null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("okunamadi");
    }

    @Test
    void handlesGenericException() {
        ResponseEntity<ApiError> response = handler.handleGeneric(new HttpMediaTypeNotSupportedException("beklenmedik"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).contains("Beklenmeyen");
    }

    @SuppressWarnings("unused")
    private void dummyTarget(String value) {
    }
}
