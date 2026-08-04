package co.uk.clarebrunton.ceremonies;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import co.uk.clarebrunton.ceremonies.config.SiteProperties;
import co.uk.clarebrunton.ceremonies.controller.SeoController;
import co.uk.clarebrunton.ceremonies.service.SiteUrlResolver;

class SeoControllerTest {

	@Test
	void sitemapUsesCanonicalPublicPagesOnly() {
		SiteProperties properties = new SiteProperties();
		properties.setBaseUrl("https://clareslifecelebrations.com");
		SeoController controller = new SeoController(properties, new SiteUrlResolver());
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setScheme("https");
		request.setServerName("clareslifecelebrations.com");
		request.setServerPort(443);

		String sitemap = controller.sitemap(request);

		assertThat(sitemap)
				.contains("https://clareslifecelebrations.com/celebrations-of-life")
				.contains("https://clareslifecelebrations.com/faq")
				.contains("https://clareslifecelebrations.com/blog/how-to-shape-a-celebration-of-life-tribute")
				.doesNotContain("https://clareslifecelebrations.com/funerals")
				.doesNotContain("how-to-shape-a-funeral-tribute");
	}
}
