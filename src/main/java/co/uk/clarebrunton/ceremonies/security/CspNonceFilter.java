package co.uk.clarebrunton.ceremonies.security;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CspNonceFilter extends OncePerRequestFilter {

	private final SecureRandom random = new SecureRandom();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		byte[] bytes = new byte[18];
		random.nextBytes(bytes);
		String nonce = Base64.getEncoder().encodeToString(bytes);
		request.setAttribute("cspNonce", nonce);
		response.setHeader("Content-Security-Policy",
				"default-src 'self'; base-uri 'self'; object-src 'none'; frame-ancestors 'none'; "
				+ "form-action 'self'; img-src 'self' data: https:; font-src 'self'; style-src 'self'; "
				+ "script-src 'self' 'nonce-" + nonce + "' https://www.googletagmanager.com https://challenges.cloudflare.com; "
				+ "frame-src https://challenges.cloudflare.com; "
				+ "connect-src 'self' https://www.google-analytics.com https://region1.google-analytics.com https://challenges.cloudflare.com");
		chain.doFilter(request, response);
	}
}
