package pe.upeu.agrosierra.web
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import jakarta.validation.Valid
import pe.upeu.agrosierra.servicio.CampoService
import pe.upeu.agrosierra.web.dto.LaborPeticion
import java.net.URI

@RestController @RequestMapping("/api/v1")
class CampoController(private val servicio: CampoService) {
    @GetMapping("/lotes") fun lotes(@RequestParam(defaultValue="0") pagina: Int,@RequestParam(defaultValue="20") tamanio: Int,
        @RequestParam(required=false) valle: String?,@RequestParam(required=false) campaniaCerrada: Boolean?) = servicio.listar(pagina,tamanio,valle,campaniaCerrada)
    @GetMapping("/lotes/{id}") fun detalle(@PathVariable id: Long) = servicio.detalle(id)
    @GetMapping("/lotes/{id}/labores") fun labores(@PathVariable id: Long,@RequestParam(defaultValue="0") pagina: Int,
        @RequestParam(defaultValue="20") tamanio: Int) = servicio.listarLabores(id,pagina,tamanio)
    @PostMapping("/labores") fun crear(@Valid @RequestBody p: LaborPeticion,@RequestHeader(value="Idempotency-Key",required=false) clave: String?) =
        servicio.crear(p,clave).let { ResponseEntity.created(URI.create("/api/v1/labores/${it.id}")).body(it) }
    @PutMapping("/labores/{id}") fun editar(@PathVariable id: Long,@Valid @RequestBody p: LaborPeticion) = servicio.editar(id,p)
    @DeleteMapping("/labores/{id}") fun eliminar(@PathVariable id: Long): ResponseEntity<Void> { servicio.eliminar(id); return ResponseEntity.noContent().build() }
    @PostMapping("/lotes/{id}/cerrar-campania") fun cerrar(@PathVariable id: Long) = servicio.cerrar(id)
}
