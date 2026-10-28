package pe.upeu.agrosierra.config
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pe.upeu.agrosierra.seguridad.*
@Component
class SemillaUsuarios(private val usuarios: UsuarioRepository, private val encoder: BCryptPasswordEncoder) : CommandLineRunner {
    override fun run(vararg args: String) {
        listOf(Triple("tecnico1","Tecnico de campo",Rol.TECNICO),Triple("jefe1","Jefe de campo",Rol.JEFE_CAMPO),Triple("admin","Administrador",Rol.ADMINISTRADOR)).forEach { (login,nombre,rol) ->
            if (usuarios.findByUsuario(login)==null) usuarios.save(Usuario(usuario=login,nombre=nombre,passwordHash=encoder.encode("Agro2026*"),rol=rol))
        }
    }
}
