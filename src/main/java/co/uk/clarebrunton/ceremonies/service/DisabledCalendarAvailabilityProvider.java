package co.uk.clarebrunton.ceremonies.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

@Service
public class DisabledCalendarAvailabilityProvider implements CalendarAvailabilityProvider {
	@Override
	public Availability availability(LocalDate date) {
		return new Availability(Availability.State.DISABLED, "Calendar checking is not enabled; confirm dates with Clare directly.");
	}
}
