package co.uk.clarebrunton.ceremonies;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import co.uk.clarebrunton.ceremonies.repository.DailyVisitRepository;
import co.uk.clarebrunton.ceremonies.repository.FunnelEventRepository;
import co.uk.clarebrunton.ceremonies.service.AnalyticsService;

@SpringBootTest
class AnalyticsServiceTest {

	@Autowired AnalyticsService service;
	@Autowired DailyVisitRepository dailyVisits;
	@Autowired FunnelEventRepository funnelEvents;

	@BeforeEach
	void reset() {
		funnelEvents.deleteAll();
		dailyVisits.deleteAll();
	}

	@Test
	void storesAggregateVisitsInDatabase() {
		service.recordVisit();
		service.recordVisit();

		var summary = service.getSummary();
		assertThat(summary.getTodayVisits()).isEqualTo(2);
		assertThat(summary.getWeekVisits()).isEqualTo(2);
		assertThat(summary.getAllTimeVisits()).isEqualTo(2);
		assertThat(summary.getLastSevenDays()).hasSize(7);
		assertThat(dailyVisits.count()).isEqualTo(1);
	}

	@Test
	void storesOnlyAllowListedFunnelEvents() {
		service.recordFunnelEvent("ENQUIRY_OPENED", "/weddings", "Wedding ceremony", "Signature — £925");
		assertThat(service.getFunnelSummary().get("ENQUIRY_OPENED")).isEqualTo(1);
		assertThat(funnelEvents.findAll()).singleElement().satisfies(event -> {
			assertThat(event.getPagePath()).isEqualTo("/weddings");
			assertThat(event.getServiceType()).isEqualTo("Wedding ceremony");
		});
	}
}
