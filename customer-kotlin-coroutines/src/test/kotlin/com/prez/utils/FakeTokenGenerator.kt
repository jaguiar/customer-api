package com.prez.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.bouncycastle.util.io.pem.PemReader
import org.springframework.util.ResourceUtils
import java.io.IOException
import java.io.StringReader
import java.nio.file.Files
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Let you load a fake private key and generate some tokens with it
 *
 * @author Jennifer Aguiar
 */
class FakeTokenGenerator(issuer: String) {

    private val rsaAlgorithm: Algorithm
    private val issuer: String

    companion object {

        @Throws(InvalidKeySpecException::class, IOException::class)
        fun readPrivateKey(factory: KeyFactory): RSAPrivateKey {
            val file = ResourceUtils.getFile("classpath:private.der")
            val content = Files.readAllBytes(file.toPath())
            val priKeySpec = PKCS8EncodedKeySpec(content)
            return factory.generatePrivate(priKeySpec) as RSAPrivateKey
        }

        @Throws(InvalidKeySpecException::class, IOException::class)
        fun readPublicKey(factory: KeyFactory, keyValue: String): RSAPublicKey {
            PemReader(StringReader(keyValue)).use { pemReader ->
                val content = pemReader.readPemObject().content
                val pubKeySpec = X509EncodedKeySpec(content)
                return factory.generatePublic(pubKeySpec) as RSAPublicKey
            }
        }
    }

    init {
        rsaAlgorithm = getRsaAlgorithm()
        this.issuer = issuer
    }

    fun generateSignedToken(sub: String, expiresAt: Date, scopes: String): String {
        return JWT.create()
                .withSubject(sub)
                .withExpiresAt(expiresAt)
                .withIssuer(issuer)
                .withClaim("name", sub)
                .withClaim("azp", "RANDOM_CLIENT_ID")
                .withClaim("tokenName", "id_token")
                .withClaim("realm", "/test-authorization-server")
                .withClaim("scope", scopes)
                .sign(rsaAlgorithm)
    }

    fun generateNotExpiredSignedToken(sub: String, nbSecondsValidity: Int, scopes: String): String {
        return generateSignedToken(sub, Date.from(Instant.now().plus(nbSecondsValidity.toLong(), ChronoUnit.SECONDS)), scopes)
    }

    private fun getRsaAlgorithm(): Algorithm {
        return try {
            val kf = KeyFactory.getInstance("RSA")
            val privateKey = readPrivateKey(kf)
            Algorithm.RSA256(null, privateKey)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}