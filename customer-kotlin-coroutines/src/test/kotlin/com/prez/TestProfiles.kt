package com.prez

import org.junit.jupiter.api.Tag
import org.testcontainers.junit.jupiter.Testcontainers

/*
 * A convenient way to group multiple annotations in one
 */

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Testcontainers
@Tag("docker")
annotation class DockerTest

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Tag("integration")
annotation class IntegrationTest
