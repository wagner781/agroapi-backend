package pe.upeu.agrosierra.seguridad

import org.springframework.web.bind.annotation.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import pe.upeu.agrosierra.config.*
import pe.upeu.agrosierra.web.dto.*

@RestController @RequestMapping("/api/v1")
class AuthController(private val usuarios: UsuarioRepository, private val encoder: BCryptPasswordEncoder, private val jwt: JwtService) {
    private val dummyHash = encoder.encode("credencial-inexistente")
    @PostMapping("/auth/login")
    fun login(@RequestBody p: LoginPeticion): SesionRespuesta {
        val u = usuarios.findByUsuario(p.usuario)
        val coincide = encoder.matches(p.password, u?.passwordHash ?: dummyHash)
        if (u == null || !coincide) throw FalloApi(401,"Credenciales incorrectas")
        return SesionRespuesta(jwt.emitir(u),u.usuario,u.nombre,u.rol.name,u.rol.privilegios.map { it.name }.toSet())
    }
    @GetMapping("/usuarios/me")
    fun me(@AuthenticationPrincipal token: Jwt): UsuarioRespuesta {
        val u = usuarios.findById(token.subject.toLong()).orElseThrow { FalloApi(401,"Usuario inexistente") }
        return UsuarioRespuesta(u.id,u.usuario,u.nombre,u.rol.name,u.rol.privilegios.map { it.name }.toSet())
    }
}
