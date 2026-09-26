package co.uk.clarebrunton.ceremonies.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("retention")
public class RetentionProperties {
	private boolean automaticDeletionEnabled;
	private Duration inquiries = Duration.ofDays(1095);
	private Duration reviews = Duration.ofDays(1825);
	private Duration analytics = Duration.ofDays(730);
	private Duration assets = Duration.ofDays(1095);
	public boolean isAutomaticDeletionEnabled() { return automaticDeletionEnabled; }
	public void setAutomaticDeletionEnabled(boolean value) { automaticDeletionEnabled = value; }
	public Duration getInquiries() { return inquiries; }
	public void setInquiries(Duration value) { inquiries = value; }
	public Duration getReviews() { return reviews; }
	public void setReviews(Duration value) { reviews = value; }
	public Duration getAnalytics() { return analytics; }
	public void setAnalytics(Duration value) { analytics = value; }
	public Duration getAssets() { return assets; }
	public void setAssets(Duration value) { assets = value; }
}
