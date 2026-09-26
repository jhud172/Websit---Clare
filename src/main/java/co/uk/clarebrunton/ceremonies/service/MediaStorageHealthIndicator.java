package co.uk.clarebrunton.ceremonies.service;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import co.uk.clarebrunton.ceremonies.config.StorageProperties;

@Component("mediaStorage")
public class MediaStorageHealthIndicator implements HealthIndicator {

	private final StorageProperties properties;

	public MediaStorageHealthIndicator(StorageProperties properties) {
		this.properties = properties;
	}

	@Override
	public Health health() {
		String provider = properties.getProvider();
		if ("database".equalsIgnoreCase(provider)) {
			return Health.up().withDetail("provider", "database").build();
		}
		if ("s3".equalsIgnoreCase(provider)
				&& hasText(properties.getBucket()) && hasText(properties.getAccessKey()) && hasText(properties.getSecretKey())) {
			return Health.up().withDetail("provider", "s3").build();
		}
		return Health.down().withDetail("reason", "media storage configuration is incomplete").build();
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
