package pe.upeu.agrosierra.repositorio
import pe.upeu.agrosierra.dominio.*
import org.springframework.data.jpa.repository.*
import org.springframework.data.repository.query.Param
import org.springframework.data.domain.*
import jakarta.persistence.LockModeType

interface LoteRepository : JpaRepository<Lote,Long> {
    @Query("select l from Lote l where (:valle is null or l.valle=:valle) and (:cerrada is null or l.campaniaCerrada=:cerrada)")
    fun filtrar(@Param("valle") valle: String?, @Param("cerrada") cerrada: Boolean?, pagina: Pageable): Page<Lote>
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select l from Lote l where l.id=:id")
    fun bloquear(@Param("id") id: Long): Lote?
}
interface LaborRepository : JpaRepository<Labor,Long> {
    fun findByLoteIdOrderByFechaDescIdDesc(loteId: Long, pagina: Pageable): Page<Labor>
    fun existsByLoteIdAndTipo(loteId: Long,tipo: TipoLabor): Boolean
    fun findByClaveCliente(claveCliente: String): Labor?
}
