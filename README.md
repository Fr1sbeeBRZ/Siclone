# Gestión de Expedientes

Aplicación web para la **alta, tramitación y comunicación de expedientes**, con procedimientos, formularios, flujos y plantillas configurables desde la propia aplicación.

> 🚧 Proyecto en desarrollo.

---

## Funcionalidades

- **Alta de expedientes** mediante formularios dinámicos definidos por cada procedimiento.
- **Tramitación** con estados y transiciones configurables, controladas por rol.
- **Gestión de procedimientos**: alta de nuevos procedimientos con sus formularios y flujos, con versionado. Los expedientes abiertos no se ven afectados por los cambios.
- **Gestión de roles y permisos**, a nivel de rol y a nivel de expediente.
- **Plantillas de comunicación** en HTML, editables desde la aplicación y versionadas.
- **Generación de documentos** HTML → PDF/A, inmutables y con hash de integridad.
- **Gestión de firmantes** y firma de documentos (PAdES).
- **Comunicaciones al cliente** con registro de envío y acuse.
- **Auditoría completa** del historial de cada expediente.
- **Observabilidad**: logs, métricas y trazas en Grafana.

---

## Stack tecnológico

| Capa | Tecnología |
| --- | --- |
| Backend | Java 25, Spring Boot 4, Spring Modulith |
| Base de datos | PostgreSQL (`jsonb` para datos dinámicos), Flyway, Hibernate Envers |
| Seguridad | Keycloak (OIDC), Spring Security (OAuth2 Resource Server) |
| Documentos | Handlebars (plantillas), OpenHTMLtoPDF (PDF/A), EU DSS (firma PAdES) |
| Almacenamiento | MinIO / S3 |
| API | REST + OpenAPI (springdoc) |
| Frontend | SvelteKit (SPA) + Svelte 5 + TypeScript, TipTap |
| Observabilidad | OpenTelemetry, Grafana Alloy, Loki, Tempo, Mimir, Grafana |
| Infraestructura | Docker, Docker Compose |

---

## Arquitectura

El backend es un **monolito modular** construido con [Spring Modulith](https://docs.spring.io/spring-modulith/reference/). Cada módulo de negocio tiene fronteras explícitas, verificadas automáticamente en los tests, y los módulos se comunican mediante **eventos de dominio persistidos**. Si un módulo falla, sus eventos quedan pendientes y se reprocesan sin perder información.

| Módulo | Responsabilidad |
| --- | --- |
| `expediente` | Alta, datos, estado e historial de expedientes |
| `procedimiento` | Definiciones versionadas de formularios y flujos de tramitación |
| `plantilla` | Plantillas HTML de comunicación y layout común |
| `documento` | Generación y almacenamiento de PDFs |
| `firma` | Firmantes y proceso de firma |
| `comunicacion` | Envíos al cliente y acuses |
| `seguridad` | Usuarios, unidades, roles y permisos |

```
expediente ──(ExpedienteCambioEstado)──► documento ──(DocumentoGenerado)──► firma
                                                                              │
comunicacion ◄───────────────────────(DocumentoFirmado)───────────────────────┘
```

---

## Estructura del repositorio

```
.
├── backend/                  # Spring Boot + Spring Modulith
│   ├── src/main/java/.../
│   │   ├── expediente/
│   │   ├── procedimiento/
│   │   ├── plantilla/
│   │   ├── documento/
│   │   ├── firma/
│   │   ├── comunicacion/
│   │   └── seguridad/
│   └── pom.xml
├── frontend/                 # SvelteKit (SPA)
├── compose.yaml              # Servicios para desarrollo local
└── README.md
```

---

## Requisitos

- Java 25
- Node.js (LTS)
- Docker y Docker Compose

---

## Puesta en marcha en local

### 1. Clonar el repositorio

```bash
git git@github.com:Fr1sbeeBRZ/Siclone.git
cd Siclone
```

### 2. Arrancar el backend

```bash
cd backend
./mvnw spring-boot:run
```

Gracias a *Docker Compose Support*, Spring Boot levanta automáticamente los servicios definidos en `compose.yaml`:

| Servicio | URL |
| --- | --- |
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Keycloak | http://localhost:8180 |
| MinIO (consola) | http://localhost:9001 |
| Grafana | http://localhost:3000 |

> Ajusta los puertos si tu `compose.yaml` usa otros.

### 3. Arrancar el frontend

```bash
cd frontend
npm install
npm run dev
```

La aplicación queda disponible en http://localhost:5173.

---

## Tests

```bash
cd backend
./mvnw verify
```

Incluye:

- **Verificación de la arquitectura modular** (`ApplicationModules.verify()`): falla si un módulo accede a partes internas de otro o si hay dependencias cíclicas.
- **Tests por módulo** con `@ApplicationModuleTest`.
- **Tests de integración** con Testcontainers (PostgreSQL, Keycloak).

La documentación de los módulos (diagramas PlantUML) se genera en `backend/target/spring-modulith-docs`.

---

## Observabilidad

La aplicación exporta logs, métricas y trazas por OTLP. En local, el contenedor `grafana/otel-lgtm` incluye el colector y el stack completo de Grafana.

- **Logs** → Loki, en JSON estructurado y con el `expedienteId` en el contexto.
- **Métricas** → Mimir: técnicas y de negocio (expedientes por procedimiento y estado, generación de documentos, comunicaciones).
- **Trazas** → Tempo, con el recorrido de cada petición a través de los módulos.

> Los logs no contienen datos personales. El historial legal de los expedientes se guarda en base de datos.

---

## Documentación

- [Spring Modulith](https://docs.spring.io/spring-modulith/reference/)
- [Spring Boot](https://docs.spring.io/spring-boot/)
- [OpenTelemetry con Spring Boot](https://spring.io/blog/2025/11/18/opentelemetry-with-spring-boot/)
- [Grafana Alloy](https://grafana.com/docs/alloy/latest/)
- [SvelteKit](https://svelte.dev/docs/kit)

---

## Licencia

Pendiente de definir.
