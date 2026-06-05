package co.uk.clarebrunton.ceremonies;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import co.uk.clarebrunton.ceremonies.config.AnalyticsProperties;
import co.uk.clarebrunton.ceremonies.model.AnalyticsSummary;
import co.uk.clarebrunton.ceremonies.service.AnalyticsService;

class AnalyticsServiceTest {

	@TempDir
	Path tempDir;

	@Test
	void recordVisitStoresAggregateCountsByDay() throws Exception {
		AnalyticsService service = serviceForTempDir();

		service.recordVisit();
		service.recordVisit();

		AnalyticsSummary summary = service.getSummary();

		assertThat(summary.getTodayVisits()).isEqualTo(2);
		assertThat(summary.getWeekVisits()).isEqualTo(2);
		assertThat(summary.getMonthVisits()).isEqualTo(2);
		assertThat(summary.getAllTimeVisits()).isEqualTo(2);
		assertThat(summary.getAverageDailyVisits()).isEqualTo(0);
		assertThat(summary.getBusiestDayVisits()).isEqualTo(2);
		assertThat(summary.getLastSevenDays()).hasSize(7);
		assertThat(Files.readString(tempDir.resolve("visits-by-day.json")))
				.contains(LocalDate.now().toString())
				.contains("2");
	}

	private AnalyticsService serviceForTempDir() {
		AnalyticsProperties properties = new AnalyticsProperties();
		properties.setStorageDirectory(tempDir.toString());
		return new AnalyticsService(properties);
	}

}
