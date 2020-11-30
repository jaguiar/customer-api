package com.prez.ws

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class WebServiceExceptionTest {

    @Test
    fun `should throw a WebServiceException with a web service error`() {
        // Given
        val ex = WebServiceException(webServiceName = "CustomerWS", httpStatusCode = HttpStatus.BAD_REQUEST, errorDescription = "My description")

        // When && Then
        assertThat(ex.message).isEqualTo("WEBSERVICE_ERROR : webService=CustomerWS, statusCode=${HttpStatus.BAD_REQUEST} : My description")
    }

    @Test
    fun `should throw a WebServiceException with a custom error`() {
        // Given
        val ex = WebServiceException(errorName = "Custom error", webServiceName = "CustomerWS", httpStatusCode = HttpStatus.BAD_REQUEST, errorDescription = "My description")

        // When && Then
        assertThat(ex.message).isEqualTo("Custom error : webService=CustomerWS, statusCode=${HttpStatus.BAD_REQUEST} : My description")
    }
}
