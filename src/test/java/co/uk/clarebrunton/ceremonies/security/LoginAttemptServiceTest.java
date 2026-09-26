package co.uk.clarebrunton.ceremonies.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import co.uk.clarebrunton.ceremonies.config.AdminProperties;

class LoginAttemptServiceTest {
	@Test void blocksOnlyTheFailingClientAndCanBeCleared() {
		AdminProperties properties = new AdminProperties();
		properties.setMaximumFailures(3);
		LoginAttemptService service = new LoginAttemptService(properties);
		service.recordFailure("198.51.100.10");
		service.recordFailure("198.51.100.10");
		assertThat(service.isBlocked("198.51.100.10")).isFalse();
		service.recordFailure("198.51.100.10");
		assertThat(service.isBlocked("198.51.100.10")).isTrue();
		assertThat(service.isBlocked("198.51.100.11")).isFalse();
		service.clear("198.51.100.10");
		assertThat(service.isBlocked("198.51.100.10")).isFalse();
	}
}
