package co.uk.clarebrunton.ceremonies.service;

import java.time.LocalDate;

public interface CalendarAvailabilityProvider {
	Availability availability(LocalDate date);
	record Availability(State state, String message) {
		public enum State { AVAILABLE, UNAVAILABLE, UNKNOWN, DISABLED }
	}
}
