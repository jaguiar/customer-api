package com.prez.ws.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

/**
 *
 */
@ExtendWith(MockitoExtension.class)
class PlainTextErrorHandlerTest {

  private PlainTextErrorHandler toTest;

  @BeforeEach
  void setup() {
    toTest = new PlainTextErrorHandler("CustomerWS");
  }

  @Test
  void hasError_shouldAnswerTrue_whenResponseStatusIsNot2xx() throws Exception {
    //Arrange
    ClientHttpResponse mockResponse = new MockClientHttpResponse(new byte[] {}, HttpStatus.BAD_GATEWAY);

    //Act
    Boolean result = toTest.hasError(mockResponse);

    //Assert
    assertThat(result).isTrue();
  }

  @Test
  void hasError_shouldAnswerFalse_whenResponseStatusIs2xx() throws Exception {
    //Arrange
    ClientHttpResponse mockResponse = new MockClientHttpResponse(new byte[] {}, HttpStatus.CREATED);

    //Act
    Boolean result = toTest.hasError(mockResponse);

    //Assert
    assertThat(result).isFalse();
  }

  @Test
  void handleError_shouldThrowAnExceptionAndUpdateHistogram() {
    //Arrange
    final String body = "houhouhou ! Should not appear in thrown exception ...";
    ClientHttpResponse mockResponse = new MockClientHttpResponse(body.getBytes(), HttpStatus.BAD_REQUEST);

    //Act
    Throwable thrown = catchThrowable(() -> toTest.handleError(mockResponse));

    assertThat(thrown)
        .hasMessage(
            "Webservice error : webService=CustomerWS, statusCode=400 BAD_REQUEST : Unexpected error occurred when calling web service CustomerWS")
        .hasFieldOrPropertyWithValue("httpStatusCode", HttpStatus.BAD_REQUEST);
  }
}