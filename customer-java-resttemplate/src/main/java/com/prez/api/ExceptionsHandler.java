package com.prez.api;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import brave.SpanCustomizer;
import com.prez.api.dto.ErrorResponse;
import com.prez.exception.NotFoundException;
import com.prez.lib.tracing.SpanCustomization;
import com.prez.ws.WebServiceException;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class ExceptionsHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionsHandler.class);
  private final SpanCustomizer spanCustomizer;

  public ExceptionsHandler(SpanCustomizer spanCustomizer) {
    this.spanCustomizer = spanCustomizer;
  }


  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
    LOGGER.error(ex.getLocalizedMessage(), ex);
    SpanCustomization.tagError(spanCustomizer, ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
    String errorMessage = processFieldsError(ex.getBindingResult());
    LOGGER.error(errorMessage, ex);
    SpanCustomization.tagError(spanCustomizer, ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("VALIDATION_ERROR", errorMessage));
  }

  private String processFieldsError(BindingResult result) {
    String prefix = result.getFieldErrorCount() + " error(s) while validating " + result.getObjectName() + " : ";
    List<String> messages = new ArrayList<>();
    result.getFieldErrors().forEach(error -> messages.add(error.getDefaultMessage()));

    return prefix + messages;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatchException(TypeMismatchException ex) {
    LOGGER.error(ex.getLocalizedMessage(), ex);
    SpanCustomization.tagError(spanCustomizer, ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()));
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
    LOGGER.error(ex.getLocalizedMessage(), ex);
    SpanCustomization.tagError(spanCustomizer, ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("CUSTOMER_BAD_REQUEST", ex.getMessage()));
  }

  @ExceptionHandler(WebServiceException.class)
  public ResponseEntity<ErrorResponse> handleCustomerWSException(final WebServiceException ex) {
    LOGGER.error(ex.getLocalizedMessage(), ex);
    SpanCustomization.tagError(spanCustomizer, ex);
    return ResponseEntity.status(ex.getHttpStatusCode()).contentType(APPLICATION_JSON)
        .body(new ErrorResponse("CUSTOMER_WS_UNEXPECTED_ERROR", ex.getError().getErrorDescription()));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> elementNotFoundErrorHandler(NotFoundException ex) {
    LOGGER.error(ex.getLocalizedMessage(), ex);
    return ResponseEntity.status(NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("NOT_FOUND", ex.getLocalizedMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> globalErrorHandler(Exception ex) {
    LOGGER.error(ex.getLocalizedMessage(), ex);
    return ResponseEntity.status(INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("UNEXPECTED_ERROR", "Something horribly wrong happened, I could tell you what but then I’d have to kill you."));
  }
}
