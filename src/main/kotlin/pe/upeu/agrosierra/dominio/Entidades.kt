package pe.upeu.agrosierra.dominio
import jakarta.persistence.*
import java.time.*

enum class TipoLabor { RIEGO, FUMIGACION, COSECHA }
@Entity
class Lote(
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) var id: Long = 0,
    @Column(unique=true) var codigo: String = "", var nombre: String = "", var hectareas: Double = 0.0,
    var cultivo: String = "", var valle: String = "", var campaniaCerrada: Boolean = false,
    var dosisMaximaPorHectarea: Double = 0.0
)
@Entity
class Labor(
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) var id: Long = 0,
    var loteId: Long = 0, @Enumerated(EnumType.STRING) var tipo: TipoLabor = TipoLabor.RIEGO,
    var fecha: LocalDate = LocalDate.now(), var responsable: String = "", var cantidad: Double = 0.0,
    var observacion: String = "", var creadorId: Long = 0, var registradaEn: Instant = Instant.now(),
    @Column(unique=true) var claveCliente: String? = null
)
