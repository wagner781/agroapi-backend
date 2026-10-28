package pe.upeu.agrosierra

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import java.time.Instant

@SpringBootTest(properties=["spring.datasource.url=jdbc:h2:mem:seguridad;DB_CLOSE_DELAY=-1"])
@AutoConfigureMockMvc
class SeguridadTest {
    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var mapper: ObjectMapper
    @Autowired lateinit var encoder: JwtEncoder

    @Test fun `sin token y con token alterado devuelve 401 uniforme`() {
        mvc.perform(get("/api/v1/usuarios/me")).andExpect(status().isUnauthorized).andExpect(jsonPath("$.path").value("/api/v1/usuarios/me"))
        mvc.perform(get("/api/v1/usuarios/me").header("Authorization","Bearer alterado")).andExpect(status().isUnauthorized)
    }
    @Test fun `credenciales incorrectas se rechazan`() {
        mvc.perform(post("/api/v1/auth/login").contentType("application/json").content("""{"usuario":"tecnico1","password":"mala"}"""))
            .andExpect(status().isUnauthorized).andExpect(jsonPath("$.message").value("Credenciales incorrectas"))
    }
    @Test fun `login firma token y devuelve privilegios`() {
        val body=mvc.perform(post("/api/v1/auth/login").contentType("application/json").content("""{"usuario":"tecnico1","password":"Agro2026*"}"""))
            .andExpect(status().isOk).andExpect(jsonPath("$.expiraEn").value(3600)).andReturn().response.contentAsString
        val token=mapper.readTree(body)["token"].asText()
        mvc.perform(get("/api/v1/usuarios/me").header("Authorization","Bearer $token")).andExpect(status().isOk)
            .andExpect(jsonPath("$.usuario").value("tecnico1")).andExpect(jsonPath("$.privilegios").isArray)
        val partes=token.split('.'); val firma=partes[2]; val alterado=partes.take(2).joinToString(".")+"."+(if(firma[0]=='A') "B" else "A")+firma.drop(1)
        mvc.perform(get("/api/v1/usuarios/me").header("Authorization","Bearer $alterado")).andExpect(status().isUnauthorized)
    }
    @Test fun `token firmado vencido se rechaza sin tolerancia`() {
        val ahora=Instant.now()
        val claims=JwtClaimsSet.builder().issuer("agroapi").subject("1").issuedAt(ahora.minusSeconds(7200)).expiresAt(ahora.minusSeconds(1)).build()
        val token=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).tokenValue
        mvc.perform(get("/api/v1/usuarios/me").header("Authorization","Bearer $token")).andExpect(status().isUnauthorized)
    }
}
