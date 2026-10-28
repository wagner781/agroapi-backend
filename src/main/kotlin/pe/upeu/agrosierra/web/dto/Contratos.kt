package pe.upeu.agrosierra.web.dto
import java.time.Instant

data class Pagina<T>(val contenido: List<T>, val pagina: Int, val tamanio: Int, val totalElementos: Long, val totalPaginas: Int, val ultima: Boolean)
data class ErrorRespuesta(val timestamp: String = Instant.now().toString(), val status: Int, val error: String,
    val message: String, val path: String, val validationErrors: Map<String, String> = emptyMap())
data class LoginPeticion(val usuario: String, val password: String)
data class SesionRespuesta(val token: String, val usuario: String, val nombre: String, val rol: String,
    val privilegios: Set<String>, val expiraEn: Long = 3600)
data class UsuarioRespuesta(val id: Long, val usuario: String, val nombre: String, val rol: String, val privilegios: Set<String>)
