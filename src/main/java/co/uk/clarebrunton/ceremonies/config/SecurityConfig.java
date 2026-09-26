package co.uk.clarebrunton.ceremonies.config;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import co.uk.clarebrunton.ceremonies.security.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	UserDetailsService adminUsers(AdminProperties properties, PasswordEncoder passwordEncoder) {
		String username = hasText(properties.getUsername()) ? properties.getUsername().trim() : "admin-disabled";
		String passwordHash = isBcryptHash(properties.getPasswordHash())
				? properties.getPasswordHash().trim()
				: passwordEncoder.encode(UUID.randomUUID().toString());
		return new InMemoryUserDetailsManager(User.withUsername(username)
				.password(passwordHash)
				.roles("ADMIN")
				.build());
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
			LoginAttemptService loginAttempts,
			AdminProperties adminProperties) throws Exception {
			http
			.authorizeHttpRequests(authorize -> authorize
					.requestMatchers("/reviews/admin/login", "/error", "/css/**", "/js/**", "/images/**", "/fonts/**", "/*.png", "/actuator/health/**", "/livez", "/readyz").permitAll()
					.requestMatchers("/actuator/**").hasRole("ADMIN")
					.requestMatchers("/reviews/admin/**", "/dashboard/**").hasRole("ADMIN")
					.anyRequest().permitAll())
			.formLogin(login -> login
					.loginPage("/reviews/admin/login")
					.loginProcessingUrl("/reviews/admin/login")
					.successHandler(successHandler(loginAttempts))
					.failureHandler(failureHandler(loginAttempts, adminProperties)))
			.logout(logout -> logout
					.logoutUrl("/reviews/admin/logout")
					.logoutSuccessUrl("/reviews/admin/login?logout")
					.invalidateHttpSession(true)
					.deleteCookies("JSESSIONID"))
			.sessionManagement(session -> session
					.sessionFixation(fixation -> fixation.migrateSession())
					.maximumSessions(2))
			.headers(headers -> headers
					.contentTypeOptions(options -> { })
					.referrerPolicy(policy -> policy.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
					.permissionsPolicyHeader(policy -> policy.policy("camera=(), microphone=(), geolocation=(), payment=()"))
					.contentSecurityPolicy(csp -> csp.policyDirectives(
							"default-src 'self'; base-uri 'self'; object-src 'none'; frame-ancestors 'none'; "
							+ "form-action 'self'; img-src 'self' data: https:; font-src 'self'; style-src 'self'; "
							+ "script-src 'self' https://www.googletagmanager.com https://challenges.cloudflare.com; "
							+ "frame-src https://challenges.cloudflare.com; connect-src 'self' https://www.google-analytics.com https://region1.google-analytics.com https://challenges.cloudflare.com"))
					.httpStrictTransportSecurity(hsts -> hsts
							.includeSubDomains(true)
							.maxAgeInSeconds(31_536_000)));
		return http.build();
	}

	private AuthenticationSuccessHandler successHandler(LoginAttemptService attempts) {
		return (request, response, authentication) -> {
			attempts.clear(clientKey(request));
			response.sendRedirect("/reviews/admin");
		};
	}

	private AuthenticationFailureHandler failureHandler(LoginAttemptService attempts, AdminProperties properties) {
		return (request, response, exception) -> {
			String key = clientKey(request);
			if (attempts.isBlocked(key)) {
				response.sendRedirect("/reviews/admin/login?locked");
				return;
			}
			attempts.recordFailure(key);
			boolean nowBlocked = attempts.isBlocked(key);
			response.sendRedirect("/reviews/admin/login?" + (nowBlocked ? "locked" : "error"));
		};
	}

	private String clientKey(HttpServletRequest request) {
		return request.getRemoteAddr();
	}

	private boolean isBcryptHash(String value) {
		return hasText(value) && value.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
