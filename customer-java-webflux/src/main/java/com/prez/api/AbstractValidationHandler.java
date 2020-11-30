package com.prez.api;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.prez.api.dto.ErrorResponse;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public abstract class AbstractValidationHandler<T, U extends Validator> {

  private final Class<T> validationClass;

  private final U validator;

  protected AbstractValidationHandler(Class<T> clazz, U validator) {
    this.validationClass = clazz;
    this.validator = validator;
  }

  public final Mono<ServerResponse> handleRequest(final ServerRequest request) {
    return request.bodyToMono(this.validationClass)
        .switchIfEmpty(Mono.error(() -> new ResponseStatusException(BAD_REQUEST, "Request body is mandatory")))
        .flatMap(body -> {
          final Errors errors = new BeanPropertyBindingResult(body, this.validationClass.getName());
          this.validator.validate(body, errors);

          if (errors.getAllErrors().isEmpty()) {
            return processBody(body, request);
          } else {
            return onValidationErrors(errors, body, request);
          }
        });
  }

  protected Mono<ServerResponse> onValidationErrors(Errors errors, T invalidBody, ServerRequest request) {
    return ServerResponse.badRequest().bodyValue(new ErrorResponse("VALIDATION_ERROR", processFieldsError(errors)));
  }

  abstract protected Mono<ServerResponse> processBody(T validBody, ServerRequest originalRequest);

  private String processFieldsError(Errors errors) {
    String prefix = errors.getFieldErrorCount() + " error(s) while validating " + errors.getObjectName() + " : ";
    return errors.getFieldErrors().stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .collect(Collectors.joining(",", prefix, ""));
  }
}