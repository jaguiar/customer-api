package com.prez.api

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.ResponseStatusException
import reactor.netty.channel.AbortedException

internal class DisconnectedClientWebExceptionHandlerTest {

    val toTest = DisconnectedClientWebExceptionHandler()

    @Test
    fun `handle should not handle "classic" errors ( non disconnected client errors )`() {
        // Given
        val mockedExchange = MockServerWebExchange
                .from(MockServerHttpRequest.head("/toto"))

        // When
        val thrown = catchThrowable { toTest.handle(mockedExchange, RuntimeException("stranger things")).block() }

        // Then
        assertThat(thrown)
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("stranger things")
    }

    @Test
    fun `handle should handle "disconnect client" errors found through message`() {
        // Given
        val mockedExchange = MockServerWebExchange
                .from(MockServerHttpRequest.head("/toto"))

        // When
        val result = toTest.handle(mockedExchange, RuntimeException("connection reset by peer")).block()

        // Then
        assertThat(result)
                .isNull()
        assertThat(mockedExchange.response.statusCode).isEqualTo(INTERNAL_SERVER_ERROR)
    }

    @Test
    fun `handle should handle "disconnect client" errors found through classname`() {
        // Given
        val mockedExchange = MockServerWebExchange
                .from(MockServerHttpRequest.head("/toto"))

        // When
        val result = toTest.handle(mockedExchange, AbortedException("abort")).block()

        // Then
        assertThat(result)
                .isNull()
        assertThat(mockedExchange.response.statusCode).isEqualTo(INTERNAL_SERVER_ERROR)
    }

    @Test
    fun `handle should handle "disconnect client" errors even if response is committed`() {
        // Given
        val mockedExchange = MockServerWebExchange
                .from(MockServerHttpRequest.head("/toto"))
        mockedExchange.response.setComplete().block()

        // When
        val thrown = catchThrowable { toTest.handle(mockedExchange, AbortedException("stranger things")).block() }

        // Then
        assertThat(thrown)
                .isInstanceOf(ResponseStatusException::class.java)
        assertThat(thrown as ResponseStatusException)
                .hasFieldOrPropertyWithValue("status", INTERNAL_SERVER_ERROR)
                .hasFieldOrPropertyWithValue("reason", "Unexpected error : reactor.netty.channel.AbortedException")
    }
}