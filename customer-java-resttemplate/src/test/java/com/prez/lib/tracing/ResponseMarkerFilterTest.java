package com.prez.lib.tracing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.DistributionSummary;
import java.net.SocketTimeoutException;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

/**
 *
 */
class ResponseMarkerFilterTest {

  private final static byte[] FAKE_BODY = new byte[] {};
  private final static HttpRequest mockRequest = mock(HttpRequest.class);

  @Test
  @DisplayName("should update summary with value 1 if the HTTP status of the response is not 5xx")
  void intercept_shouldUpdateSummaryWithValue1_whenResponseIsNot5xx() throws Exception {
    //Arrange

    ClientHttpRequestExecution mockHttpRequestExecution = mock(ClientHttpRequestExecution.class);
    ClientHttpResponse mockResponse = new MockClientHttpResponse(new byte[] {}, HttpStatus.OK);
    when(mockHttpRequestExecution.execute(mockRequest, FAKE_BODY)).thenReturn(mockResponse);
    when(mockRequest.getURI()).thenReturn(new URI("http://scoubidoooowoooo.com"));
    DistributionSummary mockHistogram = mock(DistributionSummary.class);

    //Act
    ResponseMarkerFilter toTest = new ResponseMarkerFilter(mockHistogram);
    toTest.intercept(mockRequest, new byte[] {}, mockHttpRequestExecution);

    //Assert
    verify(mockHistogram).record(1);
  }

  @Test
  @DisplayName("should update summary with value 0 if the HTTP status of the response is 5xx")
  void intercept_shouldUpdateSummaryWithalue0_whenResponseIs5xx() throws Exception {
    //Arrange

    ClientHttpRequestExecution mockHttpRequestExecution = mock(ClientHttpRequestExecution.class);
    ClientHttpResponse mockResponse = new MockClientHttpResponse(new byte[] {}, HttpStatus.BAD_GATEWAY);
    when(mockHttpRequestExecution.execute(mockRequest, FAKE_BODY)).thenReturn(mockResponse);
    when(mockRequest.getURI()).thenReturn(new URI("http://scoubidoooowoooo.com"));
    DistributionSummary mockHistogram = mock(DistributionSummary.class);

    //Act
    ResponseMarkerFilter toTest = new ResponseMarkerFilter(mockHistogram);
    toTest.intercept(mockRequest, new byte[] {}, mockHttpRequestExecution);

    //Assert
    verify(mockHistogram).record(0L);
  }

  @Test
  @DisplayName("should update summary with value 0 if an exception occurs during request execution")
  void intercept_shouldUpdateSummaryWithalue0_whenErrorWhileExecutingRequest() throws Exception {
    //Arrange

    ClientHttpRequestExecution mockHttpRequestExecution = mock(ClientHttpRequestExecution.class);
    when(mockHttpRequestExecution.execute(mockRequest, FAKE_BODY))
        .thenThrow(new SocketTimeoutException("A totally random exception"));
    DistributionSummary mockHistogram = mock(DistributionSummary.class);

    //Act
    ResponseMarkerFilter toTest = new ResponseMarkerFilter(mockHistogram);
    try {
      toTest.intercept(mockRequest, new byte[] {}, mockHttpRequestExecution);
    } catch (Exception e) {
      // do nothing, we just want to verify the histogram update
    }

    //Assert
    verify(mockHistogram).record(0L);
  }
}