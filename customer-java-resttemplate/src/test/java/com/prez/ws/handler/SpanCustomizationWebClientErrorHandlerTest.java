package com.prez.ws.handler;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import brave.SpanCustomizer;
import com.prez.ws.WebServiceException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

@ExtendWith(MockitoExtension.class)
class SpanCustomizationWebClientErrorHandlerTest {

  @Mock
  private ResponseErrorHandler responseErrorHandler;

  @Mock
  private SpanCustomizer mockSpan;


  private SpanCustomizationWebClientErrorHandler toTest;

  @BeforeEach
  void beforeEach() {
    toTest = new SpanCustomizationWebClientErrorHandler("test", responseErrorHandler, mockSpan);
  }

  @Test
  void handleError_shouldTagGivenSpan_whenPartnerExceptionThrownByWrappedErrorHandler() throws IOException {
    // Given
    final MockClientHttpResponse response = new MockClientHttpResponse(new byte[] {}, HttpStatus.BAD_REQUEST);
    Mockito.doThrow(new WebServiceException("testPartner", HttpStatus.BAD_REQUEST, "A dull error"))
        .when(responseErrorHandler).handleError(response);

    // When
    try {
      toTest.handleError(response);
    } catch (Exception e) {
      // we do nothing we just want to check the decoration of the spanCustomizer context
    }

    // Then
    verify(mockSpan).tag("http.statusCode", "400");
    verify(mockSpan).tag("error", "400");
  }

  @Test
  void handleError_shouldTagGivenSpan_whenRandomExceptionThrownByWrappedErrorHandler() throws IOException {
    // Given
    final MockClientHttpResponse response = new MockClientHttpResponse(new byte[] {}, HttpStatus.INTERNAL_SERVER_ERROR);
    doThrow(new RuntimeException("A total random exception"))
        .when(responseErrorHandler).handleError(response);

    // When
    try {
      toTest.handleError(response);
    } catch (Exception e) {
      // we do nothing we just want to check the decoration of the spanCustomizer context
    }

    // Then
    verify(mockSpan).tag("error", "java.lang.RuntimeException");
  }

  @Test
  void handleError_shouldTagHttpStatus_whenNoExceptionThrownByWrappedErrorHandler() {
    // Given && When
    try {
      toTest.handleError(new MockClientHttpResponse(new byte[] {}, HttpStatus.CONFLICT));
    } catch (IOException e) {
      // we do nothing we just want to check the decoration of the spanCustomizer context
    }

    // Then
    verify(mockSpan).tag("http.statusCode", "409");
  }

}