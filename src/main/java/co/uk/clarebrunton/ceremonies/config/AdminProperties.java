package co.uk.clarebrunton.ceremonies.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin")
public class AdminProperties {

	private String username;
	private String passwordHash;
	private int maximumFailures = 5;
	private Duration lockDuration = Duration.ofMinutes(15);

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	public String getPasswordHash() { return passwordHash; }
	public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
	public int getMaximumFailures() { return maximumFailures; }
	public void setMaximumFailures(int maximumFailures) { this.maximumFailures = maximumFailures; }
	public Duration getLockDuration() { return lockDuration; }
	public void setLockDuration(Duration lockDuration) { this.lockDuration = lockDuration; }
}
