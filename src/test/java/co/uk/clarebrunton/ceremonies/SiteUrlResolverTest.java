package co.uk.clarebrunton.ceremonies;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import co.uk.clarebrunton.ceremonies.service.SiteUrlResolver;

class SiteUrlResolverTest {

	private final SiteUrlResolver resolver = new SiteUrlResolver();

	@Test
	void usesRequestHostWhenConfiguredHostIsDifferent() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setScheme("https");
		request.setServerName("www.clarebruntonlifeceremonies.com");
		request.setServerPort(443);

		String resolved = resolver.resolvePublicBaseUrl(request, "https://configured-host.example");

		assertThat(resolved).isEqualTo("https://www.clarebruntonlifeceremonies.com");
	}

	@Test
	void keepsConfiguredHostWhenItMatchesRequestHost() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("X-Forwarded-Proto", "https");
		request.addHeader("X-Forwarded-Host", "www.clarebruntonlifeceremonies.com");

		String resolved = resolver.resolvePublicBaseUrl(request, "https://www.clarebruntonlifeceremonies.com/");

		assertThat(resolved).isEqualTo("https://www.clarebruntonlifeceremonies.com");
	}

	@Test
	void usesRequestHostWhenConfiguredUrlIsMissing() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("X-Forwarded-Proto", "https");
		request.addHeader("X-Forwarded-Host", "example.com");

		String resolved = resolver.resolvePublicBaseUrl(request, null);

		assertThat(resolved).isEqualTo("https://example.com");
	}

	@Test
	void fallsBackToLocalhostWhenNoRequestOrConfiguredUrlExists() {
		String resolved = resolver.resolvePublicBaseUrl(null, "");

		assertThat(resolved).isEqualTo("http://localhost:8081");
	}

}
