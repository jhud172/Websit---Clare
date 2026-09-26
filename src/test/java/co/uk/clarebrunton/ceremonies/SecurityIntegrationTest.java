package co.uk.clarebrunton.ceremonies;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "server.servlet.session.cookie.secure=true")
class SecurityIntegrationTest {

	@Autowired
	private WebApplicationContext context;
	@Autowired
	private org.springframework.boot.web.server.autoconfigure.ServerProperties serverProperties;

	private MockMvc mvc;

	@BeforeEach
	void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity())
				.build();
	}

	@Test
	void dashboardRequiresAuthentication() throws Exception {
		mvc.perform(get("/reviews/admin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/reviews/admin/login"));
	}

	@Test
	void authenticatedAdminMutationRequiresCsrfToken() throws Exception {
		mvc.perform(post("/reviews/admin/inquiries/1")
				.with(user("clare").roles("ADMIN"))
				.param("status", "CONTACTED"))
				.andExpect(status().isForbidden());
	}

	@Test
	void publicPagesReceiveSecurityHeaders() throws Exception {
		mvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(header().string("X-Content-Type-Options", "nosniff"))
				.andExpect(header().exists("Content-Security-Policy"))
				.andExpect(header().exists("Referrer-Policy"));
	}

	@Test
	void loginSessionCookieIsSecureAndAdminResponsesAreNotCached() throws Exception {
		assertThat(serverProperties.getServlet().getSession().getCookie().getSecure()).isTrue();
		assertThat(serverProperties.getServlet().getSession().getCookie().getHttpOnly()).isTrue();
		assertThat(serverProperties.getServlet().getSession().getCookie().getSameSite().name()).isEqualTo("LAX");
		mvc.perform(get("/reviews/admin").with(user("clare").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-cache")));
	}
}
