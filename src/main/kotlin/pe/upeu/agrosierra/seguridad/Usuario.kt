package pe.upeu.agrosierra.seguridad

import jakarta.persistence.*

enum class Privilegio { LOTE_LEER, LABOR_LEER, LABOR_CREAR, LABOR_EDITAR, LABOR_ELIMINAR, CAMPANIA_CERRAR, USUARIO_GESTIONAR }
// El alcance de edición es configuración, no una comparación del nombre del rol.
enum class Rol(val privilegios: Set<Privilegio>, val editaTodas: Boolean) {
    TECNICO(setOf(Privilegio.LOTE_LEER, Privilegio.LABOR_LEER, Privilegio.LABOR_CREAR, Privilegio.LABOR_EDITAR), false),
    JEFE_CAMPO(setOf(Privilegio.LOTE_LEER, Privilegio.LABOR_LEER, Privilegio.LABOR_CREAR, Privilegio.LABOR_EDITAR, Privilegio.LABOR_ELIMINAR, Privilegio.CAMPANIA_CERRAR), true),
    ADMINISTRADOR(setOf(Privilegio.LOTE_LEER, Privilegio.LABOR_LEER, Privilegio.USUARIO_GESTIONAR), false)
}
@Entity
class Usuario(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    @Column(unique = true) var usuario: String = "",
    var nombre: String = "",
    var passwordHash: String = "",
    @Enumerated(EnumType.STRING) var rol: Rol = Rol.TECNICO
)
