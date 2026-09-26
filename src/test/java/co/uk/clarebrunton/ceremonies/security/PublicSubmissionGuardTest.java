package co.uk.clarebrunton.ceremonies.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class PublicSubmissionGuardTest {
	@Test void rejectsHoneypotAndFormsSubmittedTooQuickly() {
		MutableClock clock = new MutableClock();
		PublicSubmissionGuard guard = new PublicSubmissionGuard(clock);
		var request = request("10.0.0.1");
		assertThatThrownBy(() -> guard.checkInquiry(request, "bot", clock.millis() - 2_000)).isInstanceOf(PublicSubmissionGuard.SubmissionRejectedException.class);
		assertThatThrownBy(() -> guard.checkInquiry(request, "", clock.millis() - 100)).isInstanceOf(PublicSubmissionGuard.SubmissionRejectedException.class);
	}

	@Test void limitsOneResolvedClientButDoesNotTrustSpoofedForwardedHeader() {
		MutableClock clock = new MutableClock();
		PublicSubmissionGuard guard = new PublicSubmissionGuard(clock);
		var request = request("10.0.0.2");
		for (int count = 0; count < 5; count++) {
			request.addHeader("X-Forwarded-For", "203.0.113." + count);
			guard.checkInquiry(request, "", clock.millis() - 2_000);
		}
		assertThatThrownBy(() -> guard.checkInquiry(request, "", clock.millis() - 2_000)).hasMessageContaining("Too many");
		assertThatCode(() -> guard.checkInquiry(request("10.0.0.3"), "", clock.millis() - 2_000)).doesNotThrowAnyException();
	}

	@Test void expiredWindowsAllowAnotherSubmission() {
		MutableClock clock = new MutableClock();
		PublicSubmissionGuard guard = new PublicSubmissionGuard(clock);
		var request = request("10.0.0.4");
		for (int count = 0; count < 5; count++) guard.checkInquiry(request, "", clock.millis() - 2_000);
		clock.advanceSeconds(16 * 60);
		assertThatCode(() -> guard.checkInquiry(request, "", clock.millis() - 2_000)).doesNotThrowAnyException();
	}

	@Test void enabledTurnstileFailureRejectsSubmission() {
		MutableClock clock = new MutableClock();
		TurnstileVerifier verifier = mock(TurnstileVerifier.class);
		when(verifier.verify("bad", "10.0.0.5")).thenReturn(false);
		PublicSubmissionGuard guard = new PublicSubmissionGuard(clock, verifier);
		assertThatThrownBy(() -> guard.checkInquiry(request("10.0.0.5"), "", clock.millis() - 2_000, "bad"))
				.hasMessageContaining("security check");
	}

	private MockHttpServletRequest request(String address) { MockHttpServletRequest request = new MockHttpServletRequest(); request.setRemoteAddr(address); return request; }

	private static final class MutableClock extends Clock {
		private Instant instant = Instant.parse("2026-08-20T12:00:00Z");
		void advanceSeconds(long seconds) { instant = instant.plusSeconds(seconds); }
		@Override public ZoneId getZone() { return ZoneId.of("UTC"); }
		@Override public Clock withZone(ZoneId zone) { return this; }
		@Override public Instant instant() { return instant; }
	}
}
