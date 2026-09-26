package co.uk.clarebrunton.ceremonies.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.uk.clarebrunton.ceremonies.config.AdminProperties;

@Service
public class LoginAttemptService {

	private static final int MAX_CLIENTS = 5_000;
	private static final Duration INACTIVE_EXPIRY = Duration.ofHours(1);
	private final AdminProperties properties;
	private final Clock clock;
	private final byte[] salt = new byte[24];
	private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();
	private final AtomicInteger writes = new AtomicInteger();

	@Autowired
	public LoginAttemptService(AdminProperties properties) { this(properties, Clock.systemUTC()); }

	LoginAttemptService(AdminProperties properties, Clock clock) {
		this.properties = properties;
		this.clock = clock;
		new SecureRandom().nextBytes(salt);
	}

	public boolean isBlocked(String clientKey) {
		String key = hash(clientKey);
		Attempt attempt = attempts.get(key);
		if (attempt == null) return false;
		Instant now = clock.instant();
		if (attempt.lastAttemptAt().plus(INACTIVE_EXPIRY).isBefore(now)
				|| (attempt.blockedUntil() != null && !now.isBefore(attempt.blockedUntil()))) {
			attempts.remove(key, attempt);
			return false;
		}
		return attempt.blockedUntil() != null;
	}

	public void recordFailure(String clientKey) {
		Instant now = clock.instant();
		if (writes.incrementAndGet() % 32 == 0 || attempts.size() >= MAX_CLIENTS) removeExpired(now);
		String key = hash(clientKey);
		if (attempts.size() >= MAX_CLIENTS && !attempts.containsKey(key)) return;
		attempts.compute(key, (ignored, current) -> {
			int failures = current == null || current.lastAttemptAt().plus(INACTIVE_EXPIRY).isBefore(now) ? 1 : current.failures() + 1;
			Instant blockedUntil = failures >= properties.getMaximumFailures() ? now.plus(properties.getLockDuration()) : null;
			return new Attempt(failures, blockedUntil, now);
		});
	}

	public void clear(String clientKey) { attempts.remove(hash(clientKey)); }

	private void removeExpired(Instant now) {
		attempts.entrySet().removeIf(entry -> entry.getValue().lastAttemptAt().plus(INACTIVE_EXPIRY).isBefore(now));
	}

	private String hash(String value) {
		String normalised = value == null || value.isBlank() ? "unknown" : value.trim();
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(salt);
			return HexFormat.of().formatHex(digest.digest(normalised.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException("SHA-256 is unavailable", impossible);
		}
	}

	int trackedClientCount() { return attempts.size(); }

	private record Attempt(int failures, Instant blockedUntil, Instant lastAttemptAt) { }
}
