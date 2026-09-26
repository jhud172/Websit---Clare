package co.uk.clarebrunton.ceremonies.security;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import co.uk.clarebrunton.ceremonies.config.IntegrationProperties;

@Service
public class TurnstileVerifier {
	private final IntegrationProperties properties;
	private final RestClient client;

	public TurnstileVerifier(IntegrationProperties properties) {
		this.properties = properties;
		this.client = RestClient.builder().baseUrl("https://challenges.cloudflare.com").build();
	}

	public boolean verify(String token, String remoteAddress) {
		if (!properties.getTurnstile().isEnabled()) return true;
		if (!StringUtils.hasText(properties.getTurnstile().getSecretKey()) || !StringUtils.hasText(token)) return false;
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> result = client.post().uri("/turnstile/v0/siteverify")
					.body(Map.of("secret", properties.getTurnstile().getSecretKey(), "response", token,
							"remoteip", remoteAddress == null ? "" : remoteAddress))
					.retrieve().body(Map.class);
			return result != null && Boolean.TRUE.equals(result.get("success"));
		} catch (RuntimeException unavailable) {
			return false;
		}
	}
}
