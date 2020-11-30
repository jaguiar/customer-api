package com.prez.interceptors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;

/**
 *
 */
@ExtendWith(MockitoExtension.class)
class JsonHeadersInterceptorTest {

  private JsonHeadersInterceptor toTest = new JsonHeadersInterceptor();

  @Test
  void intercept_shouldAddJsonConnectHeaders_whenCalled() throws Exception {
    //Arrange
    HttpRequest mockRequest = Mockito.mock(HttpRequest.class);
    ClientHttpRequestExecution mockHttpRequestExecution = Mockito.mock(ClientHttpRequestExecution.class);
    HttpHeaders httpHeaders = new HttpHeaders();
    when(mockRequest.getHeaders()).thenReturn(httpHeaders);
    //Act
    toTest.intercept(mockRequest, new byte[] {}, mockHttpRequestExecution);

    //Assert
    assertThat(httpHeaders)
        .hasSize(2)
        .hasEntrySatisfying("Content-Type", list -> assertThat(list).containsExactly(MediaType.APPLICATION_JSON_VALUE))
        .hasEntrySatisfying("Accept", list -> assertThat(list).containsExactly(MediaType.APPLICATION_JSON_VALUE));
    verify(mockRequest).getHeaders();
  }

}