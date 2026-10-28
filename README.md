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
