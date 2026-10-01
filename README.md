# Siclone · Gestión de Expedientes

Aplicación web para la **alta, tramitación y comunicación de expedientes**, con procedimientos, formularios, flujos y plantillas configurables desde la propia aplicación.

> 🚧 **Proyecto en fase inicial.** Por ahora el repositorio contiene la base del backend (dependencias, configuración, base de datos y observabilidad). Las funcionalidades de negocio, los módulos y el frontend todavía no están implementados.

---

## Estado actual

| Pieza | Estado |
| --- | --- |
| Proyecto Spring Boot (Maven, en la raíz del repositorio) | ✅ Creado |
| PostgreSQL + Flyway (tabla de eventos de Spring Modulith) | ✅ Configurado |
| Observabilidad (OpenTelemetry → Grafana LGTM) | ✅ Configurado |
| Seguridad como OAuth2 Resource Server (JWT de Keycloak) | ✅ Configurado |
| Keycloak en `compose.yaml` (realm `siclone` con cliente y usuario de desarrollo) | ✅ Configurado |
| Tests de integración con Testcontainers (PostgreSQL, LGTM, Keycloak) | ✅ Test de arranque del contexto |
| MinIO y servidor SMTP en `compose.yaml` | ⏳ Pendiente |
| Módulos de negocio (`expediente`, `procedimiento`, …) | ⏳ Pendiente |
| Frontend (SvelteKit) | ⏳ Pendiente |

---

## Funcionalidades previstas

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

Dependencias ya incluidas en el `pom.xml`:

| Capa | Tecnología |
| --- | --- |
| Backend | Java 25, Spring Boot 4.1, Spring Modulith 2.1 |
| Base de datos | PostgreSQL 18 (`jsonb` para datos dinámicos), Flyway, Hibernate Envers |
| Seguridad | Keycloak (OIDC), Spring Security (OAuth2 Resource Server) |
| Documentos | Handlebars (plantillas), OpenHTMLtoPDF (PDF/A), EU DSS 6.5 (firma PAdES) |
| Almacenamiento | MinIO / S3 |
| Email | Spring Mail (SMTP) |
| API | REST + OpenAPI (springdoc) |
| Observabilidad | OpenTelemetry (trazas, métricas y logs por OTLP), Grafana LGTM |
| Infraestructura | Docker, Docker Compose, Testcontainers |

Previsto para el frontend: SvelteKit (SPA) + Svelte 5 + TypeScript, TipTap.

---

## Arquitectura prevista

El backend será un **monolito modular** construido con [Spring Modulith](https://docs.spring.io/spring-modulith/reference/). Cada módulo de negocio tendrá fronteras explícitas, verificadas automáticamente en los tests, y los módulos se comunicarán mediante **eventos de dominio persistidos** (tabla `event_publication`, ya creada). Si un módulo falla, sus eventos quedan pendientes y se reprocesan sin perder información.

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

Cada módulo será un subpaquete de `fr1sbee.dev.siclone` (por ejemplo, `fr1sbee.dev.siclone.expediente`).

---

## Estructura del repositorio

```
.
├── src/
│   ├── main/
│   │   ├── java/fr1sbee/dev/siclone/
│   │   │   ├── SicloneApplication.java
│   │   │   ├── SecurityConfiguration.java              # JWT de Keycloak, rutas públicas
│   │   │   └── OpenTelemetryAppenderInitializer.java   # envía los logs por OTLP
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── logback-spring.xml
│   │       └── db/migration/                           # migraciones Flyway (V1__..., V2__...)
│   └── test/
│       ├── java/fr1sbee/dev/siclone/
│       │   ├── SicloneApplicationTests.java
│       │   ├── TestSicloneApplication.java             # arranque local con Testcontainers
│           └── TestcontainersConfiguration.java
├── docker/
│   ├── postgres/init/01-init.sql                       # extensiones y BD de Keycloak (solo 1ª vez)
│   └── keycloak/realms/siclone-realm.json              # realm importado en compose y en los tests
├── compose.yaml                                        # servicios para desarrollo local
├── .env.example                                        # variables para compose.yaml
├── pom.xml
└── mvnw, mvnw.cmd                                      # Maven Wrapper
```

---

## Requisitos

- Java 25
- Docker y Docker Compose

No hace falta instalar Maven: se usa el wrapper (`./mvnw`).

---

## Puesta en marcha en local

### 1. Clonar el repositorio

```bash
git clone git@github.com:Fr1sbeeBRZ/Siclone.git
cd Siclone
```

### 2. Crear el fichero `.env`

`compose.yaml` lee las contraseñas de PostgreSQL y del administrador de Keycloak de un fichero `.env` (ignorado por git):

```bash
cp .env.example .env
```

### 3. Arrancar la aplicación

```bash
./mvnw spring-boot:run
```

Gracias a *Docker Compose Support*, Spring Boot levanta automáticamente los servicios de `compose.yaml` y se conecta a ellos sin configuración adicional:

| Servicio | URL / puerto |
| --- | --- |
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Actuator | http://localhost:8080/actuator |
| PostgreSQL | `localhost:5432` (base de datos y usuario `siclone`) |
| Keycloak | http://localhost:8180 (consola: admin / `KEYCLOAK_ADMIN_PASSWORD`) |
| Grafana | http://localhost:3000 (admin / admin) |
| OTLP (gRPC / HTTP) | `localhost:4317` / `localhost:4318` |

Al arrancar, Flyway aplica las migraciones de `src/main/resources/db/migration`.

> La primera vez que se crea el volumen de PostgreSQL se ejecuta `docker/postgres/init/01-init.sql`, que instala las extensiones `pgcrypto`, `unaccent` y `pg_trgm` y crea la base de datos `keycloak`. Si el volumen ya existía, el script no se vuelve a ejecutar y Keycloak no arrancará por falta de su base de datos. Tienes dos opciones:
>
> - Borrar el volumen (se pierden los datos locales): `docker compose down -v`
> - Crear solo la base de datos: `docker compose up -d postgres && docker compose exec postgres psql -U siclone -c "CREATE DATABASE keycloak"`

> **Seguridad:** la API está protegida como OAuth2 Resource Server y espera JWT emitidos por Keycloak (`http://localhost:8180/realms/siclone` por defecto, configurable con `KEYCLOAK_ISSUER_URI`). Swagger UI, `/actuator/health` y `/actuator/info` son públicos; el resto exige token.

#### Keycloak en local

Al arrancar, Keycloak importa `docker/keycloak/realms/siclone-realm.json` si el realm `siclone` todavía no existe. Los cambios hechos después desde la consola se guardan en la base de datos `keycloak` y **no** se sobrescriben al reiniciar; para volver al realm del fichero, borra el realm desde la consola y reinicia el contenedor.

El realm incluye, **solo para desarrollo**:

| Elemento | Valor |
| --- | --- |
| Cliente público | `siclone-frontend` (Authorization Code + PKCE; redirecciones a `localhost:5173` y Swagger UI) |
| Usuario | `dev` / `dev` |

Para obtener un token y llamar a la API desde la terminal:

```bash
TOKEN=$(curl -s -X POST http://localhost:8180/realms/siclone/protocol/openid-connect/token \
  -d grant_type=password -d client_id=siclone-frontend -d username=dev -d password=dev | jq -r .access_token)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/actuator/metrics
```

> El cliente tiene activado el *password grant* solo para poder pedir tokens así en local. No reutilices este realm fuera de desarrollo.

#### Alternativa: arrancar con Testcontainers

```bash
./mvnw spring-boot:test-run
```

Usa `TestSicloneApplication`, que levanta PostgreSQL, Grafana LGTM y Keycloak (con el mismo realm `siclone`) como contenedores efímeros en puertos aleatorios. Los datos se pierden al parar.

---

## Configuración

Todo está en `src/main/resources/application.properties`. Los valores por defecto sirven para local y se pueden sobrescribir con variables de entorno:

| Variable | Uso | Valor por defecto |
| --- | --- | --- |
| `POSTGRES_PASSWORD` | Contraseña de PostgreSQL en `compose.yaml` (fichero `.env`) | — |
| `KEYCLOAK_ADMIN_PASSWORD` | Contraseña del usuario `admin` de Keycloak en `compose.yaml` (fichero `.env`) | — |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Base de datos fuera de local | La aporta Docker Compose |
| `KEYCLOAK_ISSUER_URI` | Emisor de los JWT | `http://localhost:8180/realms/siclone` |
| `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET` | Almacenamiento de documentos | `http://localhost:9000`, `minioadmin`, `minioadmin`, `siclone-documentos` |
| `MAIL_HOST`, `MAIL_PORT` | Servidor SMTP | `localhost`, `1025` |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | Destino OTLP fuera de local | Lo aporta Docker Compose |

---

## Tests

```bash
./mvnw verify
```

Requiere Docker. Por ahora incluye `SicloneApplicationTests`, que arranca el contexto completo contra PostgreSQL, Grafana LGTM y Keycloak en Testcontainers y comprueba que las migraciones de Flyway y las entidades JPA coinciden (`ddl-auto=validate`).

Previsto a medida que se añadan módulos:

- **Verificación de la arquitectura modular** (`ApplicationModules.verify()`): fallará si un módulo accede a partes internas de otro o si hay dependencias cíclicas.
- **Tests por módulo** con `@ApplicationModuleTest`.
- **Documentación de los módulos** (diagramas PlantUML) generada en `target/spring-modulith-docs`.

---

## Observabilidad

La aplicación exporta logs, métricas y trazas por OTLP. En local, el contenedor `grafana/otel-lgtm` incluye el colector y el stack completo de Grafana (Loki, Tempo, Prometheus/Mimir).

- **Logs** → Loki, mediante el appender de OpenTelemetry (`logback-spring.xml`). También se muestran por consola.
- **Métricas** → técnicas de Spring Boot y de Spring Modulith.
- **Trazas** → Tempo, con el recorrido de cada petición. En local se muestrean todas (`management.tracing.sampling.probability=1.0`).

> Los logs no deben contener datos personales. El historial legal de los expedientes se guardará en base de datos.

---

## Documentación

- [Spring Modulith](https://docs.spring.io/spring-modulith/reference/)
- [Spring Boot](https://docs.spring.io/spring-boot/)
- [OpenTelemetry con Spring Boot](https://spring.io/blog/2025/11/18/opentelemetry-with-spring-boot/)
- [Grafana otel-lgtm](https://github.com/grafana/docker-otel-lgtm)
- [Testcontainers Keycloak](https://github.com/dasniko/testcontainers-keycloak)

---

## Licencia

Pendiente de definir.
