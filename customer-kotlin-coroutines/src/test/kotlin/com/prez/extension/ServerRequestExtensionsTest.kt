package com.prez.extension

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono
import kotlin.test.assertFailsWith


class ServerRequestExtensionsTest {

  /* use runBlocking before kotlin.coroutines 1.4.0 and runBlockingTest from 1.4.0,
 see https://craigrussell.io/2019/11/unit-testing-coroutine-suspend-functions-using-testcoroutinedispatcher/ regarding issue with runBlocking */
  @Test
  fun `should await for body and map it if validation is OK`(): Unit = runBlocking {
    // Given
    val dummy = Dummy("dabedidouba")
    val dummyValidator = mock(Validator::class.java)
    //do nothing when validator.validate(...)

    // Test
    val toTest: ServerRequest = MockServerRequest.builder().body(Mono.just(dummy))
    val actual: Dummy = toTest.awaitBodyAndValidate(dummyValidator)

    // Then
    assertThat(actual)
      .isNotNull
      .hasFieldOrPropertyWithValue("id", "dabedidouba")
  }

  @Test
  fun `should await for body and throw a ServerWebInputException when body is empty`(): Unit = runBlocking {
    // Given
    val dummyValidator = mock(Validator::class.java)
    //no validation needed for this case

    // Test
    // When
    val toTest: ServerRequest = MockServerRequest.builder().body(Mono.empty<String>())
    val thrown = assertFailsWith<ServerWebInputException> { /* behind is a runCatching{} */
      toTest.awaitBodyAndValidate(dummyValidator)
    }

    // Then
    assertThat(thrown)
      .hasMessage("400 BAD_REQUEST \"Request body is mandatory\"")
  }

  @Test
  fun `should await for body and throw a ServerWebInputException when validation fails`(): Unit = runBlocking {
    // Given
    val dummy = Dummy("ole ole")
    val dummyValidator = mock(Validator::class.java)
    val ans = Answer<Void?> { invocation: InvocationOnMock ->
      val errors = invocation.arguments[1] as Errors
      errors.rejectValue("id", "forbidden_ole_ole")
      null
    }
    doAnswer(ans).`when`(dummyValidator).validate(eq(dummy), any(Errors::class.java))

    // Test
    // When
    val toTest: ServerRequest = MockServerRequest.builder().body(Mono.just(dummy))
    val thrown = assertFailsWith<ServerWebInputException> { /* behind is a runCatching{} */
      toTest.awaitBodyAndValidate(dummyValidator)
    }

    // Then
    assertThat(thrown)
      .hasMessage("400 BAD_REQUEST \"1 error(s) while validating kotlin.Unit : Field error in object 'kotlin.Unit' on field 'id': rejected value [ole ole]; codes [forbidden_ole_ole.kotlin.Unit.id,forbidden_ole_ole.id,forbidden_ole_ole.java.lang.String,forbidden_ole_ole]; arguments []; default message [null]\"")
  }

  inner class Dummy(val id: String)
}
