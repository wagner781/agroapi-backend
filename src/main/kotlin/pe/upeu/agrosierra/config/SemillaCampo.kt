package pe.upeu.agrosierra.config
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pe.upeu.agrosierra.dominio.*
import pe.upeu.agrosierra.repositorio.*
import pe.upeu.agrosierra.seguridad.*
import org.springframework.core.annotation.Order
import java.time.*

@Component @Order(2)
class SemillaCampo(private val lotes: LoteRepository,private val labores: LaborRepository,private val usuarios: UsuarioRepository): CommandLineRunner {
    @Transactional override fun run(vararg args: String) {
        if(lotes.count()>0) return
        val tecnico=usuarios.findByUsuario("tecnico1") ?: error("Semilla de usuarios no cargada")
        val valles=listOf("Mantaro","Sagrado","Colca")
        (1..8).forEach { n ->
            val l=lotes.save(Lote(codigo="LOT-${n.toString().padStart(3,'0')}",nombre="Chacra $n",hectareas=2.0+n,
                cultivo=if(n%2==0) "Maiz" else "Papa",valle=valles[(n-1)%3],campaniaCerrada=n>=7,dosisMaximaPorHectarea=3.0))
            // 4 lotes con tres labores y 4 con dos: 20 labores; el lote 2 no tiene Cosecha.
            val cantidad=if(n<=4) 3 else 2
            repeat(cantidad) { i ->
                val tipo=if(n==1 && i==2) TipoLabor.COSECHA else if(i==1) TipoLabor.FUMIGACION else TipoLabor.RIEGO
                labores.save(Labor(loteId=l.id,tipo=tipo,fecha=LocalDate.now().minusDays(i.toLong()),responsable=tecnico.nombre,
                    cantidad=1.0+i,observacion="Dato semilla de demostracion",creadorId=tecnico.id,registradaEn=Instant.now()))
            }
        }
    }
}
