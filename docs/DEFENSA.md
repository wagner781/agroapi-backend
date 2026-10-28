# Recorrido para la defensa

## Seguridad

`AuthController.login` busca el usuario en H2 y verifica BCrypt; un hash no es
cifrado reversible. `JwtService` firma HS256 con un secreto aleatorio de al
menos 256 bits y expira el token en 3600 segundos. Para mantener sesiones tras
reiniciar se debe proporcionar `JWT_SECRET_BASE64` desde el entorno; nunca se
sube el secreto. El modo local genera una clave nueva por arranque.

`SecurityConfig` instala el filtro estándar `BearerTokenAuthenticationFilter`.
Se usa la implementación del framework para verificar firma, issuer y exp,
sin escribir un segundo filtro artesanal. El claim `sub` identifica al usuario
y `privilegios` se convierte en authorities sin prefijo. Todas las rutas excepto
POST login exigen autenticación. El decoder usa tolerancia temporal cero.

El servicio usa `@PreAuthorize("hasAuthority(...)")`. Los roles y su alcance
viven en `seguridad/Usuario.kt`; ningún controlador compara nombres de rol.
`editaTodas` permite extender los roles sin modificar la regla RN-02.
401 significa credencial ausente/inválida/vencida; 403 significa identidad
válida sin autorización. El administrador lee, pero no registra labores.
USUARIO_GESTIONAR se declara en la matriz; el contrato del examen no define
endpoints de administración de usuarios y por eso no se inventan.

## Reglas y persistencia

`CampoController` recibe DTO, valida formato y delega. `CampoService` realiza
RN-01 (campaña abierta), RN-02 (autor y 24 horas), RN-03 (existe cosecha) y RN-04
(dosis por hectáreas). Responsable es texto de negocio; creadorId es la
identidad del token y se usa para autorización. Cambiar responsable no cambia
la autoría. Las transacciones y el bloqueo del lote serializan registro,
corrección y cierre. Cantidades deben ser positivas y finitas.

JPA genera IDs positivos. No se devuelve directamente la entidad: `dto()`
selecciona el contrato público. `Pagina<T>` conserva metadatos, incluso al
filtrar por valle o campaniaCerrada. Se limita tamanio a 100 para evitar consultas
sin límite. Las labores se ordenan por fecha e id descendentes.

POST acepta el encabezado adicional opcional `Idempotency-Key`. Se guarda con
el identificador del usuario y devuelve la creación previa al reintentar; el
cliente mantiene la clave hasta confirmar. Esto cubre la pérdida de respuesta
después de que la transacción ya se confirmó. Si la fila local cambió entre
intentos, el cliente aplica PUT al ID recuperado antes de reemplazarla.

Las semillas se cargan si no existen. Hay ocho lotes en Mantaro, Sagrado y
Colca, dos cerrados y veinte labores. El lote 1 tiene cosecha; el 2 no. Las
fechas semilla son relativas al arranque y las fechas de auditoría son reales.
Los usuarios de demostración son tecnico1, jefe1 y admin: clave Agro2026*.
Estas credenciales son exclusivamente para el escenario local del examen.

## Demostración y límites

1. Ejecutar `gradlew.bat bootRun` y abrir la colección Postman en docs.
2. Mostrar 401 sin token y con firma alterada; iniciar sesión.
3. Probar paginación, registrar labor, editar y eliminar (204 sin cuerpo).
4. Con técnico intentar cerrar: 403. Con jefe cerrar lote 2: 409; lote 1: 200.
5. Registrar sobre lote cerrado: 409; fumigación excesiva: 400 por cantidad.
6. Ejecutar `gradlew.bat test` y mostrar los informes en build/reports/tests.

Los ensayos se ejecutan contra H2 en memoria con MockMvc y JWT reales. La base
del uso normal está en disco. No hay evidencia inventada de despliegue, de
sesiones anteriores ni de la solicitud de cambio todavía no asignada.

Referencias: [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html),
[Spring Boot 3.5.7](https://spring.io/blog/2025/10/23/spring-boot-3-5-7-available-now/).
