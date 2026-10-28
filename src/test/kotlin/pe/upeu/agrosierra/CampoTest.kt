package pe.upeu.agrosierra

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import com.fasterxml.jackson.databind.ObjectMapper
import pe.upeu.agrosierra.repositorio.LaborRepository
import java.time.Instant

@SpringBootTest(properties=["spring.datasource.url=jdbc:h2:mem:campo;DB_CLOSE_DELAY=-1"])
@AutoConfigureMockMvc @Transactional
class CampoTest {
    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var mapper: ObjectMapper
    @Autowired lateinit var labores: LaborRepository
    fun token(usuario: String): String {
        val r=mvc.perform(post("/api/v1/auth/login").contentType("application/json")
            .content("""{"usuario":"$usuario","password":"Agro2026*"}""")).andReturn().response
        return mapper.readTree(r.contentAsString)["token"].asText()
    }
    fun cuerpo(lote: Int=2,tipo: String="RIEGO",cantidad: Double=2.0) =
        """{"loteId":$lote,"tipo":"$tipo","fecha":"2026-10-28","responsable":"Prueba","cantidad":$cantidad,"observacion":"Prueba real de integracion"}"""
    @Test fun `semilla tiene ocho lotes veinte labores y paginacion`() {
        mvc.perform(get("/api/v1/lotes?tamanio=3").header("Authorization","Bearer ${token("tecnico1")}"))
            .andExpect(status().isOk).andExpect(jsonPath("$.contenido.length()").value(3))
            .andExpect(jsonPath("$.totalElementos").value(8)).andExpect(jsonPath("$.ultima").value(false))
        assert(labores.count()==20L)
    }
    @Test fun `administrador no crea y tecnico no elimina ni cierra`() {
        mvc.perform(post("/api/v1/labores").header("Authorization","Bearer ${token("admin")}").contentType("application/json").content(cuerpo()))
            .andExpect(status().isForbidden)
        val t=token("tecnico1")
        mvc.perform(delete("/api/v1/labores/1").header("Authorization","Bearer $t")).andExpect(status().isForbidden)
        mvc.perform(post("/api/v1/lotes/1/cerrar-campania").header("Authorization","Bearer $t")).andExpect(status().isForbidden)
    }
    @Test fun `campania cerrada y dosis excesiva se rechazan`() {
        val t=token("tecnico1")
        mvc.perform(post("/api/v1/labores").header("Authorization","Bearer $t").contentType("application/json").content(cuerpo(7)))
            .andExpect(status().isConflict)
        mvc.perform(post("/api/v1/labores").header("Authorization","Bearer $t").contentType("application/json").content(cuerpo(tipo="FUMIGACION",cantidad=999.0)))
            .andExpect(status().isBadRequest).andExpect(jsonPath("$.validationErrors.cantidad").exists())
    }
    @Test fun `cierre exige cosecha y permite lote que la tiene`() {
        val t=token("jefe1")
        mvc.perform(post("/api/v1/lotes/2/cerrar-campania").header("Authorization","Bearer $t")).andExpect(status().isConflict)
        mvc.perform(post("/api/v1/lotes/1/cerrar-campania").header("Authorization","Bearer $t")).andExpect(status().isOk)
            .andExpect(jsonPath("$.campaniaCerrada").value(true))
    }
    @Test fun `tecnico solo edita propias dentro de veinticuatro horas`() {
        val l=labores.findAll().first { it.loteId==2L }
        l.registradaEn=Instant.now().minusSeconds(86401); labores.saveAndFlush(l)
        mvc.perform(put("/api/v1/labores/${l.id}").header("Authorization","Bearer ${token("tecnico1")}").contentType("application/json").content(cuerpo()))
            .andExpect(status().isForbidden)
        l.registradaEn=Instant.now(); l.creadorId=999; labores.saveAndFlush(l)
        mvc.perform(put("/api/v1/labores/${l.id}").header("Authorization","Bearer ${token("tecnico1")}").contentType("application/json").content(cuerpo()))
            .andExpect(status().isForbidden)
        mvc.perform(put("/api/v1/labores/${l.id}").header("Authorization","Bearer ${token("jefe1")}").contentType("application/json").content(cuerpo()))
            .andExpect(status().isOk)
    }
    @Test fun `crud devuelve 201 200 y 204 e idempotencia evita duplicar`() {
        val t=token("jefe1")
        val r=mvc.perform(post("/api/v1/labores").header("Authorization","Bearer $t").header("Idempotency-Key","prueba-unica")
            .contentType("application/json").content(cuerpo())).andExpect(status().isCreated).andReturn().response
        val id=mapper.readTree(r.contentAsString)["id"].asLong()
        mvc.perform(post("/api/v1/labores").header("Authorization","Bearer $t").header("Idempotency-Key","prueba-unica")
            .contentType("application/json").content(cuerpo())).andExpect(status().isCreated).andExpect(jsonPath("$.id").value(id))
        mvc.perform(put("/api/v1/labores/$id").header("Authorization","Bearer $t").contentType("application/json").content(cuerpo(cantidad=3.0))).andExpect(status().isOk)
        mvc.perform(delete("/api/v1/labores/$id").header("Authorization","Bearer $t")).andExpect(status().isNoContent).andExpect(content().string(""))
        mvc.perform(get("/api/v1/lotes/999").header("Authorization","Bearer $t")).andExpect(status().isNotFound)
    }
}
