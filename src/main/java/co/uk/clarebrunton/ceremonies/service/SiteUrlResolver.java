package co.uk.clarebrunton.ceremonies.service;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class SiteUrlResolver {

	private static final Logger logger = LoggerFactory.getLogger(SiteUrlResolver.class);

	public String resolvePublicBaseUrl(HttpServletRequest request, String configuredBaseUrl) {
		String normalisedConfigured = normaliseBaseUrl(configuredBaseUrl);
		String requestBaseUrl = normaliseBaseUrl(extractRequestBaseUrl(request));

		if (!StringUtils.hasText(normalisedConfigured) && StringUtils.hasText(requestBaseUrl)) {
			return requestBaseUrl;
		}
		if (!StringUtils.hasText(requestBaseUrl)) {
			return StringUtils.hasText(normalisedConfigured) ? normalisedConfigured : "http://localhost:8081";
		}

		String configuredHost = extractHost(normalisedConfigured);
		String requestHost = extractHost(requestBaseUrl);
		if (StringUtils.hasText(configuredHost)
				&& StringUtils.hasText(requestHost)
				&& !configuredHost.equalsIgnoreCase(requestHost)) {
			if (isLocalHost(requestHost)) {
				logger.debug(
						"Ignoring SITE_BASE_URL host '{}' for local request host '{}'. Using request host instead.",
						configuredHost,
						requestHost
				);
			}
			else {
				logger.warn(
						"Ignoring SITE_BASE_URL host '{}' because it does not match request host '{}'. Using request host instead.",
						configuredHost,
						requestHost
				);
			}
			return requestBaseUrl;
		}

		return StringUtils.hasText(normalisedConfigured) ? normalisedConfigured : requestBaseUrl;
	}

	private String extractRequestBaseUrl(HttpServletRequest request) {
		if (request == null) {
			return null;
		}

		String scheme = firstHeaderValue(request, "X-Forwarded-Proto");
		if (!StringUtils.hasText(scheme)) {
			scheme = request.getScheme();
		}

		String hostHeader = firstHeaderValue(request, "X-Forwarded-Host");
		if (!StringUtils.hasText(hostHeader)) {
			hostHeader = request.getHeader("Host");
		}

		if (!StringUtils.hasText(hostHeader)) {
			if (!StringUtils.hasText(request.getServerName())) {
				return null;
			}
			int port = request.getServerPort();
			if (port > 0 && !isDefaultPort(scheme, port)) {
				hostHeader = request.getServerName() + ":" + port;
			}
			else {
				hostHeader = request.getServerName();
			}
		}

		String host = hostHeader.split(",")[0].trim();
		if (!StringUtils.hasText(host) || !StringUtils.hasText(scheme)) {
			return null;
		}
		return scheme + "://" + host;
	}

	private String firstHeaderValue(HttpServletRequest request, String headerName) {
		String value = request.getHeader(headerName);
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.split(",")[0].trim();
	}

	private boolean isDefaultPort(String scheme, int port) {
		if ("https".equalsIgnoreCase(scheme)) {
			return port == 443;
		}
		if ("http".equalsIgnoreCase(scheme)) {
			return port == 80;
		}
		return false;
	}

	private String normaliseBaseUrl(String baseUrl) {
		if (!StringUtils.hasText(baseUrl)) {
			return null;
		}
		return baseUrl.replaceAll("/+$", "");
	}

	private String extractHost(String baseUrl) {
		if (!StringUtils.hasText(baseUrl)) {
			return null;
		}
		try {
			URI uri = new URI(baseUrl);
			return uri.getHost();
		}
		catch (URISyntaxException exception) {
			logger.warn("Could not parse base URL '{}': {}", baseUrl, exception.getMessage());
			return null;
		}
	}

	private boolean isLocalHost(String host) {
		if (!StringUtils.hasText(host)) {
			return false;
		}
		return "localhost".equalsIgnoreCase(host)
				|| "127.0.0.1".equals(host)
				|| "::1".equals(host)
				|| "[::1]".equals(host);
	}

}