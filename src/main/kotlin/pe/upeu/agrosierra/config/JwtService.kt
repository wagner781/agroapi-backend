package pe.upeu.agrosierra.config

import org.springframework.context.annotation.*
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import com.nimbusds.jose.jwk.source.ImmutableSecret
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.util.Base64
import java.time.Instant
import java.time.Duration
import pe.upeu.agrosierra.seguridad.Usuario
import org.springframework.stereotype.Service

@Configuration
class JwtClaves {
    // Sin secreto configurado se genera uno aleatorio: reiniciar invalida sesiones, nunca se usa una clave pública fija.
    private val bytes = System.getenv("JWT_SECRET_BASE64")?.let { Base64.getDecoder().decode(it) }
        ?: ByteArray(32).also { SecureRandom().nextBytes(it) }
    private val key = SecretKeySpec(bytes.also { require(it.size >= 32) { "JWT_SECRET_BASE64 requiere 32 bytes" } }, "HmacSHA256")
    @Bean fun jwtEncoder(): JwtEncoder = NimbusJwtEncoder(ImmutableSecret<com.nimbusds.jose.proc.SecurityContext>(key))
    @Bean fun jwtDecoder(): JwtDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build().also {
        it.setJwtValidator(org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator(
            JwtTimestampValidator(Duration.ZERO), JwtIssuerValidator("agroapi")))
    }
}
@Service
class JwtService(private val encoder: JwtEncoder) {
    fun emitir(u: Usuario): String {
        val ahora = Instant.now()
        val claims = JwtClaimsSet.builder().issuer("agroapi").subject(u.id.toString()).issuedAt(ahora)
            .expiresAt(ahora.plusSeconds(3600)).claim("privilegios", u.rol.privilegios.map { it.name }).build()
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).tokenValue
    }
}
