package pe.upeu.agrosierra.web.dto
import jakarta.validation.constraints.*
import pe.upeu.agrosierra.dominio.*
import java.time.LocalDate

data class LaborPeticion(
    @field:Positive val loteId: Long,
    val tipo: TipoLabor,
    val fecha: LocalDate,
    @field:NotBlank @field:Size(max=120) val responsable: String,
    @field:DecimalMin("0.0", inclusive=false) val cantidad: Double,
    @field:Size(max=1000) val observacion: String = ""
)
data class LoteRespuesta(val id: Long,val codigo: String,val nombre: String,val hectareas: Double,val cultivo: String,
    val valle: String,val campaniaCerrada: Boolean,val dosisMaximaPorHectarea: Double)
fun Lote.dto() = LoteRespuesta(id,codigo,nombre,hectareas,cultivo,valle,campaniaCerrada,dosisMaximaPorHectarea)
data class LaborRespuesta(val id: Long,val loteId: Long,val tipo: TipoLabor,val fecha: String,val responsable: String,
    val cantidad: Double,val observacion: String,val pendiente: Boolean = false)
fun Labor.dto() = LaborRespuesta(id,loteId,tipo,fecha.toString(),responsable,cantidad,observacion)
