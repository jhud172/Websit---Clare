package co.uk.clarebrunton.ceremonies.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integrations")
public class IntegrationProperties {
	private final Feature calendar = new Feature();
	private final Feature googleReviews = new Feature();
	private final Turnstile turnstile = new Turnstile();
	public Feature getCalendar() { return calendar; }
	public Feature getGoogleReviews() { return googleReviews; }
	public Turnstile getTurnstile() { return turnstile; }

	public static class Feature {
		private boolean enabled;
		public boolean isEnabled() { return enabled; }
		public void setEnabled(boolean enabled) { this.enabled = enabled; }
	}

	public static class Turnstile extends Feature {
		private String siteKey;
		private String secretKey;
		public String getSiteKey() { return siteKey; }
		public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
		public String getSecretKey() { return secretKey; }
		public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
	}
}
