package pe.upeu.agrosierra.config

import pe.upeu.agrosierra.web.dto.ErrorRespuesta
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

class FalloApi(val status: Int, override val message: String, val campos: Map<String,String> = emptyMap()) : RuntimeException(message)
@RestControllerAdvice
class ManejadorDeErrores {
    @ExceptionHandler(FalloApi::class)
    fun negocio(e: FalloApi, r: HttpServletRequest) = respuesta(e.status, e.message, r, e.campos)
    @ExceptionHandler(AccessDeniedException::class)
    fun prohibido(e: AccessDeniedException, r: HttpServletRequest) = respuesta(403, "No tiene el privilegio necesario", r)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validacion(e: MethodArgumentNotValidException, r: HttpServletRequest) = respuesta(400, "Datos invalidos", r,
        e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Valor invalido") })
    @ExceptionHandler(HttpMessageNotReadableException::class, MethodArgumentTypeMismatchException::class)
    fun formato(e: Exception, r: HttpServletRequest) = respuesta(400, "Formato de peticion invalido", r)
    @ExceptionHandler(Exception::class)
    fun inesperado(e: Exception, r: HttpServletRequest): ResponseEntity<ErrorRespuesta> {
        org.slf4j.LoggerFactory.getLogger(javaClass).error("Error no previsto", e)
        return respuesta(500, "Error del servidor", r)
    }
    private fun respuesta(s: Int, m: String, r: HttpServletRequest, campos: Map<String,String> = emptyMap()) =
        ResponseEntity.status(s).body(ErrorRespuesta(status=s, error=HttpStatus.valueOf(s).reasonPhrase, message=m, path=r.requestURI, validationErrors=campos))
}
