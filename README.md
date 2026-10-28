# AgroAPI

Backend propio de AgroSierra con Kotlin, Spring Boot, Spring Security y JPA.

## Ejecutar

Requisitos: JDK 21 e Internet para la primera descarga de Gradle/dependencias.
En Windows: `./gradlew.bat bootRun`. En Linux/macOS: `./gradlew bootRun`.
Dirección: `http://localhost:8080/api/v1`.
La base H2 persiste en `data/` (ignorada por Git). `DB_URL`, `DB_USER`,
`DB_PASSWORD` y `PORT` permiten configurar otro entorno.

## Desarrollo

`main` conserva entregas estables; `develop` integra PR de ramas `feature/...`.
Se registra un único commit raíz de infraestructura para iniciar Git. Las
funcionalidades nacen de develop y vuelven mediante PR con merge commit.
Los commits usan el reloj real. Este repositorio se inicia el 28 de octubre de
2026; no acredita trabajo en sesiones anteriores a su creación.

## Plan de implementación

1. Seguridad JWT, usuarios cifrados, privilegios y errores uniformes.
2. Persistencia, semillas, CRUD paginado y reglas RN-01 a RN-04.
3. Pruebas del contrato, colección API y guía de defensa.

La solicitud de cambio del examen requiere asignación del docente. No se
inventa una solicitud, una evaluación, fechas anteriores ni evidencias.

## Contrato y arquitectura

Paquetes: `config` (seguridad y errores), `seguridad` (usuarios/matriz),
`dominio` (entidades), `repositorio` (JPA), `servicio` (reglas/transacciones),
`web/dto` (contrato) y `web` (HTTP). Detalles y recorrido: [guía de defensa](docs/DEFENSA.md).

| Método | Ruta bajo /api/v1 | Privilegio |
|---|---|---|
| POST | /auth/login | Público |
| GET | /usuarios/me | Autenticado |
| GET | /lotes y /lotes/{id} | LOTE_LEER |
| GET | /lotes/{id}/labores | LABOR_LEER |
| POST | /labores | LABOR_CREAR |
| PUT | /labores/{id} | LABOR_EDITAR |
| DELETE | /labores/{id} | LABOR_ELIMINAR |
| POST | /lotes/{id}/cerrar-campania | CAMPANIA_CERRAR |

Listados: `pagina`, `tamanio`; lotes también `valle`, `campaniaCerrada`.
Login: `{ "usuario": "tecnico1", "password": "Agro2026*" }`.
Los tres usuarios semilla son `tecnico1`, `jefe1` y `admin`, con la misma clave.
Técnico lee/crea y edita propias en 24 h; jefe lee/crea/edita/elimina/cierra;
administrador lee y posee USUARIO_GESTIONAR. La firma JWT se verifica en Spring
Security; los privilegios se verifican en el servicio.

Pruebas: `./gradlew.bat build`. Colección: `docs/AgroAPI.postman_collection.json`.
