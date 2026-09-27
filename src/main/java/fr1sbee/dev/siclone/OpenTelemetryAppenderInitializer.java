package fr1sbee.dev.siclone;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Conecta el appender de Logback declarado en {@code logback-spring.xml} con la
 * instancia de {@link OpenTelemetry} que configura Spring Boot, para que los logs
 * se exporten por OTLP.
 */
@Component
class OpenTelemetryAppenderInitializer implements InitializingBean {

	private final OpenTelemetry openTelemetry;

	OpenTelemetryAppenderInitializer(OpenTelemetry openTelemetry) {
		this.openTelemetry = openTelemetry;
	}

	@Override
	public void afterPropertiesSet() {
		OpenTelemetryAppender.install(this.openTelemetry);
	}

}
