package co.uk.clarebrunton.ceremonies.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PublicSubmissionGuard {

	private static final int MAX_CLIENTS = 10_000;
	private static final Duration MAX_WINDOW = Duration.ofHours(1);
	private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();
	private final AtomicInteger checks = new AtomicInteger();
	private final byte[] salt = new byte[24];
	private final Clock clock;
	private final TurnstileVerifier turnstile;

	@Autowired
	public PublicSubmissionGuard(TurnstileVerifier turnstile) { this(Clock.systemUTC(), turnstile); }

	PublicSubmissionGuard(Clock clock) { this(clock, null); }

	PublicSubmissionGuard(Clock clock, TurnstileVerifier turnstile) {
		this.clock = clock;
		this.turnstile = turnstile;
		new SecureRandom().nextBytes(salt);
	}

	public void checkInquiry(HttpServletRequest request, String honeypot, long formStartedAt) {
		checkInquiry(request, honeypot, formStartedAt, null);
	}

	public void checkInquiry(HttpServletRequest request, String honeypot, long formStartedAt, String turnstileResponse) {
		checkHuman(honeypot, formStartedAt);
		checkTurnstile(request, turnstileResponse);
		checkRate("inquiry:" + clientKey(request), 5, Duration.ofMinutes(15));
	}

	public void checkReview(HttpServletRequest request, String honeypot, long formStartedAt) {
		checkReview(request, honeypot, formStartedAt, null);
	}

	public void checkReview(HttpServletRequest request, String honeypot, long formStartedAt, String turnstileResponse) {
		checkHuman(honeypot, formStartedAt);
		checkTurnstile(request, turnstileResponse);
		checkRate("review:" + clientKey(request), 3, Duration.ofHours(1));
	}

	private void checkTurnstile(HttpServletRequest request, String token) {
		if (turnstile != null && !turnstile.verify(token, request.getRemoteAddr())) {
			throw new SubmissionRejectedException("The security check could not be verified. Please try again.");
		}
	}

	private void checkHuman(String honeypot, long formStartedAt) {
		if (StringUtils.hasText(honeypot)) throw new SubmissionRejectedException("Your submission could not be accepted.");
		long elapsed = clock.millis() - formStartedAt;
		if (formStartedAt <= 0 || elapsed < 1_500 || elapsed > Duration.ofDays(2).toMillis()) {
			throw new SubmissionRejectedException("Please reopen the form and try again.");
		}
	}

	private void checkRate(String key, int limit, Duration window) {
		Instant now = clock.instant();
		if (checks.incrementAndGet() % 64 == 0 || attempts.size() >= MAX_CLIENTS) removeExpired(now);
		if (attempts.size() >= MAX_CLIENTS && !attempts.containsKey(key)) {
			throw new SubmissionRejectedException("The service is busy. Please wait before trying again.");
		}
		AttemptWindow attemptWindow = attempts.computeIfAbsent(key, ignored -> new AttemptWindow());
		synchronized (attemptWindow) {
			Instant cutoff = now.minus(window);
			while (!attemptWindow.times.isEmpty() && attemptWindow.times.peekFirst().isBefore(cutoff)) attemptWindow.times.removeFirst();
			if (attemptWindow.times.size() >= limit) {
				throw new SubmissionRejectedException("Too many recent submissions. Please wait before trying again.");
			}
			attemptWindow.times.addLast(now);
			attemptWindow.lastSeen = now;
		}
	}

	private void removeExpired(Instant now) {
		Instant cutoff = now.minus(MAX_WINDOW);
		attempts.entrySet().removeIf(entry -> entry.getValue().lastSeen.isBefore(cutoff));
	}

	private String clientKey(HttpServletRequest request) {
		String address = request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr().trim();
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(salt);
			return HexFormat.of().formatHex(digest.digest(address.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException("SHA-256 is unavailable", impossible);
		}
	}

	int trackedClientCount() { return attempts.size(); }

	private static final class AttemptWindow {
		private final Deque<Instant> times = new ArrayDeque<>();
		private Instant lastSeen = Instant.EPOCH;
	}

	public static class SubmissionRejectedException extends RuntimeException {
		public SubmissionRejectedException(String message) { super(message); }
	}
}
