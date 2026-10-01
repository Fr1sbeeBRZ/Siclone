package fr1sbee.dev.siclone;

import dasniko.testcontainers.keycloak.KeycloakContainer;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Contenedores para los tests y para arrancar la aplicación en local con
 * {@link TestSicloneApplication}.
 * <p>
 * Las versiones de las imágenes coinciden con las de {@code compose.yaml}: si se
 * cambia una, hay que cambiar la otra.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	static final String POSTGRES_IMAGE = "postgres:18";

	static final String LGTM_IMAGE = "grafana/otel-lgtm:0.33.1";

	static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:26.7.5";

	static final String KEYCLOAK_REALM = "siclone";

	@Bean
	@ServiceConnection
	LgtmStackContainer grafanaLgtmContainer() {
		return new LgtmStackContainer(DockerImageName.parse(LGTM_IMAGE));
	}

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		// Mismo script de inicialización (extensiones) que en compose.yaml
		return new PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE))
			.withCopyFileToContainer(MountableFile.forHostPath("docker/postgres/init/01-init.sql"),
					"/docker-entrypoint-initdb.d/01-init.sql");
	}

	@Bean
	KeycloakContainer keycloakContainer() {
		// Mismo realm que en compose.yaml; el contenedor arranca siempre con --import-realm
		return new KeycloakContainer(KEYCLOAK_IMAGE).withCopyFileToContainer(
				MountableFile.forHostPath("docker/keycloak/realms/siclone-realm.json"),
				"/opt/keycloak/data/import/siclone-realm.json");
	}

	@Bean
	DynamicPropertyRegistrar keycloakProperties(KeycloakContainer keycloak) {
		return registry -> registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
				() -> keycloak.getAuthServerUrl() + "/realms/" + KEYCLOAK_REALM);
	}

}
