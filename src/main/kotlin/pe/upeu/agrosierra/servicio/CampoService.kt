package pe.upeu.agrosierra.servicio

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import pe.upeu.agrosierra.dominio.*
import pe.upeu.agrosierra.repositorio.*
import pe.upeu.agrosierra.seguridad.*
import pe.upeu.agrosierra.config.FalloApi
import pe.upeu.agrosierra.web.dto.*
import java.time.*

@Service @Transactional
class CampoService(private val lotes: LoteRepository, private val labores: LaborRepository, private val usuarios: UsuarioRepository) {
    private fun usuario(): Usuario {
        val jwt = SecurityContextHolder.getContext().authentication.principal as Jwt
        return usuarios.findById(jwt.subject.toLong()).orElseThrow { FalloApi(401,"Usuario inexistente") }
    }
    private fun paginacion(p: Int,t: Int): PageRequest {
        if(p<0 || t !in 1..100) throw FalloApi(400,"Paginacion invalida",mapOf("pagina" to "Debe ser >= 0", "tamanio" to "Debe estar entre 1 y 100"))
        return PageRequest.of(p,t)
    }
    private fun <T> Page<T>.envolver() = Pagina(content,number,size,totalElements,totalPages,isLast)
    private fun lote(id: Long) = lotes.findById(id).orElseThrow { FalloApi(404,"Lote no encontrado") }
    private fun bloquear(id: Long) = lotes.bloquear(id) ?: throw FalloApi(404,"Lote no encontrado")
    private fun labor(id: Long) = labores.findById(id).orElseThrow { FalloApi(404,"Labor no encontrada") }
    private fun validar(lote: Lote,p: LaborPeticion) {
        if(lote.campaniaCerrada) throw FalloApi(409,"La campania del lote esta cerrada (RN-01)")
        if(!p.cantidad.isFinite() || p.cantidad<=0) throw FalloApi(400,"Cantidad invalida",mapOf("cantidad" to "Debe ser positiva y finita"))
        if(p.tipo==TipoLabor.FUMIGACION && p.cantidad>lote.hectareas*lote.dosisMaximaPorHectarea)
            throw FalloApi(400,"Dosis maxima excedida (RN-04)",mapOf("cantidad" to "Maximo permitido: ${lote.hectareas*lote.dosisMaximaPorHectarea}"))
    }
    @PreAuthorize("hasAuthority('LOTE_LEER')") @Transactional(readOnly=true)
    fun listar(p: Int,t: Int,valle: String?,cerrada: Boolean?) = lotes.filtrar(valle,cerrada,paginacion(p,t).withSort(Sort.by("id"))).map { it.dto() }.envolver()
    @PreAuthorize("hasAuthority('LOTE_LEER')") @Transactional(readOnly=true)
    fun detalle(id: Long) = lote(id).dto()
    @PreAuthorize("hasAuthority('LABOR_LEER')") @Transactional(readOnly=true)
    fun listarLabores(id: Long,p: Int,t: Int): Pagina<LaborRespuesta> {
        lote(id); return labores.findByLoteIdOrderByFechaDescIdDesc(id,paginacion(p,t)).map { it.dto() }.envolver()
    }
    @PreAuthorize("hasAuthority('LABOR_CREAR')")
    fun crear(p: LaborPeticion,clave: String?): LaborRespuesta {
        val u=usuario()
        val lote=bloquear(p.loteId)
        val idempotencia=clave?.let {
            if(it.length !in 1..100) throw FalloApi(400,"Clave de idempotencia invalida")
            "${u.id}:$it"
        }
        idempotencia?.let { labores.findByClaveCliente(it)?.let { anterior ->
            if(anterior.loteId!=p.loteId) throw FalloApi(409,"La clave ya identifica otro lote")
            // Replay del POST original. El cliente compara el resultado y envía PUT si
            // corrigió la fila provisional tras perder la primera respuesta.
            return anterior.dto()
        } }
        validar(lote,p)
        return labores.save(Labor(loteId=p.loteId,tipo=p.tipo,fecha=p.fecha,responsable=p.responsable,cantidad=p.cantidad,
            observacion=p.observacion,creadorId=u.id,claveCliente=idempotencia)).dto()
    }
    @PreAuthorize("hasAuthority('LABOR_EDITAR')")
    fun editar(id: Long,p: LaborPeticion): LaborRespuesta {
        // Bloquear el lote antes de leer la labor serializa edición/cierre.
        val lote=bloquear(p.loteId); val l=labor(id); val u=usuario()
        if(l.loteId!=p.loteId) throw FalloApi(400,"No puede mover una labor a otro lote",mapOf("loteId" to "Debe conservarse"))
        validar(lote,p)
        if(!u.rol.editaTodas && (l.creadorId!=u.id || !Instant.now().isBefore(l.registradaEn.plus(Duration.ofHours(24)))))
            throw FalloApi(403,"Solo puede corregir labores propias durante 24 horas (RN-02)")
        l.tipo=p.tipo; l.fecha=p.fecha; l.responsable=p.responsable; l.cantidad=p.cantidad; l.observacion=p.observacion
        return labores.save(l).dto()
    }
    @PreAuthorize("hasAuthority('LABOR_ELIMINAR')")
    fun eliminar(id: Long) { val l=labor(id); bloquear(l.loteId); labores.delete(l) }
    @PreAuthorize("hasAuthority('CAMPANIA_CERRAR')")
    fun cerrar(id: Long): LoteRespuesta {
        val lote=bloquear(id)
        if(!labores.existsByLoteIdAndTipo(id,TipoLabor.COSECHA)) throw FalloApi(409,"Se requiere al menos una Cosecha para cerrar (RN-03)")
        lote.campaniaCerrada=true; return lotes.save(lote).dto()
    }
}
