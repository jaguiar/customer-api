package com.prez.ws

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus

open class WebServiceException(
        val errorName: String = "WEBSERVICE_ERROR",
        val webServiceName: String,
        val httpStatusCode: HttpStatus,
        private val errorDescription: String
) : RuntimeException("$errorName : webService=$webServiceName, statusCode=$httpStatusCode : $errorDescription") {

  val error by lazy { Error(errorName, errorDescription) }
}

data class Error(
        @JsonProperty("error")
        val error: String,
        @JsonProperty("error_description")
        val errorDescription: String
)
