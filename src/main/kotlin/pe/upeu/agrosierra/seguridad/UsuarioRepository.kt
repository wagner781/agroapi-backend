package pe.upeu.agrosierra.seguridad
import org.springframework.data.jpa.repository.JpaRepository
interface UsuarioRepository : JpaRepository<Usuario, Long> { fun findByUsuario(usuario: String): Usuario? }
